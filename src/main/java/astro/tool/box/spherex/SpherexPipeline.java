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
	private static final int BAD_FLAGS = flagMask();

	private SpherexPipeline() {
	}

	public record Config(double raDeg, double decDeg, int cutoutArcsec, double apertureRadius,
	                     boolean bin, Path cacheDir) {
		public Config {
			if (!Double.isFinite(raDeg) || raDeg < 0 || raDeg >= 360 || !Double.isFinite(decDeg) || decDeg < -90 || decDeg > 90)
				throw new IllegalArgumentException("RA must be [0, 360) degrees and Dec must be [-90, 90] degrees.");
			if (cutoutArcsec < 16 || cutoutArcsec > 1800 || apertureRadius <= 0)
				throw new IllegalArgumentException("Use a 16–1800 arcsec cutout and positive aperture radius.");
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

	@FunctionalInterface
	public interface Progress {
		void update(String text);
	}

	public static Result run(Config config, Progress progress) throws Exception {
		Files.createDirectories(config.cacheDir());
		progress.update("Querying IRSA for SPHEREx cutouts…");
		List<String> urls = query(config);
		if (urls.isEmpty()) throw new IOException("No SPHEREx cutouts cover these coordinates.");
		List<Point> points = new ArrayList<>();
		List<String> warnings = new ArrayList<>();
		List<Path> fitsFiles = new ArrayList<>();
		Map<Integer, double[][]> sapms = new HashMap<>();
		Map<String, SpectralChannels> spectralChannels = new HashMap<>();
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
		if (points.isEmpty())
			throw new IOException("No usable cutouts were measured." + (warnings.isEmpty() ? "" : " " + warnings.get(0)));
		int measured = points.size();
		if (config.bin()) points = bin(points, spectralChannels);
		points.sort(Comparator.comparingDouble(Point::wavelengthUm));
		progress.update("Spectrum complete.");
		return new Result(points, urls.size(), measured, warnings, fitsFiles);
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

	private static Point measure(Path path, Config c, Map<Integer, double[][]> sapms,
	                             Map<String, SpectralChannels> spectralChannels) throws Exception {
		try (Fits fits = new Fits(path.toFile())) {
			BasicHDU<?> imageHdu = ext(fits, "IMAGE");
			if (imageHdu == null) throw new IOException("IMAGE extension missing");
			Header h = imageHdu.getHeader();
			int detector = h.getIntValue("DETECTOR", h.getIntValue("BAND", 0));
			if (detector < 1 || detector > 6) throw new IOException("Invalid detector number");
			String release = h.getStringValue("DATAREL", "qr2").toLowerCase(Locale.ROOT);
			SpectralChannels channels = spectralChannels.get(release);
			if (channels == null) {
				channels = spectralChannels(release, c.cacheDir);
				spectralChannels.put(release, channels);
			}
			double[] pos = worldToPixel(h, c.raDeg, c.decDeg);
			double[][] image = doubles(imageHdu.getKernel());
			double[][] sapm = sapms.get(detector);
			if (sapm == null) {
				sapm = sapm(detector, h, c.cacheDir);
				sapms.put(detector, sapm);
			}
			double[][] flux = toUjy(image, extData(fits, "ZODI"), sapm, h);
			double[][] variance = toVariance(extData(fits, "VARIANCE"), sapm, h);
			long[][] flags = longs(extData(fits, "FLAGS"));
			double[] result = aperture(flux, variance, flags, pos[0], pos[1], c.apertureRadius);
			if (!Double.isFinite(result[0]) || !Double.isFinite(result[1]) || result[1] <= 0) return null;
			double wavelength = wavelength(fits, h, pos[0], pos[1], detector, channels);
			if (!Double.isFinite(wavelength)) throw new IOException("Could not determine calibrated wavelength");
			return new Point(wavelength, result[0], result[1], detector, 1);
		}
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
		double[][] z = zodiRaw == null ? null : doubles(zodiRaw), out = new double[image.length][image[0].length];
		int x0 = (int) Math.round(1 - h.getDoubleValue("CRPIX1A", 1)), y0 = (int) Math.round(1 - h.getDoubleValue("CRPIX2A", 1));
		double scale = h.getStringValue("BUNIT", "MJy/sr").toLowerCase(Locale.ROOT).contains("mjy") ? 23.5044306 : 1;
		for (int y = 0; y < out.length; y++)
			for (int x = 0; x < out[0].length; x++) {
				int sy = y + y0, sx = x + x0;
				out[y][x] = (sy < 0 || sx < 0 || sy >= sapm.length || sx >= sapm[0].length) ? Double.NaN : (image[y][x] - (z == null ? 0 : z[y][x])) * scale * sapm[sy][sx];
			}
		return out;
	}

	private static double[][] toVariance(Object raw, double[][] sapm, Header h) {
		if (raw == null) return null;
		double[][] v = doubles(raw), out = new double[v.length][v[0].length];
		int x0 = (int) Math.round(1 - h.getDoubleValue("CRPIX1A", 1)), y0 = (int) Math.round(1 - h.getDoubleValue("CRPIX2A", 1));
		double scale = h.getStringValue("BUNIT", "MJy/sr").toLowerCase(Locale.ROOT).contains("mjy") ? 23.5044306 : 1;
		for (int y = 0; y < out.length; y++)
			for (int x = 0; x < out[0].length; x++) {
				int sy = y + y0, sx = x + x0;
				out[y][x] = (sy < 0 || sx < 0 || sy >= sapm.length || sx >= sapm[0].length) ? Double.NaN : v[y][x] * scale * scale * sapm[sy][sx] * sapm[sy][sx];
			}
		return out;
	}

	private static double[] aperture(double[][] f, double[][] v, long[][] flags, double x, double y, double radius) {
		List<Double> bg = new ArrayList<>();
		double sum = 0, var = 0;
		int n = 0;
		for (int iy = 0; iy < f.length; iy++)
			for (int ix = 0; ix < f[0].length; ix++) {
				if (!Double.isFinite(f[iy][ix]) || (flags != null && (flags[iy][ix] & BAD_FLAGS) != 0)) continue;
				double d = Math.hypot(ix - x, iy - y);
				if (d <= radius) {
					sum += f[iy][ix];
					n++;
					if (v != null && Double.isFinite(v[iy][ix])) var += v[iy][ix];
				}
				if (d >= 2 * radius && d <= 3 * radius) bg.add(f[iy][ix]);
			}
		if (n == 0 || bg.isEmpty()) return new double[]{Double.NaN, Double.NaN};
		bg.sort(Double::compare);
		double med = bg.get(bg.size() / 2), s = 0;
		for (double q : bg) s += (q - med) * (q - med);
		double bgvar = s / Math.max(1, bg.size() - 1);
		return new double[]{sum - n * med, Math.sqrt(Math.max(0, (v == null ? bgvar * n : var) + n * n * bgvar / bg.size()))};
	}

	private static double[] worldToPixel(Header h, double ra, double dec) {
		// QR2 cutouts store the projection matrix as PCi_j with CDELT=1.
		// Prefer CD when available, otherwise use that PC matrix rather than the
		// unit CDELT fallback, which would put the source far outside the cutout.
		double cv1 = h.getDoubleValue("CRVAL1"), cv2 = h.getDoubleValue("CRVAL2"), cp1 = h.getDoubleValue("CRPIX1") - 1, cp2 = h.getDoubleValue("CRPIX2") - 1, a = h.getDoubleValue("CD1_1", h.getDoubleValue("PC1_1", h.getDoubleValue("CDELT1"))), b = h.getDoubleValue("CD1_2", h.getDoubleValue("PC1_2", 0)), d = h.getDoubleValue("CD2_1", h.getDoubleValue("PC2_1", 0)), e = h.getDoubleValue("CD2_2", h.getDoubleValue("PC2_2", h.getDoubleValue("CDELT2")));
		double dx = (ra - cv1) * Math.cos(Math.toRadians(cv2)), dy = dec - cv2, det = a * e - b * d;
		if (det == 0) throw new IllegalArgumentException("Invalid spatial WCS");
		return new double[]{cp1 + (e * dx - b * dy) / det, cp2 + (-d * dx + a * dy) / det};
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

	private static List<Point> bin(List<Point> input, Map<String, SpectralChannels> calibrations) throws IOException {
		if (calibrations.isEmpty()) throw new IOException("QR2 spectral-channel calibration was not loaded");
		SpectralChannels calibration = calibrations.values().iterator().next();
		Map<Integer, List<Point>> groups = new HashMap<>();
		for (Point p : input) groups.computeIfAbsent(p.detector, ignored -> new ArrayList<>()).add(p);
		List<Point> out = new ArrayList<>();
		for (var entry : groups.entrySet())
			for (Channel channel : calibration.byDetector.getOrDefault(entry.getKey(), List.of())) {
				double weight = 0, flux = 0;
				int count = 0;
				for (Point point : entry.getValue())
					if (point.wavelengthUm >= channel.minUm && point.wavelengthUm <= channel.maxUm) {
						double w = 1 / (point.errorUjy * point.errorUjy);
						weight += w;
						flux += w * point.fluxUjy;
						count++;
					}
				if (count > 0 && weight > 0)
					out.add(new Point(channel.centerUm, flux / weight, Math.sqrt(1 / weight), entry.getKey(), count));
			}
		return out;
	}

	private static int flagMask() {
		int m = 0;
		for (int b : new int[]{0, 1, 2, 4, 6, 7, 9, 10, 11, 14, 15, 17, 19, 22, 24, 26, 27, 28, 29}) m |= 1 << b;
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
				ImageStacker.StackResult stackResult = ImageStacker.meanStackDetectorCutouts(
						detector, cutouts, fatalMask, true);

				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put("band", "D" + detector);
				resultMap.put("hdu", stackResult.stackedImage);
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
					.add(new ImageStacker.DetectorCutout(dateObs, imageData, flags, fitsPath.getFileName().toString()));
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
	 * Build fatal FLAGS bit mask from integer array.
	 */
	private static long buildFatalMask(int[] fatalFlagBits) {
		long mask = 0;
		for (int bit : fatalFlagBits) {
			mask |= (1L << bit);
		}
		return mask;
	}
}
