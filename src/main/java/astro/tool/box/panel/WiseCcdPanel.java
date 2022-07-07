package astro.tool.box.panel;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.main.ToolboxHelper.*;
import astro.tool.box.catalog.AllWiseCatalogEntry;
import astro.tool.box.catalog.CatWiseCatalogEntry;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.TwoMassCatalogEntry;
import astro.tool.box.catalog.UkidssCatalogEntry;
import astro.tool.box.catalog.UnWiseCatalogEntry;
import astro.tool.box.catalog.VhsCatalogEntry;
import astro.tool.box.container.NumberPair;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.util.CSVParser;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class WiseCcdPanel extends JPanel {

    private static final String FONT_NAME = "Tahoma";

    private final CatalogQueryService catalogQueryService;
    private final JFrame baseFrame;

    private final JTextField photSearchRadius;

    private JFreeChart chart;

    private static final List<Color> COLORS = new ArrayList();

    static {
        COLORS.add(new Color(68, 1, 84));
        COLORS.add(new Color(65, 68, 135));
        COLORS.add(new Color(42, 120, 142));
        COLORS.add(new Color(34, 168, 132));
        COLORS.add(new Color(122, 209, 81));
        COLORS.add(new Color(253, 231, 37));
    }

    private static final List<String> LABELS = new ArrayList();

    static {
        LABELS.add("Late M");
        LABELS.add("Early L");
        LABELS.add("Late L");
        LABELS.add("Early T");
        LABELS.add("Late T");
        LABELS.add("Early Y");
    }

    private StringBuilder seriesLabel;

    public WiseCcdPanel(CatalogQueryService catalogQueryService, CatalogEntry catalogEntry, JFrame baseFrame) throws IOException {
        this.catalogQueryService = catalogQueryService;
        this.baseFrame = baseFrame;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        commandPanel.add(new JLabel("Photometry search radius"));

        photSearchRadius = new JTextField("5", 3);
        commandPanel.add(photSearchRadius);
        photSearchRadius.addActionListener((ActionEvent e) -> {
            photSearchRadius.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            remove(0);
            createChartPanel(getPhotometry(catalogEntry));
            photSearchRadius.setCursor(Cursor.getDefaultCursor());
        });

        JButton createButton = new JButton("Create PDF");
        commandPanel.add(createButton);
        createButton.addActionListener((ActionEvent e) -> {
            try {
                File tmpFile = File.createTempFile("Target_" + roundTo2DecNZ(catalogEntry.getRa()) + addPlusSign(roundDouble(catalogEntry.getDec(), PATTERN_2DEC_NZ)) + "_", ".pdf");
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

        createChartPanel(getPhotometry(catalogEntry));

        add(commandPanel, 1);
    }

    private NumberPair getPhotometry(CatalogEntry catalogEntry) {
        double searchRadius = toDouble(photSearchRadius.getText());
        double W1mag = 0;
        double W2mag = 0;
        double Jmag = 0;

        AllWiseCatalogEntry allWiseEntry;

        if (catalogEntry instanceof AllWiseCatalogEntry) {
            allWiseEntry = (AllWiseCatalogEntry) catalogEntry;
        } else {
            allWiseEntry = new AllWiseCatalogEntry();
            allWiseEntry.setRa(catalogEntry.getRa());
            allWiseEntry.setDec(catalogEntry.getDec());
            allWiseEntry.setSearchRadius(searchRadius);
            CatalogEntry retrievedEntry = retrieveCatalogEntry(allWiseEntry, catalogQueryService, baseFrame);
            if (retrievedEntry != null) {
                allWiseEntry = (AllWiseCatalogEntry) retrievedEntry;
            }
        }

        seriesLabel = new StringBuilder();
        if (allWiseEntry.getSourceId() != null) {
            seriesLabel.append(allWiseEntry.getCatalogName()).append(": ").append(allWiseEntry.getSourceId()).append(" ");
            W1mag = allWiseEntry.getW1mag();
            W2mag = allWiseEntry.getW2mag();
            Jmag = allWiseEntry.getJmag();
        }

        if (allWiseEntry.getSourceId() == null) {
            UnWiseCatalogEntry unWiseEntry = new UnWiseCatalogEntry();
            unWiseEntry.setRa(catalogEntry.getRa());
            unWiseEntry.setDec(catalogEntry.getDec());
            unWiseEntry.setSearchRadius(searchRadius);
            CatalogEntry retrievedEntry = retrieveCatalogEntry(unWiseEntry, catalogQueryService, baseFrame);
            if (retrievedEntry == null) {
                CatWiseCatalogEntry catWiseEntry = new CatWiseCatalogEntry();
                catWiseEntry.setRa(catalogEntry.getRa());
                catWiseEntry.setDec(catalogEntry.getDec());
                catWiseEntry.setSearchRadius(searchRadius);
                retrievedEntry = retrieveCatalogEntry(catWiseEntry, catalogQueryService, baseFrame);
                if (retrievedEntry != null) {
                    catWiseEntry = (CatWiseCatalogEntry) retrievedEntry;
                    seriesLabel.append(catWiseEntry.getCatalogName()).append(": ").append(catWiseEntry.getSourceId()).append(" ");
                    W1mag = catWiseEntry.getW1mag();
                    W2mag = catWiseEntry.getW2mag();
                }
            } else {
                unWiseEntry = (UnWiseCatalogEntry) retrievedEntry;
                seriesLabel.append(unWiseEntry.getCatalogName()).append(": ").append(unWiseEntry.getSourceId()).append(" ");
                W1mag = unWiseEntry.getW1mag();
                W2mag = unWiseEntry.getW2mag();
            }
        }

        if (Jmag == 0) {
            CatalogEntry retrievedEntry = null;
            VhsCatalogEntry vhsEntry;
            if (catalogEntry.getDec() < 5) {
                vhsEntry = new VhsCatalogEntry();
                vhsEntry.setRa(catalogEntry.getRa());
                vhsEntry.setDec(catalogEntry.getDec());
                vhsEntry.setSearchRadius(searchRadius);
                retrievedEntry = retrieveCatalogEntry(vhsEntry, catalogQueryService, baseFrame);
            }
            if (retrievedEntry != null) {
                vhsEntry = (VhsCatalogEntry) retrievedEntry;
                seriesLabel.append(vhsEntry.getCatalogName()).append(": ").append(vhsEntry.getSourceId()).append(" ");
                Jmag = vhsEntry.getJmag();
            } else {
                UkidssCatalogEntry ukidssEntry;
                if (catalogEntry.getDec() > -5) {
                    ukidssEntry = new UkidssCatalogEntry();
                    ukidssEntry.setRa(catalogEntry.getRa());
                    ukidssEntry.setDec(catalogEntry.getDec());
                    ukidssEntry.setSearchRadius(searchRadius);
                    retrievedEntry = retrieveCatalogEntry(ukidssEntry, catalogQueryService, baseFrame);
                }
                if (retrievedEntry != null) {
                    ukidssEntry = (UkidssCatalogEntry) retrievedEntry;
                    seriesLabel.append(ukidssEntry.getCatalogName()).append(": ").append(ukidssEntry.getSourceId()).append(" ");
                    Jmag = ukidssEntry.getJmag();
                } else {
                    TwoMassCatalogEntry twoMassEntry = new TwoMassCatalogEntry();
                    twoMassEntry.setRa(catalogEntry.getRa());
                    twoMassEntry.setDec(catalogEntry.getDec());
                    twoMassEntry.setSearchRadius(searchRadius * 2);
                    retrievedEntry = retrieveCatalogEntry(twoMassEntry, catalogQueryService, baseFrame);
                    if (retrievedEntry != null) {
                        twoMassEntry = (TwoMassCatalogEntry) retrievedEntry;
                        seriesLabel.append(twoMassEntry.getCatalogName()).append(": ").append(twoMassEntry.getSourceId()).append(" ");
                        Jmag = twoMassEntry.getJmag();
                    }
                }
            }
        }

        double W1_W2;
        if (W1mag == 0 || W2mag == 0) {
            W1_W2 = 0;
        } else {
            W1_W2 = W1mag - W2mag;
        }

        double J_W2;
        if (Jmag == 0 || W2mag == 0) {
            J_W2 = 0;
        } else {
            J_W2 = Jmag - W2mag;
        }

        return new NumberPair(W1_W2, J_W2);
    }

    private void createChartPanel(NumberPair pair) {
        XYSeriesCollection targetCollection = createTargetCollection(pair);
        createChart(targetCollection);
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

    private XYSeriesCollection createTargetCollection(NumberPair pair) {
        XYSeriesCollection collection = new XYSeriesCollection();
        double xTarget = pair.getX();
        double yTarget = pair.getY();
        if (xTarget != 0 && yTarget != 0) {
            XYSeries seriesTarget = new XYSeries(seriesLabel.toString());
            seriesTarget.add(xTarget, yTarget);
            collection.addSeries(seriesTarget);
        }
        return collection;
    }

    private XYSeriesCollection createSpectralTypeCollection(String fileName) {
        XYSeriesCollection collection = new XYSeriesCollection();
        XYSeries series = new XYSeries(fileName, false);
        loadSpectralType(fileName).forEach(pair -> {
            double x = pair.getX();
            double y = pair.getY();
            if (x != 0 && y != 0) {
                series.add(x, y);
            }
        });
        collection.addSeries(series);
        return collection;
    }

    private void createChart(XYSeriesCollection targetCollection) {
        chart = ChartFactory.createXYLineChart("WISE Color-Color Diagram", "", "", null);
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));
        XYPlot plot = chart.getXYPlot();

        plot.setDataset(0, targetCollection);
        for (int i = 0, j = 1; i < 6; i++) {
            plot.setDataset(j++, createSpectralTypeCollection(LABELS.get(i)));
        }

        NumberAxis xAxis = new NumberAxis("W1-W2");
        plot.setDomainAxis(xAxis);

        NumberAxis yAxis = new NumberAxis("J-W2");
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

        plot.setRenderer(0, targetRenderer);

        int j = 1;
        for (Color color : COLORS) {
            plot.setRenderer(j++, getSpectralTypeRenderer(color));
        }

        LegendItemCollection itemCollection = new LegendItemCollection();
        int i = 0;
        for (Color color : COLORS) {
            LegendItem item = new LegendItem(LABELS.get(i++), color);
            item.setShape(targetShape);
            itemCollection.add(item);
        }
        if (targetCollection.getSeriesCount() > 0) {
            LegendItem item = new LegendItem(seriesLabel.toString(), Color.RED);
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

        Font legendFont = new Font(FONT_NAME, Font.PLAIN, 20);
        chart.getLegend().setFrame(BlockBorder.NONE);
        chart.getLegend().setItemFont(legendFont);

        Font titleFont = new Font(FONT_NAME, Font.PLAIN, 24);
        chart.getTitle().setFont(titleFont);

    }

    private XYLineAndShapeRenderer getSpectralTypeRenderer(Color color) {
        double size = 6.0;
        double delta = size / 2.0;
        Shape seriesShape = new Ellipse2D.Double(-delta, -delta, size, size);
        //size = 10.0;
        //delta = size / 2.0;
        //Shape legendShape = new Ellipse2D.Double(-delta, -delta, size, size);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, color);
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesVisibleInLegend(0, false); // Replaced by custom legend due to badly sorted legend items
        renderer.setSeriesShape(0, seriesShape);
        //renderer.setLegendShape(0, legendShape);
        return renderer;
    }

    private List<NumberPair> loadSpectralType(String fileName) {
        List<NumberPair> spectralType = new ArrayList();
        InputStream input = getClass().getResourceAsStream("/spectralTypes/" + fileName + ".csv");
        try (Scanner fileScanner = new Scanner(input)) {
            String headerLine = fileScanner.nextLine();
            String[] headers = CSVParser.parseLine(headerLine);
            Map<String, Integer> columns = new HashMap();
            for (int i = 0; i < headers.length; i++) {
                columns.put(headers[i], i);
            }
            while (fileScanner.hasNextLine()) {
                String bodyLine = fileScanner.nextLine();
                String[] values = CSVParser.parseLine(bodyLine);
                double W1_W2 = toDouble(values[columns.get("W1-W2")]);
                double J_W2 = toDouble(values[columns.get("J-W2")]);
                spectralType.add(new NumberPair(W1_W2, J_W2));
            }
        }
        return spectralType;
    }

}
