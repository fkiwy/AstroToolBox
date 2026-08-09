package astro.tool.box.spherex;

import nom.tam.fits.*;

import java.io.*;
import java.nio.file.*;
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
		public final String sourceFile;

		public DetectorCutout(String dateObs, double[][] data, long[][] flags, String sourceFile) {
			this.dateObs = dateObs;
			this.data = data;
			this.flags = flags;
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
	public static StackResult meanStackDetectorCutouts(
			String detector, List<DetectorCutout> cutouts, long fatalMask,
			boolean includeReferenceImage) throws Exception {

		if (cutouts.isEmpty()) {
			throw new IllegalArgumentException("No cutouts supplied for detector " + detector);
		}

		// Sort by date
		List<DetectorCutout> sorted = new ArrayList<>(cutouts);
		sorted.sort((a, b) -> a.dateObs.compareTo(b.dateObs));

		DetectorCutout reference = sorted.get(0);
		int refHeight = reference.data.length;
		int refWidth = reference.data[0].length;

		double[][] stacked = new double[refHeight][refWidth];
		double[][] counts = new double[refHeight][refWidth];

		List<DetectorCutout> inputCutouts = includeReferenceImage ? sorted : sorted.subList(1, sorted.size());

		for (DetectorCutout cutout : inputCutouts) {
			double[][] data = cropCenter(cutout.data, refHeight, refWidth);
			long[][] flags = cropCenterLong(cutout.flags, refHeight, refWidth);

			for (int y = 0; y < refHeight; y++) {
				for (int x = 0; x < refWidth; x++) {
					boolean validFlags = (flags[y][x] & fatalMask) == 0;
					boolean validData = Double.isFinite(data[y][x]);
					if (validFlags && validData) {
						stacked[y][x] += data[y][x];
						counts[y][x] += 1;
					}
				}
			}
		}

		double[][] meanImage = new double[refHeight][refWidth];
		for (int y = 0; y < refHeight; y++) {
			for (int x = 0; x < refWidth; x++) {
				if (counts[y][x] > 0) {
					meanImage[y][x] = stacked[y][x] / counts[y][x];
				} else {
					meanImage[y][x] = Double.NaN;
				}
			}
		}

		Header header = new Header();
		header.addValue("NSTACK", inputCutouts.size(), "Number of cutouts included in stack");
		header.addValue("NINPUT", sorted.size(), "Number of input cutouts for detector");
		header.addValue("INCLREF", includeReferenceImage, "Reference cutout included in mean stack");
		header.addValue("STACKTYP", "MEAN", "Stack combination method");
		header.addValue("ZODISUB", true, "IMAGE - ZODI before stacking");

		return new StackResult(detector, meanImage, header, sorted.size(), inputCutouts.size());
	}

	/**
	 * Center-crop a 2D array.
	 */
	private static double[][] cropCenter(double[][] data, int targetHeight, int targetWidth) {
		int height = data.length;
		int width = data[0].length;

		if (targetHeight > height || targetWidth > width) {
			throw new IllegalArgumentException(
					"Cannot crop to larger size: " + height + "x" + width + " -> " + targetHeight + "x" + targetWidth);
		}

		int startY = (height - targetHeight) / 2;
		int startX = (width - targetWidth) / 2;

		double[][] result = new double[targetHeight][targetWidth];
		for (int y = 0; y < targetHeight; y++) {
			System.arraycopy(data[startY + y], startX, result[y], 0, targetWidth);
		}
		return result;
	}

	/**
	 * Center-crop a 2D long array (for flags).
	 */
	private static long[][] cropCenterLong(long[][] data, int targetHeight, int targetWidth) {
		int height = data.length;
		int width = data[0].length;

		if (targetHeight > height || targetWidth > width) {
			throw new IllegalArgumentException(
					"Cannot crop to larger size: " + height + "x" + width + " -> " + targetHeight + "x" + targetWidth);
		}

		int startY = (height - targetHeight) / 2;
		int startX = (width - targetWidth) / 2;

		long[][] result = new long[targetHeight][targetWidth];
		for (int y = 0; y < targetHeight; y++) {
			System.arraycopy(data[startY + y], startX, result[y], 0, targetWidth);
		}
		return result;
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
