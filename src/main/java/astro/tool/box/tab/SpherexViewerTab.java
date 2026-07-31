package astro.tool.box.tab;

import astro.tool.box.spherex.SpherexPipeline;
import org.jfree.chart.*;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYErrorRenderer;
import org.jfree.data.xy.*;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

/**
 * UI for the initial public SPHEREx aperture-spectrum extractor.
 */
public class SpherexViewerTab implements Tab {
	public static final String TAB_NAME = "SPHEREx Spectrum";
	private final JFrame frame;
	private final JTabbedPane tabs;
	private JTextField ra, dec, size, radius;
	private JCheckBox bin;
	private JButton run, csv, png;
	private JLabel status;
	private JFreeChart chart;
	private ChartPanel chartPanel;
	private List<SpherexPipeline.Point> points = List.of();

	public SpherexViewerTab(JFrame frame, JTabbedPane tabs) {
		this.frame = frame;
		this.tabs = tabs;
	}

	@Override
	public void init(boolean visible) {
		JPanel main = new JPanel(new BorderLayout(8, 8));
		main.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		JPanel form = new JPanel(new GridLayout(2, 5, 6, 3));
		ra = field(form, "RA (deg)", "24.2412498");
		dec = field(form, "Dec (deg)", "9.5630705");
		size = field(form, "Cutout (arcsec)", "120");
		radius = field(form, "Aperture (pixels)", "2.0");
		bin = new JCheckBox("Bin spectrum", true);
		form.add(bin);
		run = new JButton("Generate spectrum");
		run.addActionListener(e -> generate());
		form.add(run);
		csv = new JButton("Save CSV");
		csv.setEnabled(false);
		csv.addActionListener(e -> saveCsv());
		form.add(csv);
		png = new JButton("Save PNG");
		png.setEnabled(false);
		png.addActionListener(e -> savePng());
		form.add(png);
		main.add(form, BorderLayout.NORTH);
		chart = createChart(points);
		chartPanel = new ChartPanel(chart);
		main.add(chartPanel, BorderLayout.CENTER);
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
		try {
			config = new SpherexPipeline.Config(Double.parseDouble(ra.getText()), Double.parseDouble(dec.getText()), Integer.parseInt(size.getText()), Double.parseDouble(radius.getText()), bin.isSelected(), Path.of(System.getProperty("user.home"), ".astro-tool-box", "spherex"));
		} catch (Exception ex) {
			error(ex);
			return;
		}
		run.setEnabled(false);
		new SwingWorker<SpherexPipeline.Result, String>() {
			@Override
			protected SpherexPipeline.Result doInBackground() throws Exception {
				return SpherexPipeline.run(config, this::publish);
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
					points = r.points();
					chart = createChart(points);
					chartPanel.setChart(chart);
					chartPanel.revalidate();
					chartPanel.repaint();
					status.setText("Generated " + points.size() + " spectrum points; " + r.warnings().size() + " cutouts skipped.");
					csv.setEnabled(true);
					png.setEnabled(true);
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
		XYPlot plot = out.getXYPlot();
		XYErrorRenderer renderer = new XYErrorRenderer();
		renderer.setDefaultLinesVisible(false);
		renderer.setDefaultShapesVisible(true);
		plot.setRenderer(renderer);
		plot.setBackgroundPaint(Color.WHITE);
		return out;
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
}
