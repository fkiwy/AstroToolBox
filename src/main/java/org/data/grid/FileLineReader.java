package org.data.grid;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileLineReader implements LineReader {

    private final BufferedReader reader;

    private final DataConverter converter;

    public FileLineReader(String fileName) {
        this(fileName, ',', '"');
    }

    public FileLineReader(String fileName, char valueSeparator) {
        this(fileName, valueSeparator, '"');
    }

    public FileLineReader(String fileName, char valueSeparator, char stringDelimiter) {
        converter = new DataConverter(valueSeparator, stringDelimiter);
        try {
            //reader = Files.newBufferedReader(Paths.get(fileName));
            reader = Files.newBufferedReader(Paths.get(fileName), StandardCharsets.ISO_8859_1);
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
