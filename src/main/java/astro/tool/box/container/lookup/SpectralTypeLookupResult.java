package astro.tool.box.container.lookup;

import java.util.Objects;

public class SpectralTypeLookupResult {

    // Spectral type
    private final String spt;

    // Effective temperature
    private final int teff;

    // Sun radii
    private final double rsun;

    // Sun masses
    private final double msun;

    public SpectralTypeLookupResult(String spt, int teff, double rsun, double msun) {
        this.spt = spt;
        this.teff = teff;
        this.rsun = rsun;
        this.msun = msun;
    }

    @Override
    public String toString() {
        return "SpectralTypeLookupResult{" + "spt=" + spt + ", teff=" + teff + ", rsun=" + rsun + ", msun=" + msun + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.spt);
        hash = 79 * hash + this.teff;
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.rsun) ^ (Double.doubleToLongBits(this.rsun) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.msun) ^ (Double.doubleToLongBits(this.msun) >>> 32));
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
        final SpectralTypeLookupResult other = (SpectralTypeLookupResult) obj;
        if (this.teff != other.teff) {
            return false;
        }
        if (Double.doubleToLongBits(this.rsun) != Double.doubleToLongBits(other.rsun)) {
            return false;
        }
        if (Double.doubleToLongBits(this.msun) != Double.doubleToLongBits(other.msun)) {
            return false;
        }
        return Objects.equals(this.spt, other.spt);
    }

    public String getSpt() {
        return spt;
    }

    public int getTeff() {
        return teff;
    }

    public double getRsun() {
        return rsun;
    }

    public double getMsun() {
        return msun;
    }

}
