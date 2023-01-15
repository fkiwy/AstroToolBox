package org.data.grid;

import java.math.RoundingMode;

public class Rounding {

    public static Rounding DEC0 = new Rounding(0, RoundingMode.HALF_UP);
    public static Rounding DEC1 = new Rounding(1, RoundingMode.HALF_UP);
    public static Rounding DEC2 = new Rounding(2, RoundingMode.HALF_UP);
    public static Rounding DEC3 = new Rounding(3, RoundingMode.HALF_UP);
    public static Rounding DEC4 = new Rounding(4, RoundingMode.HALF_UP);
    public static Rounding DEC5 = new Rounding(5, RoundingMode.HALF_UP);
    public static Rounding DEC6 = new Rounding(6, RoundingMode.HALF_UP);
    public static Rounding DEC7 = new Rounding(7, RoundingMode.HALF_UP);
    public static Rounding DEC8 = new Rounding(8, RoundingMode.HALF_UP);
    public static Rounding DEC9 = new Rounding(9, RoundingMode.HALF_UP);

    private final int decimals;

    private final RoundingMode roundingMode;

    public Rounding() {
        this(2, RoundingMode.HALF_UP);
    }

    public Rounding(int decimals) {
        this(decimals, RoundingMode.HALF_UP);
    }

    public Rounding(int decimals, RoundingMode roundingMode) {
        this.decimals = decimals;
        this.roundingMode = roundingMode;
    }

    public int getDecimals() {
        return decimals;
    }

    public RoundingMode getRoundingMode() {
        return roundingMode;
    }

}
