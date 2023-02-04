package astro.tool.box.enumeration;

import astro.tool.box.tab.AdqlQueryTab;
import astro.tool.box.tab.BatchQueryTab;
import astro.tool.box.tab.CatalogQueryTab;
import astro.tool.box.tab.CustomOverlaysTab;
import astro.tool.box.tab.FileBrowserTab;
import astro.tool.box.tab.FinderChartTab;
import astro.tool.box.tab.ImageSeriesTab;
import astro.tool.box.tab.LookupTab;
import astro.tool.box.tab.ObjectCollectionTab;
import astro.tool.box.tab.PhotometricClassifierTab;
import astro.tool.box.tab.SettingsTab;
import astro.tool.box.tab.ToolTab;
import astro.tool.box.tab.VizierCatalogsTab;

public enum TabCode {

    CQ(CatalogQueryTab.TAB_NAME),
    IS(ImageSeriesTab.TAB_NAME),
    PC(PhotometricClassifierTab.TAB_NAME),
    VC(VizierCatalogsTab.TAB_NAME),
    AQ(AdqlQueryTab.TAB_NAME),
    BQ(BatchQueryTab.TAB_NAME),
    FB(FileBrowserTab.TAB_NAME),
    OC(ObjectCollectionTab.TAB_NAME),
    CO(CustomOverlaysTab.TAB_NAME),
    TO(ToolTab.TAB_NAME),
    LO(LookupTab.TAB_NAME),
    SE(SettingsTab.TAB_NAME);
    //FC(FinderChartTab.TAB_NAME);

    public String val;

    private TabCode(String val) {
        this.val = val;
    }

}
