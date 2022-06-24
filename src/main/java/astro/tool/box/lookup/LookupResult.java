package astro.tool.box.lookup;

import astro.tool.box.enumeration.Color;
import java.util.Objects;

public class LookupResult {

    // Color key
    private final Color colorKey;

    // Color value
    private final double colorValue;

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

    // Numeric spectral type
    private Double sptNum;

    public LookupResult(Color colorKey, double colorValue, String spt, int teff, double rsun, double msun, double nearest, double gap) {
        this.colorKey = colorKey;
        this.colorValue = colorValue;
        this.spt = spt;
        this.teff = teff;
        this.rsun = rsun;
        this.msun = msun;
        this.nearest = nearest;
        this.gap = gap;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LookupResult{colorKey=").append(colorKey);
        sb.append(", colorValue=").append(colorValue);
        sb.append(", spt=").append(spt);
        sb.append(", teff=").append(teff);
        sb.append(", rsun=").append(rsun);
        sb.append(", msun=").append(msun);
        sb.append(", nearest=").append(nearest);
        sb.append(", gap=").append(gap);
        sb.append(", sptNum=").append(sptNum);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.colorKey);
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.colorValue) ^ (Double.doubleToLongBits(this.colorValue) >>> 32));
        hash = 17 * hash + Objects.hashCode(this.spt);
        hash = 17 * hash + this.teff;
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.rsun) ^ (Double.doubleToLongBits(this.rsun) >>> 32));
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.msun) ^ (Double.doubleToLongBits(this.msun) >>> 32));
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.nearest) ^ (Double.doubleToLongBits(this.nearest) >>> 32));
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.gap) ^ (Double.doubleToLongBits(this.gap) >>> 32));
        hash = 17 * hash + Objects.hashCode(this.sptNum);
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
        final LookupResult other = (LookupResult) obj;
        if (Double.doubleToLongBits(this.colorValue) != Double.doubleToLongBits(other.colorValue)) {
            return false;
        }
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
        if (!Objects.equals(this.spt, other.spt)) {
            return false;
        }
        if (this.colorKey != other.colorKey) {
            return false;
        }
        return Objects.equals(this.sptNum, other.sptNum);
    }

    public Color getColorKey() {
        return colorKey;
    }

    public double getColorValue() {
        return colorValue;
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

    public Double getSptNum() {
        return sptNum;
    }

    public void setSptNum(Double sptNum) {
        this.sptNum = sptNum;
    }

}
