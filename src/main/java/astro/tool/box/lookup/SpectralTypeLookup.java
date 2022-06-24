package astro.tool.box.lookup;

import astro.tool.box.enumeration.Color;
import java.util.Map;

public interface SpectralTypeLookup {

    String getSpt();

    int getTeff();

    double getRsun();

    double getMsun();

    Map<Color, Double> getColors();

}
