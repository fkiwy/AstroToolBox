package astro.tool.box.lookup;

import java.util.Map;

import astro.tool.box.enumeration.Color;

public interface SpectralTypeLookup {

    String getSpt();

    int getTeff();

    double getRsun();

    double getMsun();

    Map<Color, Double> getColors();

}
