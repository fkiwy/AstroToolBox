package astro.tool.box.container.lookup;

import static astro.tool.box.function.NumericFunctions.*;
import astro.tool.box.enumeration.Color;
import java.util.HashMap;
import java.util.Map;

public class BrownDwarfLookupEntry implements SpectralTypeLookup {

    // Spectral type
    private final String spt;

    // Colors
    private final Map<Color, Double> colors;

    public BrownDwarfLookupEntry(String[] values) {
        spt = values[0];
        colors = new HashMap<>();
        colors.put(Color.M_G, toDouble(values[11]));
        colors.put(Color.g_r, toDouble(values[12]));
        colors.put(Color.r_i, toDouble(values[13]));
        //colors.put(Color.i_z, toDouble(values[14]));
        //colors.put(Color.z_y, toDouble(values[15]));
        colors.put(Color.J_H, toDouble(values[16]));
        colors.put(Color.H_K, toDouble(values[17]));
        colors.put(Color.J_K, toDouble(values[18]));
        colors.put(Color.W1_W2, toDouble(values[19]));
        colors.put(Color.J_W2, toDouble(values[20]));
    }

    @Override
    public String toString() {
        return "BrownDwarfLookupEntry{" + "spt=" + spt + ", colors=" + colors + '}';
    }

    @Override
    public String getSpt() {
        return spt;
    }

    @Override
    public int getTeff() {
        return 0;
    }

    @Override
    public double getRsun() {
        return 0;
    }

    @Override
    public double getMsun() {
        return 0;
    }

    @Override
    public Map<Color, Double> getColors() {
        return colors;
    }

}
