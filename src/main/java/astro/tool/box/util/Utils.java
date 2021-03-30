package astro.tool.box.util;

import astro.tool.box.enumeration.TapProvider;
import static astro.tool.box.module.ModuleHelper.writeErrorLog;
import static astro.tool.box.module.tab.SettingsTab.DEFAULT_TAP_PROVIDER;
import static astro.tool.box.module.tab.SettingsTab.TAP_PROVIDER;
import static astro.tool.box.module.tab.SettingsTab.getUserSetting;
import static astro.tool.box.util.Constants.LINE_SEP_TEXT_AREA;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static TapProvider getTapProvider() {
        return TapProvider.valueOf(getUserSetting(TAP_PROVIDER, DEFAULT_TAP_PROVIDER));
    }

    public static void addRow(StringBuilder query, String row) {
        query.append(row).append(LINE_SEP_TEXT_AREA);
    }

    public static String encodeQuery(String query) {
        query = omitQueryComments(query)
                .replaceAll(LINE_SEP_TEXT_AREA, " ")
                .replaceAll(" +", " ")
                .replaceAll(";", "");
        try {
            return URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            writeErrorLog(ex);
            return query;
        }
    }

    public static String omitQueryComments(String query) {
        String[] lines = query.split(LINE_SEP_TEXT_AREA);
        List<String> results = new ArrayList<>();
        for (String line : lines) {
            if (!line.startsWith("--")) {
                results.add(line);
            }
        }
        return String.join(LINE_SEP_TEXT_AREA, results);
    }

}
