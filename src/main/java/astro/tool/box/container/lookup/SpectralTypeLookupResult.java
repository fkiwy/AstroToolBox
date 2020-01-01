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

    // Nearest color
    private final double nearest;

    // Gap to nearest color
    private final double gap;

    public SpectralTypeLookupResult(String spt, int teff, double rsun, double msun, double nearest, double gap) {
        this.spt = spt;
        this.teff = teff;
        this.rsun = rsun;
        this.msun = msun;
        this.nearest = nearest;
        this.gap = gap;
    }

    @Override
    public String toString() {
        return "SpectralTypeLookupResult{" + "spt=" + spt + ", teff=" + teff + ", rsun=" + rsun + ", msun=" + msun + ", nearest=" + nearest + ", gap=" + gap + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.spt);
        hash = 59 * hash + this.teff;
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.rsun) ^ (Double.doubleToLongBits(this.rsun) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.msun) ^ (Double.doubleToLongBits(this.msun) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.nearest) ^ (Double.doubleToLongBits(this.nearest) >>> 32));
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.gap) ^ (Double.doubleToLongBits(this.gap) >>> 32));
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
        if (Double.doubleToLongBits(this.nearest) != Double.doubleToLongBits(other.nearest)) {
            return false;
        }
        if (Double.doubleToLongBits(this.gap) != Double.doubleToLongBits(other.gap)) {
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

    public double getNearest() {
        return nearest;
    }

    public double getGap() {
        return gap;
    }

}
