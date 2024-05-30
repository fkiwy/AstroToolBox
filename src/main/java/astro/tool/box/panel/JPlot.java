package astro.tool.box.panel;

import static astro.tool.box.main.ToolboxHelper.*;
import static astro.tool.box.enumeration.FileType.*;
import astro.tool.box.enumeration.FileType;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class JPlot {

    private static final String TEMP_FILE_PREFIX = "DataPlot_";

    private final String title;

    private final JFreeChart chart;

    private final XYPlot plot;

    private NumberAxis xLinearAxis;

    private NumberAxis yLinearAxis;

    private LogAxis xLogAxis;

    private LogAxis yLogAxis;

    private boolean logarithmicScale;

    private boolean ignoreZeroValues;

    private int index = -1;

    public JPlot(String title) {
        this.title = title;
        deleteTempFiles();
        chart = ChartFactory.createXYLineChart(title, "", "", null);
        applyChartTheme();
        plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
    }

    public JPlot backgroundColor(Color color) {
        plot.setBackgroundPaint(color);
        return this;
    }

    public JPlot gridlines() {
        gridlines(Color.LIGHT_GRAY, 0.5f);
        return this;
    }

    public JPlot ignoreZeroValues(boolean ignoreZeroValues) {
        this.ignoreZeroValues = ignoreZeroValues;
        return this;
    }

    public JPlot gridlines(Color color, float thickness) {
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(color);
        plot.setRangeGridlineStroke(new BasicStroke(thickness));
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(color);
        plot.setDomainGridlineStroke(new BasicStroke(thickness));
        return this;
    }

    //
    //
    //
    //
    // Add line and shape thickness, additional shapes
    //
    //
    //
    //
    public JPlot xAxis(String axisLabel) {
        return xAxis(axisLabel, false);
    }

    public JPlot xAxis(String axisLabel, boolean logarithmicScale) {
        if (logarithmicScale) {
            xLogAxis = new LogAxis(axisLabel);
            plot.setDomainAxis(xLogAxis);
        } else {
            xLinearAxis = new NumberAxis(axisLabel);
            plot.setDomainAxis(xLinearAxis);
            xLinearAxis.setAutoRangeIncludesZero(false);
        }
        this.logarithmicScale = logarithmicScale;
        return this;
    }

    public JPlot xAxisNumberFormat(NumberFormat format) {
        if (logarithmicScale) {
            xLogAxis.setNumberFormatOverride(format);
        } else {
            xLinearAxis.setNumberFormatOverride(format);
        }
        return this;
    }

    public JPlot xAxisInverted(boolean inverted) {
        if (logarithmicScale) {
            xLogAxis.setInverted(inverted);
        } else {
            xLinearAxis.setInverted(inverted);
        }
        return this;
    }

    public JPlot xAxisRange(double from, double to) {
        if (logarithmicScale) {
            xLogAxis.setRange(from, to);
        } else {
            xLinearAxis.setRange(from, to);
        }
        return this;
    }

    public JPlot xAxisLowerBound(double lowerBound) {
        if (logarithmicScale) {
            xLogAxis.setAutoRangeMinimumSize(lowerBound);
        } else {
            xLinearAxis.setAutoRangeMinimumSize(lowerBound);
        }
        return this;
    }

    public JPlot xAxisTickInterval(double tickInterval) {
        if (logarithmicScale) {
            xLogAxis.setTickUnit(new NumberTickUnit(tickInterval));
        } else {
            xLinearAxis.setTickUnit(new NumberTickUnit(tickInterval));
        }
        return this;
    }

    public JPlot yAxis(String axisLabel) {
        return yAxis(axisLabel, false);
    }

    public JPlot yAxis(String axisLabel, boolean logarithmicScale) {
        if (logarithmicScale) {
            yLogAxis = new LogAxis(axisLabel);
            plot.setRangeAxis(yLogAxis);
        } else {
            yLinearAxis = new NumberAxis(axisLabel);
            plot.setRangeAxis(yLinearAxis);
            yLinearAxis.setAutoRangeIncludesZero(false);
        }
        this.logarithmicScale = logarithmicScale;
        return this;
    }

    public JPlot yAxisNumberFormat(NumberFormat format) {
        if (logarithmicScale) {
            yLogAxis.setNumberFormatOverride(format);
        } else {
            yLinearAxis.setNumberFormatOverride(format);
        }
        return this;
    }

    public JPlot yAxisInverted(boolean inverted) {
        if (logarithmicScale) {
            yLogAxis.setInverted(inverted);
        } else {
            yLinearAxis.setInverted(inverted);
        }
        return this;
    }

    public JPlot yAxisRange(double from, double to) {
        if (logarithmicScale) {
            yLogAxis.setRange(from, to);
        } else {
            yLinearAxis.setRange(from, to);
        }
        return this;
    }

    public JPlot yAxisLowerBound(double lowerBound) {
        if (logarithmicScale) {
            yLogAxis.setAutoRangeMinimumSize(lowerBound);
        } else {
            yLinearAxis.setAutoRangeMinimumSize(lowerBound);
        }
        return this;
    }

    public JPlot yAxisTickInterval(double tickInterval) {
        if (logarithmicScale) {
            yLogAxis.setTickUnit(new NumberTickUnit(tickInterval));
        } else {
            yLinearAxis.setTickUnit(new NumberTickUnit(tickInterval));
        }
        return this;
    }

    public JPlot error(String legendEntry, List<Double> x, List<Double> y, List<Double> error, Color color, boolean plot) {
        return error(legendEntry, x, y, error, error, color, plot);
    }

    public JPlot error(String legendEntry, List<Double> x, List<Double> y, List<Double> lowerError, List<Double> upperError, Color color, boolean plot) {
        if (plot) {
            List<Double> upperBound = new ArrayList();
            List<Double> lowerBound = new ArrayList();
            for (int i = 0; i < y.size(); i++) {
                upperBound.add(y.get(i) + upperError.get(i));
                lowerBound.add(y.get(i) - lowerError.get(i));
            }

            index++;
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setSeriesLinesVisible(0, true);
            renderer.setSeriesShapesVisible(0, true);
            addRenderer(legendEntry, x, y, color, renderer);

            index++;
            XYLineAndShapeRenderer upperErrorRenderer = new XYLineAndShapeRenderer();
            upperErrorRenderer.setSeriesLinesVisible(0, false);
            upperErrorRenderer.setSeriesShapesVisible(0, true);
            addRenderer(null, x, upperBound, color, upperErrorRenderer, getUpperErrorShape());

            index++;
            XYLineAndShapeRenderer lowerErrorRenderer = new XYLineAndShapeRenderer();
            lowerErrorRenderer.setSeriesLinesVisible(0, false);
            lowerErrorRenderer.setSeriesShapesVisible(0, true);
            addRenderer(null, x, lowerBound, color, lowerErrorRenderer, getLowerErrorShape());
        }
        return this;
    }

    public JPlot scatter(String legendEntry, List<Double> x, List<Double> y, Color color, boolean plot) {
        if (plot) {
            index++;
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setSeriesLinesVisible(0, false);
            renderer.setSeriesShapesVisible(0, true);
            addRenderer(legendEntry, x, y, color, renderer);
        }
        return this;
    }

    public JPlot line(String legendEntry, List<Double> x, List<Double> y, Color color, boolean showDataPoints, boolean plot) {
        if (plot) {
            index++;
            XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
            renderer.setSeriesLinesVisible(0, true);
            renderer.setSeriesShapesVisible(0, showDataPoints);
            addRenderer(legendEntry, x, y, color, renderer);
        }
        return this;
    }

    public JPlot curve(String legendEntry, List<Double> x, List<Double> y, Color color, boolean showDataPoints, boolean plot) {
        if (plot) {
            index++;
            XYSplineRenderer renderer = new XYSplineRenderer(100);
            renderer.setSeriesLinesVisible(0, true);
            renderer.setSeriesShapesVisible(0, showDataPoints);
            addRenderer(legendEntry, x, y, color, renderer);
        }
        return this;
    }

    public JPlot save(String filePath, FileType fileType, int width, int height) {
        try {
            File file = new File(addExtension(filePath, fileType));
            savePlot(file, fileType, width, height);
            return this;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public JPlot show(int width, int height, FileType fileType) {
        try {
            File file = File.createTempFile(TEMP_FILE_PREFIX, fileType.val);
            savePlot(file, fileType, width, height);
            Desktop.getDesktop().open(file);
            return this;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void show(int width, int height, JFrame baseFrame) {
        ChartPanel chartPanel = new ChartPanel(chart) {
            @Override
            public void mouseDragged(MouseEvent e) {
            }
        };
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chartPanel.setBackground(Color.WHITE);
        JFrame frame = new JFrame();
        frame.addWindowListener(getChildWindowAdapter(baseFrame));
        frame.setIconImage(getToolBoxImage());
        frame.setTitle(title);
        frame.add(chartPanel);
        frame.setSize(width, height);
        frame.setLocation(0, 0);
        frame.setAlwaysOnTop(false);
        frame.setResizable(true);
        frame.setVisible(true);
    }

    private void savePlot(File file, FileType fileType, int width, int height) {
        try {
            switch (fileType) {
                case JPEG ->
                    ChartUtils.saveChartAsJPEG(file, chart, width, height);
                case PNG ->
                    ChartUtils.saveChartAsPNG(file, chart, width, height);
                case PDF ->
                    savePDF(file, width, height);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void addRenderer(String legendEntry, List<Double> x, List<Double> y, Color color, XYItemRenderer renderer) {
        addRenderer(legendEntry, x, y, color, renderer, getShape());
    }

    private void addRenderer(String legendEntry, List<Double> x, List<Double> y, Color color, XYItemRenderer renderer, Shape shape) {
        if (legendEntry == null || legendEntry.isEmpty()) {
            legendEntry = "Dataset" + index;
            renderer.setSeriesVisibleInLegend(0, false);
        }
        renderer.setSeriesPaint(0, color);
        renderer.setSeriesShape(0, shape);
        plot.setRenderer(index, renderer);
        addDataset(legendEntry, x, y);
    }

    private void addDataset(String label, List<Double> x, List<Double> y) {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries(label);
        for (int i = 0; i < x.size(); i++) {
            double xVal = x.get(i);
            double yVal = y.get(i);
            if (ignoreZeroValues && (xVal == 0 || yVal == 0)) {
                continue;
            }
            series.add(xVal, yVal);
        }
        dataset.addSeries(series);
        plot.setDataset(index, dataset);
    }

    private String addExtension(String filePath, FileType fileType) {
        int lastIndex = filePath.lastIndexOf('.');
        if (lastIndex == -1) {
            return filePath + fileType.val;
        } else {
            return filePath.substring(0, lastIndex) + fileType.val;
        }
    }

    private Shape getShape() {
        double size = 6.0;
        double delta = size / 2.0;
        return new Ellipse2D.Double(-delta, -delta, size, size);
    }

    private Shape getUpperErrorShape() {
        Path2D tShape = new Path2D.Double();
        tShape.moveTo(0, 0);
        tShape.lineTo(-3, 0);
        tShape.moveTo(0, 0);
        tShape.lineTo(3, 0);
        tShape.moveTo(0, 0);
        tShape.lineTo(0, -6);
        return tShape;
    }

    private Shape getLowerErrorShape() {
        Path2D tShape = new Path2D.Double();
        tShape.moveTo(0, 0);
        tShape.lineTo(-3, 0);
        tShape.moveTo(0, 0);
        tShape.lineTo(3, 0);
        tShape.moveTo(0, 0);
        tShape.lineTo(0, 6);
        return tShape;
    }

    private void savePDF(File file, int width, int height) throws Exception {
        Rectangle pagesize = new Rectangle(width, height);
        Document document = new Document(pagesize, 50, 50, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        PdfContentByte contentByte = writer.getDirectContent();
        PdfTemplate template = contentByte.createTemplate(width, height);
        Graphics2D graphics = new PdfGraphics2D(contentByte, width, height);
        Rectangle2D rectangle = new Rectangle2D.Double(0, 0, width, height);
        chart.draw(graphics, rectangle);
        graphics.dispose();
        contentByte.addTemplate(template, 0, 0);
        document.close();
    }

    private void applyChartTheme() {
        StandardChartTheme chartTheme = (StandardChartTheme) StandardChartTheme.createJFreeTheme();

        Font extraLargeFont = chartTheme.getExtraLargeFont();
        Font largeFont = chartTheme.getLargeFont();
        Font regularFont = chartTheme.getRegularFont();
        Font smallFont = chartTheme.getSmallFont();

        extraLargeFont = new Font("Sans-serif", extraLargeFont.getStyle(), extraLargeFont.getSize());
        largeFont = new Font("Sans-serif", largeFont.getStyle(), largeFont.getSize());
        regularFont = new Font("Sans-serif", regularFont.getStyle(), regularFont.getSize());
        smallFont = new Font("Sans-serif", smallFont.getStyle(), smallFont.getSize());

        chartTheme.setExtraLargeFont(extraLargeFont);
        chartTheme.setLargeFont(largeFont);
        chartTheme.setRegularFont(regularFont);
        chartTheme.setSmallFont(smallFont);

        chartTheme.apply(chart);
    }

    private void deleteTempFiles() {
        String tmpdir = System.getProperty("java.io.tmpdir");
        File[] files = new File(tmpdir).
                listFiles(f -> f.getName().startsWith(TEMP_FILE_PREFIX));
        for (File file : files) {
            file.delete();
        }
    }

    public JFreeChart getChart() {
        return chart;
    }

}
