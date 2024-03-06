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
import astro.tool.box.container.SedBestMatch;
import astro.tool.box.lookup.BrownDwarfLookupEntry;
import astro.tool.box.lookup.SpectralTypeLookup;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Sed;
import astro.tool.box.enumeration.SpectralType;
import astro.tool.box.service.CatalogQueryService;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.io.File;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.Comparator;
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
import org.jfree.chart.renderer.xy.XYItemRenderer;
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

    private final JTextField photSearchRadius;
    private final JComboBox spectralTypes;
    private final JButton removeButton;
    private final JCheckBox bestMatch;
    private final JCheckBox overplotTemplates;
    private final JCheckBox commonReferences;

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
        spectralTypes = new JComboBox(SpectralType.values());
        removeButton = new JButton("Remove templates");
        bestMatch = new JCheckBox("Best match", true);
        overplotTemplates = new JCheckBox("Overplot templates", true);
        commonReferences = new JCheckBox("Use common zero points & wavelengths per band", false);

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
            setSeriesShape(chart);
            XYPlot plot = chart.getXYPlot();
            plot.getRenderer().setSeriesToolTipGenerator(0, addToolTips());
        });

        commandPanel.add(new JLabel("SED templates: ", JLabel.RIGHT));
        commandPanel.add(spectralTypes);
        spectralTypes.addActionListener((ActionEvent e) -> {
            addReferenceSeds(sedPhotometry, collection);
            setSeriesShape(chart);
        });

        commandPanel.add(removeButton);
        removeButton.addActionListener((ActionEvent e) -> {
            spectralTypes.setSelectedItem(SpectralType.SELECT);
            collection.removeAllSeries();
            createSed(catalogEntry, collection, false);
            setSeriesShape(chart);
        });

        commandPanel.add(bestMatch);
        bestMatch.addActionListener((ActionEvent e) -> {
            spectralTypes.setSelectedItem(SpectralType.SELECT);
            collection.removeAllSeries();
            createSed(catalogEntry, collection, true);
            setSeriesShape(chart);
        });

        commandPanel.add(overplotTemplates);
        overplotTemplates.addActionListener((ActionEvent e) -> {
            spectralTypes.setSelectedItem(SpectralType.SELECT);
            collection.removeAllSeries();
            createSed(catalogEntry, collection, true);
            setSeriesShape(chart);
        });

        commandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        add(commandPanel);

        commandPanel.add(commonReferences);
        commonReferences.addActionListener((ActionEvent e) -> {
            spectralTypes.setSelectedItem(SpectralType.SELECT);
            collection.removeAllSeries();
            createSed(catalogEntry, collection, true);
            setSeriesShape(chart);
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
        removeButton.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        bestMatch.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        overplotTemplates.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        commonReferences.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        sedReferences = new HashMap();
        sedFluxes = new HashMap();
        sedPhotometry = new HashMap();
        sedCatalogs = new HashMap();
        sedDataPoints = new StringBuilder();

        double searchRadius = toDouble(photSearchRadius.getText());
        searchRadius = searchRadius < 1 ? 1 : searchRadius;
        PanStarrsCatalogEntry panStarrsEntry;
        AllWiseCatalogEntry allWiseEntry;

        if (catalogEntry instanceof PanStarrsCatalogEntry entry) {
            panStarrsEntry = entry;
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

        if (catalogEntry instanceof AllWiseCatalogEntry entry) {
            allWiseEntry = entry;
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
        addPanStarrsReferences();
        sedPhotometry.put(Band.g, panStarrsEntry.get_g_err() == 0 ? 0 : panStarrsEntry.get_g_mag());
        sedPhotometry.put(Band.r, panStarrsEntry.get_r_err() == 0 ? 0 : panStarrsEntry.get_r_mag());
        sedPhotometry.put(Band.i, panStarrsEntry.get_i_err() == 0 ? 0 : panStarrsEntry.get_i_mag());
        sedPhotometry.put(Band.z, panStarrsEntry.get_z_err() == 0 ? 0 : panStarrsEntry.get_z_mag());
        sedPhotometry.put(Band.y, panStarrsEntry.get_y_err() == 0 ? 0 : panStarrsEntry.get_y_mag());

        // 2MASS
        sedCatalogs.put(Band.J, TwoMassCatalogEntry.CATALOG_NAME);
        sedCatalogs.put(Band.H, TwoMassCatalogEntry.CATALOG_NAME);
        sedCatalogs.put(Band.K, TwoMassCatalogEntry.CATALOG_NAME);
        add2Massferences();
        sedPhotometry.put(Band.J, allWiseEntry.getJ_err() == 0 ? 0 : allWiseEntry.getJmag());
        sedPhotometry.put(Band.H, allWiseEntry.getH_err() == 0 ? 0 : allWiseEntry.getHmag());
        sedPhotometry.put(Band.K, allWiseEntry.getK_err() == 0 ? 0 : allWiseEntry.getKmag());

        // WISE
        sedCatalogs.put(Band.W1, allWiseEntry.getCatalogName());
        sedCatalogs.put(Band.W2, allWiseEntry.getCatalogName());
        sedCatalogs.put(Band.W3, allWiseEntry.getCatalogName());
        addWiseReferences();
        sedPhotometry.put(Band.W1, allWiseEntry.getW1_err() == 0 ? 0 : allWiseEntry.getW1mag());
        sedPhotometry.put(Band.W2, allWiseEntry.getW2_err() == 0 ? 0 : allWiseEntry.getW2mag());
        sedPhotometry.put(Band.W3, allWiseEntry.getW3_err() == 0 ? 0 : allWiseEntry.getW3mag());

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
                    if (commonReferences.isSelected()) {
                        addPanStarrsReferences();
                    } else {
                        addDecamReferences();
                    }
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
                if (commonReferences.isSelected()) {
                    addPanStarrsReferences();
                } else {
                    addDecamReferences();
                }
                sedPhotometry.put(Band.g, desEntry.get_g_caut() > 3 ? 0 : desEntry.get_g_mag());
                sedPhotometry.put(Band.r, desEntry.get_r_caut() > 3 ? 0 : desEntry.get_r_mag());
                sedPhotometry.put(Band.i, desEntry.get_i_caut() > 3 ? 0 : desEntry.get_i_mag());
                sedPhotometry.put(Band.z, desEntry.get_z_caut() > 3 ? 0 : desEntry.get_z_mag());
                sedPhotometry.put(Band.y, desEntry.get_y_caut() > 3 ? 0 : desEntry.get_y_mag());
            }
        }

        if (sedPhotometry.get(Band.J) == 0 && sedPhotometry.get(Band.H) == 0 && sedPhotometry.get(Band.K) == 0) {
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
                sedCatalogs.put(Band.J, vhsEntry.getCatalogName());
                sedCatalogs.put(Band.H, vhsEntry.getCatalogName());
                sedCatalogs.put(Band.K, vhsEntry.getCatalogName());
                if (commonReferences.isSelected()) {
                    add2Massferences();
                } else {
                    sedReferences.put(Band.J, new SedReferences(Sed.VHS_J.zeropoint, Sed.VHS_J.wavelenth));
                    sedReferences.put(Band.H, new SedReferences(Sed.VHS_H.zeropoint, Sed.VHS_H.wavelenth));
                    sedReferences.put(Band.K, new SedReferences(Sed.VHS_K.zeropoint, Sed.VHS_K.wavelenth));
                }
                sedPhotometry.put(Band.J, vhsEntry.getJmag());
                sedPhotometry.put(Band.H, vhsEntry.getHmag());
                sedPhotometry.put(Band.K, vhsEntry.getKmag());

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
                    sedCatalogs.put(Band.J, ukidssEntry.getCatalogName());
                    sedCatalogs.put(Band.H, ukidssEntry.getCatalogName());
                    sedCatalogs.put(Band.K, ukidssEntry.getCatalogName());
                    if (commonReferences.isSelected()) {
                        add2Massferences();
                    } else {
                        sedReferences.put(Band.J, new SedReferences(Sed.UKIDSS_J.zeropoint, Sed.UKIDSS_J.wavelenth));
                        sedReferences.put(Band.H, new SedReferences(Sed.UKIDSS_H.zeropoint, Sed.UKIDSS_H.wavelenth));
                        sedReferences.put(Band.K, new SedReferences(Sed.UKIDSS_K.zeropoint, Sed.UKIDSS_K.wavelenth));
                    }
                    sedPhotometry.put(Band.J, ukidssEntry.getJmag());
                    sedPhotometry.put(Band.H, ukidssEntry.getHmag());
                    sedPhotometry.put(Band.K, ukidssEntry.getKmag());
                } else {
                    TwoMassCatalogEntry twoMassEntry = new TwoMassCatalogEntry();
                    twoMassEntry.setRa(catalogEntry.getRa());
                    twoMassEntry.setDec(catalogEntry.getDec());
                    twoMassEntry.setSearchRadius(searchRadius * 2);
                    retrievedEntry = retrieveCatalogEntry(twoMassEntry, catalogQueryService, baseFrame);
                    if (retrievedEntry != null) {
                        twoMassEntry = (TwoMassCatalogEntry) retrievedEntry;
                        seriesLabel.append(twoMassEntry.getCatalogName()).append(": ").append(twoMassEntry.getSourceId()).append(" ");
                        sedCatalogs.put(Band.J, twoMassEntry.getCatalogName());
                        sedCatalogs.put(Band.H, twoMassEntry.getCatalogName());
                        sedCatalogs.put(Band.K, twoMassEntry.getCatalogName());
                        add2Massferences();
                        sedPhotometry.put(Band.J, twoMassEntry.getJ_err() == 0 ? 0 : twoMassEntry.getJmag());
                        sedPhotometry.put(Band.H, twoMassEntry.getH_err() == 0 ? 0 : twoMassEntry.getHmag());
                        sedPhotometry.put(Band.K, twoMassEntry.getK_err() == 0 ? 0 : twoMassEntry.getKmag());
                    }
                }
            }
        }

        if (allWiseEntry.getSourceId() == null) {
            CatWiseCatalogEntry catWiseEntry = new CatWiseCatalogEntry();
            catWiseEntry.setRa(catalogEntry.getRa());
            catWiseEntry.setDec(catalogEntry.getDec());
            catWiseEntry.setSearchRadius(searchRadius);
            CatalogEntry retrievedEntry = retrieveCatalogEntry(catWiseEntry, catalogQueryService, baseFrame);
            if (retrievedEntry == null) {
                UnWiseCatalogEntry unWiseEntry = new UnWiseCatalogEntry();
                unWiseEntry.setRa(catalogEntry.getRa());
                unWiseEntry.setDec(catalogEntry.getDec());
                unWiseEntry.setSearchRadius(searchRadius);
                retrievedEntry = retrieveCatalogEntry(unWiseEntry, catalogQueryService, baseFrame);
                if (retrievedEntry != null) {
                    unWiseEntry = (UnWiseCatalogEntry) retrievedEntry;
                    seriesLabel.append(unWiseEntry.getCatalogName()).append(": ").append(unWiseEntry.getSourceId()).append(" ");
                    sedCatalogs.put(Band.W1, unWiseEntry.getCatalogName());
                    sedCatalogs.put(Band.W2, unWiseEntry.getCatalogName());
                    addWiseReferences();
                    sedPhotometry.put(Band.W1, unWiseEntry.getW1mag());
                    sedPhotometry.put(Band.W2, unWiseEntry.getW2mag());
                }
            } else {
                catWiseEntry = (CatWiseCatalogEntry) retrievedEntry;
                seriesLabel.append(catWiseEntry.getCatalogName()).append(": ").append(catWiseEntry.getSourceId()).append(" ");
                sedCatalogs.put(Band.W1, catWiseEntry.getCatalogName());
                sedCatalogs.put(Band.W2, catWiseEntry.getCatalogName());
                addWiseReferences();
                sedPhotometry.put(Band.W1, catWiseEntry.getW1mag());
                sedPhotometry.put(Band.W2, catWiseEntry.getW2mag());
            }
        }

        Band.getSedBands().forEach(band -> {
            sedFluxes.put(band, new SedFluxes(
                    sedPhotometry.get(band),
                    convertMagnitudeToFluxDensity(sedPhotometry.get(band), sedReferences.get(band).getZeropoint(), sedReferences.get(band).getWavelenth()),
                    convertMagnitudeToFluxJansky(sedPhotometry.get(band), sedReferences.get(band).getZeropoint()),
                    convertMagnitudeToFluxLambda(sedPhotometry.get(band), sedReferences.get(band).getZeropoint(), sedReferences.get(band).getWavelenth())
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
                        .append(sedPhotometry.get(band) == 0 ? null : sedFluxes.get(band).getFluxDensity())
                        .append(")")
                        .append(LINE_SEP);
            }
            series.add(sedReferences.get(band).getWavelenth(), sedPhotometry.get(band) == 0 ? null : sedFluxes.get(band).getFluxDensity());
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
        removeButton.setCursor(Cursor.getDefaultCursor());
        bestMatch.setCursor(Cursor.getDefaultCursor());
        overplotTemplates.setCursor(Cursor.getDefaultCursor());
        commonReferences.setCursor(Cursor.getDefaultCursor());

        return collection;
    }

    private void addDecamReferences() {
        sedReferences.put(Band.g, new SedReferences(Sed.DECAM_G.zeropoint, Sed.DECAM_G.wavelenth));
        sedReferences.put(Band.r, new SedReferences(Sed.DECAM_R.zeropoint, Sed.DECAM_R.wavelenth));
        sedReferences.put(Band.i, new SedReferences(Sed.DECAM_I.zeropoint, Sed.DECAM_I.wavelenth));
        sedReferences.put(Band.z, new SedReferences(Sed.DECAM_Z.zeropoint, Sed.DECAM_Z.wavelenth));
        sedReferences.put(Band.y, new SedReferences(Sed.DECAM_Y.zeropoint, Sed.DECAM_Y.wavelenth));
    }

    private void addPanStarrsReferences() {
        sedReferences.put(Band.g, new SedReferences(Sed.PS1_G.zeropoint, Sed.PS1_G.wavelenth));
        sedReferences.put(Band.r, new SedReferences(Sed.PS1_R.zeropoint, Sed.PS1_R.wavelenth));
        sedReferences.put(Band.i, new SedReferences(Sed.PS1_I.zeropoint, Sed.PS1_I.wavelenth));
        sedReferences.put(Band.z, new SedReferences(Sed.PS1_Z.zeropoint, Sed.PS1_Z.wavelenth));
        sedReferences.put(Band.y, new SedReferences(Sed.PS1_Y.zeropoint, Sed.PS1_Y.wavelenth));
    }

    private void add2Massferences() {
        sedReferences.put(Band.J, new SedReferences(Sed.MASS_J.zeropoint, Sed.MASS_J.wavelenth));
        sedReferences.put(Band.H, new SedReferences(Sed.MASS_H.zeropoint, Sed.MASS_H.wavelenth));
        sedReferences.put(Band.K, new SedReferences(Sed.MASS_K.zeropoint, Sed.MASS_K.wavelenth));
    }

    private void addWiseReferences() {
        sedReferences.put(Band.W1, new SedReferences(Sed.WISE_W1.zeropoint, Sed.WISE_W1.wavelenth));
        sedReferences.put(Band.W2, new SedReferences(Sed.WISE_W2.zeropoint, Sed.WISE_W2.wavelenth));
        sedReferences.put(Band.W3, new SedReferences(Sed.WISE_W3.zeropoint, Sed.WISE_W3.wavelenth));
    }

    private void addReferenceSeds(Map<Band, Double> sedPhotometry, XYSeriesCollection collection) {
        SpectralType selectedType = (SpectralType) spectralTypes.getSelectedItem();
        List<SedBestMatch> matches = new ArrayList();
        for (SpectralTypeLookup lookupEntry : brownDwarfLookupEntries) {
            BrownDwarfLookupEntry entry = (BrownDwarfLookupEntry) lookupEntry;
            Map<Band, Double> bands = entry.getMagnitudes();
            String spectralType = entry.getSpt();
            List<Double> diffMags = new ArrayList();
            Band.getSedBands().forEach(band -> {
                if (sedPhotometry.get(band) != 0 && bands.get(band) != 0) {
                    double diffMag = sedPhotometry.get(band) - bands.get(band);
                    diffMags.add(diffMag);
                }
            });
            double medianDiffMag = determineMedian(diffMags);
            if (selectedType.equals(SpectralType.SELECT)) {
                int totalMags = diffMags.size();
                if (totalMags < 6) {
                    continue;
                }
                int selectedMags = 0;
                List<Double> correctedDiffMags = new ArrayList();
                for (Double diffMag : diffMags) {
                    double correctedDiffMag = abs(diffMag - medianDiffMag);
                    correctedDiffMags.add(correctedDiffMag);
                    if (correctedDiffMag < 0.3) {
                        selectedMags++;
                    }
                }
                if (selectedMags < totalMags - 2) {
                    continue;
                }
                double meanDiffMag = calculateMean(correctedDiffMags);
                matches.add(new SedBestMatch(spectralType, medianDiffMag, meanDiffMag));
            } else if (selectedType.equals(SpectralType.valueOf(spectralType))) {
                createReferenceSed(spectralType, collection, medianDiffMag);
                return;
            }
        }
        if (matches.isEmpty()) {
            showInfoDialog(null, "No match found.");
            return;
        }
        matches.sort(Comparator.comparing(SedBestMatch::getMeanDiffMag));
        int j = bestMatch.isSelected() ? 1 : 3;
        for (int i = 0; i < j && i < matches.size(); i++) {
            SedBestMatch match = matches.get(i);
            createReferenceSed(match.getSpt(), collection, match.getMedianDiffMag());
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
        series.add(Sed.PS1_G.wavelenth, magnitudes.get(Band.g) == 0 ? null : convertMagnitudeToFluxDensity(magnitudes.get(Band.g) + medianDiffMag, Sed.PS1_G.zeropoint, Sed.PS1_G.wavelenth));
        series.add(Sed.PS1_R.wavelenth, magnitudes.get(Band.r) == 0 ? null : convertMagnitudeToFluxDensity(magnitudes.get(Band.r) + medianDiffMag, Sed.PS1_R.zeropoint, Sed.PS1_R.wavelenth));
        series.add(Sed.PS1_I.wavelenth, magnitudes.get(Band.i) == 0 ? null : convertMagnitudeToFluxDensity(magnitudes.get(Band.i) + medianDiffMag, Sed.PS1_I.zeropoint, Sed.PS1_I.wavelenth));
        series.add(Sed.PS1_Z.wavelenth, magnitudes.get(Band.z) == 0 ? null : convertMagnitudeToFluxDensity(magnitudes.get(Band.z) + medianDiffMag, Sed.PS1_Z.zeropoint, Sed.PS1_Z.wavelenth));
        series.add(Sed.PS1_Y.wavelenth, magnitudes.get(Band.y) == 0 ? null : convertMagnitudeToFluxDensity(magnitudes.get(Band.y) + medianDiffMag, Sed.PS1_Y.zeropoint, Sed.PS1_Y.wavelenth));
        series.add(Sed.MASS_J.wavelenth, magnitudes.get(Band.J) == 0 ? null : convertMagnitudeToFluxDensity(magnitudes.get(Band.J) + medianDiffMag, Sed.MASS_J.zeropoint, Sed.MASS_J.wavelenth));
        series.add(Sed.MASS_H.wavelenth, magnitudes.get(Band.H) == 0 ? null : convertMagnitudeToFluxDensity(magnitudes.get(Band.H) + medianDiffMag, Sed.MASS_H.zeropoint, Sed.MASS_H.wavelenth));
        series.add(Sed.MASS_K.wavelenth, magnitudes.get(Band.K) == 0 ? null : convertMagnitudeToFluxDensity(magnitudes.get(Band.K) + medianDiffMag, Sed.MASS_K.zeropoint, Sed.MASS_K.wavelenth));
        series.add(Sed.WISE_W1.wavelenth, magnitudes.get(Band.W1) == 0 ? null : convertMagnitudeToFluxDensity(magnitudes.get(Band.W1) + medianDiffMag, Sed.WISE_W1.zeropoint, Sed.WISE_W1.wavelenth));
        series.add(Sed.WISE_W2.wavelenth, magnitudes.get(Band.W2) == 0 ? null : convertMagnitudeToFluxDensity(magnitudes.get(Band.W2) + medianDiffMag, Sed.WISE_W2.zeropoint, Sed.WISE_W2.wavelenth));
        series.add(Sed.WISE_W3.wavelenth, magnitudes.get(Band.W3) == 0 ? null : convertMagnitudeToFluxDensity(magnitudes.get(Band.W3) + medianDiffMag, Sed.WISE_W3.zeropoint, Sed.WISE_W3.wavelenth));

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

        LogAxis xAxis = new LogAxis("Wavelength (micron)");
        xAxis.setAutoRangeMinimumSize(0.1);
        xAxis.setTickUnit(new NumberTickUnit(0.2));
        //xAxis.setNumberFormatOverride(new DecimalFormat("#.#"));
        plot.setDomainAxis(xAxis);

        LogAxis yAxis = new LogAxis("λF(λ) (W/m^2)");
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

        // Data point shape of object to estimate
        double size = 6.0;
        double delta = size / 2.0;
        Shape shape = new Ellipse2D.Double(-delta, -delta, size, size);

        //XYSplineRenderer renderer = new XYSplineRenderer(100);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesShape(0, shape);
        renderer.setSeriesPaint(0, Color.BLACK);
        renderer.setSeriesStroke(0, new BasicStroke(2));
        renderer.setSeriesToolTipGenerator(0, addToolTips());
        plot.setRenderer(renderer);

        // Data point shape of templates
        setSeriesShape(chart);

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

    private void setSeriesShape(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        XYItemRenderer renderer = plot.getRenderer();
        double size = 8.0;
        double delta = size / 2.0;
        Shape shape = new Ellipse2D.Double(-delta, -delta, size, size);
        for (int i = 1; i < plot.getSeriesCount(); i++) {
            renderer.setSeriesShape(i, shape);
        }
    }

    private CustomXYToolTipGenerator addToolTips() {
        List<String> toolTips = new ArrayList();
        Band.getSedBands().forEach(band -> {
            toolTips.add(html(sedCatalogs.get(band) + " "
                    + band.val + "=" + roundTo3DecNZ(sedFluxes.get(band).getMagnitude()) + " mag<br>"
                    + "λ=" + sedReferences.get(band).getWavelenth() + " μm<br>"
                    + "F(ν)=" + roundTo3DecSN(sedFluxes.get(band).getFluxJansky()) + " Jy<br>"
                    + "λF(λ)=" + roundTo3DecSN(sedFluxes.get(band).getFluxDensity()) + " W/m^2<br>"
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
