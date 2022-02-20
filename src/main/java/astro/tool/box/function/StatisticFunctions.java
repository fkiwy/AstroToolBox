package astro.tool.box.function;

import static java.lang.Math.*;
import java.util.Arrays;
import java.util.List;

public class StatisticFunctions {

    /**
     * Determine the median of a set of values
     *
     * @param values
     * @return the median
     */
    public static double determineMedian(List<Double> values) {
        if (values.size() % 2 == 0) {
            return (values.get(values.size() / 2) + values.get(values.size() / 2 - 1)) / 2;
        } else {
            return values.get(values.size() / 2);
        }
    }

    /**
     * Calculate the mean of a set of values
     *
     * @param values
     * @return the mean
     */
    public static double calculateMean(double... values) {
        return Arrays.stream(values).average().getAsDouble();
    }

    public static double calculateMean(List<Double> values) {
        return calculateMean(convertToArray(values));
    }

    /**
     * Calculate the standard deviation of a population
     *
     * @param values
     * @return the standard deviation of a population
     */
    public static double calculateStandardDeviation(double... values) {
        double mean = calculateMean(values);
        double variance = Arrays.stream(values).map(v -> pow(v - mean, 2)).average().getAsDouble();
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
