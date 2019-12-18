package astro.tool.box.module.tab;

import static astro.tool.box.module.ModuleHelper.*;
import astro.tool.box.enumeration.Epoch;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.LookAndFeel;
import astro.tool.box.enumeration.WiseBand;
import static astro.tool.box.util.Constants.LINE_SEP;
import java.awt.BorderLayout;
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
import java.util.List;
import java.util.Properties;
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
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;

public class SettingsTab {

    public static final String TAB_NAME = "Settings";
    private static final String PROP_FILE_NAME = "/AstroToolBox.properties";
    private static final String PROP_PATH = USER_HOME + PROP_FILE_NAME;
    private static final Properties USER_SETTINGS = new Properties();

    private final JFrame baseFrame;
    private final JTabbedPane tabbedPane;
    private final CatalogQueryTab catalogQueryTab;
    private final ImageViewerTab imageViewerTab;

    // General settings
    private static final String LOOK_AND_FEEL = "lookAndFeel";
    public static final String PROXY_ADDRESS = "proxyAddress";
    public static final String PROXY_PORT = "proxyPort";
    public static final String USE_PROXY = "useProxy";

    private LookAndFeel lookAndFeel;
    private String proxyAddress;
    private int proxyPort;
    private boolean useProxy;

    // Catalog search settings
    private static final String COPY_COORDS_TO_CLIPBOARD = "copyCoordsToClipboard";
    private static final String SEARCH_RADIUS = "searchRadius";
    private static final String PANSTARRS_FOV = "panstarrsFOV";
    private static final String ALADIN_LITE_FOV = "aladinLiteFOV";
    private static final String WISE_VIEW_FOV = "wiseViewFOV";
    private static final String FINDER_CHART_FOV = "finderChartFOV";

    private boolean copyCoordsToClipboard;
    private int searchRadius;
    private int panstarrsFOV;
    private int aladinLiteFOV;
    private int wiseViewFOV;
    private int finderChartFOV;

    // Image viewer settings
    private static final String WISE_BAND = "wiseBand";
    private static final String EPOCH = "epoch";
    private static final String SIZE = "imageSize";
    private static final String SPEED = "speed";
    private static final String ZOOM = "zoom";

    private WiseBand wiseBand;
    private Epoch epoch;
    private int size;
    private int speed;
    private int zoom;

    private ActionListener listener;
    private JComboBox wiseBandsBox;
    private JComboBox epochsBox;

    public SettingsTab(JFrame baseFrame, JTabbedPane tabbedPane, CatalogQueryTab catalogQueryTab, ImageViewerTab imageViewerTab) {
        this.baseFrame = baseFrame;
        this.tabbedPane = tabbedPane;
        this.catalogQueryTab = catalogQueryTab;
        this.imageViewerTab = imageViewerTab;
        try (InputStream input = new FileInputStream(PROP_PATH)) {
            USER_SETTINGS.load(input);
        } catch (IOException ex) {
        }
    }

    public void init() {
        try {
            JPanel settingsPanel = new JPanel(new BorderLayout());

            JPanel containerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            settingsPanel.add(containerPanel, BorderLayout.PAGE_START);

            // General settings
            JPanel generalSettings = new JPanel(new GridLayout(6, 2));
            generalSettings.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), "General Settings", TitledBorder.LEFT, TitledBorder.TOP
            ));
            generalSettings.setPreferredSize(new Dimension(350, 175));
            containerPanel.add(generalSettings);

            lookAndFeel = LookAndFeel.valueOf(USER_SETTINGS.getProperty(LOOK_AND_FEEL, "OS"));
            proxyAddress = USER_SETTINGS.getProperty(PROXY_ADDRESS, "");
            String port = USER_SETTINGS.getProperty(PROXY_PORT, "0");
            proxyPort = port.isEmpty() ? 0 : Integer.parseInt(port);
            useProxy = Boolean.parseBoolean(USER_SETTINGS.getProperty(USE_PROXY, "false"));

            setLookAndFeel(lookAndFeel);

            generalSettings.add(createLabel("Look & Feel:", PLAIN_FONT, JLabel.RIGHT));

            JPanel radioPanel = new JPanel(new GridLayout(1, 2));
            generalSettings.add(radioPanel);

            JRadioButton javaRadioButton = new JRadioButton("Java", lookAndFeel.equals(LookAndFeel.Java));
            radioPanel.add(javaRadioButton);

            JRadioButton osRadioButton = new JRadioButton("OS", lookAndFeel.equals(LookAndFeel.OS));
            radioPanel.add(osRadioButton);

            ButtonGroup radioGroup = new ButtonGroup();
            radioGroup.add(javaRadioButton);
            radioGroup.add(osRadioButton);

            generalSettings.add(createLabel("Proxy host name: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField proxyAddressField = createField(proxyAddress, PLAIN_FONT);
            generalSettings.add(proxyAddressField);

            generalSettings.add(createLabel("Proxy port: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField proxyPortField = createField(proxyPort, PLAIN_FONT);
            generalSettings.add(proxyPortField);

            generalSettings.add(createLabel("Use proxy : ", PLAIN_FONT, JLabel.RIGHT));
            JCheckBox useProxyCheckBox = new JCheckBox();
            useProxyCheckBox.setSelected(useProxy);
            generalSettings.add(useProxyCheckBox);

            for (int i = 0; i < 4; i++) {
                generalSettings.add(new JLabel());
            }

            // Catalog search settings
            JPanel catalogQuerySettings = new JPanel(new GridLayout(6, 2));
            catalogQuerySettings.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), CatalogQueryTab.TAB_NAME + " Settings", TitledBorder.LEFT, TitledBorder.TOP
            ));
            catalogQuerySettings.setPreferredSize(new Dimension(350, 175));
            containerPanel.add(catalogQuerySettings);

            copyCoordsToClipboard = Boolean.parseBoolean(USER_SETTINGS.getProperty(COPY_COORDS_TO_CLIPBOARD, "true"));
            searchRadius = Integer.parseInt(USER_SETTINGS.getProperty(SEARCH_RADIUS, "10"));
            panstarrsFOV = Integer.parseInt(USER_SETTINGS.getProperty(PANSTARRS_FOV, "30"));
            aladinLiteFOV = Integer.parseInt(USER_SETTINGS.getProperty(ALADIN_LITE_FOV, "240"));
            wiseViewFOV = Integer.parseInt(USER_SETTINGS.getProperty(WISE_VIEW_FOV, "120"));
            finderChartFOV = Integer.parseInt(USER_SETTINGS.getProperty(FINDER_CHART_FOV, "240"));

            catalogQueryTab.getRadiusField().setText(String.valueOf(searchRadius));
            if (catalogQueryTab.getPanstarrsField() != null) {
                catalogQueryTab.getPanstarrsField().setText(String.valueOf(panstarrsFOV));
                catalogQueryTab.getAladinLiteField().setText(String.valueOf(aladinLiteFOV));
                catalogQueryTab.getWiseViewField().setText(String.valueOf(wiseViewFOV));
                catalogQueryTab.getFinderChartField().setText(String.valueOf(finderChartFOV));
            }

            catalogQueryTab.setCopyCoordsToClipboard(copyCoordsToClipboard);
            catalogQueryTab.setPanstarrsFOV(panstarrsFOV);
            catalogQueryTab.setAladinLiteFOV(aladinLiteFOV);
            catalogQueryTab.setWiseViewFOV(wiseViewFOV);
            catalogQueryTab.setFinderChartFOV(finderChartFOV);

            catalogQuerySettings.add(createLabel("Copy coordinates to clipboard: ", PLAIN_FONT, JLabel.RIGHT));
            JCheckBox clipboardCheckBox = new JCheckBox();
            clipboardCheckBox.setSelected(copyCoordsToClipboard);
            catalogQuerySettings.add(clipboardCheckBox);

            catalogQuerySettings.add(createLabel("Search radius: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField searchRadiusField = createField(searchRadius, PLAIN_FONT);
            catalogQuerySettings.add(searchRadiusField);

            catalogQuerySettings.add(createLabel("PanSTARRS FoV: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField panstarrsFovField = createField(panstarrsFOV, PLAIN_FONT);
            catalogQuerySettings.add(panstarrsFovField);

            catalogQuerySettings.add(createLabel("Aladin Lite FoV: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField aladinLiteFovField = createField(aladinLiteFOV, PLAIN_FONT);
            catalogQuerySettings.add(aladinLiteFovField);

            catalogQuerySettings.add(createLabel("WiseView FoV: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField wiseViewFovField = createField(wiseViewFOV, PLAIN_FONT);
            catalogQuerySettings.add(wiseViewFovField);

            catalogQuerySettings.add(createLabel("IRSA Finder Chart FoV: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField finderChartFovField = createField(finderChartFOV, PLAIN_FONT);
            catalogQuerySettings.add(finderChartFovField);

            // Image viewer settings
            JPanel imageViewerSettings = new JPanel(new GridLayout(6, 2));
            imageViewerSettings.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), ImageViewerTab.TAB_NAME + " Settings", TitledBorder.LEFT, TitledBorder.TOP
            ));
            imageViewerSettings.setPreferredSize(new Dimension(350, 175));
            containerPanel.add(imageViewerSettings);

            wiseBand = WiseBand.valueOf(USER_SETTINGS.getProperty(WISE_BAND, ImageViewerTab.WISE_BAND.name()));
            epoch = Epoch.valueOf(USER_SETTINGS.getProperty(EPOCH, ImageViewerTab.EPOCH.name()));
            size = Integer.parseInt(USER_SETTINGS.getProperty(SIZE, String.valueOf(ImageViewerTab.SIZE)));
            speed = Integer.parseInt(USER_SETTINGS.getProperty(SPEED, String.valueOf(ImageViewerTab.SPEED)));
            zoom = Integer.parseInt(USER_SETTINGS.getProperty(ZOOM, String.valueOf(ImageViewerTab.ZOOM)));

            wiseBandsBox = imageViewerTab.getWiseBands();
            listener = wiseBandsBox.getActionListeners()[0];
            wiseBandsBox.removeActionListener(listener);
            wiseBandsBox.setSelectedItem(wiseBand);
            wiseBandsBox.addActionListener(listener);

            epochsBox = imageViewerTab.getWiseBands();
            listener = epochsBox.getActionListeners()[0];
            epochsBox.removeActionListener(listener);
            epochsBox.setSelectedItem(epoch);
            epochsBox.addActionListener(listener);

            imageViewerTab.getSizeField().setText(String.valueOf(size));
            imageViewerTab.getSpeedSlider().setValue(speed);
            imageViewerTab.getZoomSlider().setValue(zoom);

            imageViewerTab.setWiseBand(wiseBand);
            imageViewerTab.setEpoch(epoch);
            imageViewerTab.setSize(size);
            imageViewerTab.setSpeed(speed);
            imageViewerTab.setZoom(zoom);

            imageViewerSettings.add(createLabel("Bands: ", PLAIN_FONT, JLabel.RIGHT));
            JComboBox wiseBands = new JComboBox<>(new WiseBand[]{
                WiseBand.W1,
                WiseBand.W2,
                WiseBand.W1W2
            });
            wiseBands.setSelectedItem(wiseBand);
            imageViewerSettings.add(wiseBands);

            imageViewerSettings.add(createLabel("Epochs: ", PLAIN_FONT, JLabel.RIGHT));
            JComboBox epochs = new JComboBox<>(new Epoch[]{
                Epoch.ALL,
                Epoch.ALL_ASCENDING,
                Epoch.ALL_DESCENDING,
                Epoch.FIRST_REMAINING,
                Epoch.FIRST_REMAINING_PARALLAX,
                Epoch.FIRST_REMAINING_SUBTRACTED,
                Epoch.FIRST_LAST,
                Epoch.FIRST_LAST_PARALLAX,
                Epoch.FIRST_LAST_SUBTRACTED
            });
            epochs.setSelectedItem(epoch);
            imageViewerSettings.add(epochs);

            imageViewerSettings.add(createLabel("Field of view (arcsec): ", PLAIN_FONT, JLabel.RIGHT));
            JTextField sizeField = createField(String.valueOf(size), PLAIN_FONT);
            imageViewerSettings.add(sizeField);

            imageViewerSettings.add(createLabel("Speed (ms): ", PLAIN_FONT, JLabel.RIGHT));
            JTextField speedField = createField(String.valueOf(speed), PLAIN_FONT);
            imageViewerSettings.add(speedField);

            imageViewerSettings.add(createLabel("Zoom: ", PLAIN_FONT, JLabel.RIGHT));
            JTextField zoomField = createField(String.valueOf(zoom), PLAIN_FONT);
            imageViewerSettings.add(zoomField);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            settingsPanel.add(buttonPanel, BorderLayout.CENTER);

            JLabel message = createLabel("", PLAIN_FONT, JColor.DARKER_GREEN.val);
            Timer timer = new Timer(3000, (ActionEvent e) -> {
                message.setText("");
            });

            JButton applyButton = new JButton("Apply settings");
            buttonPanel.add(applyButton);
            applyButton.addActionListener((ActionEvent evt) -> {
                try {
                    // General settings
                    lookAndFeel = javaRadioButton.isSelected() ? LookAndFeel.Java : LookAndFeel.OS;
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

                    // Catalog search settings
                    copyCoordsToClipboard = clipboardCheckBox.isSelected();
                    searchRadius = Integer.parseInt(searchRadiusField.getText());
                    panstarrsFOV = Integer.parseInt(panstarrsFovField.getText());
                    aladinLiteFOV = Integer.parseInt(aladinLiteFovField.getText());
                    wiseViewFOV = Integer.parseInt(wiseViewFovField.getText());
                    finderChartFOV = Integer.parseInt(finderChartFovField.getText());

                    // Image viewer settings
                    wiseBand = (WiseBand) wiseBands.getSelectedItem();
                    epoch = (Epoch) epochs.getSelectedItem();
                    size = Integer.parseInt(sizeField.getText());
                    speed = Integer.parseInt(speedField.getText());
                    zoom = Integer.parseInt(zoomField.getText());
                } catch (Exception ex) {
                    showErrorDialog(baseFrame, "Invalid input: " + ex.getMessage());
                    return;
                }

                // General settings
                setLookAndFeel(lookAndFeel);

                USER_SETTINGS.setProperty(LOOK_AND_FEEL, lookAndFeel.name());
                USER_SETTINGS.setProperty(PROXY_ADDRESS, proxyAddressField.getText());
                USER_SETTINGS.setProperty(PROXY_PORT, proxyPortField.getText());
                USER_SETTINGS.setProperty(USE_PROXY, String.valueOf(useProxy));

                // Catalog search settings
                catalogQueryTab.getRadiusField().setText(String.valueOf(searchRadius));
                if (catalogQueryTab.getPanstarrsField() != null) {
                    catalogQueryTab.getPanstarrsField().setText(String.valueOf(panstarrsFOV));
                    catalogQueryTab.getAladinLiteField().setText(String.valueOf(aladinLiteFOV));
                    catalogQueryTab.getWiseViewField().setText(String.valueOf(wiseViewFOV));
                    catalogQueryTab.getFinderChartField().setText(String.valueOf(finderChartFOV));
                }

                catalogQueryTab.setCopyCoordsToClipboard(copyCoordsToClipboard);
                catalogQueryTab.setPanstarrsFOV(panstarrsFOV);
                catalogQueryTab.setAladinLiteFOV(aladinLiteFOV);
                catalogQueryTab.setWiseViewFOV(wiseViewFOV);
                catalogQueryTab.setFinderChartFOV(finderChartFOV);

                USER_SETTINGS.setProperty(COPY_COORDS_TO_CLIPBOARD, String.valueOf(copyCoordsToClipboard));
                USER_SETTINGS.setProperty(SEARCH_RADIUS, searchRadiusField.getText());
                USER_SETTINGS.setProperty(PANSTARRS_FOV, panstarrsFovField.getText());
                USER_SETTINGS.setProperty(ALADIN_LITE_FOV, aladinLiteFovField.getText());
                USER_SETTINGS.setProperty(WISE_VIEW_FOV, wiseViewFovField.getText());
                USER_SETTINGS.setProperty(FINDER_CHART_FOV, finderChartFovField.getText());

                // Image viewer settings
                imageViewerTab.getTimer().stop();

                wiseBandsBox = imageViewerTab.getWiseBands();
                listener = wiseBandsBox.getActionListeners()[0];
                wiseBandsBox.removeActionListener(listener);
                wiseBandsBox.setSelectedItem(wiseBand);
                wiseBandsBox.addActionListener(listener);

                epochsBox = imageViewerTab.getEpochs();
                listener = epochsBox.getActionListeners()[0];
                epochsBox.removeActionListener(listener);
                epochsBox.setSelectedItem(epoch);
                epochsBox.addActionListener(listener);

                imageViewerTab.getSizeField().setText(String.valueOf(size));
                imageViewerTab.getSpeedSlider().setValue(speed);
                imageViewerTab.getZoomSlider().setValue(zoom);

                imageViewerTab.setWiseBand(wiseBand);
                imageViewerTab.setEpoch(epoch);
                imageViewerTab.setSize(size);
                imageViewerTab.setSpeed(speed);
                imageViewerTab.setZoom(zoom);

                USER_SETTINGS.setProperty(WISE_BAND, wiseBand.name());
                USER_SETTINGS.setProperty(EPOCH, epoch.name());
                USER_SETTINGS.setProperty(SIZE, sizeField.getText());
                USER_SETTINGS.setProperty(SPEED, speedField.getText());
                USER_SETTINGS.setProperty(ZOOM, zoomField.getText());

                try (OutputStream output = new FileOutputStream(PROP_PATH)) {
                    USER_SETTINGS.store(output, "User settings");
                    message.setText("Settings have been applied!");
                } catch (IOException ex) {
                }

                timer.restart();
            });

            buttonPanel.add(message);

            tabbedPane.addTab(TAB_NAME, new JScrollPane(settingsPanel));
        } catch (Exception ex) {
            showExceptionDialog(baseFrame, ex);
        }
    }

    private void setLookAndFeel(LookAndFeel lookAndFeel) {
        try {
            if (lookAndFeel.equals(LookAndFeel.Java)) {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            SwingUtilities.updateComponentTreeUI(baseFrame);
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
        }
    }

    public static String getUserSetting(String key) {
        return USER_SETTINGS.getProperty(key);
    }

}
