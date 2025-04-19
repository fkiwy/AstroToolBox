package astro.tool.box.lookup;

import java.util.Objects;

import astro.tool.box.enumeration.Band;

public class DistanceLookupResult {

    // Band key
    private final Band bandKey;

    // Band value
    private final double bandValue;

    // Spectral type
    private final String spt;

    // Distance
    private final double distance;

    // Distance error
    private final double distanceError;

    public DistanceLookupResult(Band bandKey, double bandValue, String spt, double distance, double distanceError) {
        this.bandKey = bandKey;
        this.bandValue = bandValue;
        this.spt = spt;
        this.distance = distance;
        this.distanceError = distanceError;
    }

    @Override
    public String toString() {
        return "DistanceLookupResult{" + "bandKey=" + bandKey + ", bandValue=" + bandValue + ", spt=" + spt + ", distance=" + distance + ", distanceError=" + distanceError + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.bandKey);
        hash = 47 * hash + (int) (Double.doubleToLongBits(this.bandValue) ^ (Double.doubleToLongBits(this.bandValue) >>> 32));
        hash = 47 * hash + Objects.hashCode(this.spt);
        hash = 47 * hash + (int) (Double.doubleToLongBits(this.distance) ^ (Double.doubleToLongBits(this.distance) >>> 32));
        hash = 47 * hash + (int) (Double.doubleToLongBits(this.distanceError) ^ (Double.doubleToLongBits(this.distanceError) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final DistanceLookupResult other = (DistanceLookupResult) obj;
        if (Double.doubleToLongBits(this.bandValue) != Double.doubleToLongBits(other.bandValue)) {
            return false;
        }
        if (Double.doubleToLongBits(this.distance) != Double.doubleToLongBits(other.distance)) {
            return false;
        }
        if (Double.doubleToLongBits(this.distanceError) != Double.doubleToLongBits(other.distanceError)) {
            return false;
        }
        if (!Objects.equals(this.spt, other.spt)) {
            return false;
        }
        return this.bandKey == other.bandKey;
    }

    public Band getBandKey() {
        return bandKey;
    }

    public double getBandValue() {
        return bandValue;
    }

    public String getSpt() {
        return spt;
    }

    public double getDistance() {
        return distance;
    }

    public double getDistanceError() {
        return distanceError;
    }

}
