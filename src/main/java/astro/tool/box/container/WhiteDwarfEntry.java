package astro.tool.box.container;

import java.util.Map;

import astro.tool.box.enumeration.Band;

public record WhiteDwarfEntry(String type, int teff, double logG, double mass, String age, Map<Band, Double> bands) {

	public String getInfo() {
		return type + " Teff=" + teff + " log g=" + logG + " Mass=" + mass + " Age=" + age;
	}

}
