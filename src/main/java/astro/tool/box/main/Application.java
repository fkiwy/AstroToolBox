package astro.tool.box.main;

import astro.tool.box.container.NumberTriplet;
import astro.tool.box.container.Version;
import static astro.tool.box.main.ToolboxHelper.*;
import static astro.tool.box.tab.SettingsTab.*;
import static astro.tool.box.util.ServiceHelper.*;
import astro.tool.box.tab.AdqlQueryTab;
import astro.tool.box.tab.BatchQueryTab;
import astro.tool.box.tab.BrownDwarfTab;
import astro.tool.box.tab.CatalogQueryTab;
import astro.tool.box.tab.CustomOverlaysTab;
import astro.tool.box.tab.FileBrowserTab;
import astro.tool.box.tab.ImageViewerTab;
import astro.tool.box.tab.LookupTab;
import astro.tool.box.tab.ObjectCollectionTab;
import astro.tool.box.tab.PhotometricClassifierTab;
import astro.tool.box.tab.SettingsTab;
import astro.tool.box.tab.ImageSeriesTab;
import astro.tool.box.tab.ToolTab;
import astro.tool.box.tab.VizierCatalogsTab;
import astro.tool.box.tab.WhiteDwarfTab;
import astro.tool.box.util.CSVParser;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.time.LocalDate;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.List;
import java.util.Scanner;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.ToolTipManager;

public class Application {

    private int defaultCloseOperation;

    private JFrame baseFrame;

    private JTabbedPane tabbedPane;

    private CatalogQueryTab catalogQueryTab;

    private ImageViewerTab imageViewerTab;

    private static boolean versionLoaded;

    public static List<NumberTriplet> CMD_DATA;

    public Application() {
        try {
            loadUserSettings();
            setLookAndFeel(getLookAndFeel());
            ToolTipManager manager = ToolTipManager.sharedInstance();
            manager.setInitialDelay(100);
            manager.setDismissDelay(60000);
        } catch (Exception e) {
        }
    }

    public void init() {
        baseFrame = new JFrame();
        baseFrame.setIconImage(getToolBoxImage());
        baseFrame.setTitle(PGM_NAME + " " + PGM_VERSION);
        baseFrame.setSize(new Dimension(BASE_FRAME_WIDTH, BASE_FRAME_HEIGHT));
        baseFrame.setDefaultCloseOperation(defaultCloseOperation);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        baseFrame.add(tabbedPane);

        catalogQueryTab = new CatalogQueryTab(baseFrame, tabbedPane);
        catalogQueryTab.init();

        BrownDwarfTab brownDwarfTab = new BrownDwarfTab(baseFrame, tabbedPane, catalogQueryTab);
        brownDwarfTab.init();

        WhiteDwarfTab whiteDwarfTab = new WhiteDwarfTab(baseFrame, tabbedPane, catalogQueryTab);
        whiteDwarfTab.init();

        imageViewerTab = new ImageViewerTab(baseFrame, tabbedPane);
        imageViewerTab.init();

        ImageSeriesTab imageSeriesTab = new ImageSeriesTab(baseFrame, tabbedPane, imageViewerTab);
        imageSeriesTab.init();

        PhotometricClassifierTab photoClassTab = new PhotometricClassifierTab(baseFrame, tabbedPane, catalogQueryTab, imageViewerTab);
        photoClassTab.init();

        VizierCatalogsTab vizierCatalogsTab = new VizierCatalogsTab(baseFrame, tabbedPane);
        vizierCatalogsTab.init();

        AdqlQueryTab adqlQueryTab = new AdqlQueryTab(baseFrame, tabbedPane, catalogQueryTab);
        adqlQueryTab.init();

        BatchQueryTab batchQueryTab = new BatchQueryTab(baseFrame, tabbedPane, catalogQueryTab, imageViewerTab);
        batchQueryTab.init();

        FileBrowserTab fileBrowserTab = new FileBrowserTab(baseFrame, tabbedPane, catalogQueryTab, imageViewerTab, this, tabbedPane.getTabCount());
        fileBrowserTab.init();

        ObjectCollectionTab objectCollectionTab = new ObjectCollectionTab(baseFrame, tabbedPane, catalogQueryTab, imageViewerTab);
        objectCollectionTab.init();

        CustomOverlaysTab customOverlaysTab = new CustomOverlaysTab(baseFrame, tabbedPane, imageViewerTab);
        customOverlaysTab.init();

        ToolTab toolTab = new ToolTab(baseFrame, tabbedPane);
        toolTab.init();

        LookupTab lookupTab = new LookupTab(baseFrame, tabbedPane);
        lookupTab.init();

        SettingsTab settingsTab = new SettingsTab(baseFrame, tabbedPane, catalogQueryTab, imageViewerTab, batchQueryTab);
        settingsTab.init();

        tabbedPane.setSelectedIndex(3);

        baseFrame.setLocationRelativeTo(null);
        baseFrame.setVisible(true);

        boolean checkVersion = Boolean.parseBoolean(getUserSetting("checkVersion", "true"));

        if (!versionLoaded && checkVersion) {
            try {
                String response = readResponse(establishHttpConnection(RELEASES_URL + "versions.txt"), PGM_NAME + " version file");
                if (!response.isEmpty()) {
                    String currentVersion = PGM_VERSION;
                    String latestVersion = "Not available!";
                    LocalDate referenceDate = LocalDate.now().minusMonths(1);
                    LocalDate releaseDate = LocalDate.MIN;
                    String versionMessage = "";
                    Scanner scanner = new Scanner(response);
                    while (scanner.hasNextLine()) {
                        String[] values = CSVParser.parseLine(scanner.nextLine());
                        Version version = new Version(
                                values[0],
                                Boolean.valueOf(values[1]),
                                Integer.valueOf(values[2]),
                                Integer.valueOf(values[3]),
                                Integer.valueOf(values[4]),
                                values[5]
                        );
                        if (version.isLatest()) {
                            latestVersion = version.getNumber();
                            releaseDate = version.getDate();
                            versionMessage = version.getMessage();
                        }
                    }
                    if (!currentVersion.equals(latestVersion)) {
                        long remainingDays = DAYS.between(referenceDate, releaseDate);
                        showVersionPanel(baseFrame, currentVersion, latestVersion, remainingDays, versionMessage);
                        if (referenceDate.isEqual(releaseDate) || referenceDate.isAfter(releaseDate)) {
                            System.exit(0);
                        }
                    }
                }
            } catch (IOException ex) {
                showExceptionDialog(baseFrame, ex);
            } finally {
                versionLoaded = true;
            }
        }
    }

    private void showVersionPanel(JFrame baseFrame, String currentVersion, String latestVersion, long remainingDays, String versionMessage) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(new JLabel("There's a new " + PGM_NAME + " version available!"));
        container.add(createHyperlink("> Download new version", RELEASES_URL + String.format("executables/AstroToolBox-%s.jar", latestVersion)));
        container.add(createHyperlink("> Check release notes", RELEASES_URL + "release%20notes.md"));
        container.add(new JLabel("Please make sure to always use the latest version of this tool!"));
        container.add(new JLabel("Previous versions may contain bugs and/or may no longer work properly."));
        container.add(new JLabel("Latest version: " + latestVersion));
        String versionText = "Current version: " + currentVersion;
        JLabel versionLabel = new JLabel();
        if (remainingDays < 1) {
            versionLabel.setText(versionText + " has expired!");
            versionLabel.setForeground(Color.RED);
        } else {
            versionLabel.setText(versionText + " will expire in " + remainingDays + " days.");
        }
        container.add(versionLabel);
        if (!versionMessage.isEmpty()) {
            JTextPane textPane = new JTextPane();
            textPane.setText(versionMessage);
            textPane.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textPane);
            scrollPane.setBorder(createEtchedBorder("Info"));
            scrollPane.setPreferredSize(new Dimension(300, 100));
            container.add(scrollPane);
        }
        JOptionPane.showMessageDialog(baseFrame, container, "Version info", JOptionPane.INFORMATION_MESSAGE);
    }

    public void setDefaultCloseOperation(int defaultCloseOperation) {
        this.defaultCloseOperation = defaultCloseOperation;
    }

    public JFrame getBaseFrame() {
        return baseFrame;
    }

    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }

    public CatalogQueryTab getCatalogQueryTab() {
        return catalogQueryTab;
    }

    public ImageViewerTab getImageViewerTab() {
        return imageViewerTab;
    }

}
