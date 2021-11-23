package astro.tool.box.container;

import astro.tool.box.enumeration.Band;
import java.util.Map;

public class WhiteDwarfEntry {

    private final String type;

    private final int teff;

    private final double logG;

    private final double mass;

    private final String age;

    private final Map<Band, Double> bands;

    public WhiteDwarfEntry(String type, int teff, double logG, double mass, String age, Map<Band, Double> bands) {
        this.type = type;
        this.teff = teff;
        this.logG = logG;
        this.mass = mass;
        this.age = age;
        this.bands = bands;
    }

    public String getInfo() {
        return type + " Teff=" + teff + " log g=" + logG + " Mass=" + mass + " Age=" + age;
    }

    public String getType() {
        return type;
    }

    public int getTeff() {
        return teff;
    }

    public double getLogG() {
        return logG;
    }

    public double getMass() {
        return mass;
    }

    public String getAge() {
        return age;
    }

    public Map<Band, Double> getBands() {
        return bands;
    }

}
