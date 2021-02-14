package astro.tool.box.module;

import astro.tool.box.container.Version;
import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.module.tab.SettingsTab.*;
import astro.tool.box.enumeration.LookAndFeel;
import astro.tool.box.module.tab.AdqlQueryTab;
import astro.tool.box.module.tab.BatchQueryTab;
import astro.tool.box.module.tab.BrownDwarfTab;
import astro.tool.box.module.tab.CatalogQueryTab;
import astro.tool.box.module.tab.CustomOverlaysTab;
import astro.tool.box.module.tab.FileBrowserTab;
import astro.tool.box.module.tab.ImageViewerTab;
import astro.tool.box.module.tab.LookupTab;
import astro.tool.box.module.tab.ObjectCollectionTab;
import astro.tool.box.module.tab.SettingsTab;
import astro.tool.box.module.tab.ToolTab;
import astro.tool.box.module.tab.WhiteDwarfTab;
import astro.tool.box.util.CSVParser;
import static astro.tool.box.util.ServiceProviderUtils.*;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;
import java.time.LocalDate;
import static java.time.temporal.ChronoUnit.DAYS;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

public class Application {

    private int defaultCloseOperation;

    private JFrame baseFrame;

    private JTabbedPane tabbedPane;

    private CatalogQueryTab catalogQueryTab;

    private ImageViewerTab imageViewerTab;

    private static boolean configLoaded = true;

    public Application() {
        try {
            loadUserSettings();
            LookAndFeel lookAndFeel = LookAndFeel.valueOf(getUserSetting(LOOK_AND_FEEL, DEFAULT_LOOK_AND_FEEL));
            setLookAndFeel(lookAndFeel);
            CURRENT_LOOK_AND_FEEL = UIManager.getLookAndFeel().getName();
            ToolTipManager.sharedInstance().setDismissDelay(60000);
        } catch (Exception e) {
        }
    }

    public void init() {
        baseFrame = new JFrame();
        baseFrame.setIconImage(getToolBoxImage());
        baseFrame.setTitle(PGM_NAME + " " + PGM_VERSION);
        baseFrame.setSize(new Dimension(1255, 850));
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

        baseFrame.setLocationRelativeTo(null);
        baseFrame.setVisible(true);

        if (!configLoaded) {
            try {
                String response = readResponse(establishHttpConnection(CONFIG_FILE_URL), PGM_NAME + " config file");
                if (!response.isEmpty()) {
                    Scanner scanner = new Scanner(response);
                    String currentVersion = PGM_VERSION;
                    String latestVersion = "";
                    LocalDate referenceDate = LocalDate.now().minusMonths(1);
                    LocalDate releaseDate = referenceDate;
                    String fileId = "";
                    String notesId = "";
                    while (scanner.hasNextLine()) {
                        String[] values = CSVParser.parseLine(scanner.next());
                        Version version = new Version(
                                values[0],
                                Boolean.valueOf(values[1]),
                                Integer.valueOf(values[2]),
                                Integer.valueOf(values[3]),
                                Integer.valueOf(values[4]),
                                values[5],
                                values[6]
                        );
                        if (version.isLatest()) {
                            latestVersion = version.getNumber();
                            releaseDate = version.getDate();
                            fileId = version.getFileId();
                            notesId = version.getNotesId();
                        }
                    }
                    if (!currentVersion.equals(latestVersion)) {
                        long remainingDays = DAYS.between(referenceDate, releaseDate);
                        showVersionPanel(baseFrame, fileId, notesId, currentVersion, latestVersion, remainingDays);
                        if (referenceDate.isAfter(releaseDate)) {
                            System.exit(0);
                        }
                    }
                }
            } catch (IOException ex) {
                showExceptionDialog(baseFrame, ex);
            } finally {
                configLoaded = true;
            }
        }
    }

    private void showVersionPanel(JFrame baseFrame, String fileId, String notesId, String currentVersion, String latestVersion, long remainingDays) {
        JPanel panel = new JPanel(new GridLayout(8, 1));
        panel.add(new JLabel("There's a new " + PGM_NAME + " version available!"));
        panel.add(createHyperlink("> Download new version", DOWNLOAD_URL + fileId));
        panel.add(createHyperlink("> Check release notes", DOWNLOAD_URL + notesId));
        panel.add(new JLabel("Please always use the latest version of this tool!"));
        panel.add(new JLabel("Previous versions may contain bugs and/or may no longer work properly."));
        panel.add(new JLabel("Latest version: " + latestVersion));
        panel.add(new JLabel("Current version: " + currentVersion + (remainingDays < 1 ? " has expired!" : " will expire in " + remainingDays + " days.")));
        JOptionPane.showMessageDialog(baseFrame, panel, "Version info", JOptionPane.INFORMATION_MESSAGE);
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
