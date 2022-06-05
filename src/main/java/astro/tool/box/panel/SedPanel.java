package astro.tool.box.panel;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.function.StatisticFunctions.*;
import static astro.tool.box.main.ToolboxHelper.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.container.SedFluxes;
import astro.tool.box.container.SedReferences;
import astro.tool.box.catalog.AllWiseCatalogEntry;
import astro.tool.box.catalog.CatWiseCatalogEntry;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.DesCatalogEntry;
import astro.tool.box.catalog.NoirlabCatalogEntry;
import astro.tool.box.catalog.PanStarrsCatalogEntry;
import astro.tool.box.catalog.TwoMassCatalogEntry;
import astro.tool.box.catalog.UkidssCatalogEntry;
import astro.tool.box.catalog.UnWiseCatalogEntry;
import astro.tool.box.catalog.VhsCatalogEntry;
import astro.tool.box.lookup.BrownDwarfLookupEntry;
import astro.tool.box.lookup.SpectralTypeLookup;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.SpectralType;
import astro.tool.box.service.CatalogQueryService;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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

public class SedPanel extends JPanel {

    private static final String FONT_NAME = "Tahoma";

    private final List<SpectralTypeLookup> brownDwarfLookupEntries;
    private final CatalogQueryService catalogQueryService;
    private final JFrame baseFrame;

    private final JComboBox spectralTypes;
    private final JCheckBox overplotTemplates;
    private final JTextField photSearchRadius;
    private final JTextField maxTemplateOffset;
    private final JButton removeButton;

    private Map<Band, SedReferences> sedReferences;
    private Map<Band, SedFluxes> sedFluxes;
    private Map<Band, Double> sedPhotometry;
    private Map<Band, String> sedCatalogs;
    private StringBuilder sedDataPoints;

    public SedPanel(List<SpectralTypeLookup> brownDwarfLookupEntries, CatalogQueryService catalogQueryService, CatalogEntry catalogEntry, JFrame baseFrame) {
        this.brownDwarfLookupEntries = brownDwarfLookupEntries;
        this.catalogQueryService = catalogQueryService;
        this.baseFrame = baseFrame;

        photSearchRadius = new JTextField("5", 3);
        maxTemplateOffset = new JTextField("0.2", 3);
        spectralTypes = new JComboBox(SpectralType.values());
        overplotTemplates = new JCheckBox("Overplot templates");
        overplotTemplates.setSelected(true);
        removeButton = new JButton("Remove all templates");

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

        commandPanel.add(new JLabel("SED templates: ", JLabel.RIGHT));
        commandPanel.add(spectralTypes);
        spectralTypes.addActionListener((ActionEvent e) -> {
            addReferenceSeds(sedPhotometry, collection);
        });

        commandPanel.add(removeButton);
        removeButton.addActionListener((ActionEvent e) -> {
            spectralTypes.setSelectedItem(SpectralType.SELECT);
            collection.removeAllSeries();
            createSed(catalogEntry, collection, false);
        });

        commandPanel.add(overplotTemplates);
        overplotTemplates.addActionListener((ActionEvent e) -> {
            spectralTypes.setSelectedItem(SpectralType.SELECT);
            collection.removeAllSeries();
            createSed(catalogEntry, collection, true);
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

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    private XYSeriesCollection createSed(CatalogEntry catalogEntry, XYSeriesCollection collection, boolean addReferenceSeds) {
        photSearchRadius.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        maxTemplateOffset.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        overplotTemplates.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

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

        if ("0".equals(panStarrsEntry.getSourceId())) {
            DesCatalogEntry desEntry = new DesCatalogEntry();
            desEntry.setRa(catalogEntry.getRa());
            desEntry.setDec(catalogEntry.getDec());
            desEntry.setSearchRadius(searchRadius);
            CatalogEntry retrievedEntry = retrieveCatalogEntry(desEntry, catalogQueryService, baseFrame);
            if (retrievedEntry == null) {
                NoirlabCatalogEntry noirlabEntry = new NoirlabCatalogEntry();
                noirlabEntry.setRa(catalogEntry.getRa());
                noirlabEntry.setDec(catalogEntry.getDec());
                noirlabEntry.setSearchRadius(searchRadius);
                retrievedEntry = retrieveCatalogEntry(noirlabEntry, catalogQueryService, baseFrame);
                if (retrievedEntry != null) {
                    noirlabEntry = (NoirlabCatalogEntry) retrievedEntry;
                    seriesLabel.append(noirlabEntry.getCatalogName()).append(": ").append(noirlabEntry.getSourceId()).append(" ");
                    sedCatalogs.put(Band.g, noirlabEntry.getCatalogName());
                    sedCatalogs.put(Band.r, noirlabEntry.getCatalogName());
                    sedCatalogs.put(Band.i, noirlabEntry.getCatalogName());
                    sedCatalogs.put(Band.z, noirlabEntry.getCatalogName());
                    sedCatalogs.put(Band.y, noirlabEntry.getCatalogName());
                    sedReferences.put(Band.g, new SedReferences(3631, 0.472));
                    sedReferences.put(Band.r, new SedReferences(3631, 0.6415));
                    sedReferences.put(Band.i, new SedReferences(3631, 0.7835));
                    sedReferences.put(Band.z, new SedReferences(3631, 0.926));
                    sedReferences.put(Band.y, new SedReferences(3631, 1.0095));
                    sedPhotometry.put(Band.g, noirlabEntry.get_g_mag());
                    sedPhotometry.put(Band.r, noirlabEntry.get_r_mag());
                    sedPhotometry.put(Band.i, noirlabEntry.get_i_mag());
                    sedPhotometry.put(Band.z, noirlabEntry.get_z_mag());
                    sedPhotometry.put(Band.y, noirlabEntry.get_y_mag());
                }
            } else {
                desEntry = (DesCatalogEntry) retrievedEntry;
                seriesLabel.append(desEntry.getCatalogName()).append(": ").append(desEntry.getSourceId()).append(" ");
                sedCatalogs.put(Band.g, desEntry.getCatalogName());
                sedCatalogs.put(Band.r, desEntry.getCatalogName());
                sedCatalogs.put(Band.i, desEntry.getCatalogName());
                sedCatalogs.put(Band.z, desEntry.getCatalogName());
                sedCatalogs.put(Band.y, desEntry.getCatalogName());
                sedReferences.put(Band.g, new SedReferences(3631, 0.472));
                sedReferences.put(Band.r, new SedReferences(3631, 0.6415));
                sedReferences.put(Band.i, new SedReferences(3631, 0.7835));
                sedReferences.put(Band.z, new SedReferences(3631, 0.926));
                sedReferences.put(Band.y, new SedReferences(3631, 1.0095));
                sedPhotometry.put(Band.g, desEntry.get_g_mag());
                sedPhotometry.put(Band.r, desEntry.get_r_mag());
                sedPhotometry.put(Band.i, desEntry.get_i_mag());
                sedPhotometry.put(Band.z, desEntry.get_z_mag());
                sedPhotometry.put(Band.y, desEntry.get_y_mag());
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
                UkidssCatalogEntry ukidssEntry = new UkidssCatalogEntry();
                ukidssEntry.setRa(catalogEntry.getRa());
                ukidssEntry.setDec(catalogEntry.getDec());
                ukidssEntry.setSearchRadius(searchRadius);
                retrievedEntry = retrieveCatalogEntry(ukidssEntry, catalogQueryService, baseFrame);
                if (retrievedEntry != null) {
                    ukidssEntry = (UkidssCatalogEntry) retrievedEntry;
                    seriesLabel.append(ukidssEntry.getCatalogName()).append(": ").append(ukidssEntry.getSourceId()).append(" ");
                    sedCatalogs.put(Band.J, UkidssCatalogEntry.CATALOG_NAME);
                    sedCatalogs.put(Band.H, UkidssCatalogEntry.CATALOG_NAME);
                    sedCatalogs.put(Band.K, UkidssCatalogEntry.CATALOG_NAME);
                    sedReferences.put(Band.J, new SedReferences(1553, 1.2483));
                    sedReferences.put(Band.H, new SedReferences(1034, 1.6313));
                    sedReferences.put(Band.K, new SedReferences(641.6, 2.201));
                    sedPhotometry.put(Band.J, ukidssEntry.getJmag());
                    sedPhotometry.put(Band.H, ukidssEntry.getHmag());
                    sedPhotometry.put(Band.K, ukidssEntry.getKmag());
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
        }

        Band.getSedBands().forEach(band -> {
            sedFluxes.put(band, new SedFluxes(
                    sedPhotometry.get(band),
                    convertMagnitudeToFlux(sedPhotometry.get(band), sedReferences.get(band).getZeropoint(), sedReferences.get(band).getWavelenth()),
                    convertMagnitudeToJanskys(sedPhotometry.get(band), sedReferences.get(band).getZeropoint()),
                    convertMagnitudeToFluxDensity(sedPhotometry.get(band), sedReferences.get(band).getZeropoint(), sedReferences.get(band).getWavelenth())
            ));
        });

        XYSeries series = new XYSeries(seriesLabel.toString());

        sedDataPoints.append(seriesLabel.toString()).append(LINE_SEP);
        Band.getSedBands().forEach(band -> {
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

        photSearchRadius.setCursor(Cursor.getDefaultCursor());
        maxTemplateOffset.setCursor(Cursor.getDefaultCursor());
        removeButton.setCursor(Cursor.getDefaultCursor());
        overplotTemplates.setCursor(Cursor.getDefaultCursor());

        return collection;
    }

    private void addReferenceSeds(Map<Band, Double> sedPhotometry, XYSeriesCollection collection) {
        for (SpectralTypeLookup lookupEntry : brownDwarfLookupEntries) {
            BrownDwarfLookupEntry entry = (BrownDwarfLookupEntry) lookupEntry;
            Map<Band, Double> bands = entry.getMagnitudes();
            String spectralType = entry.getSpt();
            if ("M0M1M2M3M4M5".contains(spectralType)) {
                continue;
            }

            List<Double> diffMags = new ArrayList();
            Band.getSedBands().forEach(band -> {
                if (sedPhotometry.get(band) != 0 && bands.get(band) != null) {
                    diffMags.add(sedPhotometry.get(band) - bands.get(band));
                }
            });
            if (diffMags.isEmpty()) {
                showInfoDialog(null, "No photometry found for SED." + LINE_SEP + "Increasing the search radius may help.");
                return;
            }
            int totalMags = diffMags.size();
            double medianDiffMag = determineMedian(diffMags);

            SpectralType selectedType = (SpectralType) spectralTypes.getSelectedItem();
            if (selectedType.equals(SpectralType.SELECT)) {
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
            } else if (selectedType.equals(SpectralType.valueOf(spectralType))) {
                createReferenceSed(spectralType, collection, medianDiffMag);
                return;
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
        series.add(0.481, magnitudes.get(Band.g) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.g) + medianDiffMag, 3631, 0.481));
        series.add(0.617, magnitudes.get(Band.r) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.r) + medianDiffMag, 3631, 0.617));
        series.add(0.752, magnitudes.get(Band.i) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.i) + medianDiffMag, 3631, 0.752));
        series.add(0.866, magnitudes.get(Band.z) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.z) + medianDiffMag, 3631, 0.866));
        series.add(0.962, magnitudes.get(Band.y) == 0 ? null : convertMagnitudeToFlux(magnitudes.get(Band.y) + medianDiffMag, 3631, 0.962));
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
        Band.getSedBands().forEach(band -> {
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
        for (SpectralTypeLookup lookupEntry : brownDwarfLookupEntries) {
            BrownDwarfLookupEntry entry = (BrownDwarfLookupEntry) lookupEntry;
            if (entry.getSpt().equals(spt)) {
                absoluteMagnitudes = entry.getMagnitudes();
            }
        }
        return absoluteMagnitudes;
    }

}
