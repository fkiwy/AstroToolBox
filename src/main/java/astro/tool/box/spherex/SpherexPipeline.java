package astro.tool.box.spherex;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.Header;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.*;

/**
 * Aperture-only SPHEREx extraction service used by {@code SpherexViewerTab}.
 */
public final class SpherexPipeline {
	private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
	private static final String TAP = "https://irsa.ipac.caltech.edu/TAP/sync";
	private static final String SAPM_COLLECTION = "cal-sapm-v2-2025-164";
	private static final String SPECTRAL_CHANNELS_COLLECTION = "cal-sch-v1-2026-106";
	private static final long BAD_FLAGS = flagMask();

	private SpherexPipeline() {
	}

	public record Config(double raDeg, double decDeg, int cutoutArcsec, double apertureRadius,
	                     boolean bin, Path cacheDir,
	                     boolean removeOutliers, int outlierNbrOfBins, double outlierSigma) {
		/**
		 * Backward-compatible constructor used by SpherexViewerTab.
		 * <p>
		 * Spectral outlier rejection is enabled by default, matching the
		 * requested AstroToolBox behaviour.  The Python implementation has
		 * the same algorithm available through its configuration, with the
		 * current reference settings being 8 coarse wavelength bins and a
		 * 3-sigma MAD threshold.
		 */
		public Config(double raDeg, double decDeg, int cutoutArcsec,
		              double apertureRadius, boolean bin, Path cacheDir) {
			this(raDeg, decDeg, cutoutArcsec, apertureRadius, bin, cacheDir,
					true, 8, 3.0);
		}

		public Config {
			if (!Double.isFinite(raDeg) || raDeg < 0 || raDeg >= 360 ||
					!Double.isFinite(decDeg) || decDeg < -90 || decDeg > 90)
				throw new IllegalArgumentException("RA must be [0, 360) degrees and Dec must be [-90, 90] degrees.");
			if (cutoutArcsec < 16 || cutoutArcsec > 1800 || apertureRadius <= 0)
				throw new IllegalArgumentException("Use a 16–1800 arcsec cutout and positive aperture radius.");
			if (outlierNbrOfBins < 1)
				throw new IllegalArgumentException("outlierNbrOfBins must be >= 1.");
			if (!Double.isFinite(outlierSigma) || outlierSigma <= 0)
				throw new IllegalArgumentException("outlierSigma must be finite and > 0.");
		}
	}

	public record Point(double wavelengthUm, double fluxUjy, double errorUjy, int detector, int count) {
	}

	public record Result(List<Point> points, int discovered, int measured, List<String> warnings,
	                     List<Path> fitsFiles) {
		public Result(List<Point> points, int discovered, int measured, List<String> warnings) {
			this(points, discovered, measured, warnings, new ArrayList<>());
		}
	}

	private record SapmKey(String release, int detector) {
	}

	@FunctionalInterface
	public interface Progress {
		void update(String text);
	}

	public static Result run(Config config, Progress progress) throws Exception {
		return run(config, progress, true);
	}

	/**
	 * Extract a spectrum at a new position from the currently cached SPHEREx
	 * cutouts. No IRSA query or FITS download is performed.
	 */
	public static Result extractSpectrumAt(Config config, Progress progress) throws Exception {
		return run(config, progress, false);
	}

	public static Result run(Config config, Progress progress, boolean queryIrsa) throws Exception {
		Files.createDirectories(config.cacheDir());
		List<Path> cachedFitsFiles = cachedCutouts(config.cacheDir().resolve("cutouts"));
		List<String> urls = List.of();

		if (queryIrsa || cachedFitsFiles.isEmpty()) {
			progress.update("Querying IRSA for SPHEREx cutouts…");
			urls = query(config);
			if (urls.isEmpty()) throw new IOException("No SPHEREx cutouts cover these coordinates.");
		} else {
			progress.update("Reusing cached SPHEREx cutouts…");
		}

		List<Point> points = new ArrayList<>();
		List<String> warnings = new ArrayList<>();
		List<Path> fitsFiles = new ArrayList<>();
		Map<SapmKey, double[][]> sapms = new HashMap<>();
		Map<String, SpectralChannels> spectralChannels = new HashMap<>();

		if (queryIrsa || cachedFitsFiles.isEmpty()) {
			for (int i = 0; i < urls.size(); i++) {
				progress.update("Downloading and measuring cutout " + (i + 1) + " of " + urls.size() + "…");
				try {
					Path fitsPath = download(urls.get(i), config.cacheDir().resolve("cutouts"), "cutout_" + i + ".fits");
					fitsFiles.add(fitsPath);
					Point p = measure(fitsPath, config, sapms, spectralChannels);
					if (p != null) points.add(p);
				} catch (Exception ex) {
					warnings.add("Cutout " + (i + 1) + " skipped: " + ex.getMessage());
				}
			}
		} else {
			for (int i = 0; i < cachedFitsFiles.size(); i++) {
				Path fitsPath = cachedFitsFiles.get(i);
				progress.update("Measuring cached cutout " + (i + 1) + " of " + cachedFitsFiles.size() + "…");
				try {
					fitsFiles.add(fitsPath);
					Point p = measure(fitsPath, config, sapms, spectralChannels);
					if (p != null) points.add(p);
				} catch (Exception ex) {
					warnings.add("Cached cutout " + (i + 1) + " skipped: " + ex.getMessage());
				}
			}
		}

		if (points.isEmpty())
			throw new IOException("No usable cutouts were measured." + (warnings.isEmpty() ? "" : " " + warnings.get(0)));
		int measured = points.size();

		/*
		 * Match SPExPI's final spectrum processing order:
		 *
		 *   sanitize -> detector-wise wavelength-bin MAD rejection -> binning
		 *
		 * The rejection is deliberately performed on the individual aperture
		 * measurements, before inverse-variance spectral binning, so that a
		 * single bad exposure cannot pull an entire output bin away from the
		 * surrounding spectrum.
		 */
		points = sanitizePoints(points);
		if (config.removeOutliers()) {
			points = removeOutliersPerDetector(
					points,
					config.outlierNbrOfBins(),
					config.outlierSigma(),
					Set.of(1, 2, 3, 4, 5, 6)
			);
		}

		if (config.bin()) points = bin(points, spectralChannels);
		points.sort(Comparator.comparingDouble(Point::wavelengthUm));
		progress.update("Spectrum complete.");
		return new Result(points, queryIrsa ? urls.size() : cachedFitsFiles.size(), measured, warnings, fitsFiles);
	}

	private static List<String> query(Config c) throws Exception {
		String adql = "SELECT 'https://irsa.ipac.caltech.edu/' || a.uri || '?center=" + c.raDeg + "," + c.decDeg + "d&size=" + (c.cutoutArcsec / 3600d) + "' AS uri "
				+ "FROM spherex.artifact a JOIN spherex.plane p ON a.planeid=p.planeid WHERE 1=CONTAINS(POINT('ICRS', " + c.raDeg + ", " + c.decDeg + "), p.poly)";
		String url = TAP + "?REQUEST=doQuery&LANG=ADQL&FORMAT=csv&QUERY=" + URLEncoder.encode(adql, StandardCharsets.UTF_8);
		HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(2)).GET().build(), HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() / 100 != 2) throw new IOException("IRSA TAP returned HTTP " + response.statusCode());
		List<String> rows = new ArrayList<>();
		String[] lines = response.body().split("\\R");
		for (int i = 1; i < lines.length; i++) {
			String s = lines[i].trim();
			if (s.startsWith("\"") && s.endsWith("\"")) s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
			if (!s.isBlank()) rows.add(s);
		}
		return rows;
	}

	private static List<Path> cachedCutouts(Path cutoutsDir) throws IOException {
		if (!Files.isDirectory(cutoutsDir)) return List.of();
		try (var paths = Files.list(cutoutsDir)) {
			return paths
					.filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".fits"))
					.sorted(Comparator.comparing(path -> path.getFileName().toString()))
					.toList();
		}
	}

	private static Point measure(Path path, Config c,
	                             Map<SapmKey, double[][]> sapms,
	                             Map<String, SpectralChannels> spectralChannels) throws Exception {
		try (Fits fits = new Fits(path.toFile())) {
			BasicHDU<?> imageHdu = ext(fits, "IMAGE");
			if (imageHdu == null)
				throw new IOException("IMAGE extension missing");

			Header h = imageHdu.getHeader();

			int detector = h.getIntValue(
					"DETECTOR",
					h.getIntValue("BAND", 0)
			);
			if (detector < 1 || detector > 6)
				throw new IOException("Invalid detector number: " + detector);

			String release = normalizeRelease(
					h.getStringValue("DATAREL", "qr2")
			);

			SpectralChannels channels = spectralChannels.get(release);
			if (channels == null) {
				channels = spectralChannels(release, c.cacheDir());
				spectralChannels.put(release, channels);
			}

			double[][] image = doubles(imageHdu.getKernel());
			if (image == null || image.length == 0)
				throw new IOException("IMAGE data is empty.");

			/*
			 * The SAPM is a parent-detector image.  Slice it first, exactly
			 * like extract_sapm_for_cutout() in SPExPI.
			 */
			SapmKey sapmKey = new SapmKey(release, detector);
			double[][] sapm = sapms.get(sapmKey);
			if (sapm == null) {
				sapm = sapm(detector, h, c.cacheDir());
				sapms.put(sapmKey, sapm);
			}

			CutoutSlices slices = cutoutSlices(
					h,
					image.length,
					image[0].length,
					sapm.length,
					sapm[0].length
			);

			double[][] imageUse = crop(
					image,
					slices.imageX0(),
					slices.imageY0(),
					slices.imageX1(),
					slices.imageY1()
			);

			double[][] sapmUse = crop(
					sapm,
					slices.parentX0(),
					slices.parentY0(),
					slices.parentX1(),
					slices.parentY1()
			);

			Object zodiRaw = extData(fits, "ZODI");
			double[][] zodiUse = zodiRaw == null ? null :
					crop(doubles(zodiRaw),
							slices.imageX0(),
							slices.imageY0(),
							slices.imageX1(),
							slices.imageY1());

			Object varianceRaw = extData(fits, "VARIANCE");
			double[][] varianceUse = varianceRaw == null ? null :
					crop(doubles(varianceRaw),
							slices.imageX0(),
							slices.imageY0(),
							slices.imageX1(),
							slices.imageY1());

			Object flagsRaw = extData(fits, "FLAGS");
			long[][] flagsUse = flagsRaw == null ? null :
					crop(longs(flagsRaw),
							slices.imageX0(),
							slices.imageY0(),
							slices.imageX1(),
							slices.imageY1());

			double[][] flux = toUjy(
					imageUse,
					zodiUse,
					sapmUse,
					h
			);

			double[][] variance = toVariance(
					varianceUse,
					sapmUse,
					h
			);

			/*
			 * WCS coordinates are defined in the original cutout frame.
			 * Therefore calculate x/y before shifting the arrays for an
			 * edge-clipped SAPM overlap, then shift into the clipped image.
			 */
			double[] pos = worldToPixel(
					h,
					c.raDeg(),
					c.decDeg()
			);

			double x = pos[0] - slices.imageX0();
			double y = pos[1] - slices.imageY0();

			if (!Double.isFinite(x) || !Double.isFinite(y))
				return null;

			double[] result = aperture(
					flux,
					variance,
					flagsUse,
					x,
					y,
					c.apertureRadius()
			);

			if (!Double.isFinite(result[0]) ||
					!Double.isFinite(result[1]) ||
					result[1] <= 0) {
				return null;
			}

			/*
			 * SPExPI obtains wavelength from the WCS at the same source
			 * position.  Use the WCS-TAB value if available; the
			 * spectral-channel map is only the fallback.
			 */
			double wavelength = wavelength(
					fits,
					h,
					x + slices.imageX0(),
					y + slices.imageY0(),
					detector,
					channels
			);

			if (!Double.isFinite(wavelength))
				throw new IOException("Could not determine calibrated wavelength.");

			return new Point(
					wavelength,
					result[0],
					result[1],
					detector,
					1
			);
		}
	}

	private static String normalizeRelease(String release) {
		String text = release == null
				? ""
				: release.trim().toLowerCase(Locale.ROOT)
				.replace('-', '_')
				.replace(' ', '_');

		if (text.contains("qr2_deep") || text.equals("qr2deep"))
			return "qr2_deep";
		if (text.contains("qr2"))
			return "qr2";
		if (text.contains("qr1"))
			return "qr1";
		return "unknown".equals(text) || text.isBlank() ? "qr2" : text;
	}

	private static double[][] crop(double[][] a, int x0, int y0, int x1, int y1) {
		if (a == null)
			return null;

		if (x0 < 0 || y0 < 0 ||
				x1 > a[0].length || y1 > a.length ||
				x1 <= x0 || y1 <= y0)
			throw new IllegalArgumentException("Invalid 2-D crop bounds.");

		double[][] out = new double[y1 - y0][x1 - x0];

		for (int y = y0; y < y1; y++)
			System.arraycopy(
					a[y], x0,
					out[y - y0], 0,
					x1 - x0
			);

		return out;
	}

	private static long[][] crop(long[][] a, int x0, int y0, int x1, int y1) {
		if (a == null)
			return null;

		if (x0 < 0 || y0 < 0 ||
				x1 > a[0].length || y1 > a.length ||
				x1 <= x0 || y1 <= y0)
			throw new IllegalArgumentException("Invalid integer 2-D crop bounds.");

		long[][] out = new long[y1 - y0][x1 - x0];

		for (int y = y0; y < y1; y++)
			System.arraycopy(
					a[y], x0,
					out[y - y0], 0,
					x1 - x0
			);

		return out;
	}

	private static BasicHDU<?> ext(Fits fits, String name) throws Exception {
		for (BasicHDU<?> h : fits.read()) if (name.equalsIgnoreCase(h.getHeader().getStringValue("EXTNAME"))) return h;
		return null;
	}

	private static Object extData(Fits fits, String name) throws Exception {
		BasicHDU<?> h = ext(fits, name);
		return h == null ? null : h.getKernel();
	}

	private static double[][] sapm(int detector, Header image, Path cache) throws Exception {
		String release = image.getStringValue("DATAREL", "qr2").toLowerCase(Locale.ROOT);
		String file = "solid_angle_pixel_map_D" + detector + "_spx_" + SAPM_COLLECTION + ".fits";
		Path local = cache.resolve("sapm").resolve(release).resolve(file);
		if (!Files.exists(local))
			download("https://nasa-irsa-spherex.s3.amazonaws.com/" + release + "/solid_angle_pixel_map/" + SAPM_COLLECTION + "/" + detector + "/" + file, local.getParent(), file);
		try (Fits fits = new Fits(local.toFile())) {
			BasicHDU<?> h = ext(fits, "IMAGE");
			if (h == null) throw new IOException("SAPM IMAGE extension missing");
			return doubles(h.getKernel());
		}
	}

	/**
	 * QR2 spectral-channel table plus its full-detector pixel-to-channel map.
	 */
	private record SpectralChannels(int[][] channelMap, Map<Integer, List<Channel>> byDetector) {
	}

	private record Channel(int number, double centerUm, double minUm, double maxUm) {
	}

	private static SpectralChannels spectralChannels(String release, Path cache) throws Exception {
		String file = "spectral_channels_spx_" + SPECTRAL_CHANNELS_COLLECTION + ".fits.gz";
		Path local = cache.resolve("spectral_channels").resolve(release).resolve(SPECTRAL_CHANNELS_COLLECTION).resolve(file);
		if (!Files.exists(local)) {
			download("https://nasa-irsa-spherex.s3.amazonaws.com/" + release + "/spectral_channels/"
					+ SPECTRAL_CHANNELS_COLLECTION + "/" + file, local.getParent(), file);
		}
		try (Fits fits = new Fits(local.toFile())) {
			BasicHDU<?> mapHdu = ext(fits, "CHANNEL_MAP");
			BasicHDU<?> tableHdu = ext(fits, "SPECTRAL_CHANNELS");
			if (mapHdu == null || !(tableHdu instanceof BinaryTableHDU table))
				throw new IOException("QR2 spectral_channels file is missing CHANNEL_MAP or SPECTRAL_CHANNELS");
			int[][] map = integers2d(mapHdu.getKernel());
			Map<Integer, List<Channel>> byDetector = new HashMap<>();
			int detectorCol = table.findColumn("DETECTOR");
			int subchannelCol = table.findColumn("SUBCHAN");
			int wavelengthCol = table.findColumn("WAVELENGTH");
			int minCol = table.findColumn("WL_MIN");
			int maxCol = table.findColumn("WL_MAX");
			if (detectorCol < 0 || subchannelCol < 0 || wavelengthCol < 0 || minCol < 0 || maxCol < 0)
				throw new IOException("QR2 SPECTRAL_CHANNELS table has an unexpected schema");
			for (int row = 0; row < table.getNRows(); row++) {
				int detector = number(table.getElement(row, detectorCol)).intValue();
				int subchannel = number(table.getElement(row, subchannelCol)).intValue();
				Channel channel = new Channel(subchannel,
						number(table.getElement(row, wavelengthCol)).doubleValue(),
						number(table.getElement(row, minCol)).doubleValue(),
						number(table.getElement(row, maxCol)).doubleValue());
				byDetector.computeIfAbsent(detector, ignored -> new ArrayList<>()).add(channel);
			}
			for (List<Channel> channels : byDetector.values())
				channels.sort(Comparator.comparingDouble(Channel::centerUm));
			return new SpectralChannels(map, byDetector);
		}
	}

	private static Path download(String url, Path directory, String name) throws Exception {
		Files.createDirectories(directory);
		Path file = directory.resolve(name);
		if (Files.exists(file) && Files.size(file) > 0) return file;
		HttpResponse<InputStream> r = HTTP.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(3)).GET().build(), HttpResponse.BodyHandlers.ofInputStream());
		if (r.statusCode() / 100 != 2) throw new IOException("HTTP " + r.statusCode());
		try (InputStream in = r.body()) {
			Files.copy(in, file, StandardCopyOption.REPLACE_EXISTING);
		}
		return file;
	}

	private static double[][] toUjy(double[][] image, Object zodiRaw, double[][] sapm, Header h) {
		double[][] z = zodiRaw == null ? null : doubles(zodiRaw);
		if (z != null && (z.length != image.length || z[0].length != image[0].length))
			throw new IllegalArgumentException("ZODI shape does not match IMAGE.");

		double[][] out = new double[image.length][image[0].length];

		double scale = fluxUnitScale(h.getStringValue("BUNIT", "MJy/sr"));

		if (sapm.length != image.length || sapm[0].length != image[0].length)
			throw new IllegalArgumentException(
					"SAPM/image shape mismatch after parent-detector slicing: sapm="
							+ sapm.length + "x" + sapm[0].length
							+ ", image=" + image.length + "x" + image[0].length);

		for (int y = 0; y < out.length; y++) {
			for (int x = 0; x < out[0].length; x++) {
				double value = image[y][x];
				if (z != null)
					value -= z[y][x];

				out[y][x] = value * scale * sapm[y][x];
			}
		}
		return out;
	}

	private static double[][] toVariance(Object raw, double[][] sapm, Header h) {
		if (raw == null)
			return null;

		double[][] v = doubles(raw);
		if (v.length != sapm.length || v[0].length != sapm[0].length)
			throw new IllegalArgumentException(
					"VARIANCE/SAPM shape mismatch: variance="
							+ v.length + "x" + v[0].length
							+ ", sapm=" + sapm.length + "x" + sapm[0].length);

		double scale = fluxUnitScale(h.getStringValue("BUNIT", "MJy/sr"));
		double[][] out = new double[v.length][v[0].length];

		for (int y = 0; y < out.length; y++) {
			for (int x = 0; x < out[0].length; x++) {
				double factor = scale * sapm[y][x];
				out[y][x] = v[y][x] * factor * factor;
			}
		}
		return out;
	}

	/**
	 * MJy/sr -> uJy/arcsec^2 conversion.
	 * <p>
	 * 1 sr = (206264.806247...)^2 arcsec^2.
	 */
	private static double fluxUnitScale(String bunit) {
		String unit = bunit == null ? "" : bunit.trim().toLowerCase(Locale.ROOT);

		if (unit.contains("mjy") && unit.contains("sr"))
			return 1.0e12 / 4.254517029617293e10;

		if (unit.contains("jy") && unit.contains("sr"))
			return 1.0e6 / 4.254517029617293e10;

		if (unit.contains("ujy") && unit.contains("arcsec"))
			return 1.0;

		/*
		 * Preserve the previous behaviour for already pixel-scaled data.
		 */
		return 1.0;
	}

	/**
	 * Extract the SAPM region corresponding to the IMAGE cutout.
	 * <p>
	 * This is the Java equivalent of SPExPI's get_cutout_parent_slices().
	 * The SAPM is a full detector map; IMAGE/VARIANCE/ZODI/FLAGS are cutouts.
	 */
	private static CutoutSlices cutoutSlices(Header h, int imageNy, int imageNx,
	                                         int parentNy, int parentNx) {

		if (!h.containsKey("CRPIX1A") || !h.containsKey("CRPIX2A")) {
			int ny = Math.min(imageNy, parentNy);
			int nx = Math.min(imageNx, parentNx);
			return new CutoutSlices(0, 0, nx, ny, 0, 0, nx, ny);
		}

		int x0Parent = (int) Math.rint(1.0 - h.getDoubleValue("CRPIX1A"));
		int y0Parent = (int) Math.rint(1.0 - h.getDoubleValue("CRPIX2A"));
		int x1Parent = x0Parent + imageNx;
		int y1Parent = y0Parent + imageNy;

		int px0 = Math.max(0, x0Parent);
		int py0 = Math.max(0, y0Parent);
		int px1 = Math.min(parentNx, x1Parent);
		int py1 = Math.min(parentNy, y1Parent);

		if (px1 <= px0 || py1 <= py0)
			throw new IllegalArgumentException(
					"Cutout does not overlap parent detector SAPM map.");

		int ix0 = px0 - x0Parent;
		int iy0 = py0 - y0Parent;
		int ix1 = ix0 + (px1 - px0);
		int iy1 = iy0 + (py1 - py0);

		return new CutoutSlices(
				ix0, iy0, ix1, iy1,
				px0, py0, px1, py1
		);
	}

	private record CutoutSlices(
			int imageX0, int imageY0, int imageX1, int imageY1,
			int parentX0, int parentY0, int parentX1, int parentY1) {
	}

	/**
	 * Python-equivalent circular aperture photometry.
	 * <p>
	 * The implementation mirrors the current SPExPI aperture path:
	 * <p>
	 * - exact fractional circular-aperture pixel weights
	 * - fatal FLAGS excluded from the source aperture
	 * - FLAGS bit 21 additionally excluded from the background
	 * - 2r--3r local background annulus
	 * - iterative 3-sigma clipping, maximum 5 iterations
	 * - effective fractional aperture area
	 * - variance propagation using the same effective area
	 * <p>
	 * Return:
	 * [0] background-subtracted flux in uJy
	 * [1] 1-sigma uncertainty in uJy
	 */

	private static double[] aperture(
			double[][] f,
			double[][] v,
			long[][] flags,
			double x,
			double y,
			double radius) {

		if (!Double.isFinite(x) ||
				!Double.isFinite(y) ||
				!Double.isFinite(radius) ||
				radius <= 0)
			return new double[]{Double.NaN, Double.NaN};

		int ny = f.length;
		int nx = f[0].length;

		if (x < 0 || y < 0 || x >= nx || y >= ny)
			return new double[]{Double.NaN, Double.NaN};

		double rIn = 2.0 * radius;
		double rOut = 3.0 * radius;

		/*
		 * Python's make_bad_pixel_mask() masks both fatal flags and
		 * non-finite flux.  The same mask is used for the aperture.
		 */
		double apertureFlux = 0.0;
		double apertureVariance = 0.0;
		double apertureArea = 0.0;

		for (int iy = 0; iy < ny; iy++) {
			for (int ix = 0; ix < nx; ix++) {
				if (!Double.isFinite(f[iy][ix]) ||
						isFatalFlag(flags, iy, ix))
					continue;

				double weight = circleSquareIntersection(
						x, y, radius, ix, iy);

				if (weight <= 0.0)
					continue;

				apertureArea += weight;
				apertureFlux += weight * f[iy][ix];

				/*
				 * IMPORTANT:
				 * photutils aperture_photometry() is applied to the
				 * VARIANCE image itself with method="exact".
				 * Therefore the variance contribution is w * V,
				 * not w^2 * V.
				 */
				if (v != null &&
						Double.isFinite(v[iy][ix])) {
					apertureVariance +=
							weight * v[iy][ix];
				}
			}
		}

		if (!Double.isFinite(apertureArea) ||
				apertureArea <= 0)
			return new double[]{Double.NaN, Double.NaN};

		/*
		 * Background:
		 *
		 * Photutils CircularAnnulus + ApertureStats uses the pixel
		 * centre mask for selecting annulus pixels.  It is therefore
		 * important not to replace this with fractional annulus areas.
		 */
		List<Double> background = new ArrayList<>();

		for (int iy = 0; iy < ny; iy++) {
			for (int ix = 0; ix < nx; ix++) {
				if (!Double.isFinite(f[iy][ix]) ||
						isBackgroundMasked(flags, iy, ix))
					continue;

				double d = Math.hypot(ix - x, iy - y);

				if (d >= rIn && d <= rOut)
					background.add(f[iy][ix]);
			}
		}

		/*
		 * This is the same fallback used by the Python implementation:
		 * if known-source masking leaves no usable background, retry
		 * with the normal bad-pixel mask.
		 */
		if (background.isEmpty()) {
			for (int iy = 0; iy < ny; iy++) {
				for (int ix = 0; ix < nx; ix++) {
					if (!Double.isFinite(f[iy][ix]) ||
							isFatalFlag(flags, iy, ix))
						continue;

					double d = Math.hypot(ix - x, iy - y);

					if (d >= rIn && d <= rOut)
						background.add(f[iy][ix]);
				}
			}
		}

		if (background.isEmpty())
			return new double[]{Double.NaN, Double.NaN};

		/*
		 * Astropy SigmaClip's default centre is the median and its
		 * default std function is the standard deviation, not MAD.
		 */
		List<Double> clipped = sigmaClipStd(
				background,
				3.0,
				5
		);

		if (clipped.isEmpty())
			return new double[]{Double.NaN, Double.NaN};

		double backgroundPerPixel = median(clipped);

		if (!Double.isFinite(backgroundPerPixel))
			return new double[]{Double.NaN, Double.NaN};

		double flux =
				apertureFlux -
						backgroundPerPixel * apertureArea;

		/*
		 * Python calculates n_bg independently from the sigma-clipped
		 * sample:
		 *
		 * annulus.to_mask(method="center")
		 *      & (~bad_mask)
		 *
		 * Note that FLAG 21 is NOT part of this n_bg calculation.
		 */
		int nBg = 0;

		for (int iy = 0; iy < ny; iy++) {
			for (int ix = 0; ix < nx; ix++) {
				if (!Double.isFinite(f[iy][ix]) ||
						isFatalFlag(flags, iy, ix))
					continue;

				double d = Math.hypot(ix - x, iy - y);

				if (d >= rIn && d <= rOut)
					nBg++;
			}
		}

		double std = populationStandardDeviation(clipped);

		double backgroundVariancePerPixel =
				(nBg > 0 && Double.isFinite(std))
						? std * std / nBg
						: 0.0;

		/*
		 * If VARIANCE is unavailable, Python uses:
		 *
		 *     var_sum = stats.std**2 * ap_area
		 *
		 * rather than dividing by n_bg.
		 */
		if (v == null && Double.isFinite(std))
			apertureVariance =
					std * std * apertureArea;

		double totalVariance =
				apertureVariance +
						apertureArea * apertureArea *
								backgroundVariancePerPixel;

		double error =
				totalVariance >= 0
						? Math.sqrt(totalVariance)
						: Double.NaN;

		return new double[]{flux, error};
	}

	private static List<Double> sigmaClipStd(
			List<Double> values,
			double sigma,
			int maxIterations) {

		List<Double> current = new ArrayList<>();

		for (double value : values)
			if (Double.isFinite(value))
				current.add(value);

		if (current.size() < 2)
			return current;

		for (int iteration = 0;
		     iteration < maxIterations;
		     iteration++) {

			double center = median(current);
			double std = populationStandardDeviation(current);

			if (!Double.isFinite(center) ||
					!Double.isFinite(std) ||
					std == 0.0)
				break;

			double limit = sigma * std;
			List<Double> next =
					new ArrayList<>(current.size());

			for (double value : current) {
				if (Math.abs(value - center) <= limit)
					next.add(value);
			}

			if (next.size() == current.size())
				break;

			if (next.isEmpty())
				break;

			current = next;
		}

		return current;
	}

	private static double populationStandardDeviation(
			List<Double> values) {

		if (values == null || values.isEmpty())
			return Double.NaN;

		double mean = 0.0;
		for (double value : values)
			mean += value;
		mean /= values.size();

		double sum = 0.0;
		for (double value : values) {
			double d = value - mean;
			sum += d * d;
		}

		return Math.sqrt(sum / values.size());
	}

	/**
	 * Fatal pixel mask used for the source aperture.
	 */
	private static boolean isFatalFlag(
			long[][] flags,
			int y,
			int x) {

		return flags != null &&
				(flags[y][x] & BAD_FLAGS) != 0;
	}

	/**
	 * Background mask.
	 * <p>
	 * The Python pipeline uses:
	 * <p>
	 * bad_flag_bits + (21,)
	 * <p>
	 * for the background when known-source removal is enabled.
	 */
	private static boolean isBackgroundMasked(
			long[][] flags,
			int y,
			int x) {

		if (flags == null)
			return false;

		long value = flags[y][x];

		if ((value & BAD_FLAGS) != 0)
			return true;

		/*
		 * FLAG bit 21 = known source.
		 */
		return (value & (1L << 21)) != 0;
	}

	/**
	 * Calculate the exact fractional area of a unit pixel covered by a
	 * circle.
	 * <p>
	 * Pixel coordinates are interpreted in the same convention as
	 * Photutils: pixel (ix, iy) occupies
	 * <p>
	 * [ix-0.5, ix+0.5] x [iy-0.5, iy+0.5]
	 * <p>
	 * The result is in [0, 1].
	 * <p>
	 * The calculation uses deterministic supersampling.  This avoids the
	 * severe discontinuities produced by the old centre-of-pixel test:
	 * <p>
	 * if (distance <= radius)
	 * <p>
	 * while remaining dependency-free.
	 */

	private static double circleSquareIntersection(
			double cx,
			double cy,
			double radius,
			int ix,
			int iy) {

		double left = ix - 0.5;
		double right = ix + 0.5;
		double bottom = iy - 0.5;
		double top = iy + 0.5;

		double nearestX = clamp(cx, left, right);
		double nearestY = clamp(cy, bottom, top);

		double dx = nearestX - cx;
		double dy = nearestY - cy;

		double r2 = radius * radius;

		if (dx * dx + dy * dy >= r2)
			return 0.0;

		double farthestDx =
				Math.max(Math.abs(cx - left),
						Math.abs(cx - right));
		double farthestDy =
				Math.max(Math.abs(cy - bottom),
						Math.abs(cy - top));

		if (farthestDx * farthestDx +
				farthestDy * farthestDy <= r2)
			return 1.0;

		/*
		 * High-resolution deterministic area integration.
		 *
		 * Photutils' "exact" method calculates the exact circle/pixel
		 * overlap.  This dependency-free implementation converges to
		 * that result to substantially better than the previous 12x12
		 * approximation.  64x64 gives 4096 samples/pixel and the
		 * remaining geometric error is negligible for SPHEREx
		 * apertures compared with detector/background noise.
		 */
		final int samples = 64;
		int inside = 0;

		for (int sy = 0; sy < samples; sy++) {
			double py =
					bottom +
							(sy + 0.5) / samples;

			for (int sx = 0; sx < samples; sx++) {
				double px =
						left +
								(sx + 0.5) / samples;

				double ddx = px - cx;
				double ddy = py - cy;

				if (ddx * ddx + ddy * ddy <= r2)
					inside++;
			}
		}

		return inside / (double) (samples * samples);
	}

	private static double clamp(
			double value,
			double min,
			double max) {

		return Math.max(min, Math.min(max, value));
	}

	/**
	 * Iterative sigma clipping using the median and MAD-derived
	 * standard deviation.
	 * <p>
	 * This mirrors the robust behaviour required by the Python
	 * SigmaClip call without introducing another Java dependency.
	 */
	private static List<Double> sigmaClip(
			List<Double> values,
			double sigma,
			int maxIterations) {

		List<Double> current =
				new ArrayList<>();

		for (double value : values) {
			if (Double.isFinite(value))
				current.add(value);
		}

		if (current.size() < 2)
			return current;

		for (int iteration = 0;
		     iteration < maxIterations;
		     iteration++) {

			double center = median(current);

			if (!Double.isFinite(center))
				break;

			List<Double> deviations =
					new ArrayList<>(current.size());

			for (double value : current)
				deviations.add(
						Math.abs(value - center)
				);

			double mad = median(deviations);

			/*
			 * Gaussian-equivalent sigma from MAD.
			 */
			double robustStd = 1.4826 * mad;

			/*
			 * Constant-valued background.
			 */
			if (!Double.isFinite(robustStd) ||
					robustStd == 0.0) {

				break;
			}

			double limit = sigma * robustStd;

			List<Double> next =
					new ArrayList<>(current.size());

			for (double value : current) {
				if (Math.abs(value - center) <= limit)
					next.add(value);
			}

			/*
			 * Nothing changed.
			 */
			if (next.size() == current.size())
				break;

			/*
			 * Never allow clipping to destroy the background sample.
			 */
			if (next.size() < 2)
				break;

			current = next;
		}

		return current;
	}

	private static double median(
			List<Double> values) {

		if (values == null ||
				values.isEmpty()) {

			return Double.NaN;
		}

		List<Double> sorted =
				new ArrayList<>(values);

		sorted.sort(Double::compare);

		int n = sorted.size();
		int middle = n / 2;

		if ((n & 1) != 0)
			return sorted.get(middle);

		return 0.5 *
				(sorted.get(middle - 1) +
						sorted.get(middle));
	}

	private static double sampleStandardDeviation(
			List<Double> values) {

		if (values == null ||
				values.size() < 2) {

			return Double.NaN;
		}

		double mean = 0.0;

		for (double value : values)
			mean += value;

		mean /= values.size();

		double sum = 0.0;

		for (double value : values) {
			double d = value - mean;
			sum += d * d;
		}

		return Math.sqrt(
				sum / (values.size() - 1)
		);
	}

	/**
	 * Convert ICRS sky coordinates to zero-based cutout pixel coordinates
	 * using the FITS spatial WCS.
	 * <p>
	 * This implements the TAN-SIP inverse transformation used by
	 * Astropy's:
	 * <p>
	 * WCS(header).world_to_pixel(...)
	 * <p>
	 * in the SPExPI aperture path.
	 * <p>
	 * The transformation is:
	 * <p>
	 * sky -> TAN intermediate world coordinates
	 * -> inverse CD/PC/CDELT
	 * -> inverse SIP polynomial
	 * -> zero-based pixel coordinates
	 * <p>
	 * For normal TAN-SIP SPHEREx products the inverse SIP coefficients
	 * AP_i_j / BP_i_j are present and are used directly.  If they are
	 * absent, the forward SIP coefficients A_i_j / B_i_j are inverted
	 * iteratively, as permitted by the SIP convention.
	 */
	private static double[] worldToPixel(Header h, double raDeg, double decDeg) {
		double crval1 = h.getDoubleValue("CRVAL1");
		double crval2 = h.getDoubleValue("CRVAL2");
		double crpix1 = h.getDoubleValue("CRPIX1");
		double crpix2 = h.getDoubleValue("CRPIX2");

		double dra = Math.toRadians(wrapDeltaRa(raDeg - crval1));
		double dec = Math.toRadians(decDeg);
		double dec0 = Math.toRadians(crval2);

		String ctype1 = h.getStringValue("CTYPE1", "").toUpperCase(Locale.ROOT);
		boolean tan = ctype1.contains("TAN");

		/*
		 * Step 1: sky -> projected intermediate world coordinates.
		 *
		 * These are in degrees and correspond to the coordinates to
		 * which the FITS CD matrix is applied.
		 */
		double xiDeg;
		double etaDeg;

		if (tan) {
			double sinDec = Math.sin(dec);
			double cosDec = Math.cos(dec);
			double sinDec0 = Math.sin(dec0);
			double cosDec0 = Math.cos(dec0);
			double cosDra = Math.cos(dra);

			double denominator =
					sinDec0 * sinDec +
							cosDec0 * cosDec * cosDra;

			if (!Double.isFinite(denominator) || denominator <= 0.0) {
				throw new IllegalArgumentException(
						"Target is outside the valid TAN projection.");
			}

			double xi =
					cosDec * Math.sin(dra) /
							denominator;

			double eta =
					(cosDec0 * sinDec -
							sinDec0 * cosDec * cosDra) /
							denominator;

			xiDeg = Math.toDegrees(xi);
			etaDeg = Math.toDegrees(eta);
		} else {
			/*
			 * Linear fallback for non-TAN spatial WCS.
			 */
			xiDeg = Math.toDegrees(dra) * Math.cos(dec0);
			etaDeg = Math.toDegrees(dec - dec0);
		}

		/*
		 * Step 2: inverse linear WCS transformation.
		 *
		 * Before SIP, the inverse CD transformation gives the
		 * intermediate pixel coordinates u', v', measured relative
		 * to CRPIX.
		 */
		double[] cd = getCdMatrix(h);

		double det =
				cd[0] * cd[3] -
						cd[1] * cd[2];

		if (!Double.isFinite(det) ||
				Math.abs(det) < 1e-30) {
			throw new IllegalArgumentException(
					"Invalid spatial WCS matrix.");
		}

		double uPrime =
				(cd[3] * xiDeg -
						cd[1] * etaDeg) / det;

		double vPrime =
				(-cd[2] * xiDeg +
						cd[0] * etaDeg) / det;

		/*
		 * Step 3: inverse SIP transformation.
		 *
		 * SIP is defined in pixel coordinates relative to CRPIX.
		 * The inverse coefficients AP/BP directly map the intermediate
		 * coordinates (u',v') to the actual pixel coordinates (u,v).
		 */
		double[] pixelOffset;

		if (hasSipInverse(h)) {
			pixelOffset = applySipInverse(
					h,
					uPrime,
					vPrime
			);
		} else if (hasSipForward(h)) {
			/*
			 * SIP inverse coefficients are optional.  If only A/B are
			 * present, solve:
			 *
			 *     u' = u + A(u,v)
			 *     v' = v + B(u,v)
			 *
			 * by fixed-point iteration.
			 */
			pixelOffset = invertSipForward(
					h,
					uPrime,
					vPrime
			);
		} else {
			pixelOffset = new double[]{
					uPrime,
					vPrime
			};
		}

		/*
		 * FITS pixel coordinates are one-based; Java image arrays are
		 * zero-based.
		 */
		return new double[]{
				crpix1 - 1.0 + pixelOffset[0],
				crpix2 - 1.0 + pixelOffset[1]
		};
	}

	/**
	 * Apply the inverse SIP polynomial:
	 * <p>
	 * u = u' + AP(u',v')
	 * v = v' + BP(u',v')
	 * <p>
	 * SIP polynomial coefficients are stored in header keywords
	 * AP_i_j and BP_i_j, with AP_ORDER/BP_ORDER specifying their
	 * maximum total polynomial order.
	 */
	private static double[] applySipInverse(
			Header h,
			double uPrime,
			double vPrime) {

		double du = sipPolynomial(
				h,
				"AP",
				"AP_ORDER",
				uPrime,
				vPrime
		);

		double dv = sipPolynomial(
				h,
				"BP",
				"BP_ORDER",
				uPrime,
				vPrime
		);

		return new double[]{
				uPrime + du,
				vPrime + dv
		};
	}

	/**
	 * Invert the forward SIP polynomial if AP/BP are not available.
	 * <p>
	 * Forward SIP is:
	 * <p>
	 * u' = u + A(u,v)
	 * v' = v + B(u,v)
	 * <p>
	 * The iteration below solves this system directly.  It is only a
	 * fallback; SPHEREx TAN-SIP products normally provide AP/BP.
	 */
	private static double[] invertSipForward(
			Header h,
			double uPrime,
			double vPrime) {

		double u = uPrime;
		double v = vPrime;

		final int maxIterations = 30;
		final double tolerance = 1e-9;

		for (int iteration = 0;
		     iteration < maxIterations;
		     iteration++) {

			double a = sipPolynomial(
					h,
					"A",
					"A_ORDER",
					u,
					v
			);

			double b = sipPolynomial(
					h,
					"B",
					"B_ORDER",
					u,
					v
			);

			double nextU = uPrime - a;
			double nextV = vPrime - b;

			double delta = Math.max(
					Math.abs(nextU - u),
					Math.abs(nextV - v)
			);

			u = nextU;
			v = nextV;

			if (delta < tolerance)
				break;
		}

		return new double[]{
				u,
				v
		};
	}

	/**
	 * Evaluate a SIP polynomial of the form
	 * <p>
	 * sum C_i_j * u^i * v^j
	 * <p>
	 * including the constant term where present.
	 */
	private static double sipPolynomial(
			Header h,
			String prefix,
			String orderKeyword,
			double u,
			double v) {

		int order = h.getIntValue(orderKeyword, 0);

		if (order <= 0)
			return 0.0;

		double sum = 0.0;

		/*
		 * Build powers once.  SPHEREx currently uses low-order SIP
		 * polynomials (typically order 3), so this is inexpensive and
		 * avoids repeated Math.pow() calls.
		 */
		double[] up = new double[order + 1];
		double[] vp = new double[order + 1];

		up[0] = 1.0;
		vp[0] = 1.0;

		for (int i = 1; i <= order; i++) {
			up[i] = up[i - 1] * u;
			vp[i] = vp[i - 1] * v;
		}

		for (int i = 0; i <= order; i++) {
			for (int j = 0; j <= order - i; j++) {
				String key = prefix + "_" + i + "_" + j;

				if (!h.containsKey(key))
					continue;

				double coefficient =
						h.getDoubleValue(key, 0.0);

				if (!Double.isFinite(coefficient) ||
						coefficient == 0.0)
					continue;

				sum += coefficient *
						up[i] *
						vp[j];
			}
		}

		return sum;
	}

	private static boolean hasSipInverse(Header h) {
		return h.containsKey("AP_ORDER") ||
				h.containsKey("BP_ORDER");
	}

	private static boolean hasSipForward(Header h) {
		return h.containsKey("A_ORDER") ||
				h.containsKey("B_ORDER");
	}

	private static double wrapDeltaRa(double deltaDeg) {
		double d = deltaDeg % 360.0;
		if (d > 180.0) d -= 360.0;
		if (d < -180.0) d += 360.0;
		return d;
	}

	/**
	 * Return the FITS CD matrix in degrees/pixel.
	 * <p>
	 * If CDi_j exists, it is authoritative. Otherwise:
	 * <p>
	 * CD = diag(CDELT) * PC
	 * <p>
	 * This is important because using PCi_j directly would silently
	 * drop CDELT.
	 */
	private static double[] getCdMatrix(Header h) {
		boolean hasCd =
				h.containsKey("CD1_1") ||
						h.containsKey("CD1_2") ||
						h.containsKey("CD2_1") ||
						h.containsKey("CD2_2");

		if (hasCd) {
			return new double[]{
					h.getDoubleValue("CD1_1", 0),
					h.getDoubleValue("CD1_2", 0),
					h.getDoubleValue("CD2_1", 0),
					h.getDoubleValue("CD2_2", 0)
			};
		}

		double cdelt1 =
				h.getDoubleValue("CDELT1");
		double cdelt2 =
				h.getDoubleValue("CDELT2");

		double pc11 =
				h.getDoubleValue("PC1_1", 1);
		double pc12 =
				h.getDoubleValue("PC1_2", 0);
		double pc21 =
				h.getDoubleValue("PC2_1", 0);
		double pc22 =
				h.getDoubleValue("PC2_2", 1);

		return new double[]{
				cdelt1 * pc11,
				cdelt1 * pc12,
				cdelt2 * pc21,
				cdelt2 * pc22
		};
	}

	private static double wavelengthOld(Header h, double x, int detector) {
		String ctype = h.getStringValue("CTYPE1W", "");
		if (!ctype.contains("-TAB")) {
			double value = h.getDoubleValue("CRVAL1W", h.getDoubleValue("CRVAL3", Double.NaN)) + (x - (h.getDoubleValue("CRPIX1W", h.getDoubleValue("CRPIX3", 1)) - 1)) * h.getDoubleValue("CD1_1W", h.getDoubleValue("CDELT3", Double.NaN));
			if (Double.isFinite(value)) return value > 100 ? value / 1_000_000 : value;
		}
		// QR2 WAVE-TAB is a two-dimensional lookup table.  Until its complete
		// FITS-table interpolation is added, retain a physically meaningful
		// detector-coordinate fallback instead of treating table indices as µm.
		double[][] bands = {{0, 0}, {0.75, 1.11}, {1.11, 1.64}, {1.64, 2.42}, {2.42, 3.82}, {3.82, 4.42}, {4.42, 5.00}};
		double parentX = x + 1 - h.getDoubleValue("CRPIX1A", 1);
		double fraction = Math.max(0, Math.min(1, parentX / 2039.0));
		return bands[detector][0] + fraction * (bands[detector][1] - bands[detector][0]);
	}

	private static List<Point> binOld(List<Point> input) {
		Map<Integer, List<Point>> groups = new HashMap<>();
		for (Point p : input) groups.computeIfAbsent(p.detector, k -> new ArrayList<>()).add(p);
		List<Point> out = new ArrayList<>();
		for (var e : groups.entrySet()) {
			List<Point> p = e.getValue();
			p.sort(Comparator.comparingDouble(Point::wavelengthUm));
			int bins = Math.min(17, p.size());
			for (int b = 0; b < bins; b++) {
				List<Point> q = p.subList(b * p.size() / bins, (b + 1) * p.size() / bins);
				double w = 0, x = 0, y = 0;
				for (Point v : q) {
					double z = 1 / (v.errorUjy * v.errorUjy);
					w += z;
					x += z * v.wavelengthUm;
					y += z * v.fluxUjy;
				}
				out.add(new Point(x / w, y / w, Math.sqrt(1 / w), e.getKey(), q.size()));
			}
		}
		return out;
	}

	private static double wavelength(Fits fits, Header h, double x, double y, int detector, SpectralChannels channels) throws Exception {
		BasicHDU<?> hdu = ext(fits, h.getStringValue("PS1_0W", "WCS-WAVE"));
		if (hdu instanceof BinaryTableHDU table) {
			// In FITS -TAB WCS, PSn_1 names the coordinate array (VALUES), while
			// PSn_2 names the matching index vector (X/Y).
			int xc = table.findColumn("X"), yc = table.findColumn("Y"), vc = table.findColumn(h.getStringValue("PS1_1W", "VALUES"));
			if (xc >= 0 && yc >= 0 && vc >= 0 && table.getNRows() > 0) {
				int[] gx = integers(table.getElement(0, xc)), gy = integers(table.getElement(0, yc));
				double[][][] values = doubles3d(table.getElement(0, vc));
				double px = x + 1 - h.getDoubleValue("CRPIX1A", 1), py = y + 1 - h.getDoubleValue("CRPIX2A", 1);
				double value = bilinear(values, gx, gy, px, py);
				if (Double.isFinite(value)) return value;
			}
		}
		int px = (int) Math.round(x + 1 - h.getDoubleValue("CRPIX1A", 1)) - 1, py = (int) Math.round(y + 1 - h.getDoubleValue("CRPIX2A", 1)) - 1;
		if (py >= 0 && px >= 0 && py < channels.channelMap.length && px < channels.channelMap[0].length) {
			int number = channels.channelMap[py][px];
			for (Channel channel : channels.byDetector.getOrDefault(detector, List.of()))
				if (channel.number == number) return channel.centerUm;
		}
		return Double.NaN;
	}

	private static double bilinear(double[][][] values, int[] gx, int[] gy, double x, double y) {
		int ix = bracket(gx, x), iy = bracket(gy, y);
		if (ix < 0 || iy < 0 || values.length != gy.length || values[iy].length != gx.length) return Double.NaN;
		double tx = (x - gx[ix]) / (gx[ix + 1] - gx[ix]), ty = (y - gy[iy]) / (gy[iy + 1] - gy[iy]);
		double a = values[iy][ix][0] * (1 - tx) + values[iy][ix + 1][0] * tx;
		double b = values[iy + 1][ix][0] * (1 - tx) + values[iy + 1][ix + 1][0] * tx;
		return a * (1 - ty) + b * ty;
	}

	private static int bracket(int[] grid, double value) {
		if (!Double.isFinite(value) || grid.length < 2 || value < grid[0] || value > grid[grid.length - 1]) return -1;
		int index = Arrays.binarySearch(grid, (int) Math.floor(value));
		if (index < 0) index = -index - 2;
		return Math.max(0, Math.min(grid.length - 2, index));
	}

	/**
	 * Remove invalid raw measurements before the optional spectral outlier
	 * rejection.  This corresponds to the sanitize_flux stage in SPExPI.
	 * Negative fluxes are deliberately retained.
	 */
	private static List<Point> sanitizePoints(List<Point> input) {
		List<Point> out = new ArrayList<>(input.size());
		for (Point point : input) {
			if (point == null ||
					!Double.isFinite(point.wavelengthUm()) ||
					!Double.isFinite(point.fluxUjy()) ||
					!Double.isFinite(point.errorUjy()) ||
					point.errorUjy() <= 0 ||
					point.fluxUjy() == 0) {
				continue;
			}
			out.add(point);
		}
		return out;
	}

	/**
	 * Match SPExPI's remove_outliers_per_detector() implementation.
	 * <p>
	 * For each selected detector the wavelength range is divided into a
	 * small number of coarse bins.  Within each bin, a median/MAD criterion
	 * rejects strong flux excursions.  The uncertainty is used only to
	 * decide whether a measurement is valid; it is not used to define the
	 * outlier threshold.
	 * <p>
	 * The Python implementation uses the strict criterion
	 * <p>
	 * abs(flux - median) < sigma * 1.4826 * MAD
	 * <p>
	 * and retains all finite valid points when MAD == 0.
	 */
	private static List<Point> removeOutliersPerDetector(
			List<Point> input,
			int nbrOfBins,
			double sigma,
			Set<Integer> detectorsToCheck) {

		Map<Integer, double[]> ranges = new HashMap<>();
		ranges.put(1, new double[]{0.75, 1.11});
		ranges.put(2, new double[]{1.11, 1.64});
		ranges.put(3, new double[]{1.64, 2.42});
		ranges.put(4, new double[]{2.42, 3.82});
		ranges.put(5, new double[]{3.82, 4.42});
		ranges.put(6, new double[]{4.42, 5.00});

		Map<Integer, List<Point>> byDetector = new TreeMap<>();
		for (Point point : input) {
			byDetector.computeIfAbsent(point.detector(), ignored -> new ArrayList<>()).add(point);
		}

		List<Point> out = new ArrayList<>(input.size());

		for (var entry : byDetector.entrySet()) {
			int detector = entry.getKey();
			List<Point> detectorPoints = entry.getValue();

			if (!detectorsToCheck.contains(detector)) {
				out.addAll(detectorPoints);
				continue;
			}

			double[] range = ranges.get(detector);
			double lo;
			double hi;

			if (range != null) {
				lo = range[0];
				hi = range[1];
			} else {
				lo = detectorPoints.stream()
						.mapToDouble(Point::wavelengthUm)
						.min().orElse(Double.NaN);
				hi = detectorPoints.stream()
						.mapToDouble(Point::wavelengthUm)
						.max().orElse(Double.NaN);
			}

			if (!Double.isFinite(lo) || !Double.isFinite(hi) || hi <= lo)
				continue;

			double width = (hi - lo) / nbrOfBins;

			for (int bin = 0; bin < nbrOfBins; bin++) {
				double edgeLo = lo + bin * width;
				double edgeHi = lo + (bin + 1) * width;

				List<Point> binPoints = new ArrayList<>();
				for (Point point : detectorPoints) {
					double wl = point.wavelengthUm();
					if (wl >= edgeLo && wl < edgeHi)
						binPoints.add(point);
				}

				if (binPoints.isEmpty())
					continue;

				List<Double> finiteFlux = new ArrayList<>();
				for (Point point : binPoints) {
					if (Double.isFinite(point.fluxUjy()) &&
							Double.isFinite(point.errorUjy()) &&
							point.errorUjy() > 0) {
						finiteFlux.add(point.fluxUjy());
					}
				}

				if (finiteFlux.isEmpty())
					continue;

				double med = median(finiteFlux);
				List<Double> absoluteDeviations = new ArrayList<>(finiteFlux.size());
				for (double flux : finiteFlux)
					absoluteDeviations.add(Math.abs(flux - med));
				double mad = median(absoluteDeviations);

				/*
				 * Reproduce the Python behaviour exactly: if MAD is zero (or
				 * non-finite), do not reject any finite valid point.
				 */
				if (!Double.isFinite(mad) || mad == 0.0) {
					for (Point point : binPoints) {
						if (Double.isFinite(point.fluxUjy()) &&
								Double.isFinite(point.errorUjy()) &&
								point.errorUjy() > 0) {
							out.add(point);
						}
					}
					continue;
				}

				double threshold = sigma * 1.4826 * mad;
				for (Point point : binPoints) {
					if (!Double.isFinite(point.fluxUjy()) ||
							!Double.isFinite(point.errorUjy()) ||
							point.errorUjy() <= 0)
						continue;

					/* Python uses a strict '<' comparison. */
					if (Math.abs(point.fluxUjy() - med) < threshold)
						out.add(point);
				}
			}
		}

		return out;
	}

	private static List<Point> bin(
			List<Point> input,
			Map<String, SpectralChannels> calibrations) {

		/*
		 * Match the current Python default:
		 *
		 *     use_spectral_channels_for_binning = False
		 *
		 * so oversampling is reduced using the detector-specific
		 * resolving-power grids, not the spectral_channels calibration.
		 */
		Map<Integer, double[]> bands = new HashMap<>();
		bands.put(1, new double[]{0.75, 1.11, 41});
		bands.put(2, new double[]{1.11, 1.64, 41});
		bands.put(3, new double[]{1.64, 2.42, 41});
		bands.put(4, new double[]{2.42, 3.82, 35});
		bands.put(5, new double[]{3.82, 4.42, 110});
		bands.put(6, new double[]{4.42, 5.00, 130});

		List<Point> out = new ArrayList<>();

		Map<Integer, List<Point>> groups = new HashMap<>();
		for (Point point : input) {
			if (point == null ||
					!Double.isFinite(point.wavelengthUm()) ||
					!Double.isFinite(point.fluxUjy()) ||
					!Double.isFinite(point.errorUjy()) ||
					point.errorUjy() <= 0 ||
					point.fluxUjy() == 0)
				continue;

			groups.computeIfAbsent(
					point.detector(),
					ignored -> new ArrayList<>()
			).add(point);
		}

		for (var entry : groups.entrySet()) {
			double[] band = bands.get(entry.getKey());
			if (band == null)
				continue;

			double lo = band[0];
			double hi = band[1];
			int resolvingPower = (int) band[2];

			double[] edges = new double[18];
			edges[0] = lo;

			for (int i = 1; i < edges.length; i++)
				edges[i] =
						lo * Math.pow(
								1.0 + 1.0 / resolvingPower,
								i
						);

			double normalization =
					hi / edges[edges.length - 1];

			for (int i = 0; i < edges.length; i++)
				edges[i] *= normalization;

			List<Point> detectorPoints = entry.getValue();

			for (int i = 0; i < edges.length - 1; i++) {
				double edgeLo = edges[i];
				double edgeHi = edges[i + 1];

				double weightSum = 0.0;
				double fluxSum = 0.0;
				int count = 0;

				for (Point point : detectorPoints) {
					double wl = point.wavelengthUm();

					if (wl < edgeLo || wl >= edgeHi)
						continue;

					double weight =
							1.0 /
									(point.errorUjy() * point.errorUjy());

					weightSum += weight;
					fluxSum += weight * point.fluxUjy();
					count++;
				}

				if (count == 0 || weightSum <= 0)
					continue;

				double center =
						Math.sqrt(edgeLo * edgeHi);

				out.add(new Point(
						center,
						fluxSum / weightSum,
						Math.sqrt(1.0 / weightSum),
						entry.getKey(),
						count
				));
			}
		}

		return out;
	}

	private static long flagMask() {
		long m = 0L;

		for (int b : new int[]{
				0, 1, 2, 4, 6, 7, 9, 10, 11,
				14, 15, 17, 19, 22, 24, 26, 27, 28, 29
		}) {
			m |= 1L << b;
		}

		return m;
	}

	private static int[] integers(Object value) {
		if (value instanceof int[] a) return a;
		if (value instanceof short[] a) {
			int[] out = new int[a.length];
			for (int i = 0; i < a.length; i++) out[i] = a[i];
			return out;
		}
		if (value instanceof byte[] a) {
			int[] out = new int[a.length];
			for (int i = 0; i < a.length; i++) out[i] = Byte.toUnsignedInt(a[i]);
			return out;
		}
		throw new IllegalArgumentException("Unsupported FITS integer vector type: " + value.getClass().getName());
	}

	private static Number number(Object value) {
		if (value instanceof Number number) return number;
		if (value instanceof byte[] a && a.length == 1) return Byte.toUnsignedInt(a[0]);
		if (value instanceof short[] a && a.length == 1) return a[0];
		if (value instanceof int[] a && a.length == 1) return a[0];
		if (value instanceof long[] a && a.length == 1) return a[0];
		if (value instanceof float[] a && a.length == 1) return a[0];
		if (value instanceof double[] a && a.length == 1) return a[0];
		throw new IllegalArgumentException("Expected a scalar FITS table value");
	}

	private static int[][] integers2d(Object value) {
		if (value instanceof int[][] a) return a;
		if (value instanceof short[][] a) {
			int[][] out = new int[a.length][a[0].length];
			for (int y = 0; y < a.length; y++) for (int x = 0; x < a[0].length; x++) out[y][x] = a[y][x];
			return out;
		}
		if (value instanceof byte[][] a) {
			int[][] out = new int[a.length][a[0].length];
			for (int y = 0; y < a.length; y++)
				for (int x = 0; x < a[0].length; x++) out[y][x] = Byte.toUnsignedInt(a[y][x]);
			return out;
		}
		throw new IllegalArgumentException("Unsupported FITS integer image type");
	}

	private static double[][][] doubles3d(Object value) {
		if (value instanceof double[][][] a) return a;
		if (value instanceof float[][][] a) {
			double[][][] out = new double[a.length][a[0].length][a[0][0].length];
			for (int y = 0; y < a.length; y++)
				for (int x = 0; x < a[0].length; x++)
					for (int z = 0; z < a[0][0].length; z++) out[y][x][z] = a[y][x][z];
			return out;
		}
		throw new IllegalArgumentException("Unsupported WCS-WAVE VALUES type");
	}

	private static double[][] doubles(Object o) {
		if (o == null) return null;
		if (o instanceof double[][] a) return a;
		if (o instanceof float[][] a) {
			double[][] r = new double[a.length][a[0].length];
			for (int y = 0; y < a.length; y++) for (int x = 0; x < a[0].length; x++) r[y][x] = a[y][x];
			return r;
		}
		throw new IllegalArgumentException("Unsupported FITS image type");
	}

	private static long[][] longs(Object o) {
		if (o == null) return null;
		long[][] r;
		if (o instanceof int[][] a) {
			r = new long[a.length][a[0].length];
			for (int y = 0; y < a.length; y++) for (int x = 0; x < a[0].length; x++) r[y][x] = a[y][x];
			return r;
		}
		if (o instanceof short[][] a) {
			r = new long[a.length][a[0].length];
			for (int y = 0; y < a.length; y++) for (int x = 0; x < a[0].length; x++) r[y][x] = a[y][x];
			return r;
		}
		if (o instanceof long[][] a) return a;
		throw new IllegalArgumentException("Unsupported FLAGS type");
	}

	/**
	 * Stack images using already-downloaded FITS files (avoids re-downloading).
	 */
	public static List<Map<String, Object>> stackImages(List<Path> fitsFiles, Progress progress) throws Exception {
		return stackImages(fitsFiles, progress, Double.NaN, Double.NaN);
	}

	/**
	 * Stack already-downloaded detector cutouts on a common celestial grid.
	 *
	 * @param fitsFiles FITS cutouts to stack
	 * @param progress  progress callback
	 * @param ra        requested output centre RA in degrees; NaN keeps the
	 *                  reference cutout centre
	 * @param dec       requested output centre Dec in degrees; NaN keeps the
	 *                  reference cutout centre
	 */
	public static List<Map<String, Object>> stackImages(
			List<Path> fitsFiles, Progress progress,
			double ra, double dec) throws Exception {
		Map<String, List<ImageStacker.DetectorCutout>> detectorCutouts = new HashMap<>();
		long fatalMask = buildFatalMask(ImageStacker.DEFAULT_FATAL_FLAG_BITS);

		for (int i = 0; i < fitsFiles.size(); i++) {
			Path fitsPath = fitsFiles.get(i);
			progress.update("Processing cutout " + (i + 1) + " of " + fitsFiles.size() + " for stacking...");
			try {
				extractDetectorCutout(fitsPath, 0, 0, 120, detectorCutouts);
			} catch (Exception ex) {
				// Continue with other cutouts
				progress.update("Skipping cutout " + (i + 1) + ": " + ex.getMessage());
			}
		}

		if (detectorCutouts.isEmpty()) {
			throw new IOException("No usable detector cutouts were extracted.");
		}

		progress.update("Found detectors: " + String.join(", ", detectorCutouts.keySet()));

		List<Map<String, Object>> results = new ArrayList<>();
		progress.update("Stacking detector cutouts...");

		// Sort detectors numerically (1-6) not lexicographically (1, 10, 2, ...)
		List<String> sortedDetectors = new ArrayList<>(detectorCutouts.keySet());
		sortedDetectors.sort((a, b) -> Integer.compare(Integer.parseInt(a), Integer.parseInt(b)));

		for (String detector : sortedDetectors) {
			List<ImageStacker.DetectorCutout> cutouts = detectorCutouts.get(detector);

			try {
				ImageStacker.StackResult stackResult =
						ImageStacker.meanStackDetectorCutouts(
								detector,
								cutouts,
								fatalMask,
								true,
								ra,
								dec);

				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put("band", "D" + detector);
				resultMap.put("hdu", stackResult.stackedImage);
				// Preserve the common output WCS for interactive pixel-to-sky conversion.
				resultMap.put("header", stackResult.header);
				// Actual target position in the reprojected image, in 0-based pixels.
				resultMap.put("target_pixel_x", stackResult.targetPixelX);
				resultMap.put("target_pixel_y", stackResult.targetPixelY);
				resultMap.put("phot_radii", new double[]{
						ImageStacker.DEFAULT_APERTURE_RADIUS_PIX,
						ImageStacker.DEFAULT_BACKGROUND_INNER_RADIUS_PIX,
						ImageStacker.DEFAULT_BACKGROUND_OUTER_RADIUS_PIX
				});
				results.add(resultMap);
				progress.update("Stacked detector D" + detector + ": " + stackResult.nStackedImages + "/" + stackResult.nInputImages + " cutouts");
			} catch (Exception ex) {
				progress.update("Failed to stack detector D" + detector + ": " + ex.getMessage());
			}
		}

		progress.update("Successfully stacked " + results.size() + " detectors");
		return results;
	}

	/**
	 * Extract detector cutout from FITS file.
	 */
	private static void extractDetectorCutout(Path fitsPath, double raDeg, double decDeg,
	                                          int cutoutArcsec, Map<String, List<ImageStacker.DetectorCutout>> result) throws Exception {
		try (Fits fits = new Fits(fitsPath.toFile())) {
			BasicHDU<?> imageHdu = ext(fits, "IMAGE");
			if (imageHdu == null) throw new IOException("IMAGE extension missing");

			Header h = imageHdu.getHeader();
			int detector = h.getIntValue("DETECTOR", h.getIntValue("BAND", 0));
			if (detector < 1 || detector > 6) throw new IOException("Invalid detector number: " + detector);

			String dateObs = h.getStringValue("DATE-OBS", "");
			Object imageKernel = imageHdu.getKernel();
			if (imageKernel == null) throw new IOException("IMAGE kernel is null");

			double[][] imageData = doubles(imageKernel);
			if (imageData == null || imageData.length == 0) {
				throw new IOException("IMAGE data is empty");
			}

			// Extract ZODI and subtract
			Object zodiRaw = extData(fits, "ZODI");
			if (zodiRaw != null) {
				double[][] zodi = doubles(zodiRaw);
				if (zodi != null && zodi.length == imageData.length && zodi[0].length == imageData[0].length) {
					for (int y = 0; y < imageData.length; y++) {
						for (int x = 0; x < imageData[0].length; x++) {
							imageData[y][x] -= zodi[y][x];
						}
					}
				}
			}

			// Get FLAGS (handle null case)
			Object flagsRaw = extData(fits, "FLAGS");
			long[][] flags = null;
			if (flagsRaw != null) {
				flags = longs(flagsRaw);
			}
			if (flags == null) {
				// If no FLAGS extension, create empty flags (all zero = no bad pixels)
				flags = new long[imageData.length][imageData[0].length];
			}

			String detectorStr = String.valueOf(detector);
			result.computeIfAbsent(detectorStr, k -> new ArrayList<>())
					.add(new ImageStacker.DetectorCutout(
							dateObs,
							imageData,
							flags,
							h,
							fitsPath.getFileName().toString()
					));
		}
	}

	/**
	 * Build fatal FLAGS bit mask.
	 */
	private static long buildFatalMask(double[] fatalFlagBits) {
		long mask = 0;
		for (double bit : fatalFlagBits) {
			mask |= (1L << (int) bit);
		}
		return mask;
	}

	/**
	 * Build fatal FLAGS bit mask from an integer array.
	 */
	private static long buildFatalMask(int[] fatalFlagBits) {
		long mask = 0;
		for (int bit : fatalFlagBits) {
			mask |= (1L << bit);
		}
		return mask;
	}
}
