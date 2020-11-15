package astro.tool.box.function;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class NumericFunctions {

    public static final String PATTERN_0DEC = "0";

    public static final String PATTERN_1DEC = "0.0";

    public static final String PATTERN_2DEC = "0.00";

    public static final String PATTERN_3DEC = "0.000";

    public static final String PATTERN_4DEC = "0.0000";

    public static final String PATTERN_5DEC = "0.00000";

    public static final String PATTERN_6DEC = "0.000000";

    public static final String PATTERN_7DEC = "0.0000000";

    public static final String PATTERN_8DEC = "0.00000000";

    public static final String PATTERN_9DEC = "0.000000000";

    public static final String PATTERN_0DEC_NZ = "#";

    public static final String PATTERN_1DEC_NZ = "#.#";

    public static final String PATTERN_2DEC_NZ = "#.##";

    public static final String PATTERN_3DEC_NZ = "#.###";

    public static final String PATTERN_4DEC_NZ = "#.####";

    public static final String PATTERN_5DEC_NZ = "#.#####";

    public static final String PATTERN_6DEC_NZ = "#.######";

    public static final String PATTERN_7DEC_NZ = "#.#######";

    public static final String PATTERN_8DEC_NZ = "#.########";

    public static final String PATTERN_9DEC_NZ = "#.#########";

    public static String roundTo0Dec(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_0DEC);
    }

    public static String roundTo1Dec(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_1DEC);
    }

    public static String roundTo2Dec(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_2DEC);
    }

    public static String roundTo3Dec(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_3DEC);
    }

    public static String roundTo4Dec(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_4DEC);
    }

    public static String roundTo5Dec(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_5DEC);
    }

    public static String roundTo6Dec(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_6DEC);
    }

    public static String roundTo7Dec(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_7DEC);
    }

    public static String roundTo8Dec(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_8DEC);
    }

    public static String roundTo9Dec(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_9DEC);
    }

    public static String roundTo0DecNZ(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_0DEC_NZ);
    }

    public static String roundTo1DecNZ(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_1DEC_NZ);
    }

    public static String roundTo2DecNZ(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_2DEC_NZ);
    }

    public static String roundTo3DecNZ(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_3DEC_NZ);
    }

    public static String roundTo4DecNZ(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_4DEC_NZ);
    }

    public static String roundTo5DecNZ(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_5DEC_NZ);
    }

    public static String roundTo6DecNZ(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_6DEC_NZ);
    }

    public static String roundTo7DecNZ(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_7DEC_NZ);
    }

    public static String roundTo8DecNZ(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_8DEC_NZ);
    }

    public static String roundTo9DecNZ(double number) {
        if (number == 0) {
            return "";
        }
        return formatDouble(number, PATTERN_9DEC_NZ);
    }

    public static String roundTo1DecLZ(double number) {
        if (roundDouble(number, PATTERN_1DEC) == 0) {
            return "0";
        }
        return formatDouble(number, PATTERN_1DEC);
    }

    public static String roundTo3DecLZ(double number) {
        if (roundDouble(number, PATTERN_3DEC) == 0) {
            return "0";
        }
        return formatDouble(number, PATTERN_3DEC);
    }

    public static String roundTo1DecNZLZ(double number) {
        if (roundDouble(number, PATTERN_1DEC) == 0) {
            return "0";
        }
        return formatDouble(number, PATTERN_1DEC_NZ);
    }

    public static String roundTo3DecNZLZ(double number) {
        if (roundDouble(number, PATTERN_3DEC) == 0) {
            return "0";
        }
        return formatDouble(number, PATTERN_3DEC_NZ);
    }

    public static String formatDouble(Double number, String pattern) {
        DecimalFormat df = new DecimalFormat(pattern);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(number);
    }

    public static String formatInteger(Integer number, String pattern) {
        DecimalFormat df = new DecimalFormat(pattern);
        return df.format(number);
    }

    public static double roundDouble(Double number, String pattern) {
        DecimalFormat df = new DecimalFormat(pattern);
        df.setRoundingMode(RoundingMode.HALF_UP);
        return Double.parseDouble(df.format(number));
    }

    public static double toDouble(String value) {
        return Double.parseDouble(value.isEmpty() ? "0" : value);
    }

    public static long toLong(String value) {
        return Long.parseLong(value.isEmpty() ? "0" : value);
    }

    public static int toInteger(String value) {
        return Integer.parseInt(value.isEmpty() ? "0" : value);
    }

    public static int fromDoubleToInteger(Double value) {
        return (int) Math.round(value);
    }

    public static boolean isDecimal(String arg) {
        return arg.matches("[+-]?\\d+\\.\\d+");
    }

    public static boolean isNumeric(String arg) {
        return arg.matches("[+-]?\\d+(\\.\\d+)?");
    }

    public static boolean isInteger(String arg) {
        return arg.matches("[+-]?\\d+");
    }

    public static String suppressZero(Number arg) {
        return arg.equals(0) ? "" : String.valueOf(arg);
    }

    public static String addPlusSign(Double value) {
        return value > 0 ? "+" + value : value.toString();
    }

}
