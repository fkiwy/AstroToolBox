package astro.tool.box.module.panel;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.module.Application.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.module.tab.SettingsTab.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.container.NumberTriplet;
import astro.tool.box.container.catalog.GaiaCmd;
import astro.tool.box.util.CSVParser;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class CmdPanel extends JPanel {

    private static final String FONT_NAME = "Tahoma";

    private final JRadioButton g_rpButton;
    private final JCheckBox coolingSequencesH;
    private final JCheckBox coolingSequencesHe;

    private final int min = 2;
    private final int max = 14;

    private JFreeChart chart;

    public CmdPanel(GaiaCmd catalogEntry) throws IOException {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        g_rpButton = new JRadioButton("G-RP", true);
        commandPanel.add(g_rpButton);

        JRadioButton bp_rpButton = new JRadioButton("BP-RP", false);
        commandPanel.add(bp_rpButton);

        ButtonGroup groupOne = new ButtonGroup();
        groupOne.add(g_rpButton);
        groupOne.add(bp_rpButton);

        String coolingTracksText = "Montreal cooling tracks for white dwarfs with masses between 0.2 (top) and 1.3 (bottom) Msun in steps of 0.1 Msun " + LINE_BREAK
                + "(red is pure H atmosphere or DA WDs, blue is pure He atmosphere or DB WDs)";

        JLabel coolingTracksLabel = new JLabel("     White dwarf cooling sequences");
        coolingTracksLabel.setToolTipText(html(coolingTracksText));
        commandPanel.add(coolingTracksLabel);

        JLabel coolingTracksToolTip = new JLabel(getInfoIcon());
        coolingTracksToolTip.setToolTipText(html(coolingTracksText));
        commandPanel.add(coolingTracksToolTip);

        coolingSequencesH = new JCheckBox("DA");
        commandPanel.add(coolingSequencesH);

        coolingSequencesHe = new JCheckBox("DB");
        commandPanel.add(coolingSequencesHe);

        JButton searchButton = new JButton("Create PDF");
        commandPanel.add(searchButton);
        searchButton.addActionListener((ActionEvent e) -> {
            try {
                File tmpFile = File.createTempFile("Target_" + roundTo2DecNZ(catalogEntry.getRa()) + addPlusSign(roundDouble(catalogEntry.getDec(), PATTERN_2DEC_NZ)) + "_", ".pdf");
                createPDF(chart, tmpFile, 1000, 900);
                Desktop.getDesktop().open(tmpFile);
            } catch (Exception ex) {
                Logger.getLogger(CmdPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        String infoText = "Right-clicking on the chart, opens a context menu with additional functions like printing and saving.";

        JLabel infoLabel = new JLabel("     Tooltip");
        infoLabel.setToolTipText(infoText);
        commandPanel.add(infoLabel);

        JLabel infoToolTip = new JLabel(getInfoIcon());
        infoToolTip.setToolTipText(infoText);
        commandPanel.add(infoToolTip);

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

    private XYSeriesCollection createSequenceCollection(String sequenceName) {
        XYSeriesCollection collection = new XYSeriesCollection();
        XYSeries series = new XYSeries(sequenceName, false);
        loadCoolingSequence(sequenceName).forEach(triplet -> {
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
            XYSeries seriesTarget = new XYSeries(catalogEntry.getCatalogName() + " " + catalogEntry.getSourceId()
                    + ": G=" + roundTo3DecNZ(yTarget) + " " + (g_rpButton.isSelected() ? "G-RP" : "BP-RP") + "=" + roundTo3DecNZ(xTarget));
            seriesTarget.add(xTarget, yTarget);
            collection.addSeries(seriesTarget);
        }
        return collection;
    }

    private void createChart(XYSeriesCollection targetCollection, XYSeriesCollection mainCollection) {
        chart = ChartFactory.createXYLineChart("Gaia Color-Magnitude Diagram", "", "", null);
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));
        XYPlot plot = chart.getXYPlot();
        plot.setDataset(0, targetCollection);
        plot.setDataset(1, mainCollection);

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
        Shape shape = new Ellipse2D.Double(-delta, -delta, size, size);
        XYLineAndShapeRenderer targetRenderer = new XYLineAndShapeRenderer();
        targetRenderer.setSeriesPaint(0, Color.RED);
        targetRenderer.setSeriesLinesVisible(0, false);
        targetRenderer.setSeriesVisibleInLegend(0, true);
        targetRenderer.setSeriesShape(0, shape);
        plot.setRenderer(0, targetRenderer);

        size = 0.2;
        delta = size / 2.0;
        shape = new Ellipse2D.Double(-delta, -delta, size, size);
        XYLineAndShapeRenderer mainRenderer = new XYLineAndShapeRenderer();
        mainRenderer.setSeriesPaint(0, Color.BLACK);
        mainRenderer.setSeriesLinesVisible(0, false);
        mainRenderer.setSeriesVisibleInLegend(0, false);
        mainRenderer.setSeriesShape(0, shape);
        plot.setRenderer(1, mainRenderer);

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

    private void addCoolingSequencesH(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        for (int i = min; i < max; i++) {
            String mass = roundTo1Dec((double) i / 10);
            plot.setDataset(i, createSequenceCollection(String.format("Mass %s H", mass)));
        }

        double size = 0.2;
        double delta = size / 2.0;
        Shape shape = new Ellipse2D.Double(-delta, -delta, size, size);

        XYLineAndShapeRenderer sequenceRendererH = new XYLineAndShapeRenderer();
        sequenceRendererH.setSeriesPaint(0, Color.RED);
        sequenceRendererH.setSeriesLinesVisible(0, true);
        sequenceRendererH.setSeriesVisibleInLegend(0, false);
        sequenceRendererH.setSeriesShape(0, shape);

        for (int i = min; i < max; i++) {
            plot.setRenderer(i, sequenceRendererH);
        }
    }

    private void addCoolingSequencesHe(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        for (int i = min; i < max; i++) {
            String mass = roundTo1Dec((double) i / 10);
            plot.setDataset(i + max - min, createSequenceCollection(String.format("Mass %s He", mass)));
        }

        double size = 0.2;
        double delta = size / 2.0;
        Shape shape = new Ellipse2D.Double(-delta, -delta, size, size);

        XYLineAndShapeRenderer sequenceRendererHe = new XYLineAndShapeRenderer();
        sequenceRendererHe.setSeriesPaint(0, Color.BLUE);
        sequenceRendererHe.setSeriesLinesVisible(0, true);
        sequenceRendererHe.setSeriesVisibleInLegend(0, false);
        sequenceRendererHe.setSeriesShape(0, shape);

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
        String gaiaCmdPath = getUserSetting(GAIA_CMD_PATH);
        if (gaiaCmdPath == null || gaiaCmdPath.isEmpty()) {
            throw new RuntimeException("Specify file location of Gaia CMD data in the Settings tab.");
        }
        File objectCollectionFile = new File(gaiaCmdPath);
        try (Scanner scanner = new Scanner(objectCollectionFile)) {
            scanner.nextLine();
            CMD_DATA = new ArrayList();
            while (scanner.hasNextLine()) {
                String[] columnValues = scanner.nextLine().split(",", -1);
                double x = toDouble(columnValues[0]);
                double y = toDouble(columnValues[1]);
                double z = toDouble(columnValues[2]);
                CMD_DATA.add(new NumberTriplet(x, y, z));
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    private List<NumberTriplet> loadCoolingSequence(String sequenceName) {
        List<NumberTriplet> coolingSequence = new ArrayList();
        InputStream input = getClass().getResourceAsStream("/sequences/" + sequenceName + ".csv");
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
