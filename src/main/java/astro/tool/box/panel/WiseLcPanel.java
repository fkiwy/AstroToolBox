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

    private final JTextField searchRadius;
    private final JTextField numberOfStds;
    private final JCheckBox w1Phot;
    private final JCheckBox w2Phot;
    private final JCheckBox curves;
    private final JCheckBox errors;

    private JFreeChart chart;

    private List<List<Double>> data = new ArrayList();

    public WiseLcPanel(CatalogEntry catalogEntry, JFrame baseFrame) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        commandPanel.add(new JLabel("Photometry search radius"));

        searchRadius = new JTextField("5", 3);
        commandPanel.add(searchRadius);
        searchRadius.addActionListener((ActionEvent e) -> {
            searchRadius.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            remove(0);
            try {
                collectPhotometry(catalogEntry);
                createPlot();
            } catch (IOException ex) {
                showExceptionDialog(baseFrame, ex);
            } finally {
                searchRadius.setCursor(Cursor.getDefaultCursor());
            }
        });

        commandPanel.add(new JLabel("Number of std deviations"));

        numberOfStds = new JTextField("3", 2);
        commandPanel.add(numberOfStds);
        numberOfStds.addActionListener((ActionEvent e) -> {
            remove(0);
            createPlot();
        });

        w1Phot = new JCheckBox("W1", true);
        commandPanel.add(w1Phot);
        w1Phot.addActionListener((ActionEvent e) -> {
            remove(0);
            createPlot();
        });

        w2Phot = new JCheckBox("W2", true);
        commandPanel.add(w2Phot);
        w2Phot.addActionListener((ActionEvent e) -> {
            remove(0);
            createPlot();
        });

        curves = new JCheckBox("Curves", true);
        commandPanel.add(curves);
        curves.addActionListener((ActionEvent e) -> {
            remove(0);
            createPlot();
        });

        errors = new JCheckBox("Errors", true);
        commandPanel.add(errors);
        errors.addActionListener((ActionEvent e) -> {
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
        double radius = toDouble(searchRadius.getText());
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
                double mjd = Double.parseDouble(columnValues[col3]);
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
                double mjd = Double.parseDouble(columnValues[col3]);
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
        List<List<Double>> list;
        double stds = toDouble(numberOfStds.getText());
        list = removeOutliers(data, 0, stds, StatType.MEAN);
        list = removeOutliers(list, 1, stds, StatType.MEAN);

        List<Double> w1 = list.stream().map(v -> v.get(0)).collect(Collectors.toList());
        List<Double> w2 = list.stream().map(v -> v.get(1)).collect(Collectors.toList());
        List<Double> timeBin = list.stream().map(v -> v.get(3)).collect(Collectors.toList());

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

        w1 = list.stream().map(v -> v.get(0)).collect(Collectors.toList());
        w2 = list.stream().map(v -> v.get(1)).collect(Collectors.toList());
        List<Double> obsTime = list.stream().map(v -> v.get(2)).collect(Collectors.toList());

        boolean w1Phot_ = w1Phot.isSelected();
        boolean w2Phot_ = w2Phot.isSelected();
        boolean curves_ = curves.isSelected();
        boolean errors_ = errors.isSelected();

        JPlot plot = new JPlot("WISE light curves")
                .gridlines()
                .xAxis("Year")
                .xAxisNumberFormat(new DecimalFormat("#.#"))
                .yAxis("Magnitude (mag)")
                .yAxisInverted(true)
                .line("W2 median", w2Median.keySet().stream().map(e -> e * 0.5).collect(Collectors.toList()), new ArrayList(w2Median.values()), Color.RED, true, w2Phot_ && curves_)
                .line("W2 error", w2Error.keySet().stream().map(e -> e * 0.5).collect(Collectors.toList()), w2MedianLowerError, Color.LIGHT_GRAY, false, w2Phot_ && curves_ && errors_)
                .line(null, w2Error.keySet().stream().map(e -> e * 0.5).collect(Collectors.toList()), w2MedianUpperError, Color.LIGHT_GRAY, false, w2Phot_ && curves_ && errors_)
                .line("W1 median", w1Median.keySet().stream().map(e -> e * 0.5).collect(Collectors.toList()), new ArrayList(w1Median.values()), Color.BLUE, true, w1Phot_ && curves_)
                .line("W1 error", w1Error.keySet().stream().map(e -> e * 0.5).collect(Collectors.toList()), w1MedianLowerError, Color.LIGHT_GRAY, false, w1Phot_ && curves_ && errors_)
                .line(null, w1Error.keySet().stream().map(e -> e * 0.5).collect(Collectors.toList()), w1MedianUpperError, Color.LIGHT_GRAY, false, w1Phot_ && curves_ && errors_)
                .scatter("W2", obsTime, w2, Color.PINK, w2Phot_)
                .scatter("W1", obsTime, w1, Color.CYAN, w1Phot_);

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
        return StatisticFunctions.determineMedian(values);
    }

    private double getError(List<NumberPair> pairs) {
        List<Double> values = pairs.stream().map(NumberPair::getY).collect(Collectors.toList());
        return StatisticFunctions.calculateStandardError(values);
    }

    private double getObsTime(double mjd) {
        LocalDateTime dt = convertMJDToDateTime(new BigDecimal(mjd));
        int year = dt.getYear();
        double day = dt.getDayOfYear();
        double days = dt.toLocalDate().isLeapYear() ? 366 : 365;
        return year + (day / days);
    }

}
