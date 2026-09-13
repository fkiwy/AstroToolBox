package astro.tool.box.container;

import astro.tool.box.enumeration.Band;

import java.util.Map;

public record WhiteDwarfEntry(String type, int teff, double logG, double mass, String age, Map<Band, Double> bands) {

	public String getInfo() {
		return type + " Teff=" + teff + " log g=" + logG + " Mass=" + mass + " Age=" + age;
	}

}
