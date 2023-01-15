package org.data.grid;

public class Sorting {

    private final Column column;

    private final boolean ascending;

    public Sorting(String columnName) {
        this(new Column(columnName), true);
    }

    public Sorting(String columnName, boolean ascending) {
        this(new Column(columnName), ascending);
    }

    public Sorting(Column column) {
        this(column, true);
    }

    public Sorting(Column column, boolean ascending) {
        this.column = column;
        this.ascending = ascending;
    }

    public Column getColumn() {
        return column;
    }

    public boolean isAscending() {
        return ascending;
    }

}
