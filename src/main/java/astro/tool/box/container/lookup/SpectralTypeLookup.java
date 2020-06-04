package astro.tool.box.container.lookup;

import astro.tool.box.enumeration.Color;
import java.util.Map;

public interface SpectralTypeLookup {

    String getSpt();

    int getTeff();

    double getRsun();

    double getMsun();

    double getLogG();

    String getAge();

    Map<Color, Double> getColors();

}
