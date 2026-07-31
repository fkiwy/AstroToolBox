package astro.tool.box.spherex;

import nom.tam.fits.*;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

/**
 * Aperture-only SPHEREx extraction service used by {@code SpherexViewerTab}.
 */
public final class SpherexPipeline {
	private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
	private static final String TAP = "https://irsa.ipac.caltech.edu/TAP/sync";
	private static final String SAPM_COLLECTION = "cal-sapm-v2-2025-164";
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

	public record Result(List<Point> points, int discovered, int measured, List<String> warnings) {
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
		Map<Integer, double[][]> sapms = new HashMap<>();
		for (int i = 0; i < urls.size(); i++) {
			progress.update("Downloading and measuring cutout " + (i + 1) + " of " + urls.size() + "…");
			try {
				Point p = measure(download(urls.get(i), config.cacheDir().resolve("cutouts"), "cutout_" + i + ".fits"), config, sapms);
				if (p != null) points.add(p);
			} catch (Exception ex) {
				warnings.add("Cutout " + (i + 1) + " skipped: " + ex.getMessage());
			}
		}
		if (points.isEmpty())
			throw new IOException("No usable cutouts were measured." + (warnings.isEmpty() ? "" : " " + warnings.get(0)));
		int measured = points.size();
		if (config.bin()) points = bin(points);
		points.sort(Comparator.comparingDouble(Point::wavelengthUm));
		progress.update("Spectrum complete.");
		return new Result(points, urls.size(), measured, warnings);
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

	private static Point measure(Path path, Config c, Map<Integer, double[][]> sapms) throws Exception {
		try (Fits fits = new Fits(path.toFile())) {
			BasicHDU<?> imageHdu = ext(fits, "IMAGE");
			if (imageHdu == null) throw new IOException("IMAGE extension missing");
			Header h = imageHdu.getHeader();
			int detector = h.getIntValue("DETECTOR", h.getIntValue("BAND", 0));
			if (detector < 1 || detector > 6) throw new IOException("Invalid detector number");
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
			return new Point(wavelength(h, pos[0], detector), result[0], result[1], detector, 1);
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

	private static double wavelength(Header h, double x, int detector) {
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

	private static List<Point> bin(List<Point> input) {
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

	private static int flagMask() {
		int m = 0;
		for (int b : new int[]{0, 1, 2, 4, 6, 7, 9, 10, 11, 14, 15, 17, 19, 22, 24, 26, 27, 28, 29}) m |= 1 << b;
		return m;
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
}
