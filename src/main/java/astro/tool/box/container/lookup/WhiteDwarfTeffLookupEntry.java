package astro.tool.box.container.lookup;

import static astro.tool.box.function.NumericFunctions.*;
import astro.tool.box.enumeration.Color;
import java.util.HashMap;
import java.util.Map;

public class WhiteDwarfTeffLookupEntry implements WhiteDwarfLookup {

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

    public WhiteDwarfTeffLookupEntry(String[] values) {
        teff = (int) toDouble(values[0]);
        logG = toDouble(values[15]);
        msun = toDouble(values[16]);
        age = values[17];
        colors = new HashMap<>();
        //colors.put(Color.U_B, toDouble(values[1]));
        colors.put(Color.B_V, toDouble(values[2]));
        //colors.put(Color.V_R, toDouble(values[3]));
        //colors.put(Color.R_I, toDouble(values[4]));
        //colors.put(Color.J_H, toDouble(values[5]));
        //colors.put(Color.H_K, toDouble(values[6]));
        //colors.put(Color.u_g, toDouble(values[7]));
        colors.put(Color.g_r, toDouble(values[8]));
        colors.put(Color.r_i, toDouble(values[9]));
        colors.put(Color.i_z, toDouble(values[10]));
        colors.put(Color.V_J, toDouble(values[11]));
        colors.put(Color.G_RP, toDouble(values[13]));
        colors.put(Color.BP_RP, toDouble(values[14]));
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
