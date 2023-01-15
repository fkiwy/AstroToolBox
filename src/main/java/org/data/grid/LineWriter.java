package org.data.grid;

import java.io.Closeable;
import java.util.List;

public interface LineWriter extends Closeable {

    void writeLine(List<String> values) throws Exception;

    @Override
    void close();

}
