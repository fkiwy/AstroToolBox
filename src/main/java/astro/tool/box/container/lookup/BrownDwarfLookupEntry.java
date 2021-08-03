package astro.tool.box.container.lookup;

import astro.tool.box.enumeration.Band;
import static astro.tool.box.function.NumericFunctions.*;
import astro.tool.box.enumeration.Color;
import java.util.HashMap;
import java.util.Map;

public class BrownDwarfLookupEntry implements MainSequenceLookup {

    // Spectral type
    private final String spt;

    // Bands
    private final Map<Band, Double> bands;

    // Colors
    private final Map<Color, Double> colors;

    public BrownDwarfLookupEntry(String[] values) {
        spt = values[0];
        bands = new HashMap<>();
        bands.put(Band.g, toDouble(values[1]));
        bands.put(Band.r, toDouble(values[2]));
        bands.put(Band.i, toDouble(values[3]));
        bands.put(Band.z, toDouble(values[4]));
        bands.put(Band.y, toDouble(values[5]));
        bands.put(Band.J, toDouble(values[6]));
        bands.put(Band.H, toDouble(values[7]));
        bands.put(Band.K, toDouble(values[8]));
        bands.put(Band.W1, toDouble(values[9]));
        bands.put(Band.W2, toDouble(values[10]));
        bands.put(Band.W3, toDouble(values[11]));
        bands.put(Band.BP, toDouble(values[36]));
        bands.put(Band.G, toDouble(values[37]));
        bands.put(Band.RP, toDouble(values[38]));
        colors = new HashMap<>();
        colors.put(Color.g_r_PS1, toDouble(values[12]));
        colors.put(Color.r_i_PS1, toDouble(values[13]));
        colors.put(Color.i_z_PS1, toDouble(values[14]));
        colors.put(Color.i_y_PS1, toDouble(values[22]));
        colors.put(Color.z_y_PS1, toDouble(values[15]));
        colors.put(Color.J_H, toDouble(values[17]));
        colors.put(Color.H_K, toDouble(values[18]));
        colors.put(Color.K_W1, toDouble(values[19]));
        colors.put(Color.W1_W2, toDouble(values[20]));
        colors.put(Color.J_K, toDouble(values[25]));
        colors.put(Color.i_z_DES, toDouble(values[27]));
        colors.put(Color.z_Y_DES, toDouble(values[28]));
        colors.put(Color.i_z, toDouble(values[30]));
        colors.put(Color.z_Y, toDouble(values[31]));
        colors.put(Color.G_RP, toDouble(values[33]));
        colors.put(Color.BP_RP, toDouble(values[34]));
        colors.put(Color.BP_G, toDouble(values[35]));
        colors.put(Color.M_BP, toDouble(values[36]));
        colors.put(Color.M_G, toDouble(values[37]));
        colors.put(Color.M_RP, toDouble(values[38]));
    }

    @Override
    public String toString() {
        return "BrownDwarfLookupEntry{" + "spt=" + spt + ", bands=" + bands + ", colors=" + colors + '}';
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
    public double getLogG() {
        return 0;
    }

    @Override
    public String getAge() {
        return "";
    }

    public Map<Band, Double> getBands() {
        return bands;
    }

    @Override
    public Map<Color, Double> getColors() {
        return colors;
    }

}
