package astro.tool.box.enumeration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import astro.tool.box.tab.AdqlQueryTab;
import astro.tool.box.tab.BatchQueryTab;
import astro.tool.box.tab.CatalogQueryTab;
import astro.tool.box.tab.CustomOverlaysTab;
import astro.tool.box.tab.FileBrowserTab;
import astro.tool.box.tab.ImageSeriesTab;
import astro.tool.box.tab.LookupTab;
import astro.tool.box.tab.ObjectCollectionTab;
import astro.tool.box.tab.PhotometricClassifierTab;
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
    LO(LookupTab.TAB_NAME);

    public String val;

    private TabCode(String val) {
        this.val = val;
    }

    private static final Map<String, String> TAB_CODES = new HashMap();

    static {
        for (TabCode tabCode : values()) {
            TAB_CODES.put(tabCode.name(), tabCode.val);
        }
    }

    private static String getTabLabel(String tabCode) {
        return TAB_CODES.get(tabCode);
    }

    private static final Map<String, String> TAB_LABELS = new HashMap();

    static {
        for (TabCode tabCode : values()) {
            TAB_LABELS.put(tabCode.val, tabCode.name());
        }
    }

    private static String getTabCode(String tabLabel) {
        return TAB_LABELS.get(tabLabel);
    }

    public static List<String> convertTabCodeToLabel(String tabCodes) {
        if (tabCodes.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> tabLabels = new ArrayList();
        for (String tabCode : Arrays.asList(tabCodes.split(",", -1))) {
            String tabLabel = getTabLabel(tabCode);
            if (tabLabel != null) {
                tabLabels.add(tabLabel);
            }
        }
        return tabLabels;
    }

    public static String convertTabLabelToCode(List<String> tabLabels) {
        if (tabLabels.isEmpty()) {
            return "";
        }
        List<String> tabCodes = new ArrayList();
        for (String tabLabel : tabLabels) {
            String tabCode = getTabCode(tabLabel);
            if (tabCode != null) {
                tabCodes.add(tabCode);
            }
        }
        return tabCodes.stream().collect(Collectors.joining(","));
    }

    public static String getTabCodes() {
        List<String> tabCodes = new ArrayList();
        for (TabCode tabCode : values()) {
            tabCodes.add(tabCode.name());
        }
        return tabCodes.stream().collect(Collectors.joining(","));
    }

    public static List<String> getTabLabels() {
        List tabLabels = new ArrayList();
        for (TabCode tabCode : values()) {
            tabLabels.add(tabCode.val);
        }
        return tabLabels;
    }

}
