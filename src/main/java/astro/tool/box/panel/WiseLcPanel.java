package astro.tool.box.panel;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.StatisticFunctions.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.ServiceHelper.*;
import static astro.tool.box.main.ToolboxHelper.*;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.StatType;
import astro.tool.box.function.StatisticFunctions;
import astro.tool.box.util.ServiceHelper;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.round;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.groupingBy;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

public class WiseLcPanel extends JPanel {

    private final JTextField photSearchRadius;
    private final JCheckBox w1LightCurve;
    private final JCheckBox w2LightCurve;
    private final JCheckBox errorBars;

    private JFreeChart chart;

    private List<List<Double>> data = new ArrayList();

    public WiseLcPanel(CatalogEntry catalogEntry, JFrame baseFrame) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        commandPanel.add(new JLabel("Photometry search radius"));

        photSearchRadius = new JTextField("5", 3);
        commandPanel.add(photSearchRadius);
        photSearchRadius.addActionListener((ActionEvent e) -> {
            photSearchRadius.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            remove(0);
            try {
                collectPhotometry(catalogEntry);
                createPlot();
            } catch (IOException ex) {
                showExceptionDialog(baseFrame, ex);
            } finally {
                photSearchRadius.setCursor(Cursor.getDefaultCursor());
            }
        });

        w1LightCurve = new JCheckBox("W1", true);
        commandPanel.add(w1LightCurve);
        w1LightCurve.addActionListener((ActionEvent e) -> {
            remove(0);
            createPlot();
        });

        w2LightCurve = new JCheckBox("W2", true);
        commandPanel.add(w2LightCurve);
        w2LightCurve.addActionListener((ActionEvent e) -> {
            remove(0);
            createPlot();
        });

        errorBars = new JCheckBox("Errors", true);
        commandPanel.add(errorBars);
        errorBars.addActionListener((ActionEvent e) -> {
            remove(0);
            createPlot();
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

        try {
            collectPhotometry(catalogEntry);
            createPlot();
        } catch (IOException ex) {
            showExceptionDialog(baseFrame, ex);
        }

        add(commandPanel, 1);
    }

    private void collectPhotometry(CatalogEntry catalogEntry) throws IOException {
        double radius = toDouble(photSearchRadius.getText());;
        double ra = catalogEntry.getRa();
        double dec = catalogEntry.getDec();

        String queryUrl = ServiceHelper.createIrsaUrl(ra, dec, radius / DEG_ARCSEC, "allwise_p3as_mep");
        String response = readResponse(establishHttpConnection(queryUrl), "AllWISE");

        data = new ArrayList();
        try (Scanner scanner = new Scanner(response)) {
            String[] columnNames = scanner.nextLine().split(SPLIT_CHAR);
            int col1 = 0;
            int col2 = 0;
            int col3 = 0;
            for (int i = 0; i < columnNames.length; i++) {
                if (columnNames[i].equals("w1mpro_ep")) {
                    col1 = i;
                }
                if (columnNames[i].equals("w2mpro_ep")) {
                    col2 = i;
                }
                if (columnNames[i].equals("mjd")) {
                    col3 = i;
                }
            }
            while (scanner.hasNextLine()) {
                String[] columnValues = scanner.nextLine().split(SPLIT_CHAR);
                String w1 = columnValues[col1];
                String w2 = columnValues[col2];
                if (w1.isEmpty() || w2.isEmpty()) {
                    continue;
                }
                double mjd = Double.valueOf(columnValues[col3]);
                List<Double> row = new ArrayList();
                row.add(Double.valueOf(w1));
                row.add(Double.valueOf(w2));
                row.add(getObsTime(mjd));
                row.add((double) round(getObsTime(mjd) / 0.5));
                data.add(row);
            }
        }

        queryUrl = ServiceHelper.createIrsaUrl(ra, dec, radius / DEG_ARCSEC, "neowiser_p1bs_psd");
        response = readResponse(establishHttpConnection(queryUrl), "NeoWISE");
        try (Scanner scanner = new Scanner(response)) {
            String[] columnNames = scanner.nextLine().split(SPLIT_CHAR);
            int col1 = 0;
            int col2 = 0;
            int col3 = 0;
            for (int i = 0; i < columnNames.length; i++) {
                if (columnNames[i].equals("w1mpro")) {
                    col1 = i;
                }
                if (columnNames[i].equals("w2mpro")) {
                    col2 = i;
                }
                if (columnNames[i].equals("mjd")) {
                    col3 = i;
                }
            }
            while (scanner.hasNextLine()) {
                String[] columnValues = scanner.nextLine().split(SPLIT_CHAR);
                String w1 = columnValues[col1];
                String w2 = columnValues[col2];
                if (w1.isEmpty() || w2.isEmpty()) {
                    continue;
                }
                double mjd = Double.valueOf(columnValues[col3]);
                List<Double> row = new ArrayList();
                row.add(Double.valueOf(w1));
                row.add(Double.valueOf(w2));
                row.add(getObsTime(mjd));
                row.add((double) round(getObsTime(mjd) / 0.5));
                data.add(row);
            }
        }
    }

    private void createPlot() {
        List<Double> w1 = data.stream().map(v -> v.get(0)).collect(Collectors.toList());
        List<Double> w2 = data.stream().map(v -> v.get(1)).collect(Collectors.toList());
        List<Double> timeBin = data.stream().map(v -> v.get(3)).collect(Collectors.toList());

        List<NumberPair> w1Data = new ArrayList();
        for (int i = 0; i < w1.size(); i++) {
            w1Data.add(new NumberPair(timeBin.get(i), w1.get(i)));
        }

        Map<Double, Double> w1Median = w1Data.stream().collect(
                groupingBy(e -> e.getX(), Collectors.collectingAndThen(Collectors.toList(), e -> getMedian(e)))
        );

        Map<Double, Double> w1Error = w1Data.stream().collect(
                groupingBy(e -> e.getX(), Collectors.collectingAndThen(Collectors.toList(), e -> getError(e)))
        );

        List<Double> w1Values = new ArrayList(w1Median.values());
        List<Double> w1Errors = new ArrayList(w1Error.values());
        List<Double> w1MedianUpperError = new ArrayList();
        List<Double> w1MedianLowerError = new ArrayList();
        for (int i = 0; i < w1Values.size(); i++) {
            w1MedianUpperError.add(w1Values.get(i) + w1Errors.get(i));
            w1MedianLowerError.add(w1Values.get(i) - w1Errors.get(i));
        }

        List<NumberPair> w2Data = new ArrayList();
        for (int i = 0; i < w2.size(); i++) {
            w2Data.add(new NumberPair(timeBin.get(i), w2.get(i)));
        }

        Map<Double, Double> w2Median = w2Data.stream().collect(
                groupingBy(e -> e.getX(), Collectors.collectingAndThen(Collectors.toList(), e -> getMedian(e)))
        );

        Map<Double, Double> w2Error = w2Data.stream().collect(
                groupingBy(e -> e.getX(), Collectors.collectingAndThen(Collectors.toList(), e -> getError(e)))
        );

        List<Double> w2Values = new ArrayList(w2Median.values());
        List<Double> w2Errors = new ArrayList(w2Error.values());
        List<Double> w2MedianUpperError = new ArrayList();
        List<Double> w2MedianLowerError = new ArrayList();
        for (int i = 0; i < w2Values.size(); i++) {
            w2MedianUpperError.add(w2Values.get(i) + w2Errors.get(i));
            w2MedianLowerError.add(w2Values.get(i) - w2Errors.get(i));
        }

        List<List<Double>> list;
        list = removeOutliers(data, 0, 3, StatType.MEAN);
        list = removeOutliers(list, 1, 3, StatType.MEAN);

        w1 = list.stream().map(v -> v.get(0)).collect(Collectors.toList());
        w2 = list.stream().map(v -> v.get(1)).collect(Collectors.toList());
        List<Double> obsTime = list.stream().map(v -> v.get(2)).collect(Collectors.toList());

        boolean curves1 = w1LightCurve.isSelected();
        boolean curves2 = w2LightCurve.isSelected();
        boolean errors = errorBars.isSelected();

        JPlot plot = new JPlot("WISE light curves")
                .gridlines()
                .xAxis("Year")
                .xAxisNumberFormat(new DecimalFormat("#.#"))
                .yAxis("Magnitude (mag)")
                .yAxisInverted(true)
                .line("W2 median", w2Median.keySet().stream().map(e -> e * 0.5).collect(Collectors.toList()), new ArrayList(w2Median.values()), Color.RED, true, curves2)
                .line("W2 error", w2Error.keySet().stream().map(e -> e * 0.5).collect(Collectors.toList()), w2MedianLowerError, Color.LIGHT_GRAY, false, curves2 && errors)
                .line(null, w2Error.keySet().stream().map(e -> e * 0.5).collect(Collectors.toList()), w2MedianUpperError, Color.LIGHT_GRAY, false, curves2 && errors)
                .line("W1 median", w1Median.keySet().stream().map(e -> e * 0.5).collect(Collectors.toList()), new ArrayList(w1Median.values()), Color.BLUE, true, curves1)
                .line("W1 error", w1Error.keySet().stream().map(e -> e * 0.5).collect(Collectors.toList()), w1MedianLowerError, Color.LIGHT_GRAY, false, curves1 && errors)
                .line(null, w1Error.keySet().stream().map(e -> e * 0.5).collect(Collectors.toList()), w1MedianUpperError, Color.LIGHT_GRAY, false, curves1 && errors)
                .scatter("W2", obsTime, w2, Color.PINK, curves2)
                .scatter("W1", obsTime, w1, Color.CYAN, curves1);

        chart = plot.getChart();
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

    private double getMedian(List<NumberPair> pairs) {
        List<Double> values = pairs.stream().map(NumberPair::getY).collect(Collectors.toList());
        List<Double> clipped = removeOutliers(values, 3, StatType.MEAN);
        double median = StatisticFunctions.determineMedian(clipped);
        if (median == 0) {
            median = StatisticFunctions.determineMedian(values);
        }
        return median;
    }

    private double getError(List<NumberPair> pairs) {
        List<Double> values = pairs.stream().map(NumberPair::getY).collect(Collectors.toList());
        List<Double> clipped = removeOutliers(values, 3, StatType.MEAN);
        return StatisticFunctions.calculateStandardError(clipped);
    }

    private double getObsTime(double mjd) {
        LocalDateTime dt = convertMJDToDateTime(new BigDecimal(mjd));
        int year = dt.getYear();
        double day = dt.getDayOfYear();
        double days = dt.toLocalDate().isLeapYear() ? 366 : 365;
        return year + (day / days);
    }

}
