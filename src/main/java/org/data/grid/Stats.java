package org.data.grid;

import java.util.List;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import static org.data.grid.util.StatsUtils.convertToArray;

public class Stats {

    private final String columnName;
    private double geometricMean;
    private double kurtosis;
    private double max;
    private double mean;
    private double median;
    private double min;
    private double N;
    private double populationVariance;
    private double quadraticMean;
    private double skewness;
    private double standardDeviation;
    private double sum;
    private double sumsq;
    private double variance;

    public Stats(String columnName, List<Double> values) {
        this.columnName = columnName;
        DescriptiveStatistics ds = new DescriptiveStatistics(convertToArray(values));
        setGeometricMean(ds.getGeometricMean());
        setKurtosis(ds.getKurtosis());
        setMax(ds.getMax());
        setMean(ds.getMean());
        setMedian(ds.getPercentile(50));
        setMin(ds.getMin());
        setN(ds.getN());
        setPopulationVariance(ds.getPopulationVariance());
        setQuadraticMean(ds.getQuadraticMean());
        setSkewness(ds.getSkewness());
        setStandardDeviation(ds.getStandardDeviation());
        setSum(ds.getSum());
        setSumsq(ds.getSumsq());
        setVariance(ds.getVariance());
    }

    public void print() {
        System.out.println("========================================");
        System.out.println("Statistics for column: " + columnName);
        System.out.println("----------------------------------------");
        System.out.println("Geometric mean .... = " + formatDouble(getGeometricMean()));
        System.out.println("Kurtosis .......... = " + formatDouble(getKurtosis()));
        System.out.println("Max ............... = " + formatDouble(getMax()));
        System.out.println("Mean .............. = " + formatDouble(getMean()));
        System.out.println("Median ............ = " + formatDouble(getMedian()));
        System.out.println("Min ............... = " + formatDouble(getMin()));
        System.out.println("N ................. = " + formatDouble(getN()));
        System.out.println("Population variance = " + formatDouble(getPopulationVariance()));
        System.out.println("Quadratic mean .... = " + formatDouble(getQuadraticMean()));
        System.out.println("Skewness .......... = " + formatDouble(getSkewness()));
        System.out.println("Standard deviation  = " + formatDouble(getStandardDeviation()));
        System.out.println("Sum ............... = " + formatDouble(getSum()));
        System.out.println("Sumsq ............. = " + formatDouble(getSumsq()));
        System.out.println("Variance .......... = " + formatDouble(getVariance()));
        System.out.println("========================================");
    }

    public static String formatDouble(double number) {
        return String.format("%15.6f", number).replaceAll("0+$", " ");
    }

    public double getGeometricMean() {
        return geometricMean;
    }

    public void setGeometricMean(double geometricMean) {
        this.geometricMean = geometricMean;
    }

    public double getKurtosis() {
        return kurtosis;
    }

    public void setKurtosis(double kurtosis) {
        this.kurtosis = kurtosis;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getMedian() {
        return median;
    }

    public void setMedian(double median) {
        this.median = median;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getN() {
        return N;
    }

    public void setN(double N) {
        this.N = N;
    }

    public double getPopulationVariance() {
        return populationVariance;
    }

    public void setPopulationVariance(double populationVariance) {
        this.populationVariance = populationVariance;
    }

    public double getQuadraticMean() {
        return quadraticMean;
    }

    public void setQuadraticMean(double quadraticMean) {
        this.quadraticMean = quadraticMean;
    }

    public double getSkewness() {
        return skewness;
    }

    public void setSkewness(double skewness) {
        this.skewness = skewness;
    }

    public double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public double getSumsq() {
        return sumsq;
    }

    public void setSumsq(double sumsq) {
        this.sumsq = sumsq;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(double variance) {
        this.variance = variance;
    }

}
