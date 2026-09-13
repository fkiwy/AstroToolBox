package astro.tool.box.function;

import astro.tool.box.enumeration.StatType;

import java.util.*;
import java.util.stream.Collectors;

import static astro.tool.box.function.NumericFunctions.roundTo2Dec;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class StatisticFunctions {

	private StatisticFunctions() {
		/* This utility class should not be instantiated */
	}

	/**
	 * Remove outliers from a list of values using sigma clipping
	 *
	 * @param values
	 * @param numberOfStds (to multiply with standard deviation)
	 * @param statType     (StatType.MEAN or StatType.MEDIAN)
	 * @return the sanitized list
	 */
	public static List<Double> removeOutliers(List<Double> values, double numberOfStds, StatType statType) {
		double avg;
		if (StatType.MEAN.equals(statType)) {
			avg = calculateMean(values);
		} else {
			avg = determineMedian(values);
		}
		double std = calculateStandardDeviation(values);
		return values.stream().filter(val -> isNoOutlier(val, avg, std * numberOfStds)).collect(Collectors.toList());
	}

	public static List<List<Double>> removeOutliers(List<List<Double>> table, int columnIndex, double numberOfStds,
	                                                StatType statType) {
		List<Double> values = table.stream().map(v -> v.get(columnIndex)).collect(Collectors.toList());
		double avg;
		if (StatType.MEAN.equals(statType)) {
			avg = calculateMean(values);
		} else {
			avg = determineMedian(values);
		}
		double std = calculateStandardDeviation(values);
		return table.stream().filter(v -> isNoOutlier(v.get(columnIndex), avg, std * numberOfStds))
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
			throw new IllegalArgumentException("Low percentile (=%s) out of range. Must be between 0 and 100."
					.formatted(roundTo2Dec(lowPercentile)));
		}
		if (highPercentile < 0 || highPercentile > 100) {
			throw new IllegalArgumentException("High percentile (=%s) out of range. Must be between 0 and 100."
					.formatted(roundTo2Dec(highPercentile)));
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
	 * Calculate the median absolute deviation (MAD) of the given values
	 *
	 * @param values
	 * @return the median absolute deviation
	 */
	public static double medianAbsoluteDeviation(List<Double> values) {
		if (values.isEmpty()) {
			return 0;
		}
		double median = determineMedian(values);
		List<Double> deviations = new ArrayList<>(values.size());
		for (double value : values) {
			deviations.add(Math.abs(value - median));
		}
		return determineMedian(deviations);
	}

	/**
	 * Determine the median of the given values
	 *
	 * @param values
	 * @return the median
	 */
	public static double determineMedian(List<Double> values) {
		if (values.isEmpty()) {
			return 0;
		}
		List<Double> sorted = new ArrayList<>(values);
		Collections.sort(sorted);
		int n = sorted.size();
		if (n % 2 == 0) {
			return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
		} else {
			return sorted.get(n / 2);
		}
	}

	/**
	 * Calculate the mean of the given values
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
	 * Calculate the quadrature of the given values
	 *
	 * @param values
	 * @return the quadrature
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
	 * Calculate the standard deviation of the given values
	 *
	 * @param values
	 * @return the standard deviation
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
		double[] array = new double[values.size()];
		int i = 0;
		for (Double value : values) {
			array[i++] = value;
		}
		return array;
	}

	public static double[] getMinMax(double[] data) {
		return getMinMax(data, 0.1, 99.0);
	}

	public static double[] getMinMax(double[] data, double lo, double hi) {
		double med = nanMedian(data);

		double[] absDiff = new double[data.length];
		for (int i = 0; i < data.length; i++) {
			if (Double.isNaN(data[i])) {
				absDiff[i] = Double.NaN;
			} else {
				absDiff[i] = Math.abs(data[i] - med);
			}
		}

		double mad = nanMedian(absDiff);

		double dev = nanPercentile(data, hi) - nanPercentile(data, lo);

		double min = med - 2.0 * mad;
		double max = med + 2.0 * dev;

		return new double[]{min, max};
	}

	private static double nanMedian(double[] data) {
		double[] values = Arrays.stream(data)
				.filter(d -> !Double.isNaN(d))
				.sorted()
				.toArray();

		if (values.length == 0) {
			return Double.NaN;
		}

		int mid = values.length / 2;
		if (values.length % 2 == 0) {
			return (values[mid - 1] + values[mid]) / 2.0;
		} else {
			return values[mid];
		}
	}

	private static double nanPercentile(double[] data, double percentile) {
		double[] values = Arrays.stream(data)
				.filter(d -> !Double.isNaN(d))
				.sorted()
				.toArray();

		if (values.length == 0) {
			return Double.NaN;
		}

		if (percentile <= 0) {
			return values[0];
		}
		if (percentile >= 100) {
			return values[values.length - 1];
		}

		double index = percentile / 100.0 * (values.length - 1);
		int lower = (int) Math.floor(index);
		int upper = (int) Math.ceil(index);

		if (lower == upper) {
			return values[lower];
		}

		double weight = index - lower;
		return values[lower] * (1.0 - weight) + values[upper] * weight;
	}

}
