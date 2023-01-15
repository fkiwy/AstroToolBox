package org.data.grid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import org.data.grid.DataConverter;
import org.data.grid.GridException;
import org.data.grid.LineReader;

public class StringLineReader implements LineReader {

    private final BufferedReader reader;

    private final DataConverter converter;

    public StringLineReader(String data) {
        this(data, ',', '"');
    }

    public StringLineReader(String data, char valueSeparator) {
        this(data, valueSeparator, '"');
    }

    public StringLineReader(String data, char valueSeparator, char stringDelimiter) {
        converter = new DataConverter(valueSeparator, stringDelimiter);
        reader = new BufferedReader(new StringReader(data));
    }

    @Override
    public List<String> readLine() throws Exception {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        return converter.toList(line);
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException ex) {
            throw new GridException(ex);
        }
    }

}
