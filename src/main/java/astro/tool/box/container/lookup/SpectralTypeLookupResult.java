package astro.tool.box.container.lookup;

import astro.tool.box.enumeration.Color;
import java.util.Objects;

public class SpectralTypeLookupResult {

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

    // Surface gravity (log g)
    private final double logG;

    // Age
    private final String age;

    // Nearest color
    private final double nearest;

    // Gap to nearest color
    private final double gap;

    public SpectralTypeLookupResult(Color colorKey, double colorValue, String spt, int teff, double rsun, double msun, double logG, String age, double nearest, double gap) {
        this.colorKey = colorKey;
        this.colorValue = colorValue;
        this.spt = spt;
        this.teff = teff;
        this.rsun = rsun;
        this.msun = msun;
        this.logG = logG;
        this.age = age;
        this.nearest = nearest;
        this.gap = gap;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SpectralTypeLookupResult{colorKey=").append(colorKey);
        sb.append(", colorValue=").append(colorValue);
        sb.append(", spt=").append(spt);
        sb.append(", teff=").append(teff);
        sb.append(", rsun=").append(rsun);
        sb.append(", msun=").append(msun);
        sb.append(", logG=").append(logG);
        sb.append(", age=").append(age);
        sb.append(", nearest=").append(nearest);
        sb.append(", gap=").append(gap);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.colorKey);
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.colorValue) ^ (Double.doubleToLongBits(this.colorValue) >>> 32));
        hash = 29 * hash + Objects.hashCode(this.spt);
        hash = 29 * hash + this.teff;
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.rsun) ^ (Double.doubleToLongBits(this.rsun) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.msun) ^ (Double.doubleToLongBits(this.msun) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.logG) ^ (Double.doubleToLongBits(this.logG) >>> 32));
        hash = 29 * hash + Objects.hashCode(this.age);
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.nearest) ^ (Double.doubleToLongBits(this.nearest) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.gap) ^ (Double.doubleToLongBits(this.gap) >>> 32));
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
        if (Double.doubleToLongBits(this.logG) != Double.doubleToLongBits(other.logG)) {
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
        if (!Objects.equals(this.age, other.age)) {
            return false;
        }
        return this.colorKey == other.colorKey;
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

    public double getLogG() {
        return logG;
    }

    public String getAge() {
        return age;
    }

    public double getNearest() {
        return nearest;
    }

    public double getGap() {
        return gap;
    }

}
