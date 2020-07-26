package astro.tool.box.container.lookup;

import astro.tool.box.enumeration.Band;
import java.util.Objects;

public class DistanceLookupResult {

    // Band key
    private final Band bandKey;

    // Band value
    private final double bandValue;

    // Spectral type
    private final String spt;

    // Distance
    private final double distance;

    public DistanceLookupResult(Band colorKey, double colorValue, String spt, double distance) {
        this.bandKey = colorKey;
        this.bandValue = colorValue;
        this.spt = spt;
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "DistanceLookupResult{" + "bandKey=" + bandKey + ", bandValue=" + bandValue + ", spt=" + spt + ", distance=" + distance + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.bandKey);
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.bandValue) ^ (Double.doubleToLongBits(this.bandValue) >>> 32));
        hash = 17 * hash + Objects.hashCode(this.spt);
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.distance) ^ (Double.doubleToLongBits(this.distance) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DistanceLookupResult other = (DistanceLookupResult) obj;
        if (Double.doubleToLongBits(this.bandValue) != Double.doubleToLongBits(other.bandValue)) {
            return false;
        }
        if (Double.doubleToLongBits(this.distance) != Double.doubleToLongBits(other.distance)) {
            return false;
        }
        if (!Objects.equals(this.spt, other.spt)) {
            return false;
        }
        if (this.bandKey != other.bandKey) {
            return false;
        }
        return true;
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

}
