package astro.tool.box.module.tab;

import static astro.tool.box.function.NumericFunctions.roundTo7DecNZ;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.module.tab.SettingsTab.getSelectedCatalogs;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.catalog.AllWiseCatalogEntry;
import astro.tool.box.container.catalog.CatWiseCatalogEntry;
import astro.tool.box.container.catalog.CatalogEntry;
import astro.tool.box.container.catalog.GaiaCatalogEntry;
import astro.tool.box.container.catalog.GaiaDR3CatalogEntry;
import astro.tool.box.container.catalog.GaiaWDCatalogEntry;
import astro.tool.box.container.catalog.NoirlabCatalogEntry;
import astro.tool.box.container.catalog.PanStarrsCatalogEntry;
import astro.tool.box.container.catalog.SdssCatalogEntry;
import astro.tool.box.container.catalog.SimbadCatalogEntry;
import astro.tool.box.container.catalog.TessCatalogEntry;
import astro.tool.box.container.catalog.TwoMassCatalogEntry;
import astro.tool.box.container.catalog.UnWiseCatalogEntry;
import astro.tool.box.container.catalog.VhsCatalogEntry;
import astro.tool.box.enumeration.Epoch;
import astro.tool.box.enumeration.Shape;
import astro.tool.box.facade.CatalogQueryFacade;
import astro.tool.box.module.FlipbookComponent;
import astro.tool.box.module.shape.Circle;
import astro.tool.box.module.shape.Cross;
import astro.tool.box.module.shape.Drawable;
import astro.tool.box.service.CatalogQueryService;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import static java.lang.Math.sqrt;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.CompletableFuture;
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
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;

public class TimeSeriesTab {

    private static final String TAB_NAME = "Time Series";

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final ImageViewerTab imageViewerTab;

    private final Map<String, CatalogEntry> catalogInstances;
    private final CatalogQueryFacade catalogQueryFacade;

    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel centerPanel;
    private JScrollPane scrollPanel;
    private JTabbedPane bottomPanel;
    private JButton searchButton;
    private JTextField coordsField;
    private JTextField fovField;
    private JTable currentTable;

    private double targetRa;
    private double targetDec;
    private int fieldOfView;

    public TimeSeriesTab(JFrame baseFrame, JTabbedPane tabbedPane, ImageViewerTab imageViewerTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.imageViewerTab = imageViewerTab;
        catalogInstances = getCatalogInstances();
        catalogQueryFacade = new CatalogQueryService();
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
                    if (centerPanel.getComponentCount() > 0) {
                        centerPanel.removeAll();
                    }
                    if (bottomPanel.getComponentCount() > 0) {
                        bottomPanel.removeAll();
                    }
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
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
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
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
            baseFrame.setVisible(true);
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
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
            baseFrame.setVisible(true);
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
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }
    }

    private void displayTimeSeries(double targetRa, double targetDec, int size) throws Exception {
        JPanel bandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bandPanel.setBorder(createEmptyBorder("Cross survey time series", Color.RED));

        BufferedImage image = retrieveImage(targetRa, targetDec, size, "dss", "dss_bands=poss2ukstu_ir&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "DSS2 - IR", Shape.CROSS, 2));
        }
        image = retrieveImage(targetRa, targetDec, size, "2mass", "twomass_bands=k&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "2MASS - K", Shape.CROSS, 2));
        }
        image = retrieveImage(targetRa, targetDec, size, "sdss", "sdss_bands=z&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "SDSS - z", Shape.CROSS, 2));
        }
        image = retrieveImage(targetRa, targetDec, size, "seip", "seip_bands=spitzer.seip_science:IRAC4&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "IRAC4", Shape.CROSS, 2));
        }
        image = retrieveImage(targetRa, targetDec, size, "wise", "wise_bands=2&type=jpgurl");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "WISE - W2", Shape.CROSS, 2));
        }
        SortedMap<String, String> imageInfos = getPs1FileNames(targetRa, targetDec);
        if (!imageInfos.isEmpty()) {
            image = retrievePs1Image(String.format("red=%s", imageInfos.get("z")), targetRa, targetDec, size);
            bandPanel.add(buildImagePanel(image, "PS1 - z", Shape.CROSS, 2));
        }
        image = retrieveDecalsImage(targetRa, targetDec, size, "z");
        if (image != null) {
            image = convertToGray(image);
            bandPanel.add(buildImagePanel(image, "DECaLS - z", Shape.CROSS, 2));
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

        BufferedImage image = retrieveDecalsImage(targetRa, targetDec, size, "grz", "decals-dr5");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "DECaLS DR5", Shape.CROSS, 1));
        }
        image = retrieveDecalsImage(targetRa, targetDec, size, "grz", "decals-dr7");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "DECaLS DR7", Shape.CROSS, 1));
        }
        image = retrieveDecalsImage(targetRa, targetDec, size, "grz", "ls-dr8");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "LS DR8", Shape.CROSS, 1));
        }
        image = retrieveDecalsImage(targetRa, targetDec, size, "grz", "ls-dr9");
        if (image != null) {
            bandPanel.add(buildImagePanel(image, "LS DR9", Shape.CROSS, 1));
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

        for (FlipbookComponent component : flipbook) {
            BufferedImage image = imageViewerTab.processImage(component);
            bandPanel.add(buildImagePanel(image, component.getTitle(), Shape.CROSS, 2));
        }

        if (bandPanel.getComponentCount() > 0) {
            centerPanel.add(bandPanel);
            baseFrame.setVisible(true);
            scrollPanel.getVerticalScrollBar().setValue(centerPanel.getHeight());
        }
    }

    private JPanel buildImagePanel(BufferedImage image, String imageHeader) {
        return buildImagePanel(image, imageHeader, Shape.CIRCLE, 2);
    }

    private JPanel buildImagePanel(BufferedImage image, String imageHeader, Shape shape, float strokeWidth) {
        JPanel panel = new JPanel();
        panel.setBorder(createEtchedBorder(imageHeader));
        panel.add(new JLabel(new ImageIcon(drawCenterShape(image, shape, strokeWidth))));
        return panel;
    }

    private BufferedImage drawCenterShape(BufferedImage image, Shape shape, float strokeWidth) {
        image = zoom(image, 200);
        double x = image.getWidth() / 2;
        double y = image.getHeight() / 2;
        Graphics g = image.getGraphics();
        Drawable drawable;
        switch (shape) {
            case CROSS:
                drawable = new Cross(x, y, 50, Color.MAGENTA, strokeWidth);
                break;
            default:
                drawable = new Circle(x, y, 10, Color.MAGENTA, strokeWidth);
                break;
        }
        drawable.draw(g);
        return image;
    }

    private List<CatalogEntry> performQuery(CatalogEntry catalogQuery) throws IOException {
        List<CatalogEntry> catalogEntries = catalogQueryFacade.getCatalogEntriesByCoords(catalogQuery);
        catalogEntries.forEach(catalogEntry -> {
            catalogEntry.setTargetRa(catalogQuery.getRa());
            catalogEntry.setTargetDec(catalogQuery.getDec());
            catalogEntry.loadCatalogElements();
        });
        if (!catalogEntries.isEmpty()) {
            catalogEntries.sort(Comparator.comparingDouble(CatalogEntry::getTargetDistance));
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
        JTable catalogTable = new JTable(defaultTableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
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

        }

    }

}