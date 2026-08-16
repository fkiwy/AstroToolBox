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

		int successCount = 0;
		for (DetectorCutout cutout : inputCutouts) {
			try {
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
				successCount++;
			} catch (Exception e) {
				// Log but continue with other cutouts
				System.err.println("Failed to process cutout " + cutout.sourceFile + " for detector " + detector + ": " + e.getMessage());
			}
		}

		if (successCount == 0) {
			throw new IllegalArgumentException("No cutouts could be successfully processed for detector " + detector);
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
		header.addValue("NSTACK", successCount, "Number of cutouts included in stack");
		header.addValue("NINPUT", sorted.size(), "Number of input cutouts for detector");
		header.addValue("INCLREF", includeReferenceImage, "Reference cutout included in mean stack");
		header.addValue("STACKTYP", "MEAN", "Stack combination method");
		header.addValue("ZODISUB", true, "IMAGE - ZODI before stacking");

		return new StackResult(detector, meanImage, header, sorted.size(), successCount);
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
