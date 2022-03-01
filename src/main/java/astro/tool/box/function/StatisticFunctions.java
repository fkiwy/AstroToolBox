package astro.tool.box.function;

import astro.tool.box.enumeration.StatType;
import static astro.tool.box.function.NumericFunctions.roundTo2Dec;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticFunctions {

    /**
     * Remove outliers from a list of values using sigma clipping
     *
     * @param values
     * @param factor (to multiply with standard deviation)
     * @param statType (StatType.MEAN or StatType.MEDIAN)
     * @return the sanitized list
     */
    public static List<Double> removeOutliers(List<Double> values, int factor, StatType statType) {
        double avg;
        if (StatType.MEAN.equals(statType)) {
            avg = calculateMean(values);
        } else {
            avg = determineMedian(values);
        }
        double std = calculateStandardDeviation(values);
        return values.stream()
                .filter(val -> isNoOutlier(val, avg, std * factor))
                .collect(Collectors.toList());
    }

    private static boolean isNoOutlier(double val, double avg, double dev) {
        return val > avg - dev && val < avg + dev;
    }

    /**
     * Remove outliers from a list of values using low and high percentiles
     *
     * @param values
     * @param lowPercentile
     * @param highPercentile
     * @return the sanitized list
     */
    public static List<Double> removeOutliers(List<Double> values, double lowPercentile, double highPercentile) {
        if (lowPercentile < 0 || lowPercentile > 100) {
            throw new IllegalArgumentException(String.format("Low percentile (=%s) out of range. Must be between 0 and 100.", roundTo2Dec(lowPercentile)));
        }
        if (highPercentile < 0 || highPercentile > 100) {
            throw new IllegalArgumentException(String.format("High percentile (=%s) out of range. Must be between 0 and 100.", roundTo2Dec(highPercentile)));
        }

        values.sort(Comparator.naturalOrder());
        int size = values.size();
        int half = size / 2;

        int lowOuliers = (int) (half * lowPercentile / 100);
        List<Double> lowOuliersRemoved = values.subList(lowOuliers, half);

        int highOutliers = (int) (half * (100 - highPercentile) / 100);
        List<Double> highOutliersRemoved = values.subList(half, size - highOutliers);

        List<Double> outliersRemoved = new ArrayList();
        outliersRemoved.addAll(lowOuliersRemoved);
        outliersRemoved.addAll(highOutliersRemoved);

        return outliersRemoved;
    }

    /**
     * Determine the median of a set of values
     *
     * @param values
     * @return the median
     */
    public static double determineMedian(List<Double> values) {
        values.sort(Comparator.naturalOrder());
        if (values.isEmpty()) {
            return 0;
        }
        int size = values.size();
        int half = size / 2 - 1;
        half = half < 0 ? 0 : half;
        if (size % 2 == 0) {
            return calculateMean(values.get(half), values.get(half + 1));
        } else {
            return values.get(half);
        }
    }

    /**
     * Calculate the mean of a set of values
     *
     * @param values
     * @return the mean
     */
    public static double calculateMean(double... values) {
        if (values.length == 0) {
            return 0;
        }
        return Arrays.stream(values).average().getAsDouble();
    }

    public static double calculateMean(List<Double> values) {
        return calculateMean(convertToArray(values));
    }

    /**
     * Calculate the quadrature of some values
     *
     * @param values
     * @return the quadrature of some values
     */
    public static double calculateQuadrature(double... values) {
        if (values.length == 0) {
            return 0;
        }
        double squaredSum = Arrays.stream(values).map(value -> pow(value, 2)).sum();
        return sqrt(squaredSum);
    }

    public static double calculateQuadrature(List<Double> values) {
        return calculateQuadrature(convertToArray(values));
    }

    /**
     * Calculate the standard deviation of a population
     *
     * @param values
     * @return the standard deviation of a population
     */
    public static double calculateStandardDeviation(double... values) {
        if (values.length == 0) {
            return 0;
        }
        double mean = calculateMean(values);
        double variance = Arrays.stream(values).map(value -> pow(value - mean, 2)).average().getAsDouble();
        return sqrt(variance);
    }

    public static double calculateStandardDeviation(List<Double> values) {
        return calculateStandardDeviation(convertToArray(values));
    }

    /**
     * Calculate the standard error of the mean
     *
     * @param values
     * @return the standard error of the mean
     */
    public static double calculateStandardError(double... values) {
        if (values.length == 0) {
            return 0;
        }
        double standardDeviation = calculateStandardDeviation(values);
        return standardDeviation / sqrt(values.length);
    }

    public static double calculateStandardError(List<Double> values) {
        return calculateStandardError(convertToArray(values));
    }

    /**
     * Convert list of double values to array
     *
     * @param values
     * @return the array
     */
    public static double[] convertToArray(List<Double> values) {
        double[] doubles = new double[values.size()];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = values.get(i);
        }
        return doubles;
    }

}
