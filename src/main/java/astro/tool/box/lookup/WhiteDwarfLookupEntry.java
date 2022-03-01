package astro.tool.box.lookup;

import static astro.tool.box.function.NumericFunctions.*;
import astro.tool.box.enumeration.Color;
import java.util.HashMap;
import java.util.Map;

public class WhiteDwarfLookupEntry implements SpectralTypeLookup {

    // Effective temperature
    private final int teff;

    // Sun masses
    private final double msun;

    // Surface gravity (log g)
    private final double logG;

    // Age
    private final String age;

    // Colors
    private final Map<Color, Double> colors;

    public WhiteDwarfLookupEntry(String[] values) {
        teff = (int) toDouble(values[0]);
        logG = toDouble(values[31]);
        msun = toDouble(values[32]);
        age = values[33];
        colors = new HashMap<>();
        // 2MASS
        colors.put(Color.J_H, toDouble(values[21]));
        colors.put(Color.H_K, toDouble(values[22]));
        colors.put(Color.e_J_H, toDouble(values[21]));
        colors.put(Color.e_H_K, toDouble(values[22]));
        colors.put(Color.E_J_H, toDouble(values[21]));
        colors.put(Color.E_H_K, toDouble(values[22]));
        // SDSS
        //colors.put(Color.u_g, toDouble(values[23]));
        colors.put(Color.g_r, toDouble(values[24]));
        colors.put(Color.r_i, toDouble(values[25]));
        colors.put(Color.i_z, toDouble(values[26]));
        //colors.put(Color.e_u_g, toDouble(values[23]));
        colors.put(Color.e_g_r, toDouble(values[24]));
        colors.put(Color.e_r_i, toDouble(values[25]));
        colors.put(Color.e_i_z, toDouble(values[26]));
        //colors.put(Color.E_u_g, toDouble(values[23]));
        colors.put(Color.E_g_r, toDouble(values[24]));
        colors.put(Color.E_r_i, toDouble(values[25]));
        colors.put(Color.E_i_z, toDouble(values[26]));
        // Gaia
        colors.put(Color.G_RP, toDouble(values[29]));
        colors.put(Color.BP_RP, toDouble(values[30]));
        colors.put(Color.e_G_RP, toDouble(values[29]));
        colors.put(Color.e_BP_RP, toDouble(values[30]));
        colors.put(Color.E_G_RP, toDouble(values[29]));
        colors.put(Color.E_BP_RP, toDouble(values[30]));
    }

    @Override
    public String toString() {
        return "WhiteDwarfLookupEntry{" + "teff=" + teff + ", msun=" + msun + ", logG=" + logG + ", age=" + age + ", colors=" + colors + '}';
    }

    @Override
    public String getSpt() {
        return "";
    }

    @Override
    public int getTeff() {
        return teff;
    }

    @Override
    public double getRsun() {
        return 0;
    }

    @Override
    public double getMsun() {
        return msun;
    }

    @Override
    public double getLogG() {
        return logG;
    }

    @Override
    public String getAge() {
        return age;
    }

    @Override
    public Map<Color, Double> getColors() {
        return colors;
    }

}
