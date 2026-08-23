package astro.tool.box.tab;

import astro.tool.box.container.NumberPair;
import astro.tool.box.spherex.ImagePlotter;
import astro.tool.box.spherex.RgbImagePanel;
import astro.tool.box.spherex.SpherexPipeline;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

import static astro.tool.box.main.ToolboxHelper.getCoordinates;
import static astro.tool.box.tab.SettingsTab.getUserSetting;
import static astro.tool.box.tab.SettingsTab.setUserSetting;

/**
 * UI for the initial public SPHEREx aperture-spectrum extractor.
 * Extended to display stacked images over the spectrum plot.
 */
public class SpherexViewerTab implements Tab {
	public static final String TAB_NAME = "SPHEREx Spectrum";
	private final String FONT_NAME = "Tahoma";
	private final JFrame frame;
	private final JTabbedPane tabs;
	private JTextField coordinates, size, radius;
	private JCheckBox bin;
	private JCheckBox cleanUpDirectory;
	private JButton run, csv, png;
	private JLabel status;
	private JFreeChart chart;
	private ChartPanel chartPanel;
	private JPanel imagesPanel;
	private RgbImagePanel rgbImagePanel;
	private List<Path> currentFitsFiles = List.of();
	private SpherexPipeline.Config currentFieldConfig;
	private List<SpherexPipeline.Point> points = List.of();
	private List<Map<String, Object>> stackedImages = List.of();

	public SpherexViewerTab(JFrame frame, JTabbedPane tabs) {
		this.frame = frame;
		this.tabs = tabs;
	}

	@Override
	public void init(boolean visible) {
		JPanel main = new JPanel(new BorderLayout(8, 8));
		main.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		JPanel form = new JPanel(new GridLayout(3, 4, 6, 3));
		coordinates = field(form, "Coordinates", "152.560 -4.115");
		size = field(form, "Cutout (arcsec)", "120");
		radius = field(form, "Aperture (pixels)", "2.0");
		bin = new JCheckBox("Bin spectrum", true);
		form.add(bin);
		run = new JButton("Generate spectrum");
		run.addActionListener(e -> generate());
		form.add(run);
		cleanUpDirectory = new JCheckBox("Delete downloaded files", false);
		form.add(cleanUpDirectory);
		csv = new JButton("Save CSV");
		csv.setEnabled(false);
		csv.addActionListener(e -> saveCsv());
		form.add(csv);
		png = new JButton("Save PNG");
		png.setEnabled(false);
		png.addActionListener(e -> savePng());
		form.add(png);
		main.add(form, BorderLayout.NORTH);

		/*
		 * Horizontal split pane:
		 *
		 *   spectrum panel | draggable vertical divider | image panels
		 *
		 * The divider is immediately to the right of the spectrum, so its
		 * position directly controls only the width allocated to the spectrum.
		 */
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setContinuousLayout(true);
		splitPane.setOneTouchExpandable(false);
		splitPane.setDividerSize(6);
		splitPane.setResizeWeight(0.5);

		chart = createChart(points);
		chartPanel = new ChartPanel(chart);
		chartPanel.setMinimumSize(new Dimension(300, 300));
		splitPane.setLeftComponent(chartPanel);

		imagesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		imagesPanel.setBackground(Color.WHITE);
		rgbImagePanel = new RgbImagePanel();
		rgbImagePanel.setCoordinateClickListener(this::extractSpectrumAt);
		imagesPanel.setMinimumSize(new Dimension(300, 300));

		JLabel emptyLabel = new JLabel("Images will appear here after spectrum generation");
		emptyLabel.setHorizontalAlignment(JLabel.CENTER);
		imagesPanel.add(emptyLabel);

		JScrollPane imagesScrollPane = new JScrollPane(imagesPanel);
		imagesScrollPane.setMinimumSize(new Dimension(300, 300));
		splitPane.setRightComponent(imagesScrollPane);

		/*
		 * Set the initial spectrum width after the tab has been laid out.
		 * The user can subsequently drag the vertical divider freely.
		 */
		SwingUtilities.invokeLater(() -> {
			int width = splitPane.getWidth();
			if (width > 0) {
				splitPane.setDividerLocation(Math.max(300, width / 2));
			} else {
				splitPane.setDividerLocation(650);
			}
		});

		main.add(splitPane, BorderLayout.CENTER);

		status = new JLabel("Enter coordinates to generate an aperture spectrum from public SPHEREx cutouts.");
		main.add(status, BorderLayout.SOUTH);
		if (visible) tabs.addTab(TAB_NAME, main);
	}

	private JTextField field(JPanel panel, String label, String value) {
		JPanel p = new JPanel(new BorderLayout());
		p.add(new JLabel(label), BorderLayout.NORTH);
		JTextField f = new JTextField(value);
		p.add(f);
		panel.add(p);
		return f;
	}

	private void generate() {
		final SpherexPipeline.Config config;
		final boolean newCoordinates;
		try {
			String coordinatesStr = coordinates.getText().trim();
			if (coordinatesStr.isEmpty()) {
				JOptionPane.showMessageDialog(frame, "Coordinates must be provided.", TAB_NAME, JOptionPane.ERROR_MESSAGE);
				return;
			}

			NumberPair coords = getCoordinates(coordinatesStr);
			double raVal = coords.x();
			double decVal = coords.y();

			String normalizedRa = String.valueOf(raVal);
			String normalizedDec = String.valueOf(decVal);

			config = new SpherexPipeline.Config(
					raVal,
					decVal,
					Integer.parseInt(size.getText()),
					Double.parseDouble(radius.getText()),
					bin.isSelected(),
					Path.of(
							System.getProperty("user.home"),
							".astro-tool-box",
							"spherex"
					)
			);

			String lastRaSetting = getUserSetting("lastRa");
			String lastDecSetting = getUserSetting("lastDec");
			String lastCutoutSize = getUserSetting("lastCutoutSize");

			if (lastRaSetting != null
					&& lastDecSetting != null
					&& lastCutoutSize != null
					&& !lastRaSetting.isBlank()
					&& !lastDecSetting.isBlank()
					&& !lastCutoutSize.isBlank()) {

				double lastRa = Double.parseDouble(lastRaSetting);
				double lastDec = Double.parseDouble(lastDecSetting);
				double lastSizeArcsec = Double.parseDouble(lastCutoutSize);

				// Previous cutout half-size in degrees
				double halfSizeDeg = (lastSizeArcsec / 2.0) / 3600.0;

				// RA separation, including wrap-around at RA = 0/360
				double deltaRaDeg = Math.abs(raVal - lastRa);

				if (deltaRaDeg > 180.0) {
					deltaRaDeg = 360.0 - deltaRaDeg;
				}

				// Project RA separation at the declination of the previous cutout
				double deltaRaProjected = deltaRaDeg * Math.cos(Math.toRadians(lastDec));

				double deltaDecDeg = Math.abs(decVal - lastDec);

				// Only consider this a new object if it lies outside
				// the previous cutout field of view.
				newCoordinates = deltaRaProjected > halfSizeDeg
						|| deltaDecDeg > halfSizeDeg;
			} else {
				newCoordinates = true;
			}

			// Clean up FITS directory if explicitly requested
			// or if the new position is outside the previous field of view.
			if (cleanUpDirectory.isSelected() || newCoordinates) {
				cleanupCutoutDirectory(config.cacheDir());
				cleanUpDirectory.setSelected(false);
			}

			setUserSetting("lastRa", normalizedRa);
			setUserSetting("lastDec", normalizedDec);
			setUserSetting("lastCutoutSize", size.getText());
		} catch (Exception ex) {
			error(ex);
			return;
		}
		run.setEnabled(false);
		new SwingWorker<SpherexPipeline.Result, String>() {
			@Override
			protected SpherexPipeline.Result doInBackground() throws Exception {
				return SpherexPipeline.run(config, this::publish, newCoordinates);
			}

			@Override
			protected void process(List<String> values) {
				status.setText(values.get(values.size() - 1));
			}

			@Override
			protected void done() {
				run.setEnabled(true);
				try {
					SpherexPipeline.Result r = get();
					currentFitsFiles = r.fitsFiles();
					currentFieldConfig = config;
					points = r.points();
					chart = createChart(points);
					chartPanel.setChart(chart);
					chartPanel.revalidate();
					chartPanel.repaint();
					status.setText("Generated " + points.size() + " spectrum points; " + r.warnings().size() + " cutouts skipped.");
					csv.setEnabled(true);
					png.setEnabled(true);

					// Stack and display images using already-downloaded FITS files
					if (!r.fitsFiles().isEmpty()) {
						stackAndDisplayImages(r.fitsFiles(), config.raDeg(), config.decDeg());
					}
				} catch (Exception ex) {
					error(ex.getCause() == null ? ex : ex.getCause());
				}
			}
		}.execute();
	}

	private JFreeChart createChart(List<SpherexPipeline.Point> ps) {
		YIntervalSeries series = new YIntervalSeries("Aperture spectrum");
		for (var p : ps)
			series.add(p.wavelengthUm(), p.fluxUjy(), p.fluxUjy() - p.errorUjy(), p.fluxUjy() + p.errorUjy());
		YIntervalSeriesCollection data = new YIntervalSeriesCollection();
		data.addSeries(series);
		JFreeChart out = ChartFactory.createXYLineChart("SPHEREx aperture spectrum", "Wavelength (µm)", "Flux density (µJy)", data);

		// Set fonts for title, axes, and legend
		Font tahomaTitle = new Font(FONT_NAME, Font.PLAIN, 20);
		Font tahomaLabel = new Font(FONT_NAME, Font.PLAIN, 17);
		Font tahomaTick = new Font(FONT_NAME, Font.PLAIN, 14);

		// Chart title
		out.getTitle().setFont(tahomaTitle);

		// Legend (if present)
		if (out.getLegend() != null) {
			out.getLegend().setItemFont(tahomaLabel);
		}

		XYPlot plot = out.getXYPlot();

		// X-axis
		plot.getDomainAxis().setLabelFont(tahomaLabel);
		plot.getDomainAxis().setTickLabelFont(tahomaTick);

		// Y-axis
		plot.getRangeAxis().setLabelFont(tahomaLabel);
		plot.getRangeAxis().setTickLabelFont(tahomaTick);

		// Create grid
		plot.setBackgroundPaint(Color.WHITE);

		Color grid = new Color(230, 230, 230);
		plot.setDomainGridlinePaint(grid);
		plot.setRangeGridlinePaint(grid);

		plot.setDomainGridlineStroke(new BasicStroke(0.75f));
		plot.setRangeGridlineStroke(new BasicStroke(0.75f));

		plot.setOutlineVisible(false);

		XYErrorRenderer renderer = new XYErrorRenderer();
		renderer.setDefaultLinesVisible(false);
		renderer.setDefaultShapesVisible(true);
		renderer.setSeriesShape(0, new Ellipse2D.Double(-3, -3, 6, 6));
		plot.setRenderer(renderer);
		XYSeriesCollection guideData = createGuideData(ps);
		if (guideData.getSeriesCount() > 0) {
			plot.setDataset(1, guideData);
			XYSplineRenderer guideRenderer = new XYSplineRenderer(12);
			guideRenderer.setDefaultShapesVisible(false);
			guideRenderer.setDefaultLinesVisible(true);
			guideRenderer.setSeriesPaint(0, Color.RED);
			guideRenderer.setDefaultStroke(new BasicStroke(2f));
			plot.setRenderer(1, guideRenderer);
		}
		plot.setBackgroundPaint(Color.WHITE);
		return out;
	}

	/**
	 * Build one continuous broad-shape guide, with robust anchors computed in
	 * coarse bins for each detector. Medians keep individual noisy points from
	 * pulling the spline around, while a single series joins the full spectrum.
	 */
	private XYSeriesCollection createGuideData(List<SpherexPipeline.Point> ps) {
		Map<Integer, List<SpherexPipeline.Point>> byDetector = new TreeMap<>();
		for (var point : ps) byDetector.computeIfAbsent(point.detector(), ignored -> new ArrayList<>()).add(point);
		XYSeries guide = new XYSeries("Robust guidance spline");
		for (var entry : byDetector.entrySet()) {
			List<SpherexPipeline.Point> detectorPoints = entry.getValue();
			detectorPoints.sort(Comparator.comparingDouble(SpherexPipeline.Point::wavelengthUm));
			int bins = Math.min(8, detectorPoints.size());
			if (bins < 3) continue;
			for (int binIndex = 0; binIndex < bins; binIndex++) {
				int start = binIndex * detectorPoints.size() / bins;
				int end = (binIndex + 1) * detectorPoints.size() / bins;
				List<Double> wavelengths = new ArrayList<>(), fluxes = new ArrayList<>();
				for (int i = start; i < end; i++) {
					var point = detectorPoints.get(i);
					if (Double.isFinite(point.fluxUjy())) {
						wavelengths.add(point.wavelengthUm());
						fluxes.add(point.fluxUjy());
					}
				}
				if (!fluxes.isEmpty()) guide.add(median(wavelengths), median(fluxes));
			}
		}
		XYSeriesCollection result = new XYSeriesCollection();
		if (guide.getItemCount() >= 3) result.addSeries(guide);
		return result;
	}

	private double median(List<Double> values) {
		List<Double> sorted = new ArrayList<>(values);
		sorted.sort(Double::compare);
		int middle = sorted.size() / 2;
		return sorted.size() % 2 == 0 ? (sorted.get(middle - 1) + sorted.get(middle)) / 2 : sorted.get(middle);
	}

	private void stackAndDisplayImages(List<Path> fitsFiles, double raDeg, double decDeg) {
		new SwingWorker<List<Map<String, Object>>, String>() {
			@Override
			protected List<Map<String, Object>> doInBackground() throws Exception {
				publish("Stacking detector images from downloaded cutouts...");
				return SpherexPipeline.stackImages(fitsFiles, this::publish, raDeg, decDeg);
			}

			@Override
			protected void process(List<String> values) {
				status.setText(values.get(values.size() - 1));
			}

			@Override
			protected void done() {
				try {
					stackedImages = get();
					if (stackedImages.isEmpty()) {
						status.setText("No images were stacked.");
						return;
					}

					int sizeVal = Integer.parseInt(size.getText());
					BufferedImage imageGrid = ImagePlotter.plotImages(
							raDeg, decDeg, stackedImages, sizeVal, 10);

					ImagePlotter.RgbComposite composite =
							ImagePlotter.createRgbComposite(stackedImages, 10);
					rgbImagePanel.setComposite(composite);

					imagesPanel.removeAll();

					JPanel content = new JPanel();
					content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
					content.setBackground(Color.WHITE);

					JLabel rgbTitle = new JLabel(
							"Interactive RGB composite — click an object to extract its spectrum");
					rgbTitle.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
					rgbTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

					rgbImagePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
					rgbImagePanel.setPreferredSize(new Dimension(500, 500));
					rgbImagePanel.setMaximumSize(new Dimension(500, 500));

					JLabel diagnosticTitle = new JLabel("Detector image stacks");
					diagnosticTitle.setFont(new Font(FONT_NAME, Font.PLAIN, 14));
					diagnosticTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

					JLabel imageLabel = new JLabel(new ImageIcon(imageGrid));
					imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

					content.add(rgbTitle);
					content.add(Box.createVerticalStrut(5));
					content.add(rgbImagePanel);
					content.add(Box.createVerticalStrut(15));
					content.add(diagnosticTitle);
					content.add(Box.createVerticalStrut(5));
					content.add(imageLabel);

					imagesPanel.add(content);
					imagesPanel.revalidate();
					imagesPanel.repaint();

					status.setText("Stacked " + stackedImages.size()
							+ " detector images. Click the RGB image to inspect another spectrum.");
				} catch (Exception ex) {
					error(ex.getCause() == null ? ex : ex.getCause());
				}
			}
		}.execute();
	}

	/**
	 * Re-extract a spectrum at a clicked RGB position without changing or
	 * regenerating the current image cutouts.
	 */
	private void extractSpectrumAt(double raDeg, double decDeg) {
		if (currentFieldConfig == null || currentFitsFiles.isEmpty()) return;

		if (!isInsideCurrentField(raDeg, decDeg,
				currentFieldConfig.raDeg(),
				currentFieldConfig.decDeg(),
				currentFieldConfig.cutoutArcsec())) {
			status.setText("Selected position is outside the currently loaded field.");
			return;
		}

		SpherexPipeline.Config clickConfig = new SpherexPipeline.Config(
				raDeg, decDeg,
				currentFieldConfig.cutoutArcsec(),
				currentFieldConfig.apertureRadius(),
				currentFieldConfig.bin(),
				currentFieldConfig.cacheDir(),
				currentFieldConfig.removeOutliers(),
				currentFieldConfig.outlierNbrOfBins(),
				currentFieldConfig.outlierSigma());

		run.setEnabled(false);
		status.setText(String.format(Locale.US,
				"Extracting cached spectrum at RA %.6f, Dec %.6f...", raDeg, decDeg));

		new SwingWorker<SpherexPipeline.Result, String>() {
			@Override
			protected SpherexPipeline.Result doInBackground() throws Exception {
				return SpherexPipeline.extractSpectrumAt(clickConfig, this::publish);
			}

			@Override
			protected void process(List<String> values) {
				if (!values.isEmpty()) status.setText(values.get(values.size() - 1));
			}

			@Override
			protected void done() {
				run.setEnabled(true);
				try {
					SpherexPipeline.Result result = get();
					points = result.points();
					chart = createChart(points);
					chartPanel.setChart(chart);
					chartPanel.revalidate();
					chartPanel.repaint();

					coordinates.setText(String.format(Locale.US, "%.8f %.8f", raDeg, decDeg));
					csv.setEnabled(true);
					png.setEnabled(true);

					status.setText("Generated " + points.size()
							+ " spectrum points from cached FITS files.");
				} catch (Exception ex) {
					error(ex.getCause() == null ? ex : ex.getCause());
				}
			}
		}.execute();
	}

	private boolean isInsideCurrentField(double raDeg, double decDeg,
	                                     double fieldRaDeg, double fieldDecDeg,
	                                     double cutoutArcsec) {
		double halfSizeDeg = cutoutArcsec / 2.0 / 3600.0;

		double deltaRa = Math.abs(raDeg - fieldRaDeg);
		if (deltaRa > 180.0) deltaRa = 360.0 - deltaRa;

		double projectedRa = deltaRa * Math.cos(Math.toRadians(fieldDecDeg));
		double deltaDec = Math.abs(decDeg - fieldDecDeg);

		return projectedRa <= halfSizeDeg && deltaDec <= halfSizeDeg;
	}

	private void saveCsv() {
		JFileChooser c = new JFileChooser();
		c.setSelectedFile(new java.io.File("spherex_spectrum.csv"));
		if (c.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
		StringBuilder s = new StringBuilder("wavelength_um,flux_ujy,error_ujy,detector,measurements\n");
		for (var p : points)
			s.append(p.wavelengthUm()).append(',').append(p.fluxUjy()).append(',').append(p.errorUjy()).append(',').append(p.detector()).append(',').append(p.count()).append('\n');
		try {
			Files.writeString(c.getSelectedFile().toPath(), s, StandardCharsets.UTF_8);
		} catch (Exception ex) {
			error(ex);
		}
	}

	private void savePng() {
		JFileChooser c = new JFileChooser();
		c.setSelectedFile(new java.io.File("spherex_spectrum.png"));
		if (c.showSaveDialog(frame) != JFileChooser.APPROVE_OPTION) return;
		try {
			ChartUtils.saveChartAsPNG(c.getSelectedFile(), chart, 1400, 900);
		} catch (Exception ex) {
			error(ex);
		}
	}

	private void error(Throwable ex) {
		status.setText("Spectrum generation failed.");
		JOptionPane.showMessageDialog(frame, ex.getMessage(), TAB_NAME, JOptionPane.ERROR_MESSAGE);
	}

	private void cleanupCutoutDirectory(Path cacheDir) {
		try {
			Path cutoutsDir = cacheDir.resolve("cutouts");
			if (Files.exists(cutoutsDir)) {
				try (var paths = Files.walk(cutoutsDir)) {
					paths.sorted(Comparator.reverseOrder())
							.forEach(path -> {
								try {
									Files.delete(path);
								} catch (Exception e) {
									// Ignore errors during cleanup
								}
							});
				}
				status.setText("Cleaned up old cutout files for new object.");
			}
		} catch (Exception ex) {
			// Silently ignore cleanup errors
		}
	}
}
