package astro.tool.box.module;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.module.tab.SettingsTab.*;
import astro.tool.box.container.NumberTriplet;
import astro.tool.box.container.catalog.GaiaCmd;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class CmdPanel extends JPanel {

    private static final String FONT_NAME = "Tahoma";

    private List<NumberTriplet> cmdData;

    public CmdPanel(GaiaCmd catalogEntry) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JRadioButton g_rpButton = new JRadioButton("G-RP", true);
        commandPanel.add(g_rpButton);
        g_rpButton.addActionListener((ActionEvent e) -> {
            remove(0);
            createChartPanel(catalogEntry, true);
        });

        JRadioButton bp_rpButton = new JRadioButton("BP-RP", false);
        commandPanel.add(bp_rpButton);
        bp_rpButton.addActionListener((ActionEvent e) -> {
            remove(0);
            createChartPanel(catalogEntry, false);
        });

        ButtonGroup groupOne = new ButtonGroup();
        groupOne.add(g_rpButton);
        groupOne.add(bp_rpButton);

        String info = "Right-clicking on the chart, opens a context menu with additional functions like printing and saving.";

        JLabel infoLabel = new JLabel("     Tooltip");
        infoLabel.setToolTipText(info);
        commandPanel.add(infoLabel);

        JLabel toolTip = new JLabel(getInfoIcon());
        toolTip.setToolTipText(html(info));
        commandPanel.add(toolTip);

        loadCmdData();

        createChartPanel(catalogEntry, true);

        add(commandPanel);
    }

    private void createChartPanel(GaiaCmd catalogEntry, boolean g_rp) {
        XYSeriesCollection collection = createCollection(g_rp);
        XYSeriesCollection collectionTarget = createCollectionTarget(catalogEntry, g_rp);
        JFreeChart chart = createChart(collection, collectionTarget, g_rp);
        ChartPanel chartPanel = new ChartPanel(chart) {
            @Override
            public void mouseDragged(MouseEvent e) {
            }
        };
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.WHITE);
        add(chartPanel, 0);
        revalidate();
        repaint();
    }

    private XYSeriesCollection createCollection(boolean g_rp) {
        XYSeriesCollection collection = new XYSeriesCollection();
        XYSeries series = new XYSeries("");
        cmdData.forEach(triplet -> {
            double x = g_rp ? triplet.getY() : triplet.getZ();
            double y = triplet.getX();
            if (x != 0 && y != 0) {
                series.add(x, y);
            }
        });
        collection.addSeries(series);
        return collection;
    }

    private XYSeriesCollection createCollectionTarget(GaiaCmd catalogEntry, boolean g_rp) {
        XYSeriesCollection collection = new XYSeriesCollection();
        double xTarget = g_rp ? catalogEntry.getG_RP() : catalogEntry.getBP_RP();
        double yTarget = catalogEntry.getAbsoluteGmag();
        if (xTarget != 0 && yTarget != 0) {
            XYSeries seriesTarget = new XYSeries(catalogEntry.getCatalogName() + " " + catalogEntry.getSourceId()
                    + ": G=" + roundTo3DecNZ(yTarget) + " " + (g_rp ? "G-RP" : "BP-RP") + "=" + roundTo3DecNZ(xTarget));
            seriesTarget.add(xTarget, yTarget);
            collection.addSeries(seriesTarget);
        }
        return collection;
    }

    private JFreeChart createChart(XYSeriesCollection collection, XYSeriesCollection collectionTarget, boolean g_rp) {
        JFreeChart chart = ChartFactory.createXYLineChart("Gaia Color-Magnitude Diagram", "", "", null);
        XYPlot plot = chart.getXYPlot();
        plot.setDataset(0, collectionTarget);
        plot.setDataset(1, collection);

        NumberAxis xAxis = new NumberAxis(g_rp ? "G-RP" : "BP-RP");
        xAxis.setTickUnit(new NumberTickUnit(0.5));
        xAxis.setRange(-0.5, g_rp ? 2.5 : 5.5);
        plot.setDomainAxis(xAxis);

        NumberAxis yAxis = new NumberAxis("Abs G (mag)");
        yAxis.setTickUnit(new NumberTickUnit(5));
        yAxis.setRange(-2, 22);
        plot.setRangeAxis(yAxis);

        Font tickLabelFont = new Font(FONT_NAME, Font.PLAIN, 16);
        xAxis.setTickLabelFont(tickLabelFont);
        yAxis.setTickLabelFont(tickLabelFont);
        Font labelFont = new Font(FONT_NAME, Font.PLAIN, 20);
        xAxis.setLabelFont(labelFont);
        yAxis.setLabelFont(labelFont);

        XYLineAndShapeRenderer rendererTarget = new XYLineAndShapeRenderer();
        rendererTarget.setSeriesPaint(0, Color.RED);
        rendererTarget.setSeriesLinesVisible(0, false);

        double size = 10.0;
        double delta = size / 2.0;
        //Shape shape = new Rectangle2D.Double(-delta, -delta, size, size);
        Shape shape = new Ellipse2D.Double(-delta, -delta, size, size);
        rendererTarget.setSeriesShape(0, shape);
        plot.setRenderer(0, rendererTarget);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLACK);
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesVisibleInLegend(0, false);

        size = 0.2;
        delta = size / 2.0;
        shape = new Ellipse2D.Double(-delta, -delta, size, size);
        renderer.setSeriesShape(0, shape);
        plot.setRenderer(1, renderer);

        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.getRangeAxis().setInverted(true);

        Font legendFont = new Font(FONT_NAME, Font.PLAIN, 16);
        chart.getLegend().setFrame(BlockBorder.NONE);
        chart.getLegend().setItemFont(legendFont);

        Font titleFont = new Font(FONT_NAME, Font.BOLD, 24);
        chart.getTitle().setFont(titleFont);

        return chart;
    }

    private void loadCmdData() {
        String gaiaCmdPath = getUserSetting(GAIA_CMD_PATH);
        if (gaiaCmdPath == null || gaiaCmdPath.isEmpty()) {
            throw new RuntimeException("Specify file location of Gaia CMD data in the Settings tab.");
        }
        File objectCollectionFile = new File(gaiaCmdPath);
        try (Scanner scanner = new Scanner(objectCollectionFile)) {
            scanner.nextLine();
            cmdData = new ArrayList();
            while (scanner.hasNextLine()) {
                String[] columnValues = scanner.nextLine().split(",", -1);
                double x = toDouble(columnValues[0]);
                double y = toDouble(columnValues[1]);
                double z = toDouble(columnValues[2]);
                cmdData.add(new NumberTriplet(x, y, z));
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

}
