package astro.tool.box.util;

import static astro.tool.box.module.ModuleHelper.*;
import static astro.tool.box.module.tab.SettingsTab.*;
import static astro.tool.box.util.Constants.*;
import astro.tool.box.enumeration.TapProvider;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {

    public static final Map<String, Double> SPECTRAL_TYPES = new HashMap<>();

    static {
        List<String> spts = Arrays.asList("O", "B", "A", "F", "G", "K", "M", "L", "T", "Y");
        int i = 0;
        for (String spt : spts) {
            addSpt(spt, i);
            i += 10;
        }
    }

    static void addSpt(String spt, int sptNum) {
        for (int i = 0; i < 10; i++) {
            addSubSpt(spt, sptNum, i);
        }
    }

    static void addSubSpt(String spt, int sptNum, int i) {
        SPECTRAL_TYPES.put(spt + i, sptNum + i + .0);
        SPECTRAL_TYPES.put(spt + i + ".5", sptNum + i + .5);
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

}
