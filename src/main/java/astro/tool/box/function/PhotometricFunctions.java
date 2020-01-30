package astro.tool.box.function;

import static astro.tool.box.function.AstrometricFunctions.*;
import astro.tool.box.container.lookup.SpectralTypeLookup;
import astro.tool.box.container.lookup.SpectralTypeLookupResult;
import astro.tool.box.enumeration.Color;
import static java.lang.Math.*;

public class PhotometricFunctions {

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
        if (parallax == 0) {
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
        //return MGmag >= 10 && MGmag <= 15 && BP_RP != 0 && BP_RP <= 1.5;
        return MGmag >= 10 && MGmag <= 16 && BP_RP != 0 && BP_RP <= 1.7;
    }

}
