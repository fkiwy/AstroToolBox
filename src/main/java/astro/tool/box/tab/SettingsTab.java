package astro.tool.box.tab;

import static astro.tool.box.main.ToolboxHelper.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.catalog.CatalogEntry;
import astro.tool.box.enumeration.LookAndFeel;
import astro.tool.box.enumeration.TapProvider;
import astro.tool.box.enumeration.WiseBand;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

public class SettingsTab {

    public static final String TAB_NAME = "Settings";
    public static final String COMMENTS = "User settings";
    public static final String PROP_FILE_NAME = "/AstroToolBox.properties";
    public static final String PROP_PATH = USER_HOME + PROP_FILE_NAME;
    public static final Properties USER_SETTINGS = new Properties();
    public static String DEFAULT_TAP_PROVIDER = TapProvider.VIZIER.name();
    public static String DEFAULT_LOOK_AND_FEEL = LookAndFeel.Flat_Light.name();

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final CatalogQueryTab catalogQueryTab;
    private final ImageViewerTab imageViewerTab;
    private final BatchQueryTab batchQueryTab;

    // Global settings
    public static final String LOOK_AND_FEEL = "lookAndFeel";
    public static final String TAP_PROVIDER = "tapProvider";
    public static final String PROXY_ADDRESS = "proxyAddress";
    public static final String PROXY_PORT = "proxyPort";
    public static final String USE_PROXY = "useProxy";
    public static final String USE_SIMBAD_MIRROR = "useSimbadMirror";
    public static final String PHOTOMETRIC_ERRORS = "photometricErrors";
    public static final String CUTOUT_SERVICE = "cutoutService";
    public static final String GAIA_CMD_PATH = "gaiaCmdPath";
    public static final String OBJECT_COLLECTION_PATH = "objectCollectionPath";

    private LookAndFeel lookAndFeel;
    private TapProvider tapProvider;
    private String proxyAddress;
    private int proxyPort;
    private boolean useProxy;
    private boolean useSimbadMirror;
    private boolean photometricErrors;
    private String cutoutService;
    private String gaiaCmdPath;
    private String objectCollectionPath;

    // Catalog search settings
    private static final String COPY_COORDS_TO_CLIPBOARD = "copyCoordsToClipboard";
    private static final String SEARCH_RADIUS = "searchRadius";
    private static final String USER_NAME = "userName";
    private static final String USER_EMAIL = "userEmail";
    public static final String PANSTARRS_FOV = "panstarrsFOV";
    public static final String ALADIN_LITE_FOV = "aladinLiteFOV";
    public static final String WISE_VIEW_FOV = "wiseViewFOV";
    public static final String FINDER_CHART_FOV = "finderChartFOV";

    private boolean copyCoordsToClipboard;
    private int searchRadius;
    private int panstarrsFOV;
    private int aladinLiteFOV;
    private int wiseViewFOV;
    private int finderChartFOV;

    // Image viewer settings
    private static final String WISE_BAND = "wiseBand";
    private static final String SIZE = "imageSize";
    private static final String SPEED = "speed";
    private static final String ZOOM = "zoom";
    private static final String DIFFERENT_SIZE = "differentSize";
    private static final String PROPER_MOTION = "properMotion";
    private static final String ASYNC_DOWNLOADS = "asyncDownloads";
    private static final String LEGACY_IMAGES = "legacyImages";
    private static final String PANSTARRS_IMAGES = "panstarrsImages";
    private static final String UKIDSS_IMAGES = "ukidssImages";
    private static final String VHS_IMAGES = "vhsImages";
    private static final String SDSS_IMAGES = "sdssImages";
    private static final String DSS_IMAGES = "dssImages";

    private WiseBand wiseBand;
    private int size;
    private int speed;
    private int zoom;
    private int differentSize;
    private int properMotion;
    private boolean asyncDownloads;
    private boolean legacyImages;
    private boolean panstarrsImages;
    private boolean ukidssImages;
    private boolean vhsImages;
    private boolean sdssImages;
    private boolean dssImages;

    // Catalogs
    private static final String CATALOGS = "catalogs";
    private List<String> selectedCatalogs;

    private JPanel catalogPanel;
    private ActionListener actionListener;
    private JComboBox wiseBandsBox;

    public SettingsTab(JFrame baseFrame, JTabbedPane tabbedPane, CatalogQueryTab catalogQueryTab, ImageViewerTab imageViewerTab, BatchQueryTab batchQueryTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.catalogQueryTab = catalogQueryTab;
        this.imageViewerTab = imageViewerTab;
        this.batchQueryTab = batchQueryTab;
    }

    public void init() {
        try {
            JPanel settingsPanel = new JPanel(new BorderLayout());

            JPanel containerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            settingsPanel.add(containerPanel, BorderLayout.PAGE_START);

            // Global settings
            JPanel globalSettings = new JPanel(new GridLayout(12, 2));
            globalSettings.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Global Settings", TitledBorder.LEFT, TitledBorder.TOP
            ));
            globalSettings.setPreferredSize(new Dimension(475, 325));
            containerPanel.add(globalSettings);

            lookAndFeel = LookAndFeel.valueOf(USER_SETTINGS.getProperty(LOOK_AND_FEEL, DEFAULT_LOOK_AND_FEEL));
            tapProvider = TapProvider.valueOf(USER_SETTINGS.getProperty(TAP_PROVIDER, DEFAULT_TAP_PROVIDER));
            proxyAddress = USER_SETTINGS.getProperty(PROXY_ADDRESS, "");
            String port = USER_SETTINGS.getProperty(PROXY_PORT, "0");
            proxyPort = port.isEmpty() ? 0 : Integer.parseInt(port);
            useProxy = Boolean.parseBoolean(USER_SETTINGS.getProperty(USE_PROXY, "false"));
            useSimbadMirror = Boolean.parseBoolean(USER_SETTINGS.getProperty(USE_SIMBAD_MIRROR, "false"));
            photometricErrors = Boolean.parseBoolean(USER_SETTINGS.getProperty(PHOTOMETRIC_ERRORS, "false"));
            cutoutService = USER_SETTINGS.getProperty(CUTOUT_SERVICE);
            gaiaCmdPath = USER_SETTINGS.getProperty(GAIA_CMD_PATH, "");
            objectCollectionPath = USER_SETTINGS.getProperty(OBJECT_COLLECTION_PATH, "");

            globalSettings.add(new JLabel("Look & Feel: ", JLabel.RIGHT));

            JComboBox themes = new JComboBox(LookAndFeel.values());
            themes.setSelectedItem(lookAndFeel);
            globalSettings.add(themes);

            globalSettings.add(new JLabel("TAP provider for AllWISE, CatWISE, ", JLabel.RIGHT));
            globalSettings.add(new JLabel());

            globalSettings.add(new JLabel("2MASS, Gaia, DES and VHS: ", JLabel.RIGHT));

            JPanel radioPanel = new JPanel(new GridLayout(1, 2));
            globalSettings.add(radioPanel);

            boolean isVizierTap = tapProvider.equals(TapProvider.VIZIER);
            boolean isNoaoTap = tapProvider.equals(TapProvider.NOIRLAB);

            if (!isVizierTap && !isNoaoTap) {
                isNoaoTap = true;
            }

            JRadioButton vizierButton = new JRadioButton("VizieR", isVizierTap);
            radioPanel.add(vizierButton);

            JRadioButton noirlabButton = new JRadioButton("Other", isNoaoTap);
            radioPanel.add(noirlabButton);

            ButtonGroup buttonGroup = new ButtonGroup();

            buttonGroup.add(vizierButton);
            buttonGroup.add(noirlabButton);

            globalSettings.add(new JLabel("Proxy host name: ", JLabel.RIGHT));
            JTextField proxyAddressField = new JTextField(proxyAddress);
            globalSettings.add(proxyAddressField);

            globalSettings.add(new JLabel("Proxy port: ", JLabel.RIGHT));
            JTextField proxyPortField = new JTextField(String.valueOf(proxyPort));
            globalSettings.add(proxyPortField);

            globalSettings.add(new JLabel("Use proxy: ", JLabel.RIGHT));
            JCheckBox useProxyCheckBox = new JCheckBox();
            useProxyCheckBox.setSelected(useProxy);
            globalSettings.add(useProxyCheckBox);

            globalSettings.add(new JLabel("Use SIMBAD mirror: ", JLabel.RIGHT));
            JCheckBox useSimbadMirrorCheckBox = new JCheckBox();
            useSimbadMirrorCheckBox.setSelected(useSimbadMirror);
            globalSettings.add(useSimbadMirrorCheckBox);

            globalSettings.add(new JLabel("Consider phot. errors in SpT estimates: ", JLabel.RIGHT));
            JCheckBox photometricErrorsBox = new JCheckBox("Needs a restart after: Apply settings");
            photometricErrorsBox.setSelected(photometricErrors);
            globalSettings.add(photometricErrorsBox);

            globalSettings.add(new JLabel("WiseView cutout service URL: ", JLabel.RIGHT));
            JTextField cutoutServiceField = new JTextField(cutoutService);
            globalSettings.add(cutoutServiceField);

            globalSettings.add(new JLabel("File location of Gaia CMD data: ", JLabel.RIGHT));
            JTextField gaiaCmdPathField = new JTextField(gaiaCmdPath);
            globalSettings.add(gaiaCmdPathField);

            globalSettings.add(new JLabel("File location of object collection (*): ", JLabel.RIGHT));
            JTextField collectionPathField = new JTextField(objectCollectionPath);
            globalSettings.add(collectionPathField);

            globalSettings.add(new JLabel("(*) The file will be created by the tool. ", JLabel.RIGHT));
            globalSettings.add(new JLabel("Example: C:/Folder/MyCollection.csv", JLabel.LEFT));

            // Catalog search settings
            JPanel catalogQuerySettings = new JPanel(new GridLayout(12, 2));
            catalogQuerySettings.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Miscellaneous settings", TitledBorder.LEFT, TitledBorder.TOP
            ));
            catalogQuerySettings.setPreferredSize(new Dimension(350, 325));
            containerPanel.add(catalogQuerySettings);

            copyCoordsToClipboard = Boolean.parseBoolean(USER_SETTINGS.getProperty(COPY_COORDS_TO_CLIPBOARD, "true"));
            searchRadius = Integer.parseInt(USER_SETTINGS.getProperty(SEARCH_RADIUS, "10"));
            panstarrsFOV = Integer.parseInt(USER_SETTINGS.getProperty(PANSTARRS_FOV, "30"));
            aladinLiteFOV = Integer.parseInt(USER_SETTINGS.getProperty(ALADIN_LITE_FOV, "300"));
            wiseViewFOV = Integer.parseInt(USER_SETTINGS.getProperty(WISE_VIEW_FOV, "100"));
            finderChartFOV = Integer.parseInt(USER_SETTINGS.getProperty(FINDER_CHART_FOV, "100"));
            String userName = USER_SETTINGS.getProperty(USER_NAME, "");
            String userEmail = USER_SETTINGS.getProperty(USER_EMAIL, "");

            catalogQueryTab.getRadiusField().setText(String.valueOf(searchRadius));
            catalogQueryTab.setCopyCoordsToClipboard(copyCoordsToClipboard);

            imageViewerTab.getPanstarrsField().setText(String.valueOf(panstarrsFOV));
            imageViewerTab.getAladinLiteField().setText(String.valueOf(aladinLiteFOV));
            imageViewerTab.getWiseViewField().setText(String.valueOf(wiseViewFOV));
            imageViewerTab.getFinderChartField().setText(String.valueOf(finderChartFOV));

            catalogQuerySettings.add(new JLabel("Copy coordinates to clipboard: ", JLabel.RIGHT));
            JCheckBox clipboardCheckBox = new JCheckBox();
            clipboardCheckBox.setSelected(copyCoordsToClipboard);
            catalogQuerySettings.add(clipboardCheckBox);

            catalogQuerySettings.add(new JLabel("Catalog search radius: ", JLabel.RIGHT));
            JTextField searchRadiusField = new JTextField(String.valueOf(searchRadius));
            catalogQuerySettings.add(searchRadiusField);

            catalogQuerySettings.add(new JLabel("PanSTARRS FoV: ", JLabel.RIGHT));
            JTextField panstarrsFovField = new JTextField(String.valueOf(panstarrsFOV));
            catalogQuerySettings.add(panstarrsFovField);

            catalogQuerySettings.add(new JLabel("Aladin Lite FoV: ", JLabel.RIGHT));
            JTextField aladinLiteFovField = new JTextField(String.valueOf(aladinLiteFOV));
            catalogQuerySettings.add(aladinLiteFovField);

            catalogQuerySettings.add(new JLabel("WiseView FoV: ", JLabel.RIGHT));
            JTextField wiseViewFovField = new JTextField(String.valueOf(wiseViewFOV));
            catalogQuerySettings.add(wiseViewFovField);

            catalogQuerySettings.add(new JLabel("IRSA Finder Chart FoV: ", JLabel.RIGHT));
            JTextField finderChartFovField = new JTextField(String.valueOf(finderChartFOV));
            catalogQuerySettings.add(finderChartFovField);

            catalogQuerySettings.add(new JLabel("Your name (*): ", JLabel.RIGHT));
            JTextField userNameField = new JTextField(userName);
            catalogQuerySettings.add(userNameField);

            catalogQuerySettings.add(new JLabel("Your email (*): ", JLabel.RIGHT));
            JTextField userEmailField = new JTextField(userEmail);
            catalogQuerySettings.add(userEmailField);

            catalogQuerySettings.add(new JLabel("(*) Name & email only for auto", JLabel.RIGHT));
            catalogQuerySettings.add(new JLabel("matic TYGO form filling", JLabel.LEFT));

            // Image viewer settings
            JPanel imageViewerSettings = new JPanel(new GridLayout(12, 2));
            imageViewerSettings.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), ImageViewerTab.TAB_NAME + " Settings", TitledBorder.LEFT, TitledBorder.TOP
            ));
            imageViewerSettings.setPreferredSize(new Dimension(400, 325));
            containerPanel.add(imageViewerSettings);

            wiseBand = WiseBand.valueOf(USER_SETTINGS.getProperty(WISE_BAND, ImageViewerTab.WISE_BAND.name()));
            size = Integer.parseInt(USER_SETTINGS.getProperty(SIZE, String.valueOf(ImageViewerTab.SIZE)));
            speed = Integer.parseInt(USER_SETTINGS.getProperty(SPEED, String.valueOf(ImageViewerTab.SPEED)));
            zoom = Integer.parseInt(USER_SETTINGS.getProperty(ZOOM, String.valueOf(ImageViewerTab.ZOOM)));
            differentSize = Integer.parseInt(USER_SETTINGS.getProperty(DIFFERENT_SIZE, String.valueOf(ImageViewerTab.DIFFERENT_SIZE)));
            properMotion = Integer.parseInt(USER_SETTINGS.getProperty(PROPER_MOTION, String.valueOf(ImageViewerTab.PROPER_MOTION)));
            asyncDownloads = Boolean.parseBoolean(USER_SETTINGS.getProperty(ASYNC_DOWNLOADS, "true"));
            legacyImages = Boolean.parseBoolean(USER_SETTINGS.getProperty(LEGACY_IMAGES, "true"));
            panstarrsImages = Boolean.parseBoolean(USER_SETTINGS.getProperty(PANSTARRS_IMAGES, "true"));
            ukidssImages = Boolean.parseBoolean(USER_SETTINGS.getProperty(UKIDSS_IMAGES, "true"));
            vhsImages = Boolean.parseBoolean(USER_SETTINGS.getProperty(VHS_IMAGES, "true"));
            sdssImages = Boolean.parseBoolean(USER_SETTINGS.getProperty(SDSS_IMAGES, "true"));
            dssImages = Boolean.parseBoolean(USER_SETTINGS.getProperty(DSS_IMAGES, "true"));

            wiseBandsBox = imageViewerTab.getWiseBands();
            actionListener = wiseBandsBox.getActionListeners()[0];
            wiseBandsBox.removeActionListener(actionListener);
            wiseBandsBox.setSelectedItem(wiseBand);
            wiseBandsBox.addActionListener(actionListener);

            imageViewerTab.getSizeField().setText(String.valueOf(size));
            imageViewerTab.getSpeedSlider().setValue(speed);
            imageViewerTab.getZoomSlider().setValue(zoom);
            imageViewerTab.getDifferentSizeField().setText(String.valueOf(differentSize));
            imageViewerTab.getProperMotionField().setText(String.valueOf(properMotion));
            imageViewerTab.setWiseBand(wiseBand);
            imageViewerTab.setSize(size);
            imageViewerTab.setSpeed(speed);
            imageViewerTab.setZoom(zoom);
            imageViewerTab.setAsyncDownloads(asyncDownloads);
            imageViewerTab.setLegacyImages(legacyImages);
            imageViewerTab.setPanstarrsImages(panstarrsImages);
            imageViewerTab.setUkidssImages(ukidssImages);
            imageViewerTab.setVhsImages(vhsImages);
            imageViewerTab.setSdssImages(sdssImages);
            imageViewerTab.setDssImages(dssImages);

            imageViewerSettings.add(new JLabel("Bands: ", JLabel.RIGHT));
            JComboBox wiseBands = new JComboBox(WiseBand.values());
            wiseBands.setSelectedItem(wiseBand);
            imageViewerSettings.add(wiseBands);

            imageViewerSettings.add(new JLabel("Field of view (arcsec): ", JLabel.RIGHT));
            JTextField sizeField = new JTextField(String.valueOf(size));
            imageViewerSettings.add(sizeField);

            imageViewerSettings.add(new JLabel("Speed (ms): ", JLabel.RIGHT));
            JTextField speedField = new JTextField(String.valueOf(speed));
            imageViewerSettings.add(speedField);

            imageViewerSettings.add(new JLabel("Zoom: ", JLabel.RIGHT));
            JTextField zoomField = new JTextField(String.valueOf(zoom));
            imageViewerSettings.add(zoomField);

            imageViewerSettings.add(new JLabel("Different field of view (arcsec): ", JLabel.RIGHT));
            JTextField differentSizeField = new JTextField(String.valueOf(differentSize));
            imageViewerSettings.add(differentSizeField);

            imageViewerSettings.add(new JLabel("Total proper motion (mas/yr): ", JLabel.RIGHT));
            JTextField properMotionField = new JTextField(String.valueOf(properMotion));
            imageViewerSettings.add(properMotionField);

            imageViewerSettings.add(new JLabel("Async download of WISE images: ", JLabel.RIGHT));
            JCheckBox asynchDownloadsCheckBox = new JCheckBox();
            asynchDownloadsCheckBox.setSelected(asyncDownloads);
            imageViewerSettings.add(asynchDownloadsCheckBox);

            imageViewerSettings.add(new JLabel("Download & show images: ", JLabel.RIGHT));
            JPanel downloadPanel = new JPanel(new GridLayout(1, 2));
            imageViewerSettings.add(downloadPanel);
            JCheckBox legacyImagesCheckBox = new JCheckBox("DECaLS", legacyImages);
            downloadPanel.add(legacyImagesCheckBox);
            JCheckBox panstarrsImagesCheckBox = new JCheckBox("Pan-STARRS", panstarrsImages);
            downloadPanel.add(panstarrsImagesCheckBox);

            imageViewerSettings.add(new JLabel());
            downloadPanel = new JPanel(new GridLayout(1, 2));
            imageViewerSettings.add(downloadPanel);
            JCheckBox ukidssImagesCheckBox = new JCheckBox("UKIDSS", ukidssImages);
            downloadPanel.add(ukidssImagesCheckBox);
            JCheckBox vhsImagesCheckBox = new JCheckBox("VHS", vhsImages);
            downloadPanel.add(vhsImagesCheckBox);

            imageViewerSettings.add(new JLabel());
            downloadPanel = new JPanel(new GridLayout(1, 2));
            imageViewerSettings.add(downloadPanel);
            JCheckBox sdssImagesCheckBox = new JCheckBox("SDSS", sdssImages);
            downloadPanel.add(sdssImagesCheckBox);
            JCheckBox dssImagesCheckBox = new JCheckBox("DSS", dssImages);
            downloadPanel.add(dssImagesCheckBox);

            containerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            settingsPanel.add(containerPanel, BorderLayout.CENTER);

            JPanel gridPanel = new JPanel(new GridLayout(2, 1));
            containerPanel.add(gridPanel);
            gridPanel.setPreferredSize(new Dimension(1235, 160));

            // Catalogs
            catalogPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            catalogPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "Catalog selection", TitledBorder.LEFT, TitledBorder.TOP
            ));
            gridPanel.add(catalogPanel);

            Map<String, CatalogEntry> catalogInstances = getCatalogInstances();
            selectedCatalogs = getSelectedCatalogs(catalogInstances);

            setCheckBoxValue(catalogQueryTab.getTopPanel(), selectedCatalogs);
            setCheckBoxValue(batchQueryTab.getBottomRow(), selectedCatalogs);

            JCheckBox checkbox;
            for (String catalogKey : catalogInstances.keySet()) {
                checkbox = new JCheckBox(catalogKey);
                checkbox.setSelected(selectedCatalogs.contains(catalogKey));
                catalogPanel.add(checkbox);
            }

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            gridPanel.add(buttonPanel);

            JLabel message = createMessageLabel();
            Timer timer = new Timer(3000, (ActionEvent e) -> {
                message.setText("");
            });

            JButton applyButton = new JButton("Apply settings");
            buttonPanel.add(applyButton);
            applyButton.addActionListener((ActionEvent evt) -> {
                try {
                    // Global settings
                    lookAndFeel = (LookAndFeel) themes.getSelectedItem();
                    tapProvider = noirlabButton.isSelected() ? TapProvider.IRSA : TapProvider.VIZIER;
                    proxyAddress = proxyAddressField.getText();
                    String text = proxyPortField.getText();
                    proxyPort = text.isEmpty() ? 0 : Integer.parseInt(text);
                    useProxy = useProxyCheckBox.isSelected();
                    if (useProxy) {
                        List<String> errorMessages = new ArrayList<>();
                        if (proxyAddress.isEmpty()) {
                            errorMessages.add("You must specify a proxy host name.");
                        }
                        if (proxyPort == 0) {
                            errorMessages.add("You must specify a proxy port.");
                        }
                        if (!errorMessages.isEmpty()) {
                            String errorMessage = String.join(LINE_SEP, errorMessages);
                            showErrorDialog(baseFrame, errorMessage);
                            return;
                        }
                    }
                    useSimbadMirror = useSimbadMirrorCheckBox.isSelected();
                    photometricErrors = photometricErrorsBox.isSelected();

                    // Catalog search settings
                    copyCoordsToClipboard = clipboardCheckBox.isSelected();
                    searchRadius = Integer.parseInt(searchRadiusField.getText());
                    panstarrsFOV = Integer.parseInt(panstarrsFovField.getText());
                    aladinLiteFOV = Integer.parseInt(aladinLiteFovField.getText());
                    wiseViewFOV = Integer.parseInt(wiseViewFovField.getText());
                    finderChartFOV = Integer.parseInt(finderChartFovField.getText());

                    // Image viewer settings
                    wiseBand = (WiseBand) wiseBands.getSelectedItem();
                    size = Integer.parseInt(sizeField.getText());
                    speed = Integer.parseInt(speedField.getText());
                    zoom = Integer.parseInt(zoomField.getText());
                    differentSize = Integer.parseInt(differentSizeField.getText());
                    properMotion = Integer.parseInt(properMotionField.getText());
                    asyncDownloads = asynchDownloadsCheckBox.isSelected();
                    legacyImages = legacyImagesCheckBox.isSelected();
                    panstarrsImages = panstarrsImagesCheckBox.isSelected();
                    ukidssImages = ukidssImagesCheckBox.isSelected();
                    vhsImages = vhsImagesCheckBox.isSelected();
                    sdssImages = sdssImagesCheckBox.isSelected();
                    dssImages = dssImagesCheckBox.isSelected();
                } catch (Exception ex) {
                    showErrorDialog(baseFrame, "Invalid input: " + ex.getMessage());
                    return;
                }

                // Global settings
                setLookAndFeel(lookAndFeel);
                SwingUtilities.updateComponentTreeUI(baseFrame);

                USER_SETTINGS.setProperty(LOOK_AND_FEEL, lookAndFeel.name());
                USER_SETTINGS.setProperty(TAP_PROVIDER, tapProvider.name());
                USER_SETTINGS.setProperty(PROXY_ADDRESS, proxyAddressField.getText());
                USER_SETTINGS.setProperty(PROXY_PORT, proxyPortField.getText());
                USER_SETTINGS.setProperty(USE_PROXY, String.valueOf(useProxy));
                USER_SETTINGS.setProperty(USE_SIMBAD_MIRROR, String.valueOf(useSimbadMirror));
                USER_SETTINGS.setProperty(PHOTOMETRIC_ERRORS, String.valueOf(photometricErrors));
                USER_SETTINGS.setProperty(CUTOUT_SERVICE, cutoutServiceField.getText());
                USER_SETTINGS.setProperty(GAIA_CMD_PATH, gaiaCmdPathField.getText());
                USER_SETTINGS.setProperty(OBJECT_COLLECTION_PATH, collectionPathField.getText());

                // Catalog search settings
                catalogQueryTab.getRadiusField().setText(String.valueOf(searchRadius));
                catalogQueryTab.setCopyCoordsToClipboard(copyCoordsToClipboard);

                imageViewerTab.getPanstarrsField().setText(String.valueOf(panstarrsFOV));
                imageViewerTab.getAladinLiteField().setText(String.valueOf(aladinLiteFOV));
                imageViewerTab.getWiseViewField().setText(String.valueOf(wiseViewFOV));
                imageViewerTab.getFinderChartField().setText(String.valueOf(finderChartFOV));
                imageViewerTab.getChangeFovButton().getActionListeners()[0].actionPerformed(null);

                USER_SETTINGS.setProperty(COPY_COORDS_TO_CLIPBOARD, String.valueOf(copyCoordsToClipboard));
                USER_SETTINGS.setProperty(SEARCH_RADIUS, searchRadiusField.getText());
                USER_SETTINGS.setProperty(PANSTARRS_FOV, panstarrsFovField.getText());
                USER_SETTINGS.setProperty(ALADIN_LITE_FOV, aladinLiteFovField.getText());
                USER_SETTINGS.setProperty(WISE_VIEW_FOV, wiseViewFovField.getText());
                USER_SETTINGS.setProperty(FINDER_CHART_FOV, finderChartFovField.getText());
                USER_SETTINGS.setProperty(USER_NAME, userNameField.getText());
                USER_SETTINGS.setProperty(USER_EMAIL, userEmailField.getText());

                // Image viewer settings
                imageViewerTab.initCatalogEntries();
                imageViewerTab.getTimer().stop();

                wiseBandsBox = imageViewerTab.getWiseBands();
                actionListener = wiseBandsBox.getActionListeners()[0];
                wiseBandsBox.removeActionListener(actionListener);
                wiseBandsBox.setSelectedItem(wiseBand);
                wiseBandsBox.addActionListener(actionListener);

                imageViewerTab.getSizeField().setText(String.valueOf(size));
                imageViewerTab.getSpeedSlider().setValue(speed);
                imageViewerTab.getZoomSlider().setValue(zoom);
                imageViewerTab.getDifferentSizeField().setText(String.valueOf(differentSize));
                imageViewerTab.getProperMotionField().setText(String.valueOf(properMotion));
                imageViewerTab.setWiseBand(wiseBand);
                imageViewerTab.setSize(size);
                imageViewerTab.setSpeed(speed);
                imageViewerTab.setZoom(zoom);
                imageViewerTab.setAsyncDownloads(asyncDownloads);
                imageViewerTab.setLegacyImages(legacyImages);
                imageViewerTab.setPanstarrsImages(panstarrsImages);
                imageViewerTab.setUkidssImages(ukidssImages);
                imageViewerTab.setVhsImages(vhsImages);
                imageViewerTab.setSdssImages(sdssImages);
                imageViewerTab.setDssImages(dssImages);

                USER_SETTINGS.setProperty(WISE_BAND, wiseBand.name());
                USER_SETTINGS.setProperty(SIZE, sizeField.getText());
                USER_SETTINGS.setProperty(SPEED, speedField.getText());
                USER_SETTINGS.setProperty(ZOOM, zoomField.getText());
                USER_SETTINGS.setProperty(DIFFERENT_SIZE, differentSizeField.getText());
                USER_SETTINGS.setProperty(PROPER_MOTION, properMotionField.getText());
                USER_SETTINGS.setProperty(ASYNC_DOWNLOADS, String.valueOf(asyncDownloads));
                USER_SETTINGS.setProperty(LEGACY_IMAGES, String.valueOf(legacyImages));
                USER_SETTINGS.setProperty(PANSTARRS_IMAGES, String.valueOf(panstarrsImages));
                USER_SETTINGS.setProperty(UKIDSS_IMAGES, String.valueOf(ukidssImages));
                USER_SETTINGS.setProperty(VHS_IMAGES, String.valueOf(vhsImages));
                USER_SETTINGS.setProperty(SDSS_IMAGES, String.valueOf(sdssImages));
                USER_SETTINGS.setProperty(DSS_IMAGES, String.valueOf(dssImages));

                // Catalogs
                selectedCatalogs = new ArrayList<>();
                for (Component component : catalogPanel.getComponents()) {
                    if (component instanceof JCheckBox) {
                        JCheckBox catalogBox = (JCheckBox) component;
                        if (catalogBox.isSelected()) {
                            selectedCatalogs.add(catalogBox.getText());
                        }
                    }
                }

                setCheckBoxValue(catalogQueryTab.getTopPanel(), selectedCatalogs);
                setCheckBoxValue(batchQueryTab.getBottomRow(), selectedCatalogs);

                String catalogs = selectedCatalogs.stream().collect(Collectors.joining(","));
                USER_SETTINGS.setProperty(CATALOGS, catalogs);

                saveSettings();
                message.setText("Settings applied!");
                timer.restart();
            });

            buttonPanel.add(message);

            tabbedPane.addTab(TAB_NAME, new JScrollPane(settingsPanel));
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void setCheckBoxValue(JPanel panel, List<String> catalogList) {
        for (Component component : panel.getComponents()) {
            if (component instanceof JCheckBox) {
                JCheckBox catalogBox = (JCheckBox) component;
                catalogBox.setSelected(catalogList.contains(catalogBox.getText()));
            }
        }
    }

    public static void setLookAndFeel(LookAndFeel lookAndFeel) {
        boolean isFlatLaf = false;
        try {
            switch (lookAndFeel) {
                case OS:
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    break;
                case Flat_Light:
                    UIManager.setLookAndFeel(new FlatLightLaf());
                    isFlatLaf = true;
                    break;
                case Flat_Dark:
                    UIManager.setLookAndFeel(new FlatDarkLaf());
                    isFlatLaf = true;
                    break;
                case Flat_Darcula:
                    UIManager.setLookAndFeel(new FlatDarculaLaf());
                    isFlatLaf = true;
                    break;
                case Flat_IntelliJ:
                    UIManager.setLookAndFeel(new FlatIntelliJLaf());
                    isFlatLaf = true;
                    break;
            }
            if (isFlatLaf) {
                UIManager.put("Button.arc", 0);
                UIManager.put("Component.arc", 0);
                UIManager.put("CheckBox.arc", 0);
                UIManager.put("ProgressBar.arc", 0);
                UIManager.put("Component.arrowType", "triangle");
                UIManager.put("ScrollBar.showButtons", true);
                UIManager.put("ScrollBar.width", 15);
            }
        } catch (Exception e) {
        }
    }

    public static LookAndFeel getLookAndFeel() {
        return LookAndFeel.valueOf(getUserSetting(LOOK_AND_FEEL, DEFAULT_LOOK_AND_FEEL));
    }

    public static void loadUserSettings() {
        try (InputStream input = new FileInputStream(PROP_PATH)) {
            USER_SETTINGS.load(input);
        } catch (IOException ex) {
        }
    }

    public static void setUserSetting(String key, String value) {
        USER_SETTINGS.setProperty(key, value);
    }

    public static String getUserSetting(String key) {
        return USER_SETTINGS.getProperty(key);
    }

    public static String getUserSetting(String key, String defaultValue) {
        String property = USER_SETTINGS.getProperty(key);
        return property == null || property.isEmpty() ? defaultValue : property;
    }

    public static List<String> getSelectedCatalogs(Map<String, CatalogEntry> catalogInstances) {
        String defaultCatalogs = catalogInstances.keySet().stream().collect(Collectors.joining(","));
        String catalogs = USER_SETTINGS.getProperty(CATALOGS, defaultCatalogs);
        return Arrays.asList(catalogs.split(","));
    }

    public static void saveSettings() {
        try (OutputStream output = new FileOutputStream(PROP_PATH)) {
            USER_SETTINGS.store(output, COMMENTS);
        } catch (IOException ex) {
        }
    }

}
