package astro.tool.box.spherex;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Utilities for plotting SPHEREx cutout images.
 * Converts functionality from Python image_plotter.py to Java.
 */
public class ImagePlotter {

	public record ImageCutout(String band, Object imageData, double[] photometryRadii) {
	}

	public static class PlotConfig {
		public double imageContrast = 10.0;
		public int rows = 5;
		public int cols = 6;
		public int figureWidth = 1200;
		public int figureHeight = 1000;

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
		if (imageContrast < 0 || imageContrast >= 50)
			throw new IllegalArgumentException("image_contrast must be in [0, 50)");

		List<ImageCutout> cutouts = normalizeCutouts(images);
		int panelWidth = config.figureWidth / config.cols;
		int panelHeight = config.figureHeight / config.rows;

		BufferedImage figure = new BufferedImage(
				config.figureWidth, config.figureHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = figure.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, figure.getWidth(), figure.getHeight());

		// Python layout: six grayscale detector panels, one RGB panel, one information panel.
		int panelIndex = 0;
		for (ImageCutout cutout : cutouts) {
			if (panelIndex >= config.rows * config.cols - 1) break;
			int row = panelIndex / config.cols;
			int col = panelIndex % config.cols;
			plotSingleCutout(g2d, config, cutout, col * panelWidth, row * panelHeight, panelWidth, panelHeight);
			panelIndex++;
		}

		ImageCutout colorCutout = createD1D2D3D4D5D6ColorCutout(cutouts);
		if (colorCutout != null && panelIndex < config.rows * config.cols - 1) {
			int row = panelIndex / config.cols;
			int col = panelIndex % config.cols;
			plotSingleColorCutout(g2d, config, colorCutout, col * panelWidth, row * panelHeight, panelWidth, panelHeight);
			panelIndex++;
		}

		// Final information panel.
		if (panelIndex < config.rows * config.cols) {
			int row = panelIndex / config.cols;
			int col = panelIndex % config.cols;
			plotInformationPanel(g2d, config, col * panelWidth, row * panelHeight, panelWidth, panelHeight, ra, dec, imageSize);
		}

		drawVerticalSeparators(g2d, config, panelWidth);
		g2d.dispose();
		return figure;
	}

	private static void drawVerticalSeparators(Graphics2D g2d, PlotConfig config, int panelWidth) {
		Stroke oldStroke = g2d.getStroke();
		Color oldColor = g2d.getColor();

		g2d.setColor(Color.WHITE);
		g2d.setStroke(new BasicStroke(1.0f));

		for (int col = 1; col < config.cols; col++) {
			int x = col * panelWidth;
			g2d.drawLine(x, 0, x, config.figureHeight);
		}

		g2d.setStroke(oldStroke);
		g2d.setColor(oldColor);
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
		drawPanelBorder(g2d, x, y, width, height);
		drawTargetMarker(g2d, x, y, width, height, imageData.length, imageData[0].length);

		// Draw label (white background, half size)
		g2d.setColor(Color.WHITE);
		g2d.fillRect(x + 5, y + 5, 20, 10);
		g2d.setColor(Color.BLACK);
		g2d.setFont(new Font("Arial", Font.PLAIN, 10));
		g2d.drawString(cutout.band, x + 7, y + 15);

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
		drawPanelBorder(g2d, x, y, width, height);
		drawTargetMarker(g2d, x, y, width, height, channels[2].length, channels[2][0].length);

		g2d.setColor(Color.WHITE);
		g2d.fillRect(x + 5, y + 5, 85, 14);
		g2d.setColor(Color.BLACK);
		g2d.setFont(new Font("Arial", Font.PLAIN, 10));
		g2d.drawString(cutout.band, x + 7, y + 15);

		drawPhotometryRadii(g2d, x, y, width, height, channels[2], cutout.photometryRadii);
	}

	/**
	 * Create an RGB composite from D2, D4, and D6.
	 * Blue channel: D2, green channel: D4, red channel: D6.
	 */
	/**
	 * Create the Python-compatible RGB composite.
	 *
	 * Blue  = mean(D1, D2)
	 * Green = mean(D3, D4)
	 * Red   = mean(D5, D6)
	 */
	private static ImageCutout createD1D2D3D4D5D6ColorCutout(List<ImageCutout> cutouts) throws Exception {
		ImageCutout d1 = findBand(cutouts, "D1");
		ImageCutout d2 = findBand(cutouts, "D2");
		ImageCutout d3 = findBand(cutouts, "D3");
		ImageCutout d4 = findBand(cutouts, "D4");
		ImageCutout d5 = findBand(cutouts, "D5");
		ImageCutout d6 = findBand(cutouts, "D6");
		if (d1 == null || d2 == null || d3 == null || d4 == null || d5 == null || d6 == null)
			return null;

		double[][] blue = meanCommonCenter((double[][]) d1.imageData, (double[][]) d2.imageData);
		double[][] green = meanCommonCenter((double[][]) d3.imageData, (double[][]) d4.imageData);
		double[][] red = meanCommonCenter((double[][]) d5.imageData, (double[][]) d6.imageData);

		double[][][] common = cropCommon(red, green, blue);
		return new ImageCutout("D56-D34-D12", common, d6.photometryRadii);
	}

	private static ImageCutout findBand(List<ImageCutout> cutouts, String band) {
		for (ImageCutout c : cutouts)
			if (band.equals(c.band)) return c;
		return null;
	}

	private static double[][][] cropCommon(double[][]... arrays) {
		if (arrays.length == 0) throw new IllegalArgumentException("At least one array is required");
		int minHeight = Integer.MAX_VALUE;
		int minWidth = Integer.MAX_VALUE;
		for (double[][] array : arrays) {
			if (array == null || array.length == 0 || array[0] == null || array[0].length == 0)
				throw new IllegalArgumentException("Array has invalid shape");
			minHeight = Math.min(minHeight, array.length);
			minWidth = Math.min(minWidth, array[0].length);
		}
		double[][][] result = new double[arrays.length][minHeight][minWidth];
		for (int i = 0; i < arrays.length; i++) {
			int startY = (arrays[i].length - minHeight) / 2;
			int startX = (arrays[i][0].length - minWidth) / 2;
			for (int y = 0; y < minHeight; y++)
				System.arraycopy(arrays[i][startY + y], startX, result[i][y], 0, minWidth);
		}
		return result;
	}

	private static double[][] meanCommonCenter(double[][] a, double[][] b) {
		double[][][] common = cropCommon(a, b);
		int h = common[0].length;
		int w = common[0][0].length;
		double[][] out = new double[h][w];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				double av = common[0][y][x];
				double bv = common[1][y][x];
				if (Double.isFinite(av) && Double.isFinite(bv)) out[y][x] = 0.5 * (av + bv);
				else if (Double.isFinite(av)) out[y][x] = av;
				else if (Double.isFinite(bv)) out[y][x] = bv;
				else out[y][x] = Double.NaN;
			}
		}
		return out;
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
				gray = 255 - gray; // Python image_plotter uses cmap="gray_r".
				img.setRGB(x, y, (gray << 16) | (gray << 8) | gray);
			}
		}

		return img;
	}

	/**
	 * Draw photometry aperture circles.
	 */
	private static void drawPanelBorder(Graphics2D g2d, int x, int y, int width, int height) {
		Color old = g2d.getColor();
		Stroke oldStroke = g2d.getStroke();
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(1.0f));
		g2d.drawRect(x, y, width - 1, height - 1);
		g2d.setStroke(oldStroke);
		g2d.setColor(old);
	}

	/**
	 * Return the display coordinates corresponding to the Python image
	 * plotter's image centre convention: (nx / 2.0, ny / 2.0).
	 *
	 * Both the target marker and the photometry overlays use this exact
	 * helper so they cannot acquire a sub-pixel offset relative to each
	 * other.
	 */
	private static double[] displayImageCenter(
			int panelX, int panelY,
			int panelWidth, int panelHeight,
			int imageHeight, int imageWidth) {

		double centerX = imageWidth / 2.0;
		double centerY = imageHeight / 2.0;

		double scaleX = (double) panelWidth / imageWidth;
		double scaleY = (double) panelHeight / imageHeight;

		return new double[]{
				panelX + centerX * scaleX,
				panelY + centerY * scaleY
		};
	}

	private static void drawTargetMarker(Graphics2D g2d, int panelX, int panelY, int panelWidth, int panelHeight, int imageHeight, int imageWidth) {
		// Use exactly the same image-centre convention as the annuli.
		double[] center = displayImageCenter(
				panelX, panelY, panelWidth, panelHeight,
				imageHeight, imageWidth);

		Color old = g2d.getColor();
		g2d.setColor(Color.RED);
		g2d.fillOval(
				(int) Math.round(center[0] - 1.5),
				(int) Math.round(center[1] - 1.5),
				3, 3);
		g2d.setColor(old);
	}

	private static void drawPhotometryRadii(Graphics2D g2d, int panelX, int panelY,
	                                        int panelWidth, int panelHeight, double[][] imageData, double[] radii) {

		Object oldAntialiasing = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		Object oldStrokeControl = g2d.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		int imgHeight = imageData.length;
		int imgWidth = imageData[0].length;

		double scaleX = (double) panelWidth / imgWidth;
		double scaleY = (double) panelHeight / imgHeight;
		double radiusScale = Math.min(scaleX, scaleY);

		// Use exactly the same centre as the target marker and as Python's
		// _image_center_xy(), i.e. (nx / 2.0, ny / 2.0).
		double[] center = displayImageCenter(
				panelX, panelY, panelWidth, panelHeight,
				imgHeight, imgWidth);
		double displayCenterX = center[0];
		double displayCenterY = center[1];

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

	private static void plotInformationPanel(Graphics2D g2d, PlotConfig config, int x, int y,
	                                         int width, int height, double ra, double dec, double imageSize) {
		g2d.setColor(Color.WHITE);
		g2d.fillRect(x, y, width, height);
		g2d.setColor(Color.BLACK);
		g2d.setFont(new Font("Arial", Font.PLAIN, 13));
		g2d.drawString("Target", x + 12, y + 28);
		g2d.drawString("RA = " + formatCoordinate(ra), x + 12, y + 55);
		g2d.drawString("Dec = " + formatCoordinate(dec), x + 12, y + 82);
		g2d.drawString("Size = " + Math.round(imageSize) + " arcsec", x + 12, y + 109);
		g2d.drawString("North up, East left", x + 12, y + 136);
		g2d.setColor(Color.GRAY);
		g2d.drawRect(x, y, width - 1, height - 1);
	}

	private static String formatCoordinate(double value) {
		if (!Double.isFinite(value)) return "n/a";
		String text = String.format(java.util.Locale.ROOT, "%.7f", value);
		while (text.contains(".") && text.endsWith("0")) text = text.substring(0, text.length() - 1);
		if (text.endsWith(".")) text = text.substring(0, text.length() - 1);
		return text;
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
