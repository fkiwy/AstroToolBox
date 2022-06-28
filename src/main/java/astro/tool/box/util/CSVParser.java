package astro.tool.box.util;

import com.opencsv.CSVParserBuilder;
import java.io.IOException;

public class CSVParser {

    private static final char DEFAULT_SEPARATOR = Constants.SPLIT_CHAR.charAt(0);
    private static final char DEFAULT_QUOTE = '"';

    public static String[] parseLine(String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    public static String[] parseLine(String cvsLine, char separators) {
        return parseLine(cvsLine, separators, DEFAULT_QUOTE);
    }

    public static String[] parseLine(String cvsLine, char valueSeparator, char stringDelimiter) {
        /*
        List<String> result = new ArrayList<>();
        if (cvsLine == null || cvsLine.isEmpty()) {
            return new String[0];
        }
        if (customQuote == ' ') {
            customQuote = DEFAULT_QUOTE;
        }
        if (separators == ' ') {
            separators = DEFAULT_SEPARATOR;
        }
        StringBuilder curVal = new StringBuilder();
        boolean inQuotes = false;
        boolean startCollectChar = false;
        boolean doubleQuotesInColumn = false;
        char[] chars = cvsLine.toCharArray();
        for (char ch : chars) {
            if (inQuotes) {
                startCollectChar = true;
                if (ch == customQuote) {
                    inQuotes = false;
                    doubleQuotesInColumn = false;
                } else {
                    if (ch == '\"') {
                        if (!doubleQuotesInColumn) {
                            curVal.append(ch);
                            doubleQuotesInColumn = true;
                        }
                    } else {
                        curVal.append(ch);
                    }
                }
            } else {
                if (ch == customQuote) {
                    inQuotes = true;
                    if (startCollectChar) {
                        curVal.append('"');
                    }
                } else if (ch == separators) {
                    result.add(curVal.toString());
                    curVal = new StringBuilder();
                    startCollectChar = false;
                } else {
                    curVal.append(ch);
                }
            }
        }
        result.add(curVal.toString());
        result = result.stream().map(String::trim).collect(Collectors.toList());
        return result.toArray(new String[result.size()]);
         */
        com.opencsv.CSVParser parser = new CSVParserBuilder()
                .withSeparator(valueSeparator)
                .withQuoteChar(stringDelimiter)
                .build();
        try {
            return parser.parseLineMulti(cvsLine);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
