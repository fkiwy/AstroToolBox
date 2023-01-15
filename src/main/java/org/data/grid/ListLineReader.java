package org.data.grid;

import java.util.Iterator;
import java.util.List;

public class ListLineReader implements LineReader {

    private final List<List<String>> list;

    private final Iterator<List<String>> iterator;

    public ListLineReader(List<List<String>> list) {
        this.list = list;
        this.iterator = list.iterator();
    }

    @Override
    public List<String> readLine() throws Exception {
        return iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    public void close() {
        list.clear();
    }

}
