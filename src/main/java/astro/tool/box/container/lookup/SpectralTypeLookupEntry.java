package astro.tool.box.container.lookup;

import static astro.tool.box.function.NumericFunctions.*;
import astro.tool.box.enumeration.Color;
import java.util.HashMap;
import java.util.Map;

public class SpectralTypeLookupEntry implements MainSequenceLookup {

    // Spectral type
    private final String spt;

    // Effective temperature
    private final int teff;

    // Sun radii
    private final double rsun;

    // Sun masses
    private final double msun;

    // Colors
    private final Map<Color, Double> colors;

    public SpectralTypeLookupEntry(String[] values) {
        spt = values[0];
        teff = toInteger(values[1]);
        rsun = toDouble(values[29]);
        msun = toDouble(values[30]);
        colors = new HashMap<>();
        colors.put(Color.B_V, toDouble(values[7]));
        colors.put(Color.BP_RP, toDouble(values[10]));
        colors.put(Color.G_RP, toDouble(values[11]));
        colors.put(Color.M_G, toDouble(values[12]));
        colors.put(Color.U_B, toDouble(values[14]));
        colors.put(Color.V_R, toDouble(values[15]));
        colors.put(Color.V_I, toDouble(values[16]));
        //colors.put(Color.J_H, toDouble(values[18]));
        //colors.put(Color.H_K, toDouble(values[19]));
        colors.put(Color.K_W1, toDouble(values[20]));
        colors.put(Color.W1_W2, toDouble(values[21]));
        colors.put(Color.W1_W3, toDouble(values[22]));
        colors.put(Color.W1_W4, toDouble(values[23]));
        colors.put(Color.g_r, toDouble(values[26]));
        colors.put(Color.i_z, toDouble(values[27]));
    }

    @Override
    public String toString() {
        return "SpectralTypeLookupEntry{" + "spt=" + spt + ", teff=" + teff + ", rsun=" + rsun + ", msun=" + msun + ", colors=" + colors + '}';
    }

    @Override
    public String getSpt() {
        return spt;
    }

    @Override
    public int getTeff() {
        return teff;
    }

    @Override
    public double getRsun() {
        return rsun;
    }

    @Override
    public double getMsun() {
        return msun;
    }

    @Override
    public double getLogG() {
        return 0;
    }

    @Override
    public String getAge() {
        return "";
    }

    @Override
    public Map<Color, Double> getColors() {
        return colors;
    }

}
