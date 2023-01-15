package org.data.grid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

public class HttpLineReader implements LineReader {

    private final BufferedReader reader;

    private final DataConverter converter;

    public HttpLineReader(String url) {
        this(url, ',', '"');
    }

    public HttpLineReader(String url, char valueSeparator) {
        this(url, valueSeparator, '"');
    }

    public HttpLineReader(String url, char valueSeparator, char stringDelimiter) {
        converter = new DataConverter(valueSeparator, stringDelimiter);
        try {
            reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
        } catch (IOException ex) {
            throw new GridException(ex);
        }
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
