package astro.tool.box.util;

import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.module.tab.SettingsTab.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.enumeration.TapProvider;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static final Map<String, Double> SPECTRAL_TYPES = new LinkedHashMap<>();

    static {
        int y = 0;
        String[] spts = new String[]{"O", "B", "A", "F", "G", "K", "M", "L", "T", "Y"};
        for (String spt : spts) {
            for (int i = 0; i < 10; i++) {
                add(spt, i, y++);
            }
        }
    }

    private static void add(String spt, int i, int y) {
        SPECTRAL_TYPES.put(spt + i, y + .0);
        SPECTRAL_TYPES.put(spt + i + ".5", y + .5);
    }

    public static TapProvider getTapProvider() {
        return TapProvider.valueOf(getUserSetting(TAP_PROVIDER, DEFAULT_TAP_PROVIDER));
    }

    public static void addRow(StringBuilder query, String row) {
        query.append(row).append(LINE_SEP_TEXT_AREA);
    }

    public static String encodeQuery(String query) {
        try {
            return URLEncoder.encode(omitQueryComments(query), ENCODING);
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
                results.add(line.trim());
            }
        }
        return String.join(" ", results).replaceAll(";", "");
    }

    public static String removeFirstAndLastCharacter(String str) {
        return str.substring(1, str.length() - 1);
    }

    /**
     * This method will add elements to an array and return the resulting array
     *
     * @param arr
     * @param elements
     * @return
     */
    public static Object[] addToArray(Object[] arr, Object... elements) {
        Object[] tempArr = new Object[arr.length + elements.length];
        System.arraycopy(arr, 0, tempArr, 0, arr.length);
        System.arraycopy(elements, 0, tempArr, arr.length, elements.length);
        return tempArr;
    }

}
