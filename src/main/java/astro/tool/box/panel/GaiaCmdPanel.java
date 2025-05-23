package astro.tool.box.panel;

import static astro.tool.box.function.NumericFunctions.PATTERN_2DEC_NZ;
import static astro.tool.box.function.NumericFunctions.addPlusSign;
import static astro.tool.box.function.NumericFunctions.roundDouble;
import static astro.tool.box.function.NumericFunctions.roundTo1Dec;
import static astro.tool.box.function.NumericFunctions.roundTo2DecNZ;
import static astro.tool.box.function.NumericFunctions.roundTo3DecNZ;
import static astro.tool.box.function.NumericFunctions.toDouble;
import static astro.tool.box.main.Application.CMD_DATA;
import static astro.tool.box.main.ToolboxHelper.createPDF;
import static astro.tool.box.main.ToolboxHelper.getInfoIcon;
import static astro.tool.box.main.ToolboxHelper.getToolBoxImage;
import static astro.tool.box.main.ToolboxHelper.html;
import static astro.tool.box.main.ToolboxHelper.writeErrorLog;
import static astro.tool.box.util.Constants.LINE_BREAK;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import astro.tool.box.catalog.GaiaCmd;
import astro.tool.box.container.NumberTriplet;
import astro.tool.box.util.CSVParser;

public class GaiaCmdPanel extends JPanel {

	private static final String FONT_NAME = "Tahoma";

	private final JRadioButton g_rpButton;
	private final JCheckBox coolingSequencesH;
	private final JCheckBox coolingSequencesHe;

	private final int min = 2;
	private final int max = 14;

	private JFreeChart chart;

	private static final List<Color> COLORS = new ArrayList();

	static {
		COLORS.add(new Color(68, 1, 84));
		COLORS.add(new Color(72, 40, 120));
		COLORS.add(new Color(62, 73, 137));
		COLORS.add(new Color(49, 104, 142));
		COLORS.add(new Color(38, 130, 142));
		COLORS.add(new Color(31, 158, 137));
		COLORS.add(new Color(53, 183, 121));
		COLORS.add(new Color(110, 206, 88));
		COLORS.add(new Color(181, 222, 43));
		COLORS.add(new Color(253, 231, 37));
	}

	private String targetLabel;

	public GaiaCmdPanel(GaiaCmd catalogEntry) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

		JButton showImageButton = new JButton("Show reference CMD");
		showImageButton.addActionListener((ActionEvent e) -> {
			displayReferenceCmd();
		});

		g_rpButton = new JRadioButton("G-RP", true);
		commandPanel.add(g_rpButton);
		g_rpButton.addActionListener((ActionEvent e) -> {
			commandPanel.remove(showImageButton);
		});

		JRadioButton bp_rpButton = new JRadioButton("BP-RP", false);
		commandPanel.add(bp_rpButton);
		bp_rpButton.addActionListener((ActionEvent e) -> {
			commandPanel.add(showImageButton);
		});

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(g_rpButton);
		buttonGroup.add(bp_rpButton);

		String coolingTracksText = "Montreal cooling tracks for white dwarfs with masses between 0.2 (top) and 1.3 (bottom) Msun in steps of 0.1 Msun "
				+ LINE_BREAK + "(red is pure H atmosphere or DA WDs, blue is pure He atmosphere or DB WDs)";

		JLabel coolingTracksToolTip = new JLabel(getInfoIcon());
		coolingTracksToolTip.setToolTipText(html(coolingTracksText));
		commandPanel.add(coolingTracksToolTip);

		JLabel coolingTracksLabel = new JLabel("White dwarf cooling sequences");
		coolingTracksLabel.setToolTipText(html(coolingTracksText));
		commandPanel.add(coolingTracksLabel);

		coolingSequencesH = new JCheckBox("DA");
		commandPanel.add(coolingSequencesH);

		coolingSequencesHe = new JCheckBox("DB");
		commandPanel.add(coolingSequencesHe);

		JButton createButton = new JButton("Create PDF");
		commandPanel.add(createButton);
		createButton.addActionListener((ActionEvent e) -> {
			try {
				File tmpFile = File.createTempFile("Target_" + roundTo2DecNZ(catalogEntry.getRa())
						+ addPlusSign(roundDouble(catalogEntry.getDec(), PATTERN_2DEC_NZ)) + "_", ".pdf");
				createPDF(chart, tmpFile, 800, 700);
				Desktop.getDesktop().open(tmpFile);
			} catch (Exception ex) {
				writeErrorLog(ex);
			}
		});

		String infoText = "Right-clicking on the chart, opens a context menu with additional functions like printing and saving.";

		JLabel infoToolTip = new JLabel(getInfoIcon());
		infoToolTip.setToolTipText(infoText);
		commandPanel.add(infoToolTip);

		JLabel infoLabel = new JLabel("Tooltip");
		infoLabel.setToolTipText(infoText);
		commandPanel.add(infoLabel);

		loadCmdData();

		createChartPanel(catalogEntry);

		g_rpButton.addActionListener((ActionEvent e) -> {
			remove(0);
			createChartPanel(catalogEntry);
			if (coolingSequencesH.isSelected()) {
				addCoolingSequencesH(chart);
			}
			if (coolingSequencesHe.isSelected()) {
				addCoolingSequencesHe(chart);
			}
		});

		bp_rpButton.addActionListener((ActionEvent e) -> {
			remove(0);
			createChartPanel(catalogEntry);
			if (coolingSequencesH.isSelected()) {
				addCoolingSequencesH(chart);
			}
			if (coolingSequencesHe.isSelected()) {
				addCoolingSequencesHe(chart);
			}
		});

		coolingSequencesH.addActionListener((ActionEvent e) -> {
			if (coolingSequencesH.isSelected()) {
				addCoolingSequencesH(chart);
			} else {
				removeCoolingSequencesH(chart);
			}
		});

		coolingSequencesHe.addActionListener((ActionEvent e) -> {
			if (coolingSequencesHe.isSelected()) {
				addCoolingSequencesHe(chart);
			} else {
				removeCoolingSequencesHe(chart);
			}
		});

		add(commandPanel);
	}

	private void displayReferenceCmd() {
		ImageIcon icon = new ImageIcon(getClass().getResource("/images/Gaia CMD BP-RP.png"));
		JFrame sedFrame = new JFrame();
		sedFrame.setIconImage(getToolBoxImage());
		sedFrame.setTitle("Gaia CMD G vs. BP-RP");
		sedFrame.add(new JLabel(icon));
		sedFrame.setSize(icon.getIconWidth() + 20, icon.getIconHeight());
		sedFrame.setLocation(0, 0);
		sedFrame.setAlwaysOnTop(false);
		sedFrame.setResizable(true);
		sedFrame.setVisible(true);
	}

	private void createChartPanel(GaiaCmd catalogEntry) {
		XYSeriesCollection mainCollection = createMainCollection();
		XYSeriesCollection targetCollection = createTargetCollection(catalogEntry);
		createChart(targetCollection, mainCollection);
		ChartPanel chartPanel = new ChartPanel(chart) {
			@Override
			public void mouseDragged(MouseEvent e) {
			}
		};
		chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		chartPanel.setBackground(Color.WHITE);
		add(chartPanel, 0);
		revalidate();
		repaint();
	}

	private XYSeriesCollection createMainCollection() {
		XYSeriesCollection collection = new XYSeriesCollection();
		XYSeries series = new XYSeries("");
		CMD_DATA.forEach(triplet -> {
			double x = g_rpButton.isSelected() ? triplet.getY() : triplet.getZ();
			double y = triplet.getX();
			if (x != 0 && y != 0) {
				series.add(x, y);
			}
		});
		collection.addSeries(series);
		return collection;
	}

	private XYSeriesCollection createTargetCollection(GaiaCmd catalogEntry) {
		XYSeriesCollection collection = new XYSeriesCollection();
		double xTarget = g_rpButton.isSelected() ? catalogEntry.getG_RP() : catalogEntry.getBP_RP();
		double yTarget = catalogEntry.getAbsoluteGmag();
		if (xTarget != 0 && yTarget != 0) {
			targetLabel = catalogEntry.getCatalogName() + " " + catalogEntry.getSourceId() + ": G="
					+ roundTo3DecNZ(yTarget) + " " + (g_rpButton.isSelected() ? "G-RP" : "BP-RP") + "="
					+ roundTo3DecNZ(xTarget);
			XYSeries seriesTarget = new XYSeries(targetLabel);
			seriesTarget.add(xTarget, yTarget);
			collection.addSeries(seriesTarget);
		}
		return collection;
	}

	private XYSeriesCollection createSpectralTypeCollection(String fileName) {
		XYSeriesCollection collection = new XYSeriesCollection();
		XYSeries series = new XYSeries(fileName, false);
		loadSpectralType(fileName).forEach(triplet -> {
			double x = g_rpButton.isSelected() ? triplet.getY() : triplet.getZ();
			double y = triplet.getX();
			if (x != 0 && y != 0) {
				series.add(x, y);
			}
		});
		collection.addSeries(series);
		return collection;
	}

	private XYSeriesCollection createCoolingSequenceCollection(String fileName) {
		XYSeriesCollection collection = new XYSeriesCollection();
		XYSeries series = new XYSeries(fileName, false);
		loadCoolingSequence(fileName).forEach(triplet -> {
			double x = g_rpButton.isSelected() ? triplet.getY() : triplet.getZ();
			double y = triplet.getX();
			if (x != 0 && y != 0) {
				series.add(x, y);
			}
		});
		collection.addSeries(series);
		return collection;
	}

	private void createChart(XYSeriesCollection targetCollection, XYSeriesCollection mainCollection) {
		chart = ChartFactory.createXYLineChart("Gaia Color-Magnitude Diagram", "", "", null);
		chart.setPadding(new RectangleInsets(10, 10, 10, 10));
		XYPlot plot = chart.getXYPlot();

		plot.setDataset(30, targetCollection);
		for (int i = 0, j = 31; i < 10; i++) {
			plot.setDataset(j++, createSpectralTypeCollection("M" + i));
		}
		plot.setDataset(41, mainCollection);

		NumberAxis xAxis = new NumberAxis(g_rpButton.isSelected() ? "G-RP" : "BP-RP");
		xAxis.setTickUnit(new NumberTickUnit(0.5));
		xAxis.setRange(g_rpButton.isSelected() ? -0.5 : -1.0, g_rpButton.isSelected() ? 2.5 : 5.5);
		plot.setDomainAxis(xAxis);

		NumberAxis yAxis = new NumberAxis("Abs G (mag)");
		yAxis.setTickUnit(new NumberTickUnit(5));
		yAxis.setRange(-2, 22);
		plot.setRangeAxis(yAxis);

		Font tickLabelFont = new Font(FONT_NAME, Font.PLAIN, 20);
		xAxis.setTickLabelFont(tickLabelFont);
		yAxis.setTickLabelFont(tickLabelFont);
		Font labelFont = new Font(FONT_NAME, Font.PLAIN, 20);
		xAxis.setLabelFont(labelFont);
		yAxis.setLabelFont(labelFont);

		double size = 10.0;
		double delta = size / 2.0;
		Shape targetShape = new Ellipse2D.Double(-delta, -delta, size, size);
		XYLineAndShapeRenderer targetRenderer = new XYLineAndShapeRenderer();
		targetRenderer.setSeriesPaint(0, Color.RED);
		targetRenderer.setSeriesLinesVisible(0, false);
		targetRenderer.setSeriesVisibleInLegend(0, true);
		targetRenderer.setSeriesShape(0, targetShape);

		size = 1.0;
		delta = size / 2.0;
		Shape mainShape = new Ellipse2D.Double(-delta, -delta, size, size);
		XYLineAndShapeRenderer mainRenderer = new XYLineAndShapeRenderer();
		mainRenderer.setSeriesPaint(0, Color.GRAY);
		mainRenderer.setSeriesLinesVisible(0, false);
		mainRenderer.setSeriesVisibleInLegend(0, false);
		mainRenderer.setSeriesShape(0, mainShape);

		plot.setRenderer(30, targetRenderer);
		int j = 31;
		for (Color color : COLORS) {
			plot.setRenderer(j++, getSpectralTypeRenderer(color));
		}
		plot.setRenderer(41, mainRenderer);

		LegendItemCollection itemCollection = new LegendItemCollection();
		int i = 0;
		for (Color color : COLORS) {
			LegendItem item = new LegendItem("M" + i++, color);
			item.setShape(targetShape);
			itemCollection.add(item);
		}
		if (targetCollection.getSeriesCount() > 0) {
			LegendItem item = new LegendItem(targetLabel, Color.RED);
			item.setShape(targetShape);
			itemCollection.add(item);
		}
		plot.setFixedLegendItems(itemCollection);

		plot.setBackgroundPaint(Color.WHITE);
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
		plot.setRangeGridlineStroke(new BasicStroke());
		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
		plot.setDomainGridlineStroke(new BasicStroke());
		plot.getRangeAxis().setInverted(true);

		Font legendFont = new Font(FONT_NAME, Font.PLAIN, 20);
		chart.getLegend().setFrame(BlockBorder.NONE);
		chart.getLegend().setItemFont(legendFont);

		Font titleFont = new Font(FONT_NAME, Font.PLAIN, 24);
		chart.getTitle().setFont(titleFont);

	}

	private XYLineAndShapeRenderer getSpectralTypeRenderer(Color color) {
		double size = 1.0;
		double delta = size / 2.0;
		Shape seriesShape = new Ellipse2D.Double(-delta, -delta, size, size);
		// size = 10.0;
		// delta = size / 2.0;
		// Shape legendShape = new Ellipse2D.Double(-delta, -delta, size, size);
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesPaint(0, color);
		renderer.setSeriesLinesVisible(0, false);
		renderer.setSeriesVisibleInLegend(0, false); // Replaced by custom legend due to badly sorted legend items
		renderer.setSeriesShape(0, seriesShape);
		// renderer.setLegendShape(0, legendShape);
		return renderer;
	}

	private void addCoolingSequencesH(JFreeChart chart) {
		XYPlot plot = chart.getXYPlot();
		for (int i = min; i < max; i++) {
			String mass = roundTo1Dec((double) i / 10);
			plot.setDataset(i, createCoolingSequenceCollection("Mass %s H".formatted(mass)));
		}

		XYLineAndShapeRenderer sequenceRendererH = new XYLineAndShapeRenderer();
		sequenceRendererH.setSeriesPaint(0, Color.RED);
		sequenceRendererH.setSeriesLinesVisible(0, true);
		sequenceRendererH.setSeriesShapesVisible(0, false);
		sequenceRendererH.setSeriesVisibleInLegend(0, false);

		for (int i = min; i < max; i++) {
			plot.setRenderer(i, sequenceRendererH);
		}
	}

	private void addCoolingSequencesHe(JFreeChart chart) {
		XYPlot plot = chart.getXYPlot();
		for (int i = min; i < max; i++) {
			String mass = roundTo1Dec((double) i / 10);
			plot.setDataset(i + max - min, createCoolingSequenceCollection("Mass %s He".formatted(mass)));
		}

		XYLineAndShapeRenderer sequenceRendererHe = new XYLineAndShapeRenderer();
		sequenceRendererHe.setSeriesPaint(0, Color.BLUE);
		sequenceRendererHe.setSeriesLinesVisible(0, true);
		sequenceRendererHe.setSeriesShapesVisible(0, false);
		sequenceRendererHe.setSeriesVisibleInLegend(0, false);

		for (int i = min; i < max; i++) {
			plot.setRenderer(i + max - min, sequenceRendererHe);
		}
	}

	private void removeCoolingSequencesH(JFreeChart chart) {
		XYSeriesCollection targetCollection;
		for (int i = min; i < max; i++) {
			targetCollection = (XYSeriesCollection) chart.getXYPlot().getDataset(i);
			targetCollection.removeAllSeries();
		}
	}

	private void removeCoolingSequencesHe(JFreeChart chart) {
		XYSeriesCollection targetCollection;
		for (int i = min; i < max; i++) {
			targetCollection = (XYSeriesCollection) chart.getXYPlot().getDataset(i + max - min);
			targetCollection.removeAllSeries();
		}
	}

	private void loadCmdData() {
		if (CMD_DATA != null) {
			return;
		}
		String tempDir = System.getProperty("java.io.tmpdir");
		Path filePath = Paths.get(tempDir, "Gaia_CMD_sample.csv");
		byte[] fileBytes;
		try {
			fileBytes = Files.readAllBytes(filePath);
		} catch (IOException ex) {
			try {
				InputStream inputStream = getClass().getResourceAsStream("/Gaia CMD sample.csv");
				Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
				fileBytes = Files.readAllBytes(filePath);
			} catch (IOException e) {
				return;
			}
		}
		try (Scanner scanner = new Scanner(new String(fileBytes))) {
			scanner.nextLine();
			CMD_DATA = new ArrayList();
			while (scanner.hasNextLine()) {
				String[] columnValues = scanner.nextLine().split(",", -1);
				double x = toDouble(columnValues[0]);
				double y = toDouble(columnValues[1]);
				double z = toDouble(columnValues[2]);
				CMD_DATA.add(new NumberTriplet(x, y, z));
			}
		}
	}

	private List<NumberTriplet> loadSpectralType(String fileName) {
		List<NumberTriplet> spectralType = new ArrayList();
		InputStream input = getClass().getResourceAsStream("/spectralTypes/" + fileName + ".csv");
		try (Scanner fileScanner = new Scanner(input)) {
			String headerLine = fileScanner.nextLine();
			String[] headers = CSVParser.parseLine(headerLine);
			Map<String, Integer> columns = new HashMap<>();
			for (int i = 0; i < headers.length; i++) {
				columns.put(headers[i], i);
			}
			while (fileScanner.hasNextLine()) {
				String bodyLine = fileScanner.nextLine();
				String[] values = CSVParser.parseLine(bodyLine);
				double G = toDouble(values[columns.get("M_G")]);
				double G_RP = toDouble(values[columns.get("G-RP")]);
				double BP_RP = toDouble(values[columns.get("BP-RP")]);
				spectralType.add(new NumberTriplet(G, G_RP, BP_RP));
			}
		}
		return spectralType;
	}

	private List<NumberTriplet> loadCoolingSequence(String fileName) {
		List<NumberTriplet> coolingSequence = new ArrayList();
		InputStream input = getClass().getResourceAsStream("/coolingSequences/" + fileName + ".csv");
		try (Scanner fileScanner = new Scanner(input)) {
			String headerLine = fileScanner.nextLine();
			String[] headers = CSVParser.parseLine(headerLine);
			Map<String, Integer> columns = new HashMap<>();
			for (int i = 0; i < headers.length; i++) {
				columns.put(headers[i], i);
			}
			while (fileScanner.hasNextLine()) {
				String bodyLine = fileScanner.nextLine();
				String[] values = CSVParser.parseLine(bodyLine);
				double G = toDouble(values[columns.get("G3")]);
				double BP = toDouble(values[columns.get("G3_BP")]);
				double RP = toDouble(values[columns.get("G3_RP")]);
				coolingSequence.add(new NumberTriplet(G, G - RP, BP - RP));
			}
		}
		return coolingSequence;
	}

}
