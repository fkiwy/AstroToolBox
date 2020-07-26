package astro.tool.box.container.lookup;

import static astro.tool.box.function.NumericFunctions.*;
import astro.tool.box.enumeration.Color;
import java.util.HashMap;
import java.util.Map;

public class WhiteDwarfAgeLookupEntry implements WhiteDwarfLookup {

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

    public WhiteDwarfAgeLookupEntry(String[] values) {
        teff = toInteger(values[0]);
        logG = toDouble(values[1]);
        msun = toDouble(values[2]);
        age = values[40];
        colors = new HashMap<>();
        //colors.put(Color.U_B, toDouble(values[5]) - toDouble(values[6]));
        colors.put(Color.B_V, toDouble(values[6]) - toDouble(values[7]));
        //colors.put(Color.V_R, toDouble(values[7]) - toDouble(values[8]));
        //colors.put(Color.R_I, toDouble(values[8]) - toDouble(values[9]));
        //colors.put(Color.J_H, toDouble(values[10]) - toDouble(values[11]));
        //colors.put(Color.H_K, toDouble(values[11]) - toDouble(values[12]));
        //colors.put(Color.u_g, toDouble(values[26]) - toDouble(values[27]));
        colors.put(Color.g_r, toDouble(values[27]) - toDouble(values[28]));
        colors.put(Color.r_i, toDouble(values[28]) - toDouble(values[29]));
        //colors.put(Color.i_z, toDouble(values[29]) - toDouble(values[30]));
        colors.put(Color.V_J, toDouble(values[7]) - toDouble(values[10]));
        colors.put(Color.r_J, toDouble(values[28]) - toDouble(values[10]));
        colors.put(Color.G_RP, toDouble(values[36]) - toDouble(values[38]));
        colors.put(Color.BP_RP, toDouble(values[37]) - toDouble(values[38]));
    }

    @Override
    public String toString() {
        return "WhiteDwarfAgeLookupEntry{" + "teff=" + teff + ", msun=" + msun + ", logG=" + logG + ", age=" + age + ", colors=" + colors + '}';
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
