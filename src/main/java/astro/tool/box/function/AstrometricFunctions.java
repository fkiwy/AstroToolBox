package astro.tool.box.function;

import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.function.NumericFunctions.*;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.StringPair;
import astro.tool.box.enumeration.Unit;

import static java.lang.Math.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

public class AstrometricFunctions {

    /**
     * Calculate angular distance between 2 pairs of coordinates
     *
     * @param fromCoords (deg)
     * @param toCoords (deg)
     * @param conversionFactor
     * @return the angular distance between 2 pairs of coordinates (deg)
     */
    public static double calculateAngularDistance(NumberPair fromCoords, NumberPair toCoords, Double conversionFactor) {
        NumberPair diffCoords = calculateDifferenceBetweenCoords(fromCoords, toCoords);
        double diffRA = diffCoords.getX();
        double diffDE = diffCoords.getY();
        return sqrt(diffRA * diffRA + diffDE * diffDE) * conversionFactor;
    }

    /**
     * Calculate proper motions
     *
     * @param fromCoords (deg)
     * @param toCoords (deg)
     * @param fromDays (days)
     * @param toDays (days)
     * @param conversionFactor
     * @return the proper motions (mas/yr, mas/yr)
     */
    public static NumberPair calculateProperMotions(NumberPair fromCoords, NumberPair toCoords, int fromDays, int toDays, Double conversionFactor) {
        NumberPair diffCoords = calculateDifferenceBetweenCoords(fromCoords, toCoords);
        double diffRA = diffCoords.getX();
        double diffDE = diffCoords.getY();
        int diffDays = abs(fromDays - toDays);
        double pmRA = (diffRA / diffDays) * 365;
        double pmDE = (diffDE / diffDays) * 365;
        return new NumberPair(pmRA * conversionFactor, pmDE * conversionFactor);
    }

    /**
     * Calculate total proper motion
     *
     * @param pmRA (mas/yr)
     * @param pmDE (mas/yr
     * @return the total proper motion (mas/yr)
     */
    public static double calculateTotalProperMotion(double pmRA, double pmDE) {
        return sqrt(pmRA * pmRA + pmDE * pmDE);
    }

    /**
     * Calculate difference between 2 pairs of coordinates
     *
     * @param fromCoords (deg)
     * @param toCoords (deg)
     * @return the difference between 2 pairs of coordinates (deg, deg)
     */
    public static NumberPair calculateDifferenceBetweenCoords(NumberPair fromCoords, NumberPair toCoords) {
        double fromRA = fromCoords.getX();
        double fromDE = fromCoords.getY();
        double toRA = toCoords.getX();
        double toDE = toCoords.getY();
        double diffRA = (fromRA - toRA) * cos(toRadians((fromDE + toDE) / 2));
        double diffDE = (fromDE - toDE);
        return new NumberPair(-diffRA, -diffDE);
    }

    /**
     * Calculate transverse velocity from parallax
     *
     * @param pmRA (mas/yr)
     * @param pmDE (mas/yr
     * @param parallax (mas)
     * @return the transverse velocity (km/s)
     */
    public static double calculateTransverseVelocityFromParallax(double pmRA, double pmDE, double parallax) {
        return 4.74 * (calculateTotalProperMotion(pmRA, pmDE) / 1000) * calculateActualDistance(parallax);
    }

    /**
     * Calculate transverse velocity from distance
     *
     * @param pmRA (mas/yr)
     * @param pmDE (mas/yr
     * @param distance (pc)
     * @return the transverse velocity (km/s)
     */
    public static double calculateTransverseVelocityFromDistance(double pmRA, double pmDE, double distance) {
        return 4.74 * (calculateTotalProperMotion(pmRA, pmDE) / 1000) * distance;
    }

    /**
     * Calculate total velocity
     *
     * @param transverseVelocity (km/s)
     * @param radialVelocity (km/s)
     * @return the total velocity (km/s)
     */
    public static double calculateTotalVelocity(double transverseVelocity, double radialVelocity) {
        if (transverseVelocity == 0 || radialVelocity == 0) {
            return 0;
        } else {
            return sqrt(transverseVelocity * transverseVelocity + radialVelocity * radialVelocity);
        }
    }

    /**
     * Calculate actual distance between Sun and star
     *
     * @param parallax (mas)
     * @return the actual distance between Sun and star (pc)
     */
    public static double calculateActualDistance(double parallax) {
        if (parallax < 0.1) {
            return 0;
        } else {
            return 1 / (parallax / 1000);
        }
    }

    /**
     * Convert a value to degrees, arcseconds or milliarcseconds and vice versa
     *
     * @param toConvert
     * @param fromUnit
     * @param toUnit
     * @return the converted value
     */
    public static double convertToUnit(double toConvert, Unit fromUnit, Unit toUnit) {
        switch (fromUnit) {
            case DEGREE:
                switch (toUnit) {
                    case DEGREE:
                        return toConvert;
                    case ARCSEC:
                        return toConvert * DEG_ARCSEC;
                    case MAS:
                        return toConvert * DEG_MAS;
                    default:
                        return 0;
                }
            case ARCSEC:
                switch (toUnit) {
                    case DEGREE:
                        return toConvert / DEG_ARCSEC;
                    case ARCSEC:
                        return toConvert;
                    case MAS:
                        return toConvert * ARCSEC_MAS;
                    default:
                        return 0;
                }
            case MAS:
                switch (toUnit) {
                    case DEGREE:
                        return toConvert / DEG_MAS;
                    case ARCSEC:
                        return toConvert / ARCSEC_MAS;
                    case MAS:
                        return toConvert;
                    default:
                        return 0;
                }
            default:
                return 0;
        }
    }

    /**
     * Convert sexagesimal to decimal coordinates
     *
     * @param hmsRA
     * @param dmsDE
     * @return the decimal coordinates
     */
    public static NumberPair convertToDecimalCoords(String hmsRA, String dmsDE) {
        String[] parts;

        parts = hmsRA.trim().replaceAll("[:hms'\"]", " ").split("\\s+");
        int hRA = toInteger(parts[0]);
        int mRA = toInteger(parts[1]);
        double sRA = toDouble(parts[2]);
        double degRA = hRA * 15 + (mRA * 15) / DEG_ARCMIN + (sRA * 15) / DEG_ARCSEC;

        parts = dmsDE.trim().replaceAll("[:dms°'\"]", " ").split("\\s+");
        boolean isNegative = parts[0].startsWith("-");
        int dDE = toInteger(parts[0]);
        int mDE = toInteger(parts[1]);
        double sDE = toDouble(parts[2]);
        int sign;
        if (dDE < 0) {
            sign = -1;
        } else if (dDE == 0) {
            sign = isNegative ? -1 : 1;
        } else {
            sign = 1;
        }
        double degDE = dDE + sign * (mDE / DEG_ARCMIN) + sign * (sDE / DEG_ARCSEC);

        return new NumberPair(degRA, degDE);
    }

    /**
     * Convert decimal to sexagesimal coordinates
     *
     * @param degRA
     * @param degDE
     * @return the sexagesimal coordinates
     */
    public static StringPair convertToSexagesimalCoords(double degRA, double degDE) {
        int hRA = (int) floor(degRA / 15);
        int mRA = (int) floor((degRA / 15 - hRA) * DEG_ARCMIN);
        double sRA = (degRA / 15 - hRA) * DEG_ARCSEC - mRA * DEG_ARCMIN;

        int dDE = (int) floor(degDE);
        int mDE = (int) floor((degDE - dDE) * DEG_ARCMIN);
        double sDE = (degDE - dDE) * DEG_ARCSEC - mDE * DEG_ARCMIN;

        return new StringPair(
                formatInteger(hRA, "00") + " " + formatInteger(mRA, "00") + " " + formatDouble(sRA, "00.00"),
                formatInteger(dDE, "00") + " " + formatInteger(mDE, "00") + " " + formatDouble(sDE, "00.00")
        );
    }

    /**
     * Convert a modified Julian date to local date and time
     *
     * @param modifiedJulianDate
     * @return the local date and time (UTC)
     */
    public static LocalDateTime convertMJDToDateTime(BigDecimal modifiedJulianDate) {
        Instant epoch = OffsetDateTime.of(1858, 11, 17, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        BigInteger days = modifiedJulianDate.toBigInteger();
        BigDecimal fractionOfADay = modifiedJulianDate.subtract(new BigDecimal(days));
        BigDecimal decimalSeconds = new BigDecimal(TimeUnit.DAYS.toSeconds(1)).multiply(fractionOfADay);
        BigInteger integerSeconds = decimalSeconds.toBigInteger();
        BigInteger nanos = decimalSeconds.subtract(new BigDecimal(integerSeconds)).multiply(new BigDecimal(1_000_000_000L)).toBigInteger();
        Duration duration = Duration.ofDays(days.longValue()).plusSeconds(integerSeconds.longValue()).plusNanos(nanos.longValue());
        Instant instant = epoch.plus(duration);
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    /**
     * Convert local date and time to a modified Julian date
     *
     * @param dateTime
     * @return the modified Julian date
     */
    public static BigDecimal convertDateTimeToMJD(LocalDateTime dateTime) {
        Instant epoch = OffsetDateTime.of(1858, 11, 17, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Duration duration = Duration.between(epoch, dateTime.toInstant(ZoneOffset.UTC));
        Duration durationRemainder = duration.minusDays(duration.toDays());
        BigDecimal wholeDays = new BigDecimal(duration.toDays());
        BigDecimal partialDayInNanos = new BigDecimal(durationRemainder.toNanos());
        BigDecimal nanosInADay = new BigDecimal(TimeUnit.DAYS.toNanos(1));
        BigDecimal partialDay = partialDayInNanos.divide(nanosInADay, 9, RoundingMode.HALF_EVEN);
        return wholeDays.add(partialDay);
    }

    /**
     * Verify if proper motion is faulty
     *
     * @param value
     * @param error
     * @return true if proper motion is faulty
     */
    public static boolean isProperMotionFaulty(double value, double error) {
        value = Math.abs(value);
        return value - error < value / 2;
    }

}
