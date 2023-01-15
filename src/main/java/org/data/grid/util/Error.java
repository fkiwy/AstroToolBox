package org.data.grid.util;

import org.data.grid.Column;

public class Error {

    private final String valueColumn;

    private final String errorColumn;

    public Error(String valueColumn, String errorColumn) {
        this(new Column(valueColumn), new Column(errorColumn));
    }

    public Error(Column valueColumn, Column errorColumn) {
        this.valueColumn = valueColumn.getColumnName();
        this.errorColumn = errorColumn.getColumnName();
    }

    public String getValueColumn() {
        return valueColumn;
    }

    public String getErrorColumn() {
        return errorColumn;
    }

}
