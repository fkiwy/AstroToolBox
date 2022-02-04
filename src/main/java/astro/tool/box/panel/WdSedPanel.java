package astro.tool.box.panel;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.function.StatisticFunctions.*;
import static astro.tool.box.main.ModuleHelper.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.container.SedFluxes;
import astro.tool.box.container.WhiteDwarfEntry;
import astro.tool.box.container.SedReferences;
import astro.tool.box.catalog.AllWiseCatalogEntry;
import astro.tool.box.catalog.CatWiseCatalogEntry;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.GaiaDR3CatalogEntry;
import astro.tool.box.catalog.PanStarrsCatalogEntry;
import astro.tool.box.catalog.TwoMassCatalogEntry;
import astro.tool.box.catalog.UnWiseCatalogEntry;
import astro.tool.box.catalog.VhsCatalogEntry;
import astro.tool.box.enumeration.Band;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.util.CSVParser;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;
import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.CustomXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class WdSedPanel extends JPanel {

    private static final String FONT_NAME = "Tahoma";

    private final List<WhiteDwarfEntry> whiteDwarfEntries;
    private final CatalogQueryService catalogQueryService;
    private final JFrame baseFrame;

    private final JCheckBox overplotTemplates;
    private final JTextField photSearchRadius;
    private final JTextField maxTemplateOffset;

    private Map<Band, SedReferences> sedReferences;
    private Map<Band, SedFluxes> sedFluxes;
    private Map<Band, Double> sedPhotometry;
    private Map<Band, String> sedCatalogs;
    private StringBuilder sedDataPoints;

    private boolean useGaiaPhotometry;

    public WdSedPanel(CatalogQueryService catalogQueryService, CatalogEntry catalogEntry, JFrame baseFrame) {
        whiteDwarfEntries = new ArrayList();
        createWhiteDwarfSedEntries();

        this.catalogQueryService = catalogQueryService;
        this.baseFrame = baseFrame;

        photSearchRadius = new JTextField("5", 3);
        maxTemplateOffset = new JTextField("0.05", 3);
        overplotTemplates = new JCheckBox("Overplot templates");
        overplotTemplates.setSelected(true);

        XYSeriesCollection collection = createSed(catalogEntry, null, true);
        JFreeChart chart = createChart(collection);

        ChartPanel chartPanel = new ChartPanel(chart) {
            @Override
            public void mouseDragged(MouseEvent e) {
            }
        };
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chartPanel.setPreferredSize(new Dimension(1000, 850));
        chartPanel.setBackground(Color.WHITE);
        add(chartPanel);

        JPanel commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        add(commandPanel);

        commandPanel.add(new JLabel("Photometry search radius"));
        commandPanel.add(photSearchRadius);
        photSearchRadius.addActionListener((ActionEvent e) -> {
            collection.removeAllSeries();
            createSed(catalogEntry, collection, true);
            XYPlot plot = chart.getXYPlot();
            plot.getRenderer().setSeriesToolTipGenerator(0, addToolTips());
        });

        commandPanel.add(new JLabel("Maximum template offset"));
        commandPanel.add(maxTemplateOffset);
        maxTemplateOffset.addActionListener((ActionEvent e) -> {
            collection.removeAllSeries();
            createSed(catalogEntry, collection, true);
        });

        JButton removeButton = new JButton("Remove all templates");
        commandPanel.add(removeButton);
        removeButton.addActionListener((ActionEvent e) -> {
            collection.removeAllSeries();
            createSed(catalogEntry, collection, false);
        });

        commandPanel.add(overplotTemplates);
        overplotTemplates.addActionListener((ActionEvent e) -> {
            collection.removeAllSeries();
            createSed(catalogEntry, collection, true);
        });

        JButton searchButton = new JButton("Create PDF");
        commandPanel.add(searchButton);
        searchButton.addActionListener((ActionEvent e) -> {
            try {
                File tmpFile = File.createTempFile("Target_" + roundTo2DecNZ(catalogEntry.getRa()) + addPlusSign(roundDouble(catalogEntry.getDec(), PATTERN_2DEC_NZ)) + "_", ".pdf");
                createPDF(chart, tmpFile, 800, 700);
                Desktop.getDesktop().open(tmpFile);
            } catch (Exception ex) {
                writeErrorLog(ex);
            }
        });

        commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        add(commandPanel);

        JButton dataButton = new JButton("Get SED data points");
        commandPanel.add(dataButton);
        dataButton.addActionListener((ActionEvent e) -> {
            showScrollableDialog(baseFrame, "SED data points", sedDataPoints.toString());
        });

        String info = "Holding the mouse pointer over a data point on your object's SED (black line), shows the corresponding filter and wavelength." + LINE_BREAK
                + "Right-clicking on the chart, opens a context menu with additional functions like printing and saving.";

        JLabel infoLabel = new JLabel("Tooltip");
        infoLabel.setToolTipText(html(info));
        commandPanel.add(infoLabel);

        JLabel toolTip = new JLabel(getInfoIcon());
        toolTip.setToolTipText(html(info));
        commandPanel.add(toolTip);

        commandPanel.add(new JLabel("This feature uses the"));
        commandPanel.add(createHyperlink("Montreal cooling sequences.", "http://www.astro.umontreal.ca/~bergeron/CoolingModels"));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    private XYSeriesCollection createSed(CatalogEntry catalogEntry, XYSeriesCollection collection, boolean addReferenceSeds) {
        sedReferences = new HashMap();
        sedFluxes = new HashMap();
        sedPhotometry = new HashMap();
        sedCatalogs = new HashMap();
        sedDataPoints = new StringBuilder();

        double searchRadius = toDouble(photSearchRadius.getText());
        PanStarrsCatalogEntry panStarrsEntry;
        AllWiseCatalogEntry allWiseEntry;

        if (catalogEntry instanceof PanStarrsCatalogEntry) {
            panStarrsEntry = (PanStarrsCatalogEntry) catalogEntry;
        } else {
            panStarrsEntry = new PanStarrsCatalogEntry();
            panStarrsEntry.setRa(catalogEntry.getRa());
            panStarrsEntry.setDec(catalogEntry.getDec());
            panStarrsEntry.setSearchRadius(searchRadius);
            CatalogEntry retrievedEntry = retrieveCatalogEntry(panStarrsEntry, catalogQueryService, baseFrame);
            if (retrievedEntry != null) {
                panStarrsEntry = (PanStarrsCatalogEntry) retrievedEntry;
            }
        }

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

        StringBuilder seriesLabel = new StringBuilder();
        if (!"0".equals(panStarrsEntry.getSourceId())) {
            seriesLabel.append(panStarrsEntry.getCatalogName()).append(": ").append(panStarrsEntry.getSourceId()).append(" ");
        }
        if (allWiseEntry.getSourceId() != null) {
            seriesLabel.append(allWiseEntry.getCatalogName()).append(": ").append(allWiseEntry.getSourceId()).append(" ");
        }

        // Pan-STARRS
        sedCatalogs.put(Band.g, panStarrsEntry.getCatalogName());
        sedCatalogs.put(Band.r, panStarrsEntry.getCatalogName());
        sedCatalogs.put(Band.i, panStarrsEntry.getCatalogName());
        sedCatalogs.put(Band.z, panStarrsEntry.getCatalogName());
        sedCatalogs.put(Band.y, panStarrsEntry.getCatalogName());
        sedReferences.put(Band.g, new SedReferences(3631, 0.481));
        sedReferences.put(Band.r, new SedReferences(3631, 0.617));
        sedReferences.put(Band.i, new SedReferences(3631, 0.752));
        sedReferences.put(Band.z, new SedReferences(3631, 0.866));
        sedReferences.put(Band.y, new SedReferences(3631, 0.962));
        sedPhotometry.put(Band.g, panStarrsEntry.get_g_mag());
        sedPhotometry.put(Band.r, panStarrsEntry.get_r_mag());
        sedPhotometry.put(Band.i, panStarrsEntry.get_i_mag());
        sedPhotometry.put(Band.z, panStarrsEntry.get_z_mag());
        sedPhotometry.put(Band.y, panStarrsEntry.get_y_mag());

        // WISE
        sedCatalogs.put(Band.W1, AllWiseCatalogEntry.CATALOG_NAME);
        sedCatalogs.put(Band.W2, AllWiseCatalogEntry.CATALOG_NAME);
        sedCatalogs.put(Band.W3, AllWiseCatalogEntry.CATALOG_NAME);
        //sedCatalogs.put(Band.W4, AllWiseCatalogEntry.CATALOG_NAME);
        sedReferences.put(Band.W1, new SedReferences(309.54, 3.4));
        sedReferences.put(Band.W2, new SedReferences(171.79, 4.6));
        sedReferences.put(Band.W3, new SedReferences(31.676, 12));
        //sedReferences.put(Band.W4, new SedReferences(8.3635, 22));
        sedPhotometry.put(Band.W1, allWiseEntry.getW1mag());
        sedPhotometry.put(Band.W2, allWiseEntry.getW2mag());
        sedPhotometry.put(Band.W3, allWiseEntry.getW3mag());
        //sedPhotometry.put(Band.W4, allWiseEntry.getW4mag());

        // 2MASS
        sedCatalogs.put(Band.J, TwoMassCatalogEntry.CATALOG_NAME);
        sedCatalogs.put(Band.H, TwoMassCatalogEntry.CATALOG_NAME);
        sedCatalogs.put(Band.K, TwoMassCatalogEntry.CATALOG_NAME);
        sedReferences.put(Band.J, new SedReferences(1594, 1.235));
        sedReferences.put(Band.H, new SedReferences(1024, 1.662));
        sedReferences.put(Band.K, new SedReferences(666.7, 2.159));
        sedPhotometry.put(Band.J, allWiseEntry.getJmag());
        sedPhotometry.put(Band.H, allWiseEntry.getHmag());
        sedPhotometry.put(Band.K, allWiseEntry.getKmag());

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
                    sedCatalogs.put(Band.W1, CatWiseCatalogEntry.CATALOG_NAME);
                    sedCatalogs.put(Band.W2, CatWiseCatalogEntry.CATALOG_NAME);
                    sedReferences.put(Band.W1, new SedReferences(309.54, 3.4));
                    sedReferences.put(Band.W2, new SedReferences(171.79, 4.6));
                    sedPhotometry.put(Band.W1, catWiseEntry.getW1mag());
                    sedPhotometry.put(Band.W2, catWiseEntry.getW2mag());
                }
            } else {
                unWiseEntry = (UnWiseCatalogEntry) retrievedEntry;
                seriesLabel.append(unWiseEntry.getCatalogName()).append(": ").append(unWiseEntry.getSourceId()).append(" ");
                sedCatalogs.put(Band.W1, UnWiseCatalogEntry.CATALOG_NAME);
                sedCatalogs.put(Band.W2, UnWiseCatalogEntry.CATALOG_NAME);
                sedReferences.put(Band.W1, new SedReferences(309.54, 3.4));
                sedReferences.put(Band.W2, new SedReferences(171.79, 4.6));
                sedPhotometry.put(Band.W1, unWiseEntry.getW1mag());
                sedPhotometry.put(Band.W2, unWiseEntry.getW2mag());
            }
        }

        useGaiaPhotometry = false;
        if ("0".equals(panStarrsEntry.getSourceId())) {
            GaiaDR3CatalogEntry gaiaEntry = new GaiaDR3CatalogEntry();
            gaiaEntry.setRa(catalogEntry.getRa());
            gaiaEntry.setDec(catalogEntry.getDec());
            gaiaEntry.setSearchRadius(searchRadius);
            CatalogEntry retrievedEntry = retrieveCatalogEntry(gaiaEntry, catalogQueryService, baseFrame);
            if (retrievedEntry != null) {
                useGaiaPhotometry = true;
                gaiaEntry = (GaiaDR3CatalogEntry) retrievedEntry;
                seriesLabel.append(gaiaEntry.getCatalogName()).append(": ").append(gaiaEntry.getSourceId()).append(" ");
                // Zero points and wavelengths are for GAIA3 (http://svo2.cab.inta-csic.es/svo/theory/fps/index.php?mode=browse&gname=GAIA&gname2=GAIA3)
                sedCatalogs.put(Band.BP, gaiaEntry.getCatalogName());
                sedCatalogs.put(Band.G, gaiaEntry.getCatalogName());
                sedCatalogs.put(Band.RP, gaiaEntry.getCatalogName());
                sedReferences.put(Band.BP, new SedReferences(3552, 0.504));
                sedReferences.put(Band.G, new SedReferences(3229, 0.582));
                sedReferences.put(Band.RP, new SedReferences(2555, 0.762));
                sedPhotometry.put(Band.BP, gaiaEntry.getBPmag());
                sedPhotometry.put(Band.G, gaiaEntry.getGmag());
                sedPhotometry.put(Band.RP, gaiaEntry.getRPmag());
            }
        }

        if (sedPhotometry.get(Band.J) == 0 && sedPhotometry.get(Band.H) == 0 && sedPhotometry.get(Band.K) == 0) {
            VhsCatalogEntry vhsEntry = new VhsCatalogEntry();
            vhsEntry.setRa(catalogEntry.getRa());
            vhsEntry.setDec(catalogEntry.getDec());
            vhsEntry.setSearchRadius(searchRadius);
            CatalogEntry retrievedEntry = retrieveCatalogEntry(vhsEntry, catalogQueryService, baseFrame);
            if (retrievedEntry != null) {
                vhsEntry = (VhsCatalogEntry) retrievedEntry;
                seriesLabel.append(vhsEntry.getCatalogName()).append(": ").append(vhsEntry.getSourceId()).append(" ");
                sedCatalogs.put(Band.J, VhsCatalogEntry.CATALOG_NAME);
                sedCatalogs.put(Band.H, VhsCatalogEntry.CATALOG_NAME);
                sedCatalogs.put(Band.K, VhsCatalogEntry.CATALOG_NAME);
                sedReferences.put(Band.J, new SedReferences(1594, 1.235));
                sedReferences.put(Band.H, new SedReferences(1024, 1.662));
                sedReferences.put(Band.K, new SedReferences(666.7, 2.159));
                sedPhotometry.put(Band.J, vhsEntry.getJmag());
                sedPhotometry.put(Band.H, vhsEntry.getHmag());
                sedPhotometry.put(Band.K, vhsEntry.getKmag());
            } else {
                TwoMassCatalogEntry twoMassEntry = new TwoMassCatalogEntry();
                twoMassEntry.setRa(catalogEntry.getRa());
                twoMassEntry.setDec(catalogEntry.getDec());
                twoMassEntry.setSearchRadius(searchRadius);
                retrievedEntry = retrieveCatalogEntry(twoMassEntry, catalogQueryService, baseFrame);
                if (retrievedEntry != null) {
                    twoMassEntry = (TwoMassCatalogEntry) retrievedEntry;
                    seriesLabel.append(twoMassEntry.getCatalogName()).append(": ").append(twoMassEntry.getSourceId()).append(" ");
                    sedCatalogs.put(Band.J, TwoMassCatalogEntry.CATALOG_NAME);
                    sedCatalogs.put(Band.H, TwoMassCatalogEntry.CATALOG_NAME);
                    sedCatalogs.put(Band.K, TwoMassCatalogEntry.CATALOG_NAME);
                    sedReferences.put(Band.J, new SedReferences(1594, 1.235));
                    sedReferences.put(Band.H, new SedReferences(1024, 1.662));
                    sedReferences.put(Band.K, new SedReferences(666.7, 2.159));
                    sedPhotometry.put(Band.J, twoMassEntry.getJmag());
                    sedPhotometry.put(Band.H, twoMassEntry.getHmag());
                    sedPhotometry.put(Band.K, twoMassEntry.getKmag());
                }
            }
        }

        List<Band> sedBands = useGaiaPhotometry ? Band.getWdSedBands() : Band.getSedBands();
        sedBands.forEach(band -> {
            sedFluxes.put(band, new SedFluxes(
                    sedPhotometry.get(band),
                    convertMagnitudeToFlux(sedPhotometry.get(band), sedReferences.get(band).getZeropoint(), sedReferences.get(band).getWavelenth()),
                    convertMagnitudeToJanskys(sedPhotometry.get(band), sedReferences.get(band).getZeropoint()),
                    convertMagnitudeToFluxDensity(sedPhotometry.get(band), sedReferences.get(band).getZeropoint(), sedReferences.get(band).getWavelenth())
            ));
        });

        XYSeries series = new XYSeries(seriesLabel.toString());

        sedDataPoints.append(seriesLabel.toString()).append(LINE_SEP);
        sedBands.forEach(band -> {
            if (sedPhotometry.get(band) != 0) {
                sedDataPoints
                        .append("(")
                        .append(sedReferences.get(band).getWavelenth())
                        .append(",")
                        .append(sedPhotometry.get(band) == 0 ? null : sedFluxes.get(band).getFlux())
                        .append(")")
                        .append(LINE_SEP);
            }
            series.add(sedReferences.get(band).getWavelenth(), sedPhotometry.get(band) == 0 ? null : sedFluxes.get(band).getFlux());
        });

        if (collection == null) {
            collection = new XYSeriesCollection();
            collection.addSeries(series);
        } else {
            List<XYSeries> savedSeries = new ArrayList();
            savedSeries.addAll(collection.getSeries());
            collection.removeAllSeries();
            collection.addSeries(series);
            for (int y = 1; y < savedSeries.size(); y++) {
                collection.addSeries(savedSeries.get(y));
            }
        }

        if (addReferenceSeds) {
            addReferenceSeds(sedPhotometry, collection);
        }

        return collection;
    }

    private void addReferenceSeds(Map<Band, Double> sedPhotometry, XYSeriesCollection collection) {
        for (WhiteDwarfEntry entry : whiteDwarfEntries) {
            Map<Band, Double> bands = entry.getBands();
            String spectralType = entry.getInfo();

            List<Double> diffMags = new ArrayList();
            List<Band> sedBands = useGaiaPhotometry ? Band.getWdSedBands() : Band.getSedBands();
            sedBands.forEach(band -> {
                if (sedPhotometry.get(band) != 0 && bands.get(band) != null) {
                    diffMags.add(abs(sedPhotometry.get(band) - bands.get(band)));
                }
            });
            if (diffMags.isEmpty()) {
                showInfoDialog(null, "No photometry found for SED." + LINE_SEP + "Increasing the search radius may help.");
                return;
            }
            diffMags.sort(Comparator.naturalOrder());
            int totalMags = diffMags.size();
            double medianDiffMag = determineMedian(diffMags);

            if (totalMags >= 4) {
                double offset = toDouble(maxTemplateOffset.getText());
                int selectedMags = 0;
                for (Double diffMag : diffMags) {
                    if (diffMag >= medianDiffMag - offset && diffMag <= medianDiffMag + offset) {
                        selectedMags++;
                    }
                }
                if (selectedMags >= totalMags - (totalMags <= 5 ? 1 : 2)) {
                    createReferenceSed(spectralType, collection, medianDiffMag);
                }
            }
        }
    }

    private void createReferenceSed(String spectralType, XYSeriesCollection collection, double medianDiffMag) {
        Map<Band, Double> magnitudes = provideReferenceMagnitudes(spectralType);
        if (magnitudes == null) {
            return;
        }
        if (!overplotTemplates.isSelected()) {
            medianDiffMag = 0;
        }
        XYSeries series = new XYSeries(spectralType);
        if (useGaiaPhotometry) {
            series.add(0.504, magnitudes.get(Band.BP) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.BP) + medianDiffMag, 3552.01, 0.504));
            series.add(0.582, magnitudes.get(Band.G) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.G) + medianDiffMag, 3228.75, 0.582));
            series.add(0.762, magnitudes.get(Band.RP) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.RP) + medianDiffMag, 2554.95, 0.762));
        } else {
            series.add(0.481, magnitudes.get(Band.g) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.g) + medianDiffMag, 3631, 0.481));
            series.add(0.617, magnitudes.get(Band.r) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.r) + medianDiffMag, 3631, 0.617));
            series.add(0.752, magnitudes.get(Band.i) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.i) + medianDiffMag, 3631, 0.752));
            series.add(0.866, magnitudes.get(Band.z) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.z) + medianDiffMag, 3631, 0.866));
            series.add(0.962, magnitudes.get(Band.y) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.y) + medianDiffMag, 3631, 0.962));
        }
        series.add(1.235, magnitudes.get(Band.J) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.J) + medianDiffMag, 1594, 1.235));
        series.add(1.662, magnitudes.get(Band.H) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.H) + medianDiffMag, 1024, 1.662));
        series.add(2.159, magnitudes.get(Band.K) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.K) + medianDiffMag, 666.7, 2.159));
        series.add(3.4, magnitudes.get(Band.W1) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.W1) + medianDiffMag, 309.54, 3.4));
        series.add(4.6, magnitudes.get(Band.W2) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.W2) + medianDiffMag, 171.79, 4.6));
        series.add(12, magnitudes.get(Band.W3) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.W3) + medianDiffMag, 31.676, 12));

        sedDataPoints.append(spectralType).append(":").append(LINE_SEP);
        for (Object item : series.getItems()) {
            XYDataItem dataItem = (XYDataItem) item;
            if (!Double.isNaN(dataItem.getYValue())) {
                sedDataPoints
                        .append("(")
                        .append(dataItem.getXValue())
                        .append(",")
                        .append(dataItem.getYValue())
                        .append(")")
                        .append(LINE_SEP);
            }
        }

        try {
            collection.addSeries(series);
        } catch (IllegalArgumentException ex) {
        }
    }

    private JFreeChart createChart(XYSeriesCollection collection) {
        JFreeChart chart = ChartFactory.createXYLineChart("Spectral Energy Distribution", "", "", collection);
        chart.setPadding(new RectangleInsets(10, 10, 10, 10));
        XYPlot plot = chart.getXYPlot();

        LogAxis xAxis = new LogAxis("Wavelength (μm)");
        xAxis.setAutoRangeMinimumSize(0.1);
        xAxis.setTickUnit(new NumberTickUnit(0.2));
        //xAxis.setNumberFormatOverride(new DecimalFormat("#.#"));
        plot.setDomainAxis(xAxis);

        LogAxis yAxis = new LogAxis("λF(λ) (W/m²)");
        yAxis.setAutoRangeMinimumSize(1E-18);
        yAxis.setTickUnit(new NumberTickUnit(0.5));
        //yAxis.setNumberFormatOverride(new DecimalFormat("0E0"));
        plot.setRangeAxis(yAxis);

        Font tickLabelFont = new Font(FONT_NAME, Font.PLAIN, 18);
        xAxis.setTickLabelFont(tickLabelFont);
        yAxis.setTickLabelFont(tickLabelFont);
        Font labelFont = new Font(FONT_NAME, Font.PLAIN, 18);
        xAxis.setLabelFont(labelFont);
        yAxis.setLabelFont(labelFont);

        //XYSplineRenderer renderer = new XYSplineRenderer(100);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLACK);
        renderer.setSeriesStroke(0, new BasicStroke(2));
        renderer.setSeriesToolTipGenerator(0, addToolTips());

        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlineStroke(new BasicStroke());
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlineStroke(new BasicStroke());

        Font legendFont = new Font(FONT_NAME, Font.PLAIN, 18);
        chart.getLegend().setFrame(BlockBorder.NONE);
        chart.getLegend().setItemFont(legendFont);

        Font titleFont = new Font(FONT_NAME, Font.PLAIN, 22);
        chart.getTitle().setFont(titleFont);

        return chart;
    }

    private CustomXYToolTipGenerator addToolTips() {
        List<String> toolTips = new ArrayList();
        List<Band> sedBands = useGaiaPhotometry ? Band.getWdSedBands() : Band.getSedBands();
        sedBands.forEach(band -> {
            toolTips.add(html(sedCatalogs.get(band) + " "
                    + band.val + "=" + roundTo3DecNZ(sedFluxes.get(band).getMagnitude()) + " mag<br>"
                    + "λ=" + sedReferences.get(band).getWavelenth() + " μm<br>"
                    + "F(ν)=" + roundTo3DecSN(sedFluxes.get(band).getFluxDensity()) + " Jy<br>"
                    + "νF(ν)=" + roundTo3DecSN(sedFluxes.get(band).getFlux()) + " W/m^2<br>"
                    + "F(λ)=" + roundTo3DecSN(sedFluxes.get(band).getFluxLambda()) + " W/m^2/μm"));
        });
        CustomXYToolTipGenerator generator = new CustomXYToolTipGenerator();
        generator.addToolTipSeries(toolTips);
        return generator;
    }

    private Map<Band, Double> provideReferenceMagnitudes(String spt) {
        Map<Band, Double> absoluteMagnitudes = null;
        for (WhiteDwarfEntry entry : whiteDwarfEntries) {
            if (entry.getInfo().equals(spt)) {
                absoluteMagnitudes = entry.getBands();
            }
        }
        return absoluteMagnitudes;
    }

    public void createWhiteDwarfSedEntries() {
        InputStream input = getClass().getResourceAsStream("/WhiteDwarfLookupTable.csv");
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
                String type = values[columns.get("Type")];
                int teff = toInteger(values[columns.get("Teff")]);
                double logG = toDouble(values[columns.get("log g")]);
                double mass = toDouble(values[columns.get("M/Mo")]);
                String age = values[columns.get("Age")];
                double Gmag = toDouble(values[columns.get("G3")]);
                double BPmag = toDouble(values[columns.get("G3_BP")]);
                double RPmag = toDouble(values[columns.get("G3_RP")]);
                double g_mag = toDouble(values[columns.get("PS1_g")]);
                double r_mag = toDouble(values[columns.get("PS1_r")]);
                double i_mag = toDouble(values[columns.get("PS1_i")]);
                double z_mag = toDouble(values[columns.get("PS1_z")]);
                double y_mag = toDouble(values[columns.get("PS1_y")]);
                double Jmag = toDouble(values[columns.get("2MASS_J")]);
                double Hmag = toDouble(values[columns.get("2MASS_H")]);
                double Kmag = toDouble(values[columns.get("2MASS_Ks")]);
                double W1mag = toDouble(values[columns.get("W1")]);
                double W2mag = toDouble(values[columns.get("W2")]);
                double W3mag = toDouble(values[columns.get("W3")]);
                double W4mag = toDouble(values[columns.get("W4")]);
                Map<Band, Double> bands = new HashMap();
                bands.put(Band.G, Gmag);
                bands.put(Band.BP, BPmag);
                bands.put(Band.RP, RPmag);
                bands.put(Band.g, g_mag);
                bands.put(Band.r, r_mag);
                bands.put(Band.i, i_mag);
                bands.put(Band.z, z_mag);
                bands.put(Band.y, y_mag);
                bands.put(Band.J, Jmag);
                bands.put(Band.H, Hmag);
                bands.put(Band.K, Kmag);
                bands.put(Band.W1, W1mag);
                bands.put(Band.W2, W2mag);
                bands.put(Band.W3, W3mag);
                bands.put(Band.W4, W4mag);
                whiteDwarfEntries.add(new WhiteDwarfEntry(type, teff, logG, mass, age, bands));
            }
        }
    }

}
