package astro.tool.box.function;

import static java.lang.Math.*;
import java.util.Arrays;

public class StatisticFunctions {

    /**
     * Calculate mean of a set of values
     *
     * @param values
     * @return the mean
     */
    public static double calculateMean(double... values) {
        return Arrays.stream(values).average().getAsDouble();
    }

    /**
     * Calculate standard deviation of a population
     *
     * @param values
     * @return the standard deviation of a population
     */
    public static double calculateStandardDeviation(double... values) {
        double mean = calculateMean(values);
        double variance = Arrays.stream(values).map(v -> pow(v - mean, 2)).average().getAsDouble();
        return sqrt(variance);
    }

    /**
     * Calculate standard error of the mean
     *
     * @param values
     * @return the standard error of the mean
     */
    public static double calculateStandardError(double... values) {
        double standardDeviation = calculateStandardDeviation(values);
        return standardDeviation / sqrt(values.length);
    }

}
