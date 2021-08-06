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
     * Calculate angular distance between 2 stars
     *
     * @param fromCoords (deg)
     * @param toCoords (deg)
     * @param conversionFactor
     * @return the angular distance between 2 stars
     */
    public static double calculateAngularDistance(NumberPair fromCoords, NumberPair toCoords, Double conversionFactor) {
        double ra = toRadians(toCoords.getX());
        double dec = toRadians(toCoords.getY());
        double ra0 = toRadians(fromCoords.getX());
        double dec0 = toRadians(fromCoords.getY());
        double cosc = sin(dec0) * sin(dec) + cos(dec0) * cos(dec) * cos(ra - ra0);
        double distance = toDegrees(acos(cosc)) * conversionFactor;
        return Double.isInfinite(distance) || Double.isNaN(distance) ? 0 : distance;
    }

    /**
     * Calculate difference between coordinates
     *
     * @param fromCoords (deg)
     * @param toCoords (deg)
     * @return the difference between coordinates (deg, deg)
     */
    public static NumberPair calculateDifferenceBetweenCoords(NumberPair fromCoords, NumberPair toCoords) {
        double ra = toRadians(toCoords.getX());
        double dec = toRadians(toCoords.getY());
        double ra0 = toRadians(fromCoords.getX());
        double dec0 = toRadians(fromCoords.getY());
        double cosc = sin(dec0) * sin(dec) + cos(dec0) * cos(dec) * cos(ra - ra0);
        double x = (cos(dec) * sin(ra - ra0)) / cosc;
        double y = (cos(dec0) * sin(dec) - sin(dec0) * cos(dec) * cos(ra - ra0)) / cosc;
        return new NumberPair(toDegrees(x), toDegrees(y));
    }

    /**
     * Calculate new position resulting from proper motion
     *
     * @param coords (deg)
     * @param properMotion (deg)
     * @return the new position resulting from proper motion (deg, deg)
     */
    public static NumberPair calculatePositionFromProperMotion(NumberPair coords, NumberPair properMotion) {
        if (properMotion.getX() == 0 && properMotion.getY() == 0) {
            return coords;
        }
        double x = toRadians(properMotion.getX());
        double y = toRadians(properMotion.getY());
        double ra0 = toRadians(coords.getX());
        double dec0 = toRadians(coords.getY());
        double p = sqrt(x * x + y * y);
        double c = atan(p);
        double ra = ra0 + atan2(x * sin(c), p * cos(dec0) * cos(c) - y * sin(dec0) * sin(c));
        double dec = asin(cos(c) * sin(dec0) + (y * sin(c) * cos(dec0)) / p);
        return new NumberPair(toDegrees(ra), toDegrees(dec));
    }

    /**
     * Calculate linear distance between 2 stars
     *
     * @param fromCoords (deg)
     * @param toCoords (deg)
     * @param fromParallax (mas)
     * @param toParallax (mas)
     * @return the linear distance between stars (pc)
     */
    public static double calculateLinearDistance(NumberPair fromCoords, NumberPair toCoords, double fromParallax, double toParallax) {
        double fromRA = toRadians(fromCoords.getX());
        double fromDE = toRadians(fromCoords.getY());
        double fromDist = calculateParallacticDistance(fromParallax);

        double toRA = toRadians(toCoords.getX());
        double toDE = toRadians(toCoords.getY());
        double toDist = calculateParallacticDistance(toParallax);

        double x1 = fromDist * cos(fromRA) * cos(fromDE);
        double y1 = fromDist * sin(fromRA) * cos(fromDE);
        double z1 = fromDist * sin(fromDE);

        double x2 = toDist * cos(toRA) * cos(toDE);
        double y2 = toDist * sin(toRA) * cos(toDE);
        double z2 = toDist * sin(toDE);

        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;

        return sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Calculate distance between Sun and star
     *
     * @param parallax (mas)
     * @return the distance between Sun and star (pc)
     */
    public static double calculateParallacticDistance(double parallax) {
        if (parallax < 1) {
            return 0;
        } else {
            return 1 / (parallax / ARCSEC_MAS);
        }
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
     * Calculate transverse velocity from parallax
     *
     * @param pmRA (mas/yr)
     * @param pmDE (mas/yr
     * @param parallax (mas)
     * @return the transverse velocity (km/s)
     */
    public static double calculateTransverseVelocityFromParallax(double pmRA, double pmDE, double parallax) {
        return 4.74 * (calculateTotalProperMotion(pmRA, pmDE) / ARCSEC_MAS) * calculateParallacticDistance(parallax);
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
        return 4.74 * (calculateTotalProperMotion(pmRA, pmDE) / ARCSEC_MAS) * distance;
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
     * @param hmsRA (hms)
     * @param dmsDE (dms)
     * @return the decimal coordinates (deg, deg)
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
     * @param degRA (deg)
     * @param degDE (deg)
     * @return the sexagesimal coordinates (hms, dms)
     */
    public static StringPair convertToSexagesimalCoords(double degRA, double degDE) {
        int hRA = (int) floor(degRA / 15);
        int mRA = (int) floor((degRA / 15 - hRA) * DEG_ARCMIN);
        double sRA = (degRA / 15 - hRA) * DEG_ARCSEC - mRA * DEG_ARCMIN;

        String signDE = "";
        if (degDE < 0) {
            degDE = -degDE;
            signDE = "-";
        }
        int dDE = (int) floor(degDE);
        int mDE = (int) floor((degDE - dDE) * DEG_ARCMIN);
        double sDE = (degDE - dDE) * DEG_ARCSEC - mDE * DEG_ARCMIN;

        return new StringPair(
                formatInteger(hRA, "00") + " " + formatInteger(mRA, "00") + " " + formatDouble(sRA, "00.00"),
                signDE + formatInteger(dDE, "00") + " " + formatInteger(mDE, "00") + " " + formatDouble(sDE, "00.00")
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
        return error > value * 0.5;
    }

    /**
     * Calculate chance alignment probability
     *
     * @param properMotionMatches
     * @param seperation (arcsec)
     * @return the chance alignment probability
     */
    public static double calculateChanceAlignmentProbability(int properMotionMatches, double seperation) {
        // sky = 41252.96125 deg^2
        return (properMotionMatches / 41252.96125) * pow(seperation / DEG_ARCSEC, 2) * Math.PI;
    }

}
