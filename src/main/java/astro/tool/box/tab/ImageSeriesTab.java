package astro.tool.box.tab;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.main.ToolboxHelper.*;
import static astro.tool.box.tab.SettingsTab.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.ExternalResources.*;
import astro.tool.box.container.Couple;
import astro.tool.box.container.NumberPair;
import astro.tool.box.catalog.AllWiseCatalogEntry;
import astro.tool.box.catalog.CatWiseCatalogEntry;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.catalog.DesCatalogEntry;
import astro.tool.box.catalog.GaiaCatalogEntry;
import astro.tool.box.catalog.GaiaDR3CatalogEntry;
import astro.tool.box.catalog.GaiaWDCatalogEntry;
import astro.tool.box.catalog.NoirlabCatalogEntry;
import astro.tool.box.catalog.PanStarrsCatalogEntry;
import astro.tool.box.catalog.ProperMotionCatalog;
import astro.tool.box.catalog.SdssCatalogEntry;
import astro.tool.box.catalog.SimbadCatalogEntry;
import astro.tool.box.catalog.TessCatalogEntry;
import astro.tool.box.catalog.TwoMassCatalogEntry;
import astro.tool.box.catalog.UkidssCatalogEntry;
import astro.tool.box.catalog.UnWiseCatalogEntry;
import astro.tool.box.catalog.VhsCatalogEntry;
import astro.tool.box.enumeration.FileType;
import astro.tool.box.container.FlipbookComponent;
import astro.tool.box.container.NirImage;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.util.Counter;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import static java.lang.Math.sqrt;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

public class ImageSeriesTab {

    private static final String TAB_NAME = "Image Series";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final ImageViewerTab imageViewerTab;

    private final Map<String, CatalogEntry> catalogInstances;
    private final CatalogQueryService catalogQueryService;

    private List<Couple<String, NirImage>> timeSeries;
    private List<Couple<String, BufferedImage>> desiImages;
    private List<Couple<String, BufferedImage>> wiseImages;

    private Timer timeSeriesTimer;
    private Timer desiTimeSeriesTimer;
    private Timer wiseTimeSeriesTimer;

    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JScrollPane scrollPanel;
    private JTabbedPane bottomPanel;
    private JButton searchButton;
    private JTextField coordsField;
    private JTextField fovField;
    private JTable currentTable;

    private TwoMassCatalogEntry twoMassEntry;
    private AllWiseCatalogEntry allWiseEntry;
    private SdssCatalogEntry sdssEntry;
    private PanStarrsCatalogEntry panStarrsEntry;
    private GaiaDR3CatalogEntry gaiaDR3Entry;
    private CatWiseCatalogEntry catWiseEntry;
    private NoirlabCatalogEntry noirlabEntry;
    private UkidssCatalogEntry ukidssEntry;

    private double targetRa;
    private double targetDec;
    private int fieldOfView;

    private double prevTargetRa;
    private double prevTargetDec;
    private int prevFieldOfView;

    public ImageSeriesTab(JFrame baseFrame, JTabbedPane tabbedPane, ImageViewerTab imageViewerTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.imageViewerTab = imageViewerTab;
        catalogInstances = getCatalogInstances();
        catalogQueryService = new CatalogQueryService();
    }

    public void init() {
        try {
            mainPanel = new JPanel(new BorderLayout());
            tabbedPane.addTab(TAB_NAME, mainPanel);

            topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            mainPanel.add(topPanel, BorderLayout.PAGE_START);

            centerPanel = new JPanel();
            centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
            scrollPanel = new JScrollPane(centerPanel);
            mainPanel.add(scrollPanel, BorderLayout.CENTER);

            bottomPanel = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
            bottomPanel.setPreferredSize(new Dimension(bottomPanel.getWidth(), 200));
            mainPanel.add(bottomPanel, BorderLayout.PAGE_END);

            JLabel coordsLabel = new JLabel("Coordinates:");
            topPanel.add(coordsLabel);

            coordsField = new JTextField(25);
            topPanel.add(coordsField);

            JLabel fovLabel = new JLabel("Field of view (arcsec):");
            topPanel.add(fovLabel);

            fovField = new JTextField(5);
            topPanel.add(fovField);
            fovField.setText("30");

            searchButton = new JButton("Search");
            topPanel.add(searchButton);
            searchButton.addActionListener((ActionEvent e) -> {
                try {
                    String coords = coordsField.getText();
                    if (coords.isEmpty()) {
                        showErrorDialog(baseFrame, "Coordinates must not be empty!");
                        return;
                    }
                    String fov = fovField.getText();
                    if (fov.isEmpty()) {
                        showErrorDialog(baseFrame, "Field of view must not be empty!");
                        return;
                    }
                    List<String> errorMessages = new ArrayList<>();
                    try {
                        NumberPair coordinates = getCoordinates(coords);
                        targetRa = coordinates.getX();
                        targetDec = coordinates.getY();
                        if (targetRa < 0) {
                            errorMessages.add("RA must not be smaller than 0 deg.");
                        }
                        if (targetRa > 360) {
                            errorMessages.add("RA must not be greater than 360 deg.");
                        }
                        if (targetDec < -90) {
                            errorMessages.add("Dec must not be smaller than -90 deg.");
                        }
                        if (targetDec > 90) {
                            errorMessages.add("Dec must not be greater than 90 deg.");
                        }
                    } catch (Exception ex) {
                        targetRa = 0;
                        targetDec = 0;
                        errorMessages.add("Invalid coordinates!");
                    }
                    try {
                        fieldOfView = Integer.valueOf(fov);
                        if (fieldOfView < 10) {
                            errorMessages.add("Field of view must not be smaller than 10 arcsec.");
                        }
                        if (fieldOfView > 300) {
                            errorMessages.add("Field of view must not be larger than 300 arcsec.");
                        }
                    } catch (Exception ex) {
                        fieldOfView = 0;
                        errorMessages.add("Invalid field of view!");
                    }
                    if (targetRa == prevTargetRa && targetDec == prevTargetDec && fieldOfView == prevFieldOfView) {
                        return;
                    }
                    timeSeries = new ArrayList<>();
                    desiImages = new ArrayList<>();
                    wiseImages = new ArrayList<>();
                    timeSeriesTimer = null;
                    desiTimeSeriesTimer = null;
                    wiseTimeSeriesTimer = null;
                    if (centerPanel.getComponentCount() > 0) {
                        centerPanel.removeAll();
                    }
                    if (bottomPanel.getComponentCount() > 0) {
                        bottomPanel.removeAll();
                    }
                    twoMassEntry = null;
                    allWiseEntry = null;
                    sdssEntry = null;
                    panStarrsEntry = null;
                    gaiaDR3Entry = null;
                    catWiseEntry = null;
                    noirlabEntry = null;
                    ukidssEntry = null;
                    prevTargetRa = targetRa;
                    prevTargetDec = targetDec;
                    prevFieldOfView = fieldOfView;
                    stopTimers();
                    if (!errorMessages.isEmpty()) {
                        String message = String.join(LINE_SEP, errorMessages);
                        showErrorDialog(baseFrame, message);
                    } else {
                        imageViewerTab.setWaitCursor(false);
                        JTextField coordinateField = imageViewerTab.getCoordsField();
                        ActionListener actionListener = coordinateField.getActionListeners()[0];
                        coordinateField.removeActionListener(actionListener);
                        coordinateField.setText(roundTo7DecNZ(targetRa) + " " + roundTo7DecNZ(targetDec));
                        coordinateField.addActionListener(actionListener);
                        JTextField sizeField = imageViewerTab.getSizeField();
                        actionListener = sizeField.getActionListeners()[0];
                        sizeField.removeActionListener(actionListener);
                        sizeField.setText(String.valueOf(fieldOfView));
                        sizeField.addActionListener(actionListener);
                        imageViewerTab.getZoomSlider().setValue(250);
                        imageViewerTab.getSkipIntermediateEpochs().setSelected(false);
                        imageViewerTab.createFlipbook();

                        CompletableFuture.supplyAsync(() -> {
                            try {
                                setWaitCursor();
                                displayImages(targetRa, targetDec, fieldOfView);
                                baseFrame.setVisible(true);
                            } catch (Exception ex) {
                                showExceptionDialog(baseFrame, ex);
                            } finally {
                                imageViewerTab.setWaitCursor(true);
                                setDefaultCursor();
                            }
                            return null;
                        });

                        CompletableFuture.supplyAsync(() -> {
                            try {
                                List<String> selectedCatalogs = getSelectedCatalogs(catalogInstances);
                                for (CatalogEntry catalogEntry : catalogInstances.values()) {
                                    if (selectedCatalogs.contains(catalogEntry.getCatalogName())) {
                                        double searchRadius = fieldOfView * sqrt(2) / 2; // diagonal of the fov divided by 2
                                        catalogEntry.setRa(targetRa);
                                        catalogEntry.setDec(targetDec);
                                        catalogEntry.setSearchRadius(searchRadius);
                                        List<CatalogEntry> results;
                                        results = performQuery(catalogEntry);
                                        if (results != null) {
                                            displayCatalogResults(results);
                                        }
                                    }
                                }
                                List<String[]> resultRows = new ArrayList<>();
                                if (twoMassEntry != null && allWiseEntry != null) {
                                    long days = Duration.between(twoMassEntry.getObsDate(), convertMJDToDateTime(new BigDecimal("55400"))).toDays();
                                    NumberPair properMotions = calculateProperMotions(
                                            new NumberPair(twoMassEntry.getRa(), twoMassEntry.getDec()),
                                            new NumberPair(allWiseEntry.getRa_pm(), allWiseEntry.getDec_pm()),
                                            0, (int) days, DEG_MAS);
                                    double pmRa = properMotions.getX();
                                    double pmDec = properMotions.getY();
                                    double tpm = calculateTotalProperMotion(pmRa, pmDec);
                                    resultRows.add(new String[]{
                                        "Calculated from " + TwoMassCatalogEntry.CATALOG_NAME + " and " + AllWiseCatalogEntry.CATALOG_NAME + " coordinates",
                                        twoMassEntry.getSourceId(),
                                        roundTo3DecLZ(twoMassEntry.getTargetDistance()),
                                        allWiseEntry.getSourceId(),
                                        roundTo3DecLZ(allWiseEntry.getTargetDistance()),
                                        roundTo3DecLZ(tpm), roundTo3DecLZ(pmRa), roundTo3DecLZ(pmDec), "N/A", "N/A"
                                    });
                                }
                                if (sdssEntry != null && panStarrsEntry != null) {
                                    long days = Duration.between(sdssEntry.getObsDate(), panStarrsEntry.getObsDate()).toDays();
                                    NumberPair properMotions = calculateProperMotions(
                                            new NumberPair(sdssEntry.getRa(), sdssEntry.getDec()),
                                            new NumberPair(panStarrsEntry.getRa(), panStarrsEntry.getDec()),
                                            0, (int) days, DEG_MAS);
                                    double pmRa = properMotions.getX();
                                    double pmDec = properMotions.getY();
                                    double tpm = calculateTotalProperMotion(pmRa, pmDec);
                                    resultRows.add(new String[]{
                                        "Calculated from " + SdssCatalogEntry.CATALOG_NAME + " and " + PanStarrsCatalogEntry.CATALOG_NAME + " coordinates",
                                        sdssEntry.getSourceId(),
                                        roundTo3DecLZ(sdssEntry.getTargetDistance()),
                                        panStarrsEntry.getSourceId(),
                                        roundTo3DecLZ(panStarrsEntry.getTargetDistance()),
                                        roundTo3DecLZ(tpm), roundTo3DecLZ(pmRa), roundTo3DecLZ(pmDec), "N/A", "N/A"
                                    });
                                }
                                addProperMotionEntry(gaiaDR3Entry, resultRows);
                                addProperMotionEntry(catWiseEntry, resultRows);
                                if (noirlabEntry != null && noirlabEntry.getNdet() >= 5 && noirlabEntry.getDelta_mjd() >= 180) {
                                    addProperMotionEntry(noirlabEntry, resultRows);
                                }
                                addProperMotionEntry(ukidssEntry, resultRows);
                                if (!resultRows.isEmpty()) {
                                    String[] columns = new String[]{"Proper motion origin", "source 1", "dist. from target (arcsec)", "source 2", "dist. from target (arcsec)", "tpm (mas/yr)", "pmRA (mas/yr)", "pmDE (mas/yr)", "pmRA error", "pmDE error"};
                                    Object[][] rows = new Object[][]{};
                                    JTable resultTable = new JTable(resultRows.toArray(rows), columns);
                                    resultTable.setAutoCreateRowSorter(true);
                                    resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                                    resultTable.getSelectionModel().addListSelectionListener((ListSelectionEvent event) -> {
                                        if (!event.getValueIsAdjusting()) {
                                            String label = (String) resultTable.getValueAt(resultTable.getSelectedRow(), 0);
                                            if (label.isEmpty()) {
                                                return;
                                            }
                                            deselectedCatalogOverlay(imageViewerTab);
                                            if (label.contains(TwoMassCatalogEntry.CATALOG_NAME)) {
                                                activateSelectedCatalogOverlay(imageViewerTab, twoMassEntry);
                                                activateSelectedCatalogOverlay(imageViewerTab, allWiseEntry);
                                            } else if (label.contains(SdssCatalogEntry.CATALOG_NAME)) {
                                                activateSelectedCatalogOverlay(imageViewerTab, sdssEntry);
                                                activateSelectedCatalogOverlay(imageViewerTab, panStarrsEntry);
                                            } else if (label.contains(GaiaDR3CatalogEntry.CATALOG_NAME)) {
                                                activateSelectedCatalogOverlay(imageViewerTab, gaiaDR3Entry);
                                            } else if (label.contains(CatWiseCatalogEntry.CATALOG_NAME)) {
                                                activateSelectedCatalogOverlay(imageViewerTab, catWiseEntry);
                                            } else if (label.contains(NoirlabCatalogEntry.CATALOG_NAME)) {
                                                activateSelectedCatalogOverlay(imageViewerTab, noirlabEntry);
                                            } else if (label.contains(UkidssCatalogEntry.CATALOG_NAME)) {
                                                activateSelectedCatalogOverlay(imageViewerTab, ukidssEntry);
                                            }
                                            tabbedPane.setSelectedIndex(3);
                                        }
                                    });
                                    TableColumnModel columnModel = resultTable.getColumnModel();
                                    DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
                                    leftRenderer.setHorizontalAlignment(JLabel.LEFT);
                                    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
                                    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
                                    // Column width
                                    columnModel.getColumn(0).setPreferredWidth(325);
                                    columnModel.getColumn(1).setPreferredWidth(150);
                                    columnModel.getColumn(2).setPreferredWidth(100);
                                    columnModel.getColumn(3).setPreferredWidth(150);
                                    columnModel.getColumn(4).setPreferredWidth(100);
                                    columnModel.getColumn(5).setPreferredWidth(100);
                                    columnModel.getColumn(6).setPreferredWidth(100);
                                    columnModel.getColumn(7).setPreferredWidth(100);
                                    columnModel.getColumn(8).setPreferredWidth(100);
                                    columnModel.getColumn(9).setPreferredWidth(100);
                                    // Column alignment
                                    columnModel.getColumn(0).setCellRenderer(leftRenderer);
                                    columnModel.getColumn(1).setCellRenderer(leftRenderer);
                                    columnModel.getColumn(2).setCellRenderer(rightRenderer);
                                    columnModel.getColumn(3).setCellRenderer(leftRenderer);
                                    columnModel.getColumn(4).setCellRenderer(rightRenderer);
                                    columnModel.getColumn(5).setCellRenderer(rightRenderer);
                                    columnModel.getColumn(6).setCellRenderer(rightRenderer);
                                    columnModel.getColumn(7).setCellRenderer(rightRenderer);
                                    columnModel.getColumn(8).setCellRenderer(rightRenderer);
                                    columnModel.getColumn(9).setCellRenderer(rightRenderer);
                                    JPanel container = new JPanel();
                                    container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
                                    container.add(new JScrollPane(resultTable));
                                    JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                                    messagePanel.add(new JLabel(red("Warning:")));
                                    messagePanel.add(new JLabel("The entries listed above do not necessarily belong to the same object."));
                                    messagePanel.add(new JLabel("Clicking on an entry will take you to the Image Viewer with the appropriate overlays enabled."));
                                    container.add(messagePanel);
                                    bottomPanel.addTab("Proper motions", container);
                                    bottomPanel.setSelectedIndex(bottomPanel.getTabCount() - 1);
                                }
                                baseFrame.setVisible(true);
                            } catch (Exception ex) {
                                showExceptionDialog(baseFrame, ex);
                            }
                            return null;
                        });
                    }
                } catch (Exception ex) {
                    showExceptionDialog(baseFrame, ex);
                }
            });

            coordsField.addActionListener((ActionEvent evt) -> {
                searchButton.getActionListeners()[0].actionPerformed(evt);
            });
            fovField.addActionListener((ActionEvent evt) -> {
                searchButton.getActionListeners()[0].actionPerformed(evt);
            });
            tabbedPane.addChangeListener((ChangeEvent evt) -> {
                JTabbedPane sourceTabbedPane = (JTabbedPane) evt.getSource();
                int index = sourceTabbedPane.getSelectedIndex();
                if (sourceTabbedPane.getTitleAt(index).equals(TAB_NAME)) {
                    restartTimers();
                } else {
                    stopTimers();
                }
            });
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void restartTimers() {
        if (timeSeriesTimer != null) {
            timeSeriesTimer.restart();
        }
        if (desiTimeSeriesTimer != null) {
            desiTimeSeriesTimer.restart();
        }
        if (wiseTimeSeriesTimer != null) {
            wiseTimeSeriesTimer.restart();
        }
    }

    private void stopTimers() {
        if (timeSeriesTimer != null) {
            timeSeriesTimer.stop();
        }
        if (desiTimeSeriesTimer != null) {
            desiTimeSeriesTimer.stop();
        }
        if (wiseTimeSeriesTimer != null) {
            wiseTimeSeriesTimer.stop();
        }
    }

    private void setWaitCursor() {
        baseFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        fovField.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    private void setDefaultCursor() {
        baseFrame.setCursor(Cursor.getDefaultCursor());
        coordsField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        fovField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
    }

    private void addProperMotionEntry(ProperMotionCatalog entry, List<String[]> resultRows) {
        if (entry != null && entry.getTotalProperMotion() > 0) {
            double tpm = entry.getTotalProperMotion();
            double pmRa = entry.getPmra();
            double pmDec = entry.getPmdec();
            double pmRaErr = entry.getPmraErr();
            double pmDecErr = entry.getPmdecErr();
            resultRows.add(new String[]{
                entry.getCatalogName(),
                entry.getSourceId(),
                roundTo3DecLZ(entry.getTargetDistance()),
                "N/A", "N/A",
                roundTo3DecLZ(tpm), roundTo3DecLZ(pmRa), roundTo3DecLZ(pmDec), roundTo3DecLZ(pmRaErr), roundTo3DecLZ(pmDecErr)
            });
        }
    }

    private void displayImages(double targetRa, double targetDec, int size) throws Exception {
        JPanel bandPanel;

        // ______________________________
        //           DSS
        // ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
        int year_1b = getEpoch(targetRa, targetDec, size, "dss", "dss_bands=poss1_blue");
        int year_1r = getEpoch(targetRa, targetDec, size, "dss", "dss_bands=poss1_red");
        int year_2b = getEpoch(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_blue");
        int year_2r = getEpoch(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_red");
        int year_2ir = getEpoch(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir");
        //int year_2ir_1r_1b = getMeanEpoch(year_2ir, year_1r, year_1b);
        int year_2ir_1r_1b = year_2ir;

        bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        BufferedImage image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss1_blue&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("DSS1 B", year_1b)));
        }
        image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss1_red&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("DSS1 R", year_1r)));
        }
        image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_blue&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("DSS2 B", year_2b)));
        }
        image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_red&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("DSS2 R", year_2r)));
        }
        image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("DSS IR", year_2ir)));
        }
        image = retrieveImage(targetRa, targetDec, size, "dss", "file_type=colorimage");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("DSS IR-R-B", year_2ir_1r_1b)));
            timeSeries.add(new Couple(getImageLabel("DSS IR-R-B", year_2ir_1r_1b), new NirImage(year_2ir_1r_1b, image)));
        }

        if (bandPanel.getComponentCount() > 0) {
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }

        // ______________________________
        //           2MASS
        // ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
        int year_j = getEpoch(targetRa, targetDec, size, "2mass", "twomass_bands=j");
        int year_h = getEpoch(targetRa, targetDec, size, "2mass", "twomass_bands=h");
        int year_k = getEpoch(targetRa, targetDec, size, "2mass", "twomass_bands=k");
        int year_k_h_j = getMeanEpoch(year_k, year_h, year_j);

        bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=j&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("2MASS J", year_j)));
        }
        image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=h&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("2MASS H", year_h)));
        }
        image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=k&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("2MASS K", year_k)));
        }
        image = retrieveImage(targetRa, targetDec, size, "2mass", "file_type=colorimage");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("2MASS K-H-J", year_k_h_j)));
            timeSeries.add(new Couple(getImageLabel("2MASS K-H-J", year_k_h_j), new NirImage(year_k_h_j, image)));
        }

        if (bandPanel.getComponentCount() > 0) {
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }

        // ______________________________
        //           SDSS
        // ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
        int year_u = getEpoch(targetRa, targetDec, size, "sdss", "sdss_bands=u");
        int year_g = getEpoch(targetRa, targetDec, size, "sdss", "sdss_bands=g");
        int year_r = getEpoch(targetRa, targetDec, size, "sdss", "sdss_bands=r");
        int year_i = getEpoch(targetRa, targetDec, size, "sdss", "sdss_bands=i");
        int year_z = getEpoch(targetRa, targetDec, size, "sdss", "sdss_bands=z");
        int year_z_g_u = getMeanEpoch(year_z, year_g, year_u);

        bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=u&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("SDSS u", year_u)));
        }
        image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=g&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("SDSS g", year_g)));
        }
        image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=r&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("SDSS r", year_r)));
        }
        image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=i&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("SDSS i", year_i)));
        }
        image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=z&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("SDSS z", year_z)));
        }
        image = retrieveImage(targetRa, targetDec, size, "sdss", "file_type=colorimage");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("SDSS z-g-u", year_z_g_u)));
            timeSeries.add(new Couple(getImageLabel("SDSS z-g-u", year_z_g_u), new NirImage(year_z_g_u, image)));
        }

        if (bandPanel.getComponentCount() > 0) {
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }

        // ______________________________
        //           Spitzer
        // ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
        int year_ch1 = getEpoch(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC1");
        int year_ch2 = getEpoch(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC2");
        int year_ch3 = getEpoch(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC3");
        int year_ch4 = getEpoch(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC4");
        int year_mips24 = getEpoch(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:MIPS24");
        int year_ch3_ch2_ch1 = getMeanEpoch(year_ch3, year_ch2, year_ch1);

        bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC1&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("IRAC1", year_ch1)));
        }
        image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC2&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("IRAC2", year_ch2)));
        }
        image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC3&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("IRAC3", year_ch3)));
        }
        image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC4&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("IRAC4", year_ch4)));
        }
        image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:MIPS24&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("MIPS24", year_mips24)));
        }
        image = retrieveImage(targetRa, targetDec, size, "seip", "file_type=colorimage");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("IRAC3-2-1", year_ch3_ch2_ch1)));
            timeSeries.add(new Couple(getImageLabel("IRAC3-2-1", year_ch3_ch2_ch1), new NirImage(SPITZER_EPOCH, image)));
        }

        if (bandPanel.getComponentCount() > 0) {
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }

        // ______________________________
        //           WISE
        // ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
        int year_w1 = getEpoch(targetRa, targetDec, size, "wise", "wise_bands=1");
        int year_w2 = getEpoch(targetRa, targetDec, size, "wise", "wise_bands=2");
        int year_w3 = getEpoch(targetRa, targetDec, size, "wise", "wise_bands=3");
        int year_w4 = getEpoch(targetRa, targetDec, size, "wise", "wise_bands=4");
        int year_w4_w2_w1 = getMeanEpoch(year_w4, year_w2, year_w1);

        bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=1&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("WISE W1", year_w1)));
        }
        image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=2&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("WISE W2", year_w2)));
        }
        image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=3&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("WISE W3", year_w3)));
        }
        image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=4&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("WISE W4", year_w4)));
        }
        image = retrieveImage(targetRa, targetDec, size, "wise", "file_type=colorimage");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("WISE W4-W2-W1", year_w4_w2_w1)));
            timeSeries.add(new Couple(getImageLabel("WISE W4-W2-W1", year_w4_w2_w1), new NirImage(2010, image)));
        }

        if (bandPanel.getComponentCount() > 0) {
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }

        // ______________________________
        //           UKIDSS
        // ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
        if (targetDec > -5) {
            Map<String, NirImage> images = retrieveNearInfraredImages(targetRa, targetDec, size, UKIDSS_SURVEY_URL, UKIDSS_LABEL);
            if (!images.isEmpty()) {
                bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                for (Entry<String, NirImage> entry : images.entrySet()) {
                    String band = entry.getKey();
                    NirImage nirImage = entry.getValue();
                    image = nirImage.getImage();
                    int year = nirImage.getYear();
                    bandPanel.add(buildImagePanel(image, getImageLabel(UKIDSS_LABEL + " " + band, year)));
                    if (band.equals("K-H-J") || band.equals("K-J")) {
                        timeSeries.add(new Couple(getImageLabel(UKIDSS_LABEL + " " + band, year), new NirImage(year, image)));
                    }
                }
                if (bandPanel.getComponentCount() > 0) {
                    centerPanel.add(bandPanel);
                    baseFrame.setVisible(true);
                    scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
                }
            }
        }

        // ______________________________
        //           VHS
        // ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
        if (targetDec < 5) {
            Map<String, NirImage> images = retrieveNearInfraredImages(targetRa, targetDec, size, VHS_SURVEY_URL, VHS_LABEL);
            if (!images.isEmpty()) {
                bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                for (Entry<String, NirImage> entry : images.entrySet()) {
                    String band = entry.getKey();
                    NirImage nirImage = entry.getValue();
                    image = nirImage.getImage();
                    int year = nirImage.getYear();
                    bandPanel.add(buildImagePanel(image, getImageLabel(VHS_LABEL + " " + band, year)));
                    if (band.equals("K-H-J") || band.equals("K-J")) {
                        timeSeries.add(new Couple(getImageLabel(VHS_LABEL + " " + band, year), new NirImage(year, image)));
                    }
                }
                if (bandPanel.getComponentCount() > 0) {
                    centerPanel.add(bandPanel);
                    baseFrame.setVisible(true);
                    scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
                }
            }
        }

        // ______________________________
        //           Pan-STARRS
        // ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
        Map<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
        if (!imageInfos.isEmpty()) {
            Map<String, Double> years = getPs1Epochs(targetRa, targetDec);
            year_g = years.get("g").intValue();
            year_r = years.get("r").intValue();
            year_i = years.get("i").intValue();
            year_z = years.get("z").intValue();
            int year_y = years.get("y").intValue();
            int year_y_i_g = getMeanEpoch(year_y, year_i, year_g);

            bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("g")), targetRa, targetDec, size, true), getImageLabel("PS1 g", year_g)));
            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("r")), targetRa, targetDec, size, true), getImageLabel("PS1 r", year_r)));
            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("i")), targetRa, targetDec, size, true), getImageLabel("PS1 i", year_i)));
            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("z")), targetRa, targetDec, size, true), getImageLabel("PS1 z", year_z)));
            bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("y")), targetRa, targetDec, size, true), getImageLabel("PS1 y", year_y)));
            bandPanel.add(buildImagePanel(image = retrievePs1Image(String.format("red=%s&green=%s&blue=%s", imageInfos.get("y"), imageInfos.get("i"), imageInfos.get("g")), targetRa, targetDec, size, false), getImageLabel("PS1 y-i-g", year_y_i_g)));
            timeSeries.add(new Couple(getImageLabel("PS1 y-i-g", year_y_i_g), new NirImage(year_y_i_g, image)));

            if (bandPanel.getComponentCount() > 0) {
                bandPanel.add(createHyperlink("WARP images", getPanstarrsUrl(targetRa, targetDec, size, FileType.WARP)));
                centerPanel.add(bandPanel);
                baseFrame.setVisible(true);
                scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
            }
        }

        // ______________________________
        //           DESI LS
        // ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
        bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        image = retrieveDesiImage(targetRa, targetDec, size, "g", true);
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("DESI LS g", DESI_LS_DR_LABEL)));
        }
        image = retrieveDesiImage(targetRa, targetDec, size, "r", true);
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("DESI LS r", DESI_LS_DR_LABEL)));
        }
        image = retrieveDesiImage(targetRa, targetDec, size, "z", true);
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("DESI LS z", DESI_LS_DR_LABEL)));
        }
        image = retrieveDesiImage(targetRa, targetDec, size, "grz", false);
        if (image != null) {
            bandPanel.add(buildImagePanel(image, getImageLabel("DESI LS g-r-z", DESI_LS_DR_LABEL)));
            timeSeries.add(new Couple(getImageLabel("DESI LS g-r-z", DESI_LS_DR_LABEL), new NirImage(DESI_LS_EPOCH, image)));
        }

        if (bandPanel.getComponentCount() > 0) {
            bandPanel.add(createHyperlink("Single exposures", getLegacySingleExposuresUrl(targetRa, targetDec, DESI_LS_DR_PARAM)));
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }

        // ________________________________________
        //           Cross survey time series
        // ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
        bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        timeSeries.sort(Comparator.comparing(c -> c.getB().getYear()));
        List<Couple<String, BufferedImage>> imageList = new ArrayList();
        for (Couple<String, NirImage> couple : timeSeries) {
            bandPanel.add(buildImagePanel(couple.getB().getImage(), couple.getA()));
            if (!couple.getA().contains("WISE")) {
                imageList.add(new Couple(couple.getA(), couple.getB().getImage()));
            }
        }

        timeSeriesTimer = new Timer(500, null);
        createTimeSeriesTimer(bandPanel, imageList, timeSeriesTimer);

        if (timeSeries.size() > 1) {
            JButton saveButton = new JButton("Save as GIF");
            bandPanel.add(saveButton);
            saveButton.addActionListener((ActionEvent evt) -> {
                try {
                    saveAnimatedGif(imageList, saveButton);
                } catch (Exception ex) {
                    showExceptionDialog(baseFrame, ex);
                }
            });
        }

        if (bandPanel.getComponentCount() > 0) {
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }

        // ________________________________________
        //           DESI LS time series
        // ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
        bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        image = retrieveDesiImage(targetRa, targetDec, size, "grz", false, "decals-dr5");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "DESI LS DR5"));
            desiImages.add(new Couple("DESI LS DR5", image));
        }
        image = retrieveDesiImage(targetRa, targetDec, size, "grz", false, "decals-dr7");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "DESI LS DR7"));
            desiImages.add(new Couple("DESI LS DR7", image));
        }
        image = retrieveDesiImage(targetRa, targetDec, size, "grz", false, "ls-dr8");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "DESI LS DR8"));
            desiImages.add(new Couple("LS DR8", image));
        }
        image = retrieveDesiImage(targetRa, targetDec, size, "grz", false, "ls-dr9");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "DESI LS DR9"));
            desiImages.add(new Couple("LS DR9", image));
        }

        if (desiImages.size() > 2) {
            desiTimeSeriesTimer = new Timer(500, null);
            createTimeSeriesTimer(bandPanel, desiImages, desiTimeSeriesTimer);
            JButton saveButton = new JButton("Save as GIF");
            bandPanel.add(saveButton);
            saveButton.addActionListener((ActionEvent evt) -> {
                try {
                    saveAnimatedGif(desiImages, saveButton);
                } catch (Exception ex) {
                    showExceptionDialog(baseFrame, ex);
                }
            });
        }

        if (bandPanel.getComponentCount() > 0) {
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }

        // ________________________________________
        //           WISE time series
        // ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
        List<FlipbookComponent> flipbook = imageViewerTab.getFlipbook();
        if (flipbook != null) {
            bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            for (FlipbookComponent component : flipbook) {
                image = imageViewerTab.processImage(component);
                bandPanel.add(buildImagePanel(image, component.getTitle()));
                wiseImages.add(new Couple(component.getTitle(), image));
            }

            wiseTimeSeriesTimer = new Timer(500, null);
            createTimeSeriesTimer(bandPanel, wiseImages, wiseTimeSeriesTimer);

            JButton saveButton = new JButton("Save as GIF");
            bandPanel.add(saveButton);
            saveButton.addActionListener((ActionEvent evt) -> {
                try {
                    saveAnimatedGif(wiseImages, saveButton);
                } catch (Exception ex) {
                    showExceptionDialog(baseFrame, ex);
                }
            });

            if (bandPanel.getComponentCount() > 0) {
                centerPanel.add(bandPanel);
                baseFrame.setVisible(true);
                scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
            }
        }
    }

    private void createTimeSeriesTimer(JPanel bandPanel, List<Couple<String, BufferedImage>> imageList, Timer timer) {
        int componentCount = imageList.size();
        if (componentCount > 1) {
            JPanel displayPanel = new JPanel();
            bandPanel.add(displayPanel);
            Counter imageCounter = new Counter();
            timer.addActionListener((ActionEvent e) -> {
                if (imageCounter.value() > componentCount - 1) {
                    imageCounter.init();
                }
                displayPanel.removeAll();
                Couple<String, BufferedImage> imageData = imageList.get(imageCounter.value());
                displayPanel.add(buildImagePanel(imageData.getB(), imageData.getA()));
                baseFrame.repaint();
                imageCounter.add();
            });
            timer.start();
        }
    }

    private JPanel buildImagePanel(BufferedImage image, String imageLabel) {
        JLabel label = addTextToImage(image, imageLabel);
        JPanel panel = new JPanel();
        panel.add(label);
        return panel;
    }

    private List<CatalogEntry> performQuery(CatalogEntry catalogQuery) throws IOException {
        List<CatalogEntry> catalogEntries = catalogQueryService.getCatalogEntriesByCoords(catalogQuery);
        catalogEntries.forEach(catalogEntry -> {
            catalogEntry.setTargetRa(catalogQuery.getRa());
            catalogEntry.setTargetDec(catalogQuery.getDec());
            catalogEntry.loadCatalogElements();
        });
        if (!catalogEntries.isEmpty()) {
            catalogEntries.sort(Comparator.comparingDouble(CatalogEntry::getTargetDistance));
            CatalogEntry nearestEntry = catalogEntries.get(0);
            if (nearestEntry.getTargetDistance() < 10) {
                switch (nearestEntry.getCatalogName()) {
                    case TwoMassCatalogEntry.CATALOG_NAME:
                        twoMassEntry = (TwoMassCatalogEntry) nearestEntry;
                        break;
                    case AllWiseCatalogEntry.CATALOG_NAME:
                        allWiseEntry = (AllWiseCatalogEntry) nearestEntry;
                        break;
                    case SdssCatalogEntry.CATALOG_NAME:
                        sdssEntry = (SdssCatalogEntry) nearestEntry;
                        break;
                    case PanStarrsCatalogEntry.CATALOG_NAME:
                        panStarrsEntry = (PanStarrsCatalogEntry) nearestEntry;
                        break;
                    case GaiaDR3CatalogEntry.CATALOG_NAME:
                        gaiaDR3Entry = (GaiaDR3CatalogEntry) nearestEntry;
                        break;
                    case CatWiseCatalogEntry.CATALOG_NAME:
                        catWiseEntry = (CatWiseCatalogEntry) nearestEntry;
                        break;
                    case NoirlabCatalogEntry.CATALOG_NAME:
                        noirlabEntry = (NoirlabCatalogEntry) nearestEntry;
                        break;
                    case UkidssCatalogEntry.CATALOG_NAME:
                        ukidssEntry = (UkidssCatalogEntry) nearestEntry;
                        break;
                }
            }
            return catalogEntries;
        }
        return null;
    }

    private void displayCatalogResults(List<CatalogEntry> catalogEntries) {
        List<Object[]> list = new ArrayList<>();
        catalogEntries.forEach(entry -> {
            list.add(entry.getColumnValues());
        });
        CatalogEntry catalogEntry = catalogEntries.get(0);
        Object[] columns = catalogEntry.getColumnTitles();
        Object[][] rows = new Object[][]{};
        DefaultTableModel defaultTableModel = new DefaultTableModel(list.toArray(rows), columns);
        JTable catalogTable = new JTable(defaultTableModel);
        alignCatalogColumns(catalogTable, catalogEntry);
        catalogTable.setAutoCreateRowSorter(true);
        catalogTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        catalogTable.setRowSorter(createCatalogTableSorter(defaultTableModel, catalogEntry));
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        catalogTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                if (currentTable != null && currentTable != catalogTable) {
                    try {
                        currentTable.clearSelection();
                    } catch (Exception ex) {
                    }
                }
                currentTable = catalogTable;
                String sourceId = (String) catalogTable.getValueAt(catalogTable.getSelectedRow(), 1);
                CatalogEntry selected = catalogEntries.stream().filter(entry -> {
                    return entry.getSourceId().equals(sourceId);
                }).findFirst().get();
                if (selected != null) {
                    activateSelectedCatalogOverlay(imageViewerTab, selected);
                    tabbedPane.setSelectedIndex(3);
                }
            }
        });

        resizeColumnWidth(catalogTable);

        JScrollPane catalogScrollPanel = new JScrollPane(catalogTable);
        catalogScrollPanel.setBorder(BorderFactory.createTitledBorder(
                new LineBorder(catalogEntry.getCatalogColor(), 3), catalogEntry.getCatalogName() + " results", TitledBorder.LEFT, TitledBorder.TOP
        ));
        bottomPanel.addTab(catalogEntry.getCatalogName(), catalogScrollPanel);
    }

    private void activateSelectedCatalogOverlay(ImageViewerTab imageViewerTab, CatalogEntry selected) {
        switch (selected.getCatalogName()) {
            case SimbadCatalogEntry.CATALOG_NAME:
                imageViewerTab.getSimbadOverlay().setSelected(true);
                break;
            case GaiaCatalogEntry.CATALOG_NAME:
                imageViewerTab.getGaiaOverlay().setSelected(true);
                break;
            case GaiaDR3CatalogEntry.CATALOG_NAME:
                imageViewerTab.getGaiaDR3Overlay().setSelected(true);
                break;
            case AllWiseCatalogEntry.CATALOG_NAME:
                imageViewerTab.getAllWiseOverlay().setSelected(true);
                break;
            case CatWiseCatalogEntry.CATALOG_NAME:
                imageViewerTab.getCatWiseOverlay().setSelected(true);
                break;
            case UnWiseCatalogEntry.CATALOG_NAME:
                imageViewerTab.getUnWiseOverlay().setSelected(true);
                break;
            case PanStarrsCatalogEntry.CATALOG_NAME:
                imageViewerTab.getPanStarrsOverlay().setSelected(true);
                break;
            case SdssCatalogEntry.CATALOG_NAME:
                imageViewerTab.getSdssOverlay().setSelected(true);
                break;
            case TwoMassCatalogEntry.CATALOG_NAME:
                imageViewerTab.getTwoMassOverlay().setSelected(true);
                break;
            case VhsCatalogEntry.CATALOG_NAME:
                imageViewerTab.getVhsOverlay().setSelected(true);
                break;
            case GaiaWDCatalogEntry.CATALOG_NAME:
                imageViewerTab.getGaiaWDOverlay().setSelected(true);
                break;
            case NoirlabCatalogEntry.CATALOG_NAME:
                imageViewerTab.getNoirlabOverlay().setSelected(true);
                break;
            case TessCatalogEntry.CATALOG_NAME:
                imageViewerTab.getTessOverlay().setSelected(true);
                break;
            case DesCatalogEntry.CATALOG_NAME:
                imageViewerTab.getDesOverlay().setSelected(true);
                break;
            case UkidssCatalogEntry.CATALOG_NAME:
                imageViewerTab.getUkidssOverlay().setSelected(true);
                break;
        }
    }

    private void deselectedCatalogOverlay(ImageViewerTab imageViewerTab) {
        imageViewerTab.getSimbadOverlay().setSelected(false);
        imageViewerTab.getGaiaOverlay().setSelected(false);
        imageViewerTab.getGaiaDR3Overlay().setSelected(false);
        imageViewerTab.getAllWiseOverlay().setSelected(false);
        imageViewerTab.getCatWiseOverlay().setSelected(false);
        imageViewerTab.getUnWiseOverlay().setSelected(false);
        imageViewerTab.getPanStarrsOverlay().setSelected(false);
        imageViewerTab.getSdssOverlay().setSelected(false);
        imageViewerTab.getTwoMassOverlay().setSelected(false);
        imageViewerTab.getVhsOverlay().setSelected(false);
        imageViewerTab.getGaiaWDOverlay().setSelected(false);
        imageViewerTab.getNoirlabOverlay().setSelected(false);
        imageViewerTab.getTessOverlay().setSelected(false);
        imageViewerTab.getDesOverlay().setSelected(false);
    }

}
