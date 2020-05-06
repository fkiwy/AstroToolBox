package astro.tool.box.module;

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
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

public class Application {

    private int defaultCloseOperation;

    private JFrame baseFrame;

    private JTabbedPane tabbedPane;

    private CatalogQueryTab catalogQueryTab;

    private ImageViewerTab imageViewerTab;

    public Application() {
        try {
            loadUserSettings();
            LookAndFeel lookAndFeel = LookAndFeel.valueOf(getUserSetting(LOOK_AND_FEEL, LookAndFeel.OS.name()));
            if (lookAndFeel.equals(LookAndFeel.Java)) {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } else {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
        } catch (Exception e) {
        }
    }

    public void init() {
        baseFrame = new JFrame();
        baseFrame.setIconImage(getToolBoxImage());
        baseFrame.setTitle(PGM_NAME + " " + PGM_VERSION);
        baseFrame.setSize(new Dimension(1250, 850));
        baseFrame.setDefaultCloseOperation(defaultCloseOperation);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        baseFrame.add(tabbedPane);

        catalogQueryTab = new CatalogQueryTab(baseFrame, tabbedPane);
        catalogQueryTab.init();

        BrownDwarfTab brownDwarfTab = new BrownDwarfTab(baseFrame, tabbedPane, catalogQueryTab);
        brownDwarfTab.init();

        WhiteDwarfTab whiteDwarfTab = new WhiteDwarfTab(baseFrame, tabbedPane, catalogQueryTab);
        whiteDwarfTab.init();

        CustomOverlaysTab customOverlaysTab = new CustomOverlaysTab(baseFrame, tabbedPane);

        imageViewerTab = new ImageViewerTab(baseFrame, tabbedPane, customOverlaysTab);
        imageViewerTab.init();

        AdqlQueryTab adqlQueryTab = new AdqlQueryTab(baseFrame, tabbedPane, catalogQueryTab);
        adqlQueryTab.init();

        BatchQueryTab batchQueryTab = new BatchQueryTab(baseFrame, tabbedPane, catalogQueryTab, imageViewerTab);
        batchQueryTab.init();

        FileBrowserTab fileBrowserTab = new FileBrowserTab(baseFrame, tabbedPane, catalogQueryTab, imageViewerTab, this, tabbedPane.getTabCount());
        fileBrowserTab.init();

        ObjectCollectionTab objectCollectionTab = new ObjectCollectionTab(baseFrame, tabbedPane, catalogQueryTab, imageViewerTab);
        objectCollectionTab.init();

        customOverlaysTab.init();

        ToolTab toolTab = new ToolTab(baseFrame, tabbedPane);
        toolTab.init();

        LookupTab lookupTab = new LookupTab(baseFrame, tabbedPane);
        lookupTab.init();

        SettingsTab settingsTab = new SettingsTab(baseFrame, tabbedPane, catalogQueryTab, imageViewerTab);
        settingsTab.init();

        baseFrame.setLocationRelativeTo(null);
        baseFrame.setVisible(true);
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
