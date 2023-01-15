package org.data.grid;

public class ColumnException extends RuntimeException {

    public ColumnException() {
    }

    public ColumnException(String string) {
        super(string);
    }

    public ColumnException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public ColumnException(Throwable thrwbl) {
        super(thrwbl);
    }

    public ColumnException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }

}
