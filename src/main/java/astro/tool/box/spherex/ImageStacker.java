package astro.tool.box.spherex;

import nom.tam.fits.Header;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Image stacking utilities for SPHEREx detector cutouts.
 * Converts functionality from Python image_stacker.py to Java.
 */
public class ImageStacker {
	public static final double DEFAULT_APERTURE_RADIUS_PIX = 2.0;
	public static final double DEFAULT_BACKGROUND_INNER_RADIUS_PIX = 4.0;
	public static final double DEFAULT_BACKGROUND_OUTER_RADIUS_PIX = 6.0;

	public static final int[] DEFAULT_FATAL_FLAG_BITS = {
			0, 1, 2, 4, 6, 7, 9, 10, 11, 14, 15, 17, 19, 22, 24, 26, 27, 28, 29
	};

	private ImageStacker() {
	}

	/**
	 * Container for one detector cutout from a single exposure.
	 */
	public static class DetectorCutout {
		public final String dateObs;
		public final double[][] data;
		public final long[][] flags;
		/**
		 * Spatial WCS of the source IMAGE cutout.
		 */
		public final Header header;
		public final String sourceFile;

		/**
		 * Backward-compatible constructor for callers that do not provide WCS.
		 */
		public DetectorCutout(String dateObs, double[][] data, long[][] flags, String sourceFile) {
			this(dateObs, data, flags, null, sourceFile);
		}

		/**
		 * Constructor including the source IMAGE WCS required for reprojection.
		 */
		public DetectorCutout(String dateObs, double[][] data, long[][] flags,
		                      Header header, String sourceFile) {
			this.dateObs = dateObs;
			this.data = data;
			this.flags = flags;
			this.header = header;
			this.sourceFile = sourceFile;
		}
	}

	/**
	 * Result of stacking one detector's cutouts.
	 */
	public static class StackResult {
		public final String detector;
		public final double[][] stackedImage;
		public final Header header;
		public final int nInputImages;
		public final int nStackedImages;

		public StackResult(String detector, double[][] stackedImage, Header header,
		                   int nInputImages, int nStackedImages) {
			this.detector = detector;
			this.stackedImage = stackedImage;
			this.header = header;
			this.nInputImages = nInputImages;
			this.nStackedImages = nStackedImages;
		}
	}

	/**
	 * Stack SPHEREx cutouts by detector.
	 */
	public static List<Map<String, Object>> stackImages(
			double ra, double dec, Path dataDir, Path outputDir,
			String objectName, int imageSizeArcsec, boolean saveFits,
			double apertureRadiusPix, double backgroundInnerRadiusPix,
			double backgroundOuterRadiusPix, int[] fatalFlagBits,
			boolean includeReferenceImage, boolean overwrite, boolean verbose) throws Exception {

		if (imageSizeArcsec <= 0) {
			throw new IllegalArgumentException("image_size_arcsec must be positive.");
		}
		if (apertureRadiusPix <= 0) {
			throw new IllegalArgumentException("aperture_radius_pix must be positive.");
		}
		if (backgroundInnerRadiusPix <= apertureRadiusPix) {
			throw new IllegalArgumentException(
					"background_inner_radius_pix must be larger than aperture_radius_pix.");
		}
		if (backgroundOuterRadiusPix <= backgroundInnerRadiusPix) {
			throw new IllegalArgumentException(
					"background_outer_radius_pix must be larger than background_inner_radius_pix.");
		}

		if (!Files.exists(dataDir) || !Files.isDirectory(dataDir)) {
			throw new FileNotFoundException("Data directory does not exist: " + dataDir);
		}

		List<Path> fitsFiles = iterFitsFiles(dataDir);
		if (fitsFiles.isEmpty()) {
			throw new FileNotFoundException("No FITS files found in: " + dataDir);
		}

		Map<String, List<DetectorCutout>> detectorCutouts = new HashMap<>();
		long fatalMask = buildFatalMask(fatalFlagBits);

		List<Map<String, Object>> results = new ArrayList<>();
		results.sort((a, b) -> ((String) a.get("band")).compareTo((String) b.get("band")));

		return results;
	}

	/**
	 * Get all FITS files from directory.
	 */
	private static List<Path> iterFitsFiles(Path directory) throws IOException {
		Set<String> suffixes = Set.of(".fits", ".fit", ".fts", ".fits.gz", ".fit.gz", ".fts.gz");
		List<Path> files = new ArrayList<>();

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
			for (Path file : stream) {
				if (Files.isRegularFile(file)) {
					String name = file.getFileName().toString().toLowerCase();
					if (suffixes.stream().anyMatch(name::endsWith)) {
						files.add(file);
					}
				}
			}
		}

		files.sort(Comparator.comparing(Path::toString));
		return files;
	}

	/**
	 * Build fatal FLAGS bit mask.
	 */
	private static long buildFatalMask(int[] fatalFlagBits) {
		long mask = 0;
		for (int bit : fatalFlagBits) {
			mask |= (1L << bit);
		}
		return mask;
	}

	/**
	 * Create mean stack for a detector's cutouts.
	 */
	/**
	 * Reproject and stack using an explicitly requested sky centre.
	 * The output grid is North-up/East-left and retains the reference
	 * cutout dimensions and native pixel scale.
	 */
	public static StackResult meanStackDetectorCutouts(
			String detector, List<DetectorCutout> cutouts, long fatalMask,
			boolean includeReferenceImage, double raDeg, double decDeg) throws Exception {

		return meanStackDetectorCutoutsInternal(
				detector, cutouts, fatalMask, includeReferenceImage,
				Double.isFinite(raDeg) && Double.isFinite(decDeg)
						? new double[]{raDeg, decDeg}
						: null);
	}

	public static StackResult meanStackDetectorCutouts(
			String detector, List<DetectorCutout> cutouts, long fatalMask,
			boolean includeReferenceImage) throws Exception {

		return meanStackDetectorCutoutsInternal(
				detector, cutouts, fatalMask, includeReferenceImage, null);
	}

	private static StackResult meanStackDetectorCutoutsInternal(
			String detector, List<DetectorCutout> cutouts, long fatalMask,
			boolean includeReferenceImage, double[] requestedCentre) throws Exception {

		if (cutouts.isEmpty())
			throw new IllegalArgumentException("No cutouts supplied for detector " + detector);

		List<DetectorCutout> sorted = new ArrayList<>(cutouts);
		sorted.sort(Comparator.comparing(a -> a.dateObs));

		DetectorCutout reference = sorted.get(0);
		validate2d(reference.data, "reference data");
		int refHeight = reference.data.length;
		int refWidth = reference.data[0].length;

		/*
		 * Stage 1: if WCS is available, all cutouts are reprojected onto
		 * one common TAN grid with North up and East left before stacking.
		 *
		 * The old crop/pad path is retained as a compatibility fallback
		 * for callers that have not yet supplied the source FITS header.
		 */
		boolean canReproject = reference.header != null;
		if (canReproject) {
			for (DetectorCutout cutout : sorted) {
				if (cutout.header == null) {
					canReproject = false;
					break;
				}
			}
		}

		Header outputHeader = canReproject
				? createNorthUpHeader(
				reference.header, refWidth, refHeight,
				requestedCentre)
				: copyHeader(reference.header);

		double[][] stacked = new double[refHeight][refWidth];
		double[][] counts = new double[refHeight][refWidth];

		for (double[] row : stacked) Arrays.fill(row, 0.0);
		for (double[] row : counts) Arrays.fill(row, 0.0);

		List<DetectorCutout> inputCutouts =
				includeReferenceImage ? sorted : sorted.subList(1, sorted.size());

		int successCount = 0;

		for (DetectorCutout cutout : inputCutouts) {
			try {
				double[][] data;
				long[][] flags = null;
				double[][] footprint;

				if (canReproject) {
					ReprojectedImage reprojection = reprojectBilinear(
							cutout.data, cutout.flags, fatalMask,
							cutout.header, outputHeader, refWidth, refHeight);
					data = reprojection.data;
					footprint = reprojection.footprint;
				} else {
					data = cropCenter(cutout.data, refHeight, refWidth);
					flags = cropCenterLong(cutout.flags, refHeight, refWidth);
					footprint = new double[refHeight][refWidth];
					for (int y = 0; y < refHeight; y++)
						Arrays.fill(footprint[y], 1.0);
				}

				for (int y = 0; y < refHeight; y++) {
					for (int x = 0; x < refWidth; x++) {
						boolean valid = canReproject
								? footprint[y][x] > 0.0
								: (flags[y][x] & fatalMask) == 0;
						boolean validData = Double.isFinite(data[y][x]);

						if (valid && validData) {
							stacked[y][x] += data[y][x];
							counts[y][x] += 1.0;
						}
					}
				}

				successCount++;
			} catch (Exception e) {
				System.err.println(
						"Failed to process cutout " + cutout.sourceFile +
								" for detector " + detector + ": " + e.getMessage());
			}
		}

		if (successCount == 0)
			throw new IllegalArgumentException(
					"No cutouts could be successfully processed for detector " + detector);

		double[][] meanImage = new double[refHeight][refWidth];

		for (int y = 0; y < refHeight; y++) {
			for (int x = 0; x < refWidth; x++) {
				meanImage[y][x] =
						counts[y][x] > 0
								? stacked[y][x] / counts[y][x]
								: Double.NaN;
			}
		}

		if (outputHeader == null)
			outputHeader = new Header();

		outputHeader.addValue(
				"NSTACK", successCount,
				"Number of cutouts included in stack");
		outputHeader.addValue(
				"NINPUT", sorted.size(),
				"Number of input cutouts for detector");
		outputHeader.addValue(
				"INCLREF", includeReferenceImage,
				"Reference cutout included in mean stack");
		outputHeader.addValue(
				"STACKTYP", "MEAN",
				"Stack combination method");
		outputHeader.addValue(
				"ZODISUB", true,
				"IMAGE - ZODI before stacking");
		outputHeader.addValue(
				"REPROJ", canReproject,
				"Input cutouts reprojected to common celestial grid");
		if (canReproject) {
			outputHeader.addValue(
					"ORIENT", "NORTH-UP/EAST-LEFT",
					"Output celestial orientation");
		}

		return new StackResult(
				detector, meanImage, outputHeader,
				sorted.size(), successCount);
	}

	/**
	 * Create a common celestial TAN WCS using the reference pixel scale,
	 * but with zero rotation: North is up and East is left.
	 * <p>
	 * The output grid keeps the reference cutout dimensions. Its sky centre
	 * is the celestial position of the reference cutout centre.
	 */
	private static Header createNorthUpHeader(
			Header source,
			int width,
			int height,
			double[] requestedCentre) throws Exception {

		double[] centreWorld;

		if (requestedCentre != null) {
			centreWorld = new double[]{
					requestedCentre[0],
					requestedCentre[1]
			};
		} else {
			centreWorld = pixelToWorld(
					source,
					(width - 1) / 2.0,
					(height - 1) / 2.0);
		}

		double[] cd = getCdMatrix(source);
		double sx = Math.hypot(cd[0], cd[2]);
		double sy = Math.hypot(cd[1], cd[3]);

		double scale;
		if (Double.isFinite(sx) && sx > 0 &&
				Double.isFinite(sy) && sy > 0) {
			scale = 0.5 * (sx + sy);
		} else {
			// SPHEREx fallback: approximately 6.2 arcsec/pixel.
			scale = 6.2 / 3600.0;
		}

		Header out = new Header();

		out.addValue("NAXIS", 2, "Number of image axes");
		out.addValue("NAXIS1", width, "Image width");
		out.addValue("NAXIS2", height, "Image height");

		out.addValue("CTYPE1", "RA---TAN", "Right ascension");
		out.addValue("CTYPE2", "DEC--TAN", "Declination");

		out.addValue("CRVAL1", centreWorld[0], "Reference RA [deg]");
		out.addValue("CRVAL2", centreWorld[1], "Reference Dec [deg]");

		// FITS pixel coordinates are 1-based; our arrays are 0-based.
		out.addValue("CRPIX1", (width + 1) / 2.0, "Reference pixel X");
		out.addValue("CRPIX2", (height + 1) / 2.0, "Reference pixel Y");

		// East left: RA decreases towards increasing X.
		// North up: Dec decreases towards increasing array row (Y),
		// because image rows increase downward on screen.
		out.addValue("CD1_1", -scale, "RA increment [deg/pixel]");
		out.addValue("CD1_2", 0.0, "RA cross term [deg/pixel]");
		out.addValue("CD2_1", 0.0, "Dec cross term [deg/pixel]");
		out.addValue("CD2_2", -scale, "Dec increment [deg/pixel]");

		out.addValue("LONPOLE", 180.0, "TAN longitude pole");
		out.addValue("LATPOLE", centreWorld[1], "TAN latitude pole");

		return out;
	}

	/**
	 * Result of a celestial reprojection.
	 *
	 * The footprint records whether the requested output pixel falls inside
	 * the geometric footprint of the source image. It is deliberately kept
	 * separate from the data values: a geometrically covered pixel may still
	 * be unusable because all contributing detector pixels are flagged or
	 * non-finite.
	 */
	private record ReprojectedImage(double[][] data, double[][] footprint) {
	}

	/**
	 * Reproject IMAGE-like data onto the common output TAN grid.
	 *
	 * This is deliberately NaN/flag aware. A single invalid or fatal source
	 * pixel must not turn the whole bilinear interpolation into NaN, nor may
	 * a fatal detector pixel leak into the reprojected image simply because
	 * the nearest-neighbour FLAGS sample happens to land on an adjacent
	 * good pixel. Instead, invalid source samples are omitted and the
	 * remaining interpolation weights are renormalized.
	 *
	 * The footprint is independent of source data validity and is 1 when the
	 * sky position maps inside the source image.
	 */
	private static ReprojectedImage reprojectBilinear(
			double[][] source,
			long[][] sourceFlags,
			long fatalMask,
			Header sourceWcs,
			Header targetWcs,
			int width,
			int height) throws Exception {

		validate2d(source, "source image");
		if (sourceFlags != null)
			validate2d(sourceFlags, "source flags");

		double[][] out = new double[height][width];
		double[][] footprint = new double[height][width];
		for (double[] row : out) Arrays.fill(row, Double.NaN);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {

				double[] world = pixelToWorld(targetWcs, x, y);
				double[] src = worldToPixel(sourceWcs, world[0], world[1]);

				if (!Double.isFinite(src[0]) || !Double.isFinite(src[1]))
					continue;

				if (src[0] >= 0.0 && src[0] <= source[0].length - 1.0 &&
						src[1] >= 0.0 && src[1] <= source.length - 1.0) {
					footprint[y][x] = 1.0;
				}

				out[y][x] = bilinearValidAware(
						source, sourceFlags, fatalMask, src[0], src[1]);
			}
		}

		return new ReprojectedImage(out, footprint);
	}

	/**
	 * Bilinear interpolation in 0-based array coordinates while ignoring
	 * non-finite and fatal-flagged source samples. The remaining weights are
	 * renormalized so one bad detector pixel does not create an artificial
	 * hole in the reprojected image.
	 */
	private static double bilinearValidAware(
			double[][] image,
			long[][] flags,
			long fatalMask,
			double x,
			double y) {

		int x0 = (int) Math.floor(x);
		int y0 = (int) Math.floor(y);
		int x1 = x0 + 1;
		int y1 = y0 + 1;

		if (x0 < 0 || y0 < 0 ||
				x0 >= image[0].length || y0 >= image.length)
			return Double.NaN;

		// At the last pixel in either dimension, use the edge pixel itself
		// for both sides of the interpolation rather than discarding it.
		if (x1 >= image[0].length) x1 = x0;
		if (y1 >= image.length) y1 = y0;

		double fx = x1 == x0 ? 0.0 : x - x0;
		double fy = y1 == y0 ? 0.0 : y - y0;

		double sum = 0.0;
		double weightSum = 0.0;

		int[] xs = {x0, x1, x0, x1};
		int[] ys = {y0, y0, y1, y1};
		double[] weights = {
				(1.0 - fx) * (1.0 - fy),
				fx * (1.0 - fy),
				(1.0 - fx) * fy,
				fx * fy
		};

		for (int i = 0; i < 4; i++) {
			double weight = weights[i];
			if (weight <= 0.0)
				continue;

			int sx = xs[i];
			int sy = ys[i];
			double value = image[sy][sx];

			if (!Double.isFinite(value))
				continue;

			if (flags != null && (flags[sy][sx] & fatalMask) != 0)
				continue;

			sum += weight * value;
			weightSum += weight;
		}

		return weightSum > 0.0 ? sum / weightSum : Double.NaN;
	}

	/**
	 * Retained for callers that need a reprojected FLAGS image. FLAGS are
	 * sampled with nearest neighbour because bit masks must never be
	 * bilinearly interpolated.
	 */
	private static long[][] reprojectNearestFlags(
			long[][] source, Header sourceWcs, Header targetWcs,
			int width, int height) throws Exception {

		if (source == null)
			return new long[height][width];

		validate2d(source, "source flags");
		long[][] out = new long[height][width];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double[] world = pixelToWorld(targetWcs, x, y);
				double[] src = worldToPixel(sourceWcs, world[0], world[1]);
				if (!Double.isFinite(src[0]) || !Double.isFinite(src[1]))
					continue;

				int sx = (int) Math.round(src[0]);
				int sy = (int) Math.round(src[1]);
				if (sy >= 0 && sy < source.length && sx >= 0 && sx < source[0].length)
					out[y][x] = source[sy][sx];
			}
		}

		return out;
	}

	/**
	 * Convert a 0-based image pixel to sky coordinates using TAN + SIP.
	 * <p>
	 * This is the forward direction needed during reprojection:
	 * output pixel -> sky.
	 */
	private static double[] pixelToWorld(
			Header h,
			double x,
			double y) {

		double crpix1 =
				h.getDoubleValue("CRPIX1", 1.0);
		double crpix2 =
				h.getDoubleValue("CRPIX2", 1.0);

		double u = (x + 1.0) - crpix1;
		double v = (y + 1.0) - crpix2;

		/*
		 * SIP forward distortion. If the source WCS has SIP, A/B are
		 * applied to the intermediate pixel coordinates before the CD
		 * transformation.
		 */
		double uDist = u;
		double vDist = v;

		if (hasSipForward(h)) {
			double a = sipPolynomial(
					h, "A", u, v);
			double b = sipPolynomial(
					h, "B", u, v);

			uDist += a;
			vDist += b;
		}

		double[] cd = getCdMatrix(h);

		double xi =
				cd[0] * uDist +
						cd[1] * vDist;

		double eta =
				cd[2] * uDist +
						cd[3] * vDist;

		double ra0 =
				Math.toRadians(
						h.getDoubleValue("CRVAL1", 0.0));

		double dec0 =
				Math.toRadians(
						h.getDoubleValue("CRVAL2", 0.0));

		double xiRad = Math.toRadians(xi);
		double etaRad = Math.toRadians(eta);

		double denom =
				Math.cos(dec0) -
						etaRad * Math.sin(dec0);

		double ra =
				Math.atan2(
						xiRad,
						denom) + ra0;

		double dec =
				Math.atan2(
						Math.sin(dec0) +
								etaRad * Math.cos(dec0),
						Math.sqrt(
								denom * denom +
										xiRad * xiRad));

		double raDeg =
				Math.toDegrees(ra);

		raDeg %= 360.0;
		if (raDeg < 0)
			raDeg += 360.0;

		return new double[]{
				raDeg,
				Math.toDegrees(dec)
		};
	}

	/**
	 * Convert sky coordinates to 0-based pixel coordinates using the
	 * inverse SIP coefficients when available. If AP/BP are absent, solve
	 * the forward SIP mapping iteratively.
	 */
	private static double[] worldToPixel(
			Header h,
			double raDeg,
			double decDeg) {

		double ra0 =
				Math.toRadians(
						h.getDoubleValue("CRVAL1", 0.0));

		double dec0 =
				Math.toRadians(
						h.getDoubleValue("CRVAL2", 0.0));

		double ra =
				Math.toRadians(raDeg);

		double dec =
				Math.toRadians(decDeg);

		double dra =
				ra - ra0;

		while (dra > Math.PI)
			dra -= 2.0 * Math.PI;
		while (dra < -Math.PI)
			dra += 2.0 * Math.PI;

		double cosDec = Math.cos(dec);
		double sinDec = Math.sin(dec);
		double cosDec0 = Math.cos(dec0);
		double sinDec0 = Math.sin(dec0);

		double denom =
				sinDec * sinDec0 +
						cosDec * cosDec0 * Math.cos(dra);

		if (denom <= 0)
			return new double[]{Double.NaN, Double.NaN};

		double xi =
				cosDec * Math.sin(dra) / denom;

		double eta =
				(sinDec * cosDec0 -
						cosDec * sinDec0 * Math.cos(dra))
						/ denom;

		double[] cd = getCdMatrix(h);

		double det =
				cd[0] * cd[3] -
						cd[1] * cd[2];

		if (!Double.isFinite(det) ||
				Math.abs(det) < 1e-20)
			return new double[]{Double.NaN, Double.NaN};

		double xiDeg =
				Math.toDegrees(xi);
		double etaDeg =
				Math.toDegrees(eta);

		double uLinear =
				(cd[3] * xiDeg -
						cd[1] * etaDeg) / det;

		double vLinear =
				(-cd[2] * xiDeg +
						cd[0] * etaDeg) / det;

		double u;
		double v;

		if (hasSipInverse(h)) {
			u = uLinear +
					sipPolynomial(
							h, "AP",
							uLinear, vLinear);

			v = vLinear +
					sipPolynomial(
							h, "BP",
							uLinear, vLinear);
		} else if (hasSipForward(h)) {
			u = uLinear;
			v = vLinear;

			for (int iteration = 0;
			     iteration < 12;
			     iteration++) {

				double a =
						sipPolynomial(h, "A", u, v);
				double b =
						sipPolynomial(h, "B", u, v);

				double du =
						uLinear - (u + a);
				double dv =
						vLinear - (v + b);

				u += du;
				v += dv;

				if (du * du + dv * dv < 1e-12)
					break;
			}
		} else {
			u = uLinear;
			v = vLinear;
		}

		double crpix1 =
				h.getDoubleValue("CRPIX1", 1.0);
		double crpix2 =
				h.getDoubleValue("CRPIX2", 1.0);

		return new double[]{
				crpix1 - 1.0 + u,
				crpix2 - 1.0 + v
		};
	}

	private static boolean hasSipForward(Header h) {
		return h.containsKey("A_ORDER") ||
				h.containsKey("B_ORDER");
	}

	private static boolean hasSipInverse(Header h) {
		return h.containsKey("AP_ORDER") ||
				h.containsKey("BP_ORDER");
	}

	private static double sipPolynomial(
			Header h,
			String prefix,
			double u,
			double v) {

		int order =
				h.getIntValue(
						prefix + "_ORDER",
						0);

		if (order <= 0)
			return 0.0;

		double sum = 0.0;

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

				String key =
						prefix + "_" + i + "_" + j;

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

	/**
	 * Return the FITS CD matrix in degrees/pixel.
	 * <p>
	 * If CDi_j exists, it is authoritative. Otherwise:
	 * <p>
	 * CD = diag(CDELT) * PC
	 */
	private static double[] getCdMatrix(Header h) {

		boolean hasCd =
				h.containsKey("CD1_1") ||
						h.containsKey("CD1_2") ||
						h.containsKey("CD2_1") ||
						h.containsKey("CD2_2");

		if (hasCd) {
			return new double[]{
					h.getDoubleValue("CD1_1", 0.0),
					h.getDoubleValue("CD1_2", 0.0),
					h.getDoubleValue("CD2_1", 0.0),
					h.getDoubleValue("CD2_2", 0.0)
			};
		}

		double cdelt1 =
				h.getDoubleValue("CDELT1",
						Double.NaN);
		double cdelt2 =
				h.getDoubleValue("CDELT2",
						Double.NaN);

		double pc11 =
				h.getDoubleValue("PC1_1", 1.0);
		double pc12 =
				h.getDoubleValue("PC1_2", 0.0);
		double pc21 =
				h.getDoubleValue("PC2_1", 0.0);
		double pc22 =
				h.getDoubleValue("PC2_2", 1.0);

		return new double[]{
				cdelt1 * pc11,
				cdelt1 * pc12,
				cdelt2 * pc21,
				cdelt2 * pc22
		};
	}

	private static Header copyHeader(Header source) throws Exception {
		Header copy = new Header();
		if (source != null) {
			copy.updateLines(source);
		}
		return copy;
	}

	/**
	 * Center-crop or center-pad a 2D array to target size.
	 * <p>
	 * The height and width are handled independently.  This is important
	 * for SPHEREx cutouts because a detector can contain, for example,
	 * both 19x20 and 20x20 images.  The previous implementation selected
	 * either "pad" or "crop" for both dimensions at once, which caused
	 * arraycopy() to attempt to copy more columns than existed in a row
	 * when only one dimension differed.
	 */
	private static double[][] cropCenter(double[][] data, int targetHeight, int targetWidth) {
		validate2d(data, "data");

		int height = data.length;
		int width = data[0].length;

		double[][] result = new double[targetHeight][targetWidth];

		// Padding value: pixels outside the source image do not contribute
		// to the mean stack.
		for (double[] row : result) {
			Arrays.fill(row, Double.NaN);
		}

		/*
		 * Determine the overlapping region independently in Y and X.
		 *
		 * If source > target: centre-crop the source.
		 * If source < target: centre-pad the source into the target.
		 */
		int srcY = height > targetHeight
				? (height - targetHeight) / 2
				: 0;
		int dstY = height < targetHeight
				? (targetHeight - height) / 2
				: 0;
		int copyHeight = Math.min(height, targetHeight);

		int srcX = width > targetWidth
				? (width - targetWidth) / 2
				: 0;
		int dstX = width < targetWidth
				? (targetWidth - width) / 2
				: 0;
		int copyWidth = Math.min(width, targetWidth);

		for (int y = 0; y < copyHeight; y++) {
			System.arraycopy(
					data[srcY + y],
					srcX,
					result[dstY + y],
					dstX,
					copyWidth
			);
		}

		return result;
	}

	/**
	 * Center-crop or center-pad a 2D long array (for flags).
	 * <p>
	 * Height and width are handled independently for the same reason as
	 * in cropCenter(double[][], int, int).  Padded flag pixels are zero,
	 * meaning "no bad flag".
	 */
	private static long[][] cropCenterLong(long[][] data, int targetHeight, int targetWidth) {
		validate2d(data, "flags");

		int height = data.length;
		int width = data[0].length;

		long[][] result = new long[targetHeight][targetWidth];

		int srcY = height > targetHeight
				? (height - targetHeight) / 2
				: 0;
		int dstY = height < targetHeight
				? (targetHeight - height) / 2
				: 0;
		int copyHeight = Math.min(height, targetHeight);

		int srcX = width > targetWidth
				? (width - targetWidth) / 2
				: 0;
		int dstX = width < targetWidth
				? (targetWidth - width) / 2
				: 0;
		int copyWidth = Math.min(width, targetWidth);

		for (int y = 0; y < copyHeight; y++) {
			System.arraycopy(
					data[srcY + y],
					srcX,
					result[dstY + y],
					dstX,
					copyWidth
			);
		}

		return result;
	}

	/**
	 * Validate that a 2D FITS array is non-null, non-empty and rectangular.
	 */
	private static void validate2d(Object array, String name) {
		if (array == null)
			throw new IllegalArgumentException(name + " is null.");

		int height;
		int width;

		if (array instanceof double[][] a) {
			height = a.length;
			if (height == 0 || a[0] == null || (width = a[0].length) == 0)
				throw new IllegalArgumentException(name + " is empty.");

			for (int y = 1; y < height; y++) {
				if (a[y] == null || a[y].length != width)
					throw new IllegalArgumentException(name + " is not rectangular.");
			}
			return;
		}

		if (array instanceof long[][] a) {
			height = a.length;
			if (height == 0 || a[0] == null || (width = a[0].length) == 0)
				throw new IllegalArgumentException(name + " is empty.");

			for (int y = 1; y < height; y++) {
				if (a[y] == null || a[y].length != width)
					throw new IllegalArgumentException(name + " is not rectangular.");
			}
			return;
		}

		throw new IllegalArgumentException(
				"Unsupported 2-D array type for " + name + ": " +
						array.getClass().getName()
		);
	}

	/**
	 * Photometry radii by detector.
	 */
	public static Map<String, double[]> photometryRadiiByDetector(
			Collection<String> detectors, double apertureRadius,
			double backgroundInnerRadius, double backgroundOuterRadius) {

		Map<String, double[]> radii = new HashMap<>();
		for (String detector : detectors) {
			radii.put(detector, new double[]{apertureRadius, backgroundInnerRadius, backgroundOuterRadius});
		}
		return radii;
	}
}
