package astro.tool.box.function;

import astro.tool.box.container.StringPair;
import static astro.tool.box.function.AstrometricFunctions.*;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.SpectralTypeLookupResult;
import astro.tool.box.enumeration.Color;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhotometricFunctions {

    private static final Map<Integer, String> OBJECT_INFO_FLAGS = new HashMap<>();

    private static final Map<Integer, String> QUALITY_FLAGS = new HashMap<>();

    static {
        OBJECT_INFO_FLAGS.put(1, "used within relphot (FEW) skip star");
        OBJECT_INFO_FLAGS.put(2, "used within relphot (POOR) skip star");
        OBJECT_INFO_FLAGS.put(4, "object IDed with known ICRF quasar (may have ICRF position measurement)");
        OBJECT_INFO_FLAGS.put(8, "identified as likely QSO (Hernitschek+ 2015ApJ...801...45H) PQSO>=0.60");
        OBJECT_INFO_FLAGS.put(16, "identified as possible QSO (Hernitschek+ 2015ApJ...801...45H) PQSO>=0.05");
        OBJECT_INFO_FLAGS.put(32, "identified as likely RR Lyra (Hernitschek+ 2015ApJ...801...45H) PRRLyra>=0.60");
        OBJECT_INFO_FLAGS.put(64, "identified as possible RR Lyra (Hernitschek+ 2015ApJ...801...45H) PRRLyra>=0.05");
        OBJECT_INFO_FLAGS.put(128, "identified as a variable based on ChiSq (Hernitschek+ 2015ApJ...801...45H)");
        OBJECT_INFO_FLAGS.put(256, "identified as a non-periodic (stationary) transient");
        OBJECT_INFO_FLAGS.put(512, "at least one detection identified with a known solar-system object (asteroid or other).");
        OBJECT_INFO_FLAGS.put(1024, "most detections identified with a known solar-system object (asteroid or other).");
        OBJECT_INFO_FLAGS.put(2048, "star with large proper motion");
        OBJECT_INFO_FLAGS.put(4096, "simple weighted average position was used (no IRLS fitting)");
        OBJECT_INFO_FLAGS.put(8192, "average position was fitted");
        OBJECT_INFO_FLAGS.put(16384, "proper motion model was fitted");
        OBJECT_INFO_FLAGS.put(32768, "parallax model was fitted");
        OBJECT_INFO_FLAGS.put(65536, "average position used (not PM or PAR)");
        OBJECT_INFO_FLAGS.put(131072, "proper motion used (not AVE or PAR)");
        OBJECT_INFO_FLAGS.put(262144, "parallax used (not AVE or PM)");
        OBJECT_INFO_FLAGS.put(524288, "mean astrometry could not be measured");
        OBJECT_INFO_FLAGS.put(1048576, "stack position used for mean astrometry");
        OBJECT_INFO_FLAGS.put(2097152, "mean astrometry used for stack position");
        OBJECT_INFO_FLAGS.put(4194304, "failure to measure proper-motion model");
        OBJECT_INFO_FLAGS.put(8388608, "extended in our data (eg. PS)");
        OBJECT_INFO_FLAGS.put(16777216, "extended in external data (eg. 2MASS)");
        OBJECT_INFO_FLAGS.put(33554432, "good-quality measurement in our data (eg. PS)");
        OBJECT_INFO_FLAGS.put(67108864, "good-quality measurement in external data (eg. 2MASS)");
        OBJECT_INFO_FLAGS.put(134217728, "good-quality object in the stack (>1 good stack measurement)");
        OBJECT_INFO_FLAGS.put(268435456, "the primary stack measurements are the best measurements");
        OBJECT_INFO_FLAGS.put(536870912, "suspect object in the stack (no more than 1 good measurement or 2 or more suspect or good stack measurement)");
        OBJECT_INFO_FLAGS.put(1073741824, "poor-quality stack object (no more than 1 good or suspect measurement)");
    }

    static {
        /*
        QUALITY_FLAGS.put(1, "extended in our data (eg. PS)");
        QUALITY_FLAGS.put(2, "extended in external data (eg. 2MASS)");
        QUALITY_FLAGS.put(4, "good-quality measurement in our data (eg. PS)");
        QUALITY_FLAGS.put(8, "good-quality measurement in external data (eg. 2MASS)");
        QUALITY_FLAGS.put(16, "good-quality object in the stack (>1 good stack measurement)");
        QUALITY_FLAGS.put(32, "the primary stack measurements are the best measurements");
        QUALITY_FLAGS.put(64, "suspect object in the stack (no more than 1 good measurement or 2 or more suspect or good stack measurement)");
        QUALITY_FLAGS.put(128, "poor-quality stack object (no more than 1 good or suspect measurement)");*/
        QUALITY_FLAGS.put(1, "extended in Pan-STARRS data");
        QUALITY_FLAGS.put(2, "extended in external data (2MASS)");
        QUALITY_FLAGS.put(4, "good-quality measurement in Pan-STARRS data");
        QUALITY_FLAGS.put(8, "good-quality measurement in external data (2MASS)");
        QUALITY_FLAGS.put(16, "good-quality object in the stack");
        QUALITY_FLAGS.put(32, "the primary stack measurements are the best");
        QUALITY_FLAGS.put(64, "suspect object in the stack (no more than 1 good measurement)");
        QUALITY_FLAGS.put(128, "poor-quality stack object (no more than 1 good or suspect measurement)");
    }

    /**
     * Look up spectral type
     *
     * @param colorKey
     * @param colorValue
     * @param minEntry
     * @param maxEntry
     * @return the spectral type
     */
    public static SpectralTypeLookupResult evaluateSpectralType(Color colorKey, double colorValue, SpectralTypeLookup minEntry, SpectralTypeLookup maxEntry) {
        Double minColorValue = minEntry.getColors().get(colorKey);
        Double maxColorValue = maxEntry.getColors().get(colorKey);
        if (minColorValue == null || maxColorValue == null || minColorValue == 0 || maxColorValue == 0 || colorValue == 0) {
            return null;
        }
        if (minColorValue > maxColorValue) {
            double tempColorValue = minColorValue;
            minColorValue = maxColorValue;
            maxColorValue = tempColorValue;
            SpectralTypeLookup tempEntry = minEntry;
            minEntry = maxEntry;
            maxEntry = tempEntry;
        }
        double avgColorValue = (minColorValue + maxColorValue) / 2;
        if (colorValue >= minColorValue && colorValue < avgColorValue) {
            return new SpectralTypeLookupResult(minEntry.getSpt(), minEntry.getTeff(), minEntry.getRsun(), minEntry.getMsun(), minColorValue, abs(colorValue - minColorValue));
        } else if (colorValue >= avgColorValue && colorValue <= maxColorValue) {
            return new SpectralTypeLookupResult(maxEntry.getSpt(), maxEntry.getTeff(), maxEntry.getRsun(), maxEntry.getMsun(), maxColorValue, abs(colorValue - maxColorValue));
        } else {
            return null;
        }
    }

    /**
     * Calculate absolute magnitude from parallax
     *
     * @param apparentMagnitude (mag)
     * @param parallax (mas)
     * @return the absolute magnitude (mag)
     */
    public static double calculateAbsoluteMagnitudeFromParallax(double apparentMagnitude, double parallax) {
        if (apparentMagnitude == 0 || parallax == 0) {
            return 0;
        } else {
            double absoluteMagnitude = apparentMagnitude + 5 - 5 * log10(calculateActualDistance(parallax));
            return Double.isInfinite(absoluteMagnitude) || Double.isNaN(absoluteMagnitude) ? 0 : absoluteMagnitude;
        }
    }

    /**
     * Calculate absolute magnitude from distance
     *
     * @param apparentMagnitude (mag)
     * @param distance (pc)
     * @return the absolute magnitude (mag)
     */
    public static double calculateAbsoluteMagnitudeFromDistance(double apparentMagnitude, double distance) {
        if (distance == 0) {
            return 0;
        } else {
            return apparentMagnitude + 5 - 5 * log10(distance);
        }
    }

    /**
     * Check if the object in question is a possible AGN
     *
     * @param W1_W2
     * @param W2_W3
     * @return a boolean indicating that the object is a possible AGN
     */
    public static boolean isAPossibleAGN(double W1_W2, double W2_W3) {
        return W1_W2 > 0.5 && W1_W2 < 3.0 && W2_W3 > 2.5 && W2_W3 < 6.0;
    }

    /**
     * Check if the object in question is a possible white dwarf
     *
     * @param MGmag
     * @param BP_RP
     * @return a boolean indicating that the object is a possible white dwarf
     */
    public static boolean isAPossibleWD(double MGmag, double BP_RP) {
        return MGmag >= 10 && MGmag <= 15 && BP_RP != 0 && BP_RP <= 1.5;
    }

    /**
     * Get a list of Pan-STARRS object info flags
     *
     * @param objInfoFlag
     * @return a list of Pan-STARRS object info flags
     */
    public static List<StringPair> getPanStarrsObjectInfoFlags(Integer objInfoFlag) {
        List<StringPair> objectInfoFlags = new ArrayList<>();
        char[] chars = Integer.toBinaryString(objInfoFlag).toCharArray();
        int x = 1;
        for (int i = chars.length - 1; i > -1; i--) {
            if (chars[i] == '1') {
                objectInfoFlags.add(new StringPair(String.valueOf(x), OBJECT_INFO_FLAGS.get(x)));
            }
            x *= 2;
        }
        return objectInfoFlags;
    }

    /**
     * Get a list of Pan-STARRS quality flags
     *
     * @param qualityFlag
     * @return a list of Pan-STARRS quality flags
     */
    public static List<StringPair> getPanStarrsQualityFlags(Integer qualityFlag) {
        List<StringPair> qualityFlags = new ArrayList<>();
        char[] chars = Integer.toBinaryString(qualityFlag).toCharArray();
        int x = 1;
        for (int i = chars.length - 1; i > -1; i--) {
            if (chars[i] == '1') {
                qualityFlags.add(new StringPair(String.valueOf(x), QUALITY_FLAGS.get(x)));
            }
            x *= 2;
        }
        return qualityFlags;
    }

}
