package astro.tool.box.tab;

import astro.tool.box.container.Couple;
import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.main.ModuleHelper.*;
import static astro.tool.box.tab.SettingsTab.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.ExternalResources.*;
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
import astro.tool.box.catalog.UnWiseCatalogEntry;
import astro.tool.box.catalog.VhsCatalogEntry;
import astro.tool.box.enumeration.Epoch;
import astro.tool.box.enumeration.FileType;
import astro.tool.box.main.FlipbookComponent;
import astro.tool.box.service.CatalogQueryService;
import astro.tool.box.util.Counter;
import static astro.tool.box.util.ServiceHelper.establishHttpConnection;
import static astro.tool.box.util.ServiceHelper.readResponse;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import static java.lang.Math.sqrt;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
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

    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JScrollPane scrollPanel;
    private JTabbedPane bottomPanel;
    private JButton searchButton;
    private JTextField coordsField;
    private JTextField fovField;
    private JTable currentTable;

    private Timer infraredTimeSeriesTimer;
    private Timer opticalTimeSeriesTimer;
    private Timer decalsTimeSeriesTimer;
    private Timer wiseTimeSeriesTimer;

    private TwoMassCatalogEntry twoMassEntry;
    private AllWiseCatalogEntry allWiseEntry;
    private SdssCatalogEntry sdssEntry;
    private PanStarrsCatalogEntry panStarrsEntry;
    private GaiaDR3CatalogEntry gaiaDR3Entry;
    private CatWiseCatalogEntry catWiseEntry;
    private NoirlabCatalogEntry noirlabEntry;

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
                    infraredTimeSeriesTimer = null;
                    opticalTimeSeriesTimer = null;
                    decalsTimeSeriesTimer = null;
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
                        imageViewerTab.getEpochs().setSelectedItem(Epoch.YEAR);

                        CompletableFuture.supplyAsync(() -> {
                            try {
                                setWaitCursor();
                                displayDssImages(targetRa, targetDec, fieldOfView);
                                display2MassImages(targetRa, targetDec, fieldOfView);
                                displaySdssImages(targetRa, targetDec, fieldOfView);
                                displaySpitzerImages(targetRa, targetDec, fieldOfView);
                                displayAllwiseImages(targetRa, targetDec, fieldOfView);
                                displayUkidssImages(targetRa, targetDec, fieldOfView);
                                displayVhsImages(targetRa, targetDec, fieldOfView);
                                displayPs1Images(targetRa, targetDec, fieldOfView);
                                displayDecalsImages(targetRa, targetDec, fieldOfView);
                                displayTimeSeries(targetRa, targetDec, fieldOfView);
                                displayDecalsTimeSeries(targetRa, targetDec, fieldOfView);
                                displayWiseTimeSeries();
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
                                if (!resultRows.isEmpty()) {
                                    String[] columns = new String[]{"Proper motion origin (*)", "source 1", "dist. from target (arcsec)", "source 2", "dist. from target (arcsec)", "tpm (mas/yr)", "pmRA (mas/yr)", "pmDE (mas/yr)", "pmRA error", "pmDE error"};
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
                                    messagePanel.add(new JLabel(red("(*) Please check that all the sources listed above correspond to the same object!")));
                                    messagePanel.add(new JLabel("Clicking on any row above will take you to the Image Viewer with the appropriate overlays enabled."));
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
        if (infraredTimeSeriesTimer != null) {
            infraredTimeSeriesTimer.restart();
        }
        if (opticalTimeSeriesTimer != null) {
            opticalTimeSeriesTimer.restart();
        }
        if (decalsTimeSeriesTimer != null) {
            decalsTimeSeriesTimer.restart();
        }
        if (wiseTimeSeriesTimer != null) {
            wiseTimeSeriesTimer.restart();
        }
    }

    private void stopTimers() {
        if (infraredTimeSeriesTimer != null) {
            infraredTimeSeriesTimer.stop();
        }
        if (opticalTimeSeriesTimer != null) {
            opticalTimeSeriesTimer.stop();
        }
        if (decalsTimeSeriesTimer != null) {
            decalsTimeSeriesTimer.stop();
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
        if (entry != null) {
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

    private void displayDssImages(double targetRa, double targetDec, int size) {
        JPanel bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandPanel.setBorder(createEmptyBorder("DSS"));

        BufferedImage image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss1_blue&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "poss1_blue"));
        }
        image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss1_red&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "poss1_red"));
        }
        image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_blue&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "poss2ukstu_blue"));
        }
        image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_red&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "poss2ukstu_red"));
        }
        image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "poss2ukstu_ir"));
        }
        image = retrieveImage(targetRa, targetDec, size, "dss", "file_type=colorimage");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "dss2IR-dss1Red-dss1Blue"));
        }

        if (bandPanel.getComponentCount() > 0) {
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }
    }

    private void display2MassImages(double targetRa, double targetDec, int size) {
        JPanel bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandPanel.setBorder(createEmptyBorder("2MASS"));

        BufferedImage image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=j&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "J"));
        }
        image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=h&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "H"));
        }
        image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=k&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "K"));
        }
        image = retrieveImage(targetRa, targetDec, size, "2mass", "file_type=colorimage");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "K-H-J"));
        }

        if (bandPanel.getComponentCount() > 0) {
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }
    }

    private void displaySdssImages(double targetRa, double targetDec, int size) {
        JPanel bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandPanel.setBorder(createEmptyBorder("SDSS"));

        BufferedImage image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=u&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "u"));
        }
        image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=g&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "g"));
        }
        image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=r&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "r"));
        }
        image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=i&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "i"));
        }
        image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=z&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "z"));
        }
        image = retrieveImage(targetRa, targetDec, size, "sdss", "file_type=colorimage");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "z-g-u"));
        }

        if (bandPanel.getComponentCount() > 0) {
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }
    }

    private void displaySpitzerImages(double targetRa, double targetDec, int size) {
        JPanel bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandPanel.setBorder(createEmptyBorder("Spitzer (SEIP)"));

        BufferedImage image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC1&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "IRAC1"));
        }
        image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC2&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "IRAC2"));
        }
        image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC3&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "IRAC3"));
        }
        image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC4&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "IRAC4"));
        }
        image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:MIPS24&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "MIPS24"));
        }
        image = retrieveImage(targetRa, targetDec, size, "seip", "file_type=colorimage");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "3-color"));
        }

        if (bandPanel.getComponentCount() > 0) {
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }
    }

    private void displayAllwiseImages(double targetRa, double targetDec, int size) {
        JPanel bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandPanel.setBorder(createEmptyBorder("AllWISE"));

        BufferedImage image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=1&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "W1"));
        }
        image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=2&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "W2"));
        }
        image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=3&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "W3"));
        }
        image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=4&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "W4"));
        }
        image = retrieveImage(targetRa, targetDec, size, "wise", "file_type=colorimage");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "W4-W2-W1"));
        }

        if (bandPanel.getComponentCount() > 0) {
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }
    }

    private void displayUkidssImages(double targetRa, double targetDec, int size) throws Exception {
        if (targetDec < -5) {
            return;
        }
        double imageSize = size / 60f;
        SortedMap<String, String> downloadLinks = new TreeMap<>();
        String[] bands = new String[]{"2", "3", "4", "5"};
        for (String band : bands) {
            String imageUrl = String.format("http://wsa.roe.ac.uk:8080/wsa/GetImage?database=UKIDSSDR11PLUS&programmeID=all&ra=%f&dec=%f&sys=J&filterID=%s&xsize=%f&ysize=%f&obsType=object&frameType=stack", targetRa, targetDec, band, imageSize, imageSize);
            String response = readResponse(establishHttpConnection(imageUrl), "UKIDSS");
            try (Scanner scanner = new Scanner(response)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.contains("href")) {
                        String[] parts = line.split("href=\"");
                        parts = parts[1].split("\"");
                        downloadLinks.put(band, parts[0].replace("getImage", "getJImage"));
                        break;
                    }
                }
            }
        }
        if (downloadLinks.isEmpty()) {
            return;
        }

        JPanel bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandPanel.setBorder(createEmptyBorder("UKIDSS DR11 PLUS"));

        for (Entry<String, String> entry : downloadLinks.entrySet()) {
            String band = getBand(entry.getKey());
            String downloadLink = entry.getValue();
            try {
                HttpURLConnection connection = establishHttpConnection(downloadLink);
                BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
                BufferedImage image = ImageIO.read(stream);
                if (image != null) {
                    bandPanel.add(buildImagePanel(flip(image), band));
                }
            } catch (IOException ex) {
            }
        }

        if (bandPanel.getComponentCount() > 0) {
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }
    }

    private void displayVhsImages(double targetRa, double targetDec, int size) throws Exception {
        if (targetDec > 5) {
            return;
        }
        double imageSize = size / 60f;
        SortedMap<String, String> downloadLinks = new TreeMap<>();
        String[] bands = new String[]{"2", "3", "4", "5"};
        for (String band : bands) {
            String imageUrl = String.format("http://horus.roe.ac.uk:8080/vdfs/GetImage?database=VHSDR6&programmeID=110&ra=%f&dec=%f&sys=J&filterID=%s&xsize=%f&ysize=%f&obsType=object&frameType=tilestack", targetRa, targetDec, band, imageSize, imageSize);
            String response = readResponse(establishHttpConnection(imageUrl), "VISTA VHS");
            try (Scanner scanner = new Scanner(response)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.contains("href")) {
                        String[] parts = line.split("href=\"");
                        parts = parts[1].split("\"");
                        downloadLinks.put(band, parts[0].replace("getImage", "getJImage"));
                        break;
                    }
                }
            }
        }
        if (downloadLinks.isEmpty()) {
            return;
        }

        JPanel bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandPanel.setBorder(createEmptyBorder("VISTA VHS DR6"));

        for (Entry<String, String> entry : downloadLinks.entrySet()) {
            String band = getBand(entry.getKey());
            String downloadLink = entry.getValue();
            try {
                HttpURLConnection connection = establishHttpConnection(downloadLink);
                BufferedInputStream stream = new BufferedInputStream(connection.getInputStream());
                BufferedImage image = ImageIO.read(stream);
                if (image != null) {
                    bandPanel.add(buildImagePanel(flip(image), band));
                }
            } catch (IOException ex) {
            }
        }

        if (bandPanel.getComponentCount() > 0) {
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }
    }

    private String getBand(String filterId) {
        switch (filterId) {
            case "2":
                return "Y";
            case "3":
                return "J";
            case "4":
                return "H";
            case "5":
                return "Ks";
            default:
                return "?";
        }
    }

    private BufferedImage flip(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
        tx.translate(0, -image.getHeight(null));
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }

    private void displayPs1Images(double targetRa, double targetDec, int size) throws Exception {
        // Fetch file names for Pan-STARRS filters
        SortedMap<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
        if (imageInfos.isEmpty()) {
            return;
        }

        // Fetch images for Pan-STARRS filters
        JPanel bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandPanel.setBorder(createEmptyBorder("Pan-STARRS"));

        bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("g")), targetRa, targetDec, size), "g"));
        bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("r")), targetRa, targetDec, size), "r"));
        bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("i")), targetRa, targetDec, size), "i"));
        bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("z")), targetRa, targetDec, size), "z"));
        bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s", imageInfos.get("y")), targetRa, targetDec, size), "y"));
        bandPanel.add(buildImagePanel(retrievePs1Image(String.format("red=%s&green=%s&blue=%s", imageInfos.get("y"), imageInfos.get("i"), imageInfos.get("g")), targetRa, targetDec, size), "y-i-g"));

        if (bandPanel.getComponentCount() > 0) {
            bandPanel.add(buildLinkPanel(getPanstarrsUrl(targetRa, targetDec, size, FileType.WARP), "WARP images"));
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }
    }

    private void displayDecalsImages(double targetRa, double targetDec, int size) {
        JPanel bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandPanel.setBorder(createEmptyBorder("DECaLS"));

        BufferedImage image = retrieveDecalsImage(targetRa, targetDec, size, "g");
        if (image != null) {
            image = convertToGray(image);
            bandPanel.add(buildImagePanel(image, "g"));
        }
        image = retrieveDecalsImage(targetRa, targetDec, size, "r");
        if (image != null) {
            image = convertToGray(image);
            bandPanel.add(buildImagePanel(image, "r"));
        }
        image = retrieveDecalsImage(targetRa, targetDec, size, "z");
        if (image != null) {
            image = convertToGray(image);
            bandPanel.add(buildImagePanel(image, "z"));
        }
        image = retrieveDecalsImage(targetRa, targetDec, size, "grz");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "g-r-z"));
        }

        if (bandPanel.getComponentCount() > 0) {
            bandPanel.add(buildLinkPanel(getLegacySingleExposuresUrl(targetRa, targetDec, "ls-dr9"), "Single exposures"));
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }
    }

    private void displayTimeSeries(double targetRa, double targetDec, int size) throws Exception {
        JPanel bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandPanel.setBorder(createEmptyBorder("Cross survey time series", Color.RED));

        List<Couple<String, BufferedImage>> infraredImageList = new ArrayList<>();
        List<Couple<String, BufferedImage>> opticalImageList = new ArrayList<>();

        BufferedImage image = retrieveImage(targetRa, targetDec, size, "dss", "file_type=colorimage");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "DSS"));
            image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir&type=jpgurl");
            if (image != null) {
                infraredImageList.add(new Couple("DSS2 - IR", image));
            }
        }
        image = retrieveImage(targetRa, targetDec, size, "2mass", "file_type=colorimage");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "2MASS"));
            image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=k&type=jpgurl");
            if (image != null) {
                infraredImageList.add(new Couple("2MASS - K", image));
            }
        }
        image = retrieveImage(targetRa, targetDec, size, "sdss", "file_type=colorimage");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "SDSS"));
            opticalImageList.add(new Couple("SDSS", image));
        }
        image = retrieveImage(targetRa, targetDec, size, "seip", "file_type=colorimage");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "Spitzer"));
            image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC4&type=jpgurl");
            if (image != null) {
                infraredImageList.add(new Couple("Spitzer - CH4", image));
            }
        }
        image = retrieveImage(targetRa, targetDec, size, "wise", "file_type=colorimage");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "AllWISE"));
            image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=2&type=jpgurl");
            if (image != null) {
                infraredImageList.add(new Couple("WISE - W2", image));
            }
        }
        SortedMap<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
        if (!imageInfos.isEmpty()) {
            image = retrievePs1Image(String.format("red=%s&green=%s&blue=%s", imageInfos.get("y"), imageInfos.get("i"), imageInfos.get("g")), targetRa, targetDec, size);
            bandPanel.add(buildImagePanel(image, "Pan-STARRS"));
            opticalImageList.add(new Couple("Pan-STARRS", image));
        }
        image = retrieveDecalsImage(targetRa, targetDec, size, "g-r-z");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "DECaLS"));
            opticalImageList.add(new Couple("DECaLS", image));
        }

        infraredTimeSeriesTimer = new Timer(500, null);
        createTimeSeriesTimer(bandPanel, infraredImageList, infraredTimeSeriesTimer);

        opticalTimeSeriesTimer = new Timer(500, null);
        createTimeSeriesTimer(bandPanel, opticalImageList, opticalTimeSeriesTimer);

        JPanel buttonPanel = buildButtonPanel("Save as GIF");
        bandPanel.add(buttonPanel);

        if (infraredImageList.size() > 1) {
            JButton saveInfraredButton = new JButton("Infrared series");
            buttonPanel.add(saveInfraredButton);
            saveInfraredButton.addActionListener((ActionEvent evt) -> {
                try {
                    saveAnimatedGif(infraredImageList, buttonPanel);
                } catch (Exception ex) {
                    showExceptionDialog(baseFrame, ex);
                }
            });
        }

        if (opticalImageList.size() > 1) {
            JButton saveOpticalButton = new JButton("Optical series");
            buttonPanel.add(saveOpticalButton);
            saveOpticalButton.addActionListener((ActionEvent evt) -> {
                try {
                    saveAnimatedGif(opticalImageList, buttonPanel);
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
    }

    private void displayDecalsTimeSeries(double targetRa, double targetDec, int size) {
        JPanel bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandPanel.setBorder(createEmptyBorder("DECaLS time series", Color.RED));

        List<Couple<String, BufferedImage>> imageList = new ArrayList<>();

        BufferedImage image = retrieveDecalsImage(targetRa, targetDec, size, "grz", "decals-dr5");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "DECaLS DR5"));
            imageList.add(new Couple("DECaLS DR5", image));
        }
        image = retrieveDecalsImage(targetRa, targetDec, size, "grz", "decals-dr7");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "DECaLS DR7"));
            imageList.add(new Couple("DECaLS DR7", image));
        }
        image = retrieveDecalsImage(targetRa, targetDec, size, "grz", "ls-dr8");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "LS DR8"));
            imageList.add(new Couple("LS DR8", image));
        }
        image = retrieveDecalsImage(targetRa, targetDec, size, "grz", "ls-dr9");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "LS DR9"));
            imageList.add(new Couple("LS DR9", image));
        }

        if (imageList.size() > 2) {
            decalsTimeSeriesTimer = new Timer(500, null);
            createTimeSeriesTimer(bandPanel, imageList, decalsTimeSeriesTimer);

            JPanel buttonPanel = buildButtonPanel("Save as GIF");
            bandPanel.add(buttonPanel);

            JButton saveButton = new JButton("DECaLS series");
            buttonPanel.add(saveButton);
            saveButton.addActionListener((ActionEvent evt) -> {
                try {
                    saveAnimatedGif(imageList, buttonPanel);
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
    }

    private void displayWiseTimeSeries() throws Exception {
        FlipbookComponent[] flipbook = imageViewerTab.getFlipbook();
        if (flipbook == null) {
            return;
        }

        JPanel bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandPanel.setBorder(createEmptyBorder("WISE time series", Color.RED));

        List<Couple<String, BufferedImage>> imageList = new ArrayList<>();

        for (FlipbookComponent component : flipbook) {
            BufferedImage image = imageViewerTab.processImage(component);
            bandPanel.add(buildImagePanel(image, component.getTitle()));
            imageList.add(new Couple(component.getTitle(), image));
        }

        wiseTimeSeriesTimer = new Timer(500, null);
        createTimeSeriesTimer(bandPanel, imageList, wiseTimeSeriesTimer);

        JPanel buttonPanel = buildButtonPanel("Save as GIF");
        bandPanel.add(buttonPanel);

        JButton saveButton = new JButton("WISE series");
        buttonPanel.add(saveButton);
        saveButton.addActionListener((ActionEvent evt) -> {
            try {
                saveAnimatedGif(imageList, buttonPanel);
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

    private JPanel buildImagePanel(BufferedImage image, String imageHeader) {
        JPanel panel = new JPanel();
        panel.setBorder(createEtchedBorder(imageHeader));
        panel.add(new JLabel(new ImageIcon(drawCenterShape(image))));
        return panel;
    }

    private JPanel buildLinkPanel(String link, String imageHeader) {
        JPanel panel = new JPanel();
        panel.setBorder(createEtchedBorder(imageHeader));
        panel.add(createHyperlink("Display in web browser", link));
        return panel;
    }

    private JPanel buildButtonPanel(String imageHeader) {
        JPanel panel = new JPanel();
        panel.setBorder(createEtchedBorder(imageHeader));
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
