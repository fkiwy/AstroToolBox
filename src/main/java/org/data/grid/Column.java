package org.data.grid;

public class Column<T> {

    private final String columnName;

    private Class columnType;

    public Column(String columnName) {
        this(columnName, Double.class);
    }

    public Column(String columnName, Class columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }

    public String getColumnName() {
        return columnName;
    }

    public Class getColumnType() {
        return columnType;
    }

    public void setColumnType(Class columnType) {
        this.columnType = columnType;
    }

}
