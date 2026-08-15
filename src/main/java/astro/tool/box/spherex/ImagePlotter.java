package astro.tool.box.spherex;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utilities for plotting SPHEREx cutout images.
 * Converts functionality from Python image_plotter.py to Java.
 */
public class ImagePlotter {

	public record ImageCutout(String band, Object imageData, double[] photometryRadii) {
	}

	public static class PlotConfig {
		public double imageContrast = 10.0;
		public int rows = 1;
		public int cols = 7;
		public int figureWidth = 1400;
		public int figureHeight = 200;

		public PlotConfig(double imageContrast) {
			this.imageContrast = imageContrast;
		}
	}

	private ImagePlotter() {
	}

	/**
	 * Create a diagnostic image grid for a target.
	 */
	public static BufferedImage plotImages(double ra, double dec, List<Map<String, Object>> images,
	                                       double imageSize, double imageContrast) throws Exception {

		PlotConfig config = new PlotConfig(imageContrast);

		if (imageContrast < 0 || imageContrast >= 50) {
			throw new IllegalArgumentException("image_contrast must be in [0, 50)");
		}
		if (config.rows <= 0 || config.cols <= 0) {
			throw new IllegalArgumentException("rows and cols must be positive");
		}

		// Create figure
		BufferedImage figure = new BufferedImage(
				config.figureWidth, config.figureHeight,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = figure.createGraphics();
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, figure.getWidth(), figure.getHeight());

		// Plot individual cutouts
		List<ImageCutout> cutouts = normalizeCutouts(images);
		ImageCutout colorCutout = createD2D4D6ColorCutout(cutouts);
		int panelWidth = config.figureWidth / config.cols;
		int panelHeight = config.figureHeight / config.rows;

		int panelIndex = 0;
		for (ImageCutout cutout : cutouts) {
			int row = panelIndex / config.cols;
			int col = panelIndex % config.cols;
			plotSingleCutout(g2d, config, cutout, col * panelWidth, row * panelHeight,
					panelWidth, panelHeight);
			panelIndex++;
			if ("D6".equals(cutout.band) && colorCutout != null) {
				row = panelIndex / config.cols;
				col = panelIndex % config.cols;
				plotSingleColorCutout(g2d, config, colorCutout, col * panelWidth, row * panelHeight,
						panelWidth, panelHeight);
				panelIndex++;
			}
		}

		g2d.dispose();
		return figure;
	}

	/**
	 * Normalize cutouts from input dictionaries.
	 */
	private static List<ImageCutout> normalizeCutouts(List<Map<String, Object>> images) throws Exception {
		if (images == null || images.isEmpty()) {
			throw new IllegalArgumentException("At least one image cutout is required.");
		}

		List<ImageCutout> cutouts = new java.util.ArrayList<>();
		for (Map<String, Object> image : images) {
			String band = (String) image.get("band");
			if (band == null) {
				throw new IllegalArgumentException("Missing 'band' in image entry");
			}

			Object hduObj = image.get("hdu");
			if (hduObj == null || !(hduObj instanceof double[][])) {
				throw new IllegalArgumentException("Missing or invalid 'hdu' in image entry for band " + band);
			}

			Object radiObj = image.get("phot_radii");
			if (radiObj == null || !(radiObj instanceof double[])) {
				throw new IllegalArgumentException("Missing or invalid 'phot_radii' in image entry for band " + band);
			}

			double[][] imageData = (double[][]) hduObj;
			double[] radii = (double[]) radiObj;

			if (radii.length != 3) {
				throw new IllegalArgumentException("phot_radii must contain exactly 3 values for band " + band);
			}

			validateImageData(imageData, band);
			cutouts.add(new ImageCutout(band, imageData, radii));
		}

		return cutouts;
	}

	/**
	 * Validate image data.
	 */
	private static void validateImageData(double[][] data, String band) throws Exception {
		if (data == null || data.length == 0) {
			throw new IllegalArgumentException("Image data for band " + band + " is empty");
		}
		if (data.length == 0 || data[0].length == 0) {
			throw new IllegalArgumentException("Image data for band " + band + " has invalid shape");
		}
	}

	/**
	 * Plot a single cutout image.
	 */
	private static void plotSingleCutout(Graphics2D g2d, PlotConfig config, ImageCutout cutout,
	                                     int x, int y, int width, int height) {

		double[][] imageData = (double[][]) cutout.imageData;

		// Find min/max for scaling
		double[] limits = robustLimits(imageData, config.imageContrast);
		double vmin = limits[0];
		double vmax = limits[1];

		// Draw grayscale image
		BufferedImage img = imageDataToBufferedImage(imageData, vmin, vmax);
		g2d.drawImage(img, x, y, width, height, null);

		// Draw label (white background, half size)
		g2d.setColor(Color.WHITE);
		g2d.fillRect(x + 5, y + 5, 20, 10);
		g2d.setColor(Color.BLACK);
		g2d.setFont(new Font("Tahoma", Font.PLAIN, 10));
		g2d.drawString(cutout.band, x + 7, y + 13);

		// Draw aperture circles
		drawPhotometryRadii(g2d, x, y, width, height, imageData, cutout.photometryRadii);
	}

	/**
	 * Plot the D2/D4/D6 RGB composite cutout.
	 */
	private static void plotSingleColorCutout(Graphics2D g2d, PlotConfig config, ImageCutout cutout,
	                                          int x, int y, int width, int height) {

		double[][][] channels = (double[][][]) cutout.imageData;
		BufferedImage img = colorChannelsToBufferedImage(channels[0], channels[1], channels[2], config.imageContrast);
		g2d.drawImage(img, x, y, width, height, null);

		g2d.setColor(Color.WHITE);
		g2d.fillRect(x + 5, y + 5, 58, 10);
		g2d.setColor(Color.BLACK);
		g2d.setFont(new Font("Tahoma", Font.PLAIN, 10));
		g2d.drawString(cutout.band, x + 7, y + 13);

		drawPhotometryRadii(g2d, x, y, width, height, channels[2], cutout.photometryRadii);
	}

	/**
	 * Create an RGB composite from D2, D4, and D6.
	 * Blue channel: D2, green channel: D4, red channel: D6.
	 */
	private static ImageCutout createD2D4D6ColorCutout(List<ImageCutout> cutouts) throws Exception {
		ImageCutout d2 = null;
		ImageCutout d4 = null;
		ImageCutout d6 = null;

		for (ImageCutout cutout : cutouts) {
			if ("D2".equals(cutout.band)) {
				d2 = cutout;
			} else if ("D4".equals(cutout.band)) {
				d4 = cutout;
			} else if ("D6".equals(cutout.band)) {
				d6 = cutout;
			}
		}

		if (d2 == null || d4 == null || d6 == null) {
			return null;
		}

		double[][] d2Data = (double[][]) d2.imageData;
		double[][] d4Data = (double[][]) d4.imageData;
		double[][] d6Data = (double[][]) d6.imageData;

		double[][] blue = cropToCommonCenter(d2Data, d4Data, d6Data);
		double[][] green = cropToCommonCenter(d4Data, d2Data, d6Data);
		double[][] red = cropToCommonCenter(d6Data, d2Data, d4Data);

		return new ImageCutout("D2/D4/D6", new double[][][]{red, green, blue}, d6.photometryRadii);
	}

	/**
	 * Convert three image channels to an RGB BufferedImage.
	 */
	private static BufferedImage colorChannelsToBufferedImage(double[][] red, double[][] green,
	                                                          double[][] blue, double imageContrast) {
		int height = red.length;
		int width = red[0].length;

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		double[] redLimits = robustLimits(red, imageContrast);
		double[] greenLimits = robustLimits(green, imageContrast);
		double[] blueLimits = robustLimits(blue, imageContrast);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int r = scaleChannel(red[y][x], redLimits[0], redLimits[1]);
				int g = scaleChannel(green[y][x], greenLimits[0], greenLimits[1]);
				int b = scaleChannel(blue[y][x], blueLimits[0], blueLimits[1]);
				img.setRGB(x, y, (r << 16) | (g << 8) | b);
			}
		}

		return img;
	}

	private static int scaleChannel(double value, double vmin, double vmax) {
		if (!Double.isFinite(value)) {
			value = 0;
		}

		double range = vmax - vmin;
		if (range <= 0) {
			range = 1;
		}

		return (int) Math.min(255, Math.max(0, 255 * (value - vmin) / range));
	}

	/**
	 * Convert image data array to BufferedImage.
	 */
	private static BufferedImage imageDataToBufferedImage(double[][] data, double vmin, double vmax) {
		int height = data.length;
		int width = data[0].length;

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

		double range = vmax - vmin;
		if (range <= 0) range = 1;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double value = data[y][x];
				if (!Double.isFinite(value)) {
					value = 0;
				}
				int gray = (int) Math.min(255, Math.max(0, 255 * (value - vmin) / range));
				img.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
			}
		}

		return img;
	}

	/**
	 * Draw photometry aperture circles.
	 */
	private static void drawPhotometryRadii(Graphics2D g2d, int panelX, int panelY,
	                                        int panelWidth, int panelHeight, double[][] imageData, double[] radii) {

		Object oldAntialiasing = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		Object oldStrokeControl = g2d.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		int imgHeight = imageData.length;
		int imgWidth = imageData[0].length;

		double centerX = imgWidth / 2.0;
		double centerY = imgHeight / 2.0;

		double scaleX = (double) panelWidth / imgWidth;
		double scaleY = (double) panelHeight / imgHeight;
		double radiusScale = Math.min(scaleX, scaleY);

		double displayCenterX = panelX + centerX * scaleX;
		double displayCenterY = panelY + centerY * scaleY;

		double apertureRadius = radii[0];
		double innerBgRadius = radii[1];
		double outerBgRadius = radii[2];

		// Draw aperture (red)
		g2d.setColor(Color.MAGENTA);
		g2d.setStroke(new BasicStroke(1.0f));
		drawScaledRadius(g2d, displayCenterX, displayCenterY, apertureRadius, radiusScale);

		// Draw background annulus (blue)
		g2d.setColor(Color.CYAN);
		drawScaledRadius(g2d, displayCenterX, displayCenterY, innerBgRadius, radiusScale);
		drawScaledRadius(g2d, displayCenterX, displayCenterY, outerBgRadius, radiusScale);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAntialiasing);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, oldStrokeControl);
	}

	private static void drawScaledRadius(Graphics2D g2d, double centerX, double centerY,
	                                     double radius, double scale) {

		double scaledRadius = radius * scale;
		double diameter = scaledRadius * 2;

		g2d.draw(new Ellipse2D.Double(
				centerX - scaledRadius,
				centerY - scaledRadius,
				diameter,
				diameter
		));
	}

	/**
	 * Calculate robust image-display limits.
	 */
	public static double[] robustLimits(double[][] data, double imageContrast) {
		// Flatten and filter finite values
		List<Double> finite = new java.util.ArrayList<>();
		for (double[] row : data) {
			for (double val : row) {
				if (Double.isFinite(val)) {
					finite.add(val);
				}
			}
		}

		if (finite.isEmpty()) {
			return new double[]{0.0, 1.0};
		}

		Collections.sort(finite);

		double lo = imageContrast;
		double hi = 100.0 - imageContrast;
		int loIdx = (int) (finite.size() * lo / 100.0);
		int hiIdx = (int) (finite.size() * hi / 100.0);

		double median = finite.get(finite.size() / 2);
		double mad = calculateMAD(finite, median);
		double percentileRange = finite.get(Math.min(hiIdx, finite.size() - 1))
				- finite.get(Math.max(0, loIdx));

		double vmin = median - 2.0 * mad;
		double vmax = median + 2.0 * percentileRange;

		if (!Double.isFinite(vmin) || !Double.isFinite(vmax) || vmax <= vmin) {
			double std = calculateStd(finite, median);
			vmin = median - std;
			vmax = median + std;
		}

		return new double[]{vmin, vmax};
	}

	/**
	 * Calculate median absolute deviation.
	 */
	private static double calculateMAD(List<Double> values, double median) {
		List<Double> deviations = new java.util.ArrayList<>();
		for (double val : values) {
			deviations.add(Math.abs(val - median));
		}
		Collections.sort(deviations);
		return deviations.get(deviations.size() / 2);
	}

	/**
	 * Calculate standard deviation.
	 */
	private static double calculateStd(List<Double> values, double mean) {
		if (values.size() < 2) return 1.0;

		double sum = 0;
		for (double val : values) {
			sum += (val - mean) * (val - mean);
		}
		return Math.sqrt(sum / (values.size() - 1));
	}

	/**
	 * Center-crop arrays to common shape.
	 */
	public static double[][] cropToCommonCenter(double[][]... arrays) throws Exception {
		if (arrays.length == 0) {
			throw new IllegalArgumentException("At least one array is required");
		}

		int minHeight = Integer.MAX_VALUE;
		int minWidth = Integer.MAX_VALUE;

		for (double[][] array : arrays) {
			if (array.length == 0 || array[0].length == 0) {
				throw new IllegalArgumentException("Array has invalid shape");
			}
			minHeight = Math.min(minHeight, array.length);
			minWidth = Math.min(minWidth, array[0].length);
		}

		double[][] result = new double[minHeight][minWidth];
		double[][] first = arrays[0];
		int startY = (first.length - minHeight) / 2;
		int startX = (first[0].length - minWidth) / 2;

		for (int y = 0; y < minHeight; y++) {
			System.arraycopy(first[startY + y], startX, result[y], 0, minWidth);
		}

		return result;
	}
}
