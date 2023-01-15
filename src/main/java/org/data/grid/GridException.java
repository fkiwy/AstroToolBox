package org.data.grid;

public class GridException extends RuntimeException {

    public GridException() {
    }

    public GridException(String string) {
        super(string);
    }

    public GridException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public GridException(Throwable thrwbl) {
        super(thrwbl);
    }

    public GridException(String string, Throwable thrwbl, boolean bln, boolean bln1) {
        super(string, thrwbl, bln, bln1);
    }

}
