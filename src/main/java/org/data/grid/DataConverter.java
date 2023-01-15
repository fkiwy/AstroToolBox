package org.data.grid;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataConverter {

    private char valueSeparator;

    private char stringDelimiter;

    public DataConverter() {
        this(',', '"');
    }

    public DataConverter(char valueSeparator) {
        this(valueSeparator, '"');
    }

    public DataConverter(char valueSeparator, char stringDelimiter) {
        this.valueSeparator = valueSeparator;
        this.stringDelimiter = stringDelimiter;

    }

    public List<String> toList(String line) {
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(valueSeparator)
                .withQuoteChar(stringDelimiter)
                .build();
        try {
            List<String> values = new ArrayList();
            values.addAll(Arrays.asList(parser.parseLine(line)));
            return values;
        } catch (IOException ex) {
            throw new GridException(ex);
        }
    }

    public String toString(List<String> values) {
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(valueSeparator)
                .withQuoteChar(stringDelimiter)
                .build();
        return parser.parseToLine(values.toArray(new String[0]), false);
    }

}
