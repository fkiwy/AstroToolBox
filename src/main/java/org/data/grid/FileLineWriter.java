package org.data.grid;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileLineWriter implements LineWriter {

    private final BufferedWriter writer;

    private final DataConverter converter;

    public FileLineWriter(String fileName) {
        this(fileName, ',', '"');
    }

    public FileLineWriter(String fileName, char valueSeparator) {
        this(fileName, valueSeparator, '"');
    }

    public FileLineWriter(String fileName, char valueSeparator, char stringDelimiter) {
        converter = new DataConverter(valueSeparator, stringDelimiter);
        try {
            writer = Files.newBufferedWriter(Paths.get(fileName));
        } catch (IOException ex) {
            throw new GridException(ex);
        }
    }

    @Override
    public void writeLine(List<String> values) throws Exception {
        writer.write(converter.toString(values));
        writer.newLine();
    }

    @Override
    public void close() {
        try {
            writer.close();
        } catch (IOException ex) {
            throw new GridException(ex);
        }
    }

}
