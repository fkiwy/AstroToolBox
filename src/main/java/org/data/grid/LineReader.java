package org.data.grid;

import java.io.Closeable;
import java.util.List;

public interface LineReader extends Closeable {

    List<String> readLine() throws Exception;

    @Override
    void close();

}
