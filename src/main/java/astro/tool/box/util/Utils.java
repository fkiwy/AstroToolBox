package astro.tool.box.util;

import static astro.tool.box.util.Constants.LINE_SEP_TEXT_AREA;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static void addRow(StringBuilder query, String row) {
        query.append(row).append(LINE_SEP_TEXT_AREA);
    }

    public static String encodeQuery(String query) {
        return omitQueryComments(query).replaceAll(LINE_SEP_TEXT_AREA, " ")
                .replaceAll(" +", "%20")
                .replaceAll("\\+", "%2B")
                .replaceAll(";", "");
    }

    private static String omitQueryComments(String query) {
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
