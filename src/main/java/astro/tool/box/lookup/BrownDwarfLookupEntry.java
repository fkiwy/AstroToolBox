package astro.tool.box.lookup;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.tab.SettingsTab.*;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import java.util.HashMap;
import java.util.Map;

public class BrownDwarfLookupEntry implements MainSequenceLookup {

    // Spectral type
    private final String spt;

    // Absolute magnitudes
    private final Map<Band, Double> magnitudes;

    // Errors on absolute magnitudes
    private final Map<Band, Double> errors;

    // Colors
    private final Map<Color, Double> colors;

    public BrownDwarfLookupEntry(String[] values) {
        spt = values[0];

        //------------------------------
        // Absolute magnitudes
        //------------------------------
        magnitudes = new HashMap<>();
        magnitudes.put(Band.g, toDouble(values[1]));
        magnitudes.put(Band.r, toDouble(values[2]));
        magnitudes.put(Band.i, toDouble(values[3]));
        magnitudes.put(Band.z, toDouble(values[4]));
        magnitudes.put(Band.y, toDouble(values[5]));
        magnitudes.put(Band.J, toDouble(values[6]));
        magnitudes.put(Band.H, toDouble(values[7]));
        magnitudes.put(Band.K, toDouble(values[8]));
        magnitudes.put(Band.W1, toDouble(values[9]));
        magnitudes.put(Band.W2, toDouble(values[10]));
        magnitudes.put(Band.W3, toDouble(values[11]));
        magnitudes.put(Band.BP, toDouble(values[36]));
        magnitudes.put(Band.G, toDouble(values[37]));
        magnitudes.put(Band.RP, toDouble(values[38]));

        //------------------------------
        // Colors
        //------------------------------
        colors = new HashMap<>();

        // PS1
        colors.put(Color.g_r_PS1, toDouble(values[12]));
        colors.put(Color.r_i_PS1, toDouble(values[13]));
        colors.put(Color.i_z_PS1, toDouble(values[14]));
        colors.put(Color.z_y_PS1, toDouble(values[15]));
        colors.put(Color.i_y_PS1, toDouble(values[22]));

        // 2MASS
        colors.put(Color.J_H, toDouble(values[17]));
        colors.put(Color.H_K, toDouble(values[18]));
        //colors.put(Color.J_K, toDouble(values[25]));

        // WISE
        colors.put(Color.K_W1, toDouble(values[19]));
        colors.put(Color.W1_W2, toDouble(values[20]));

        // NSC
        colors.put(Color.i_z_NSC, toDouble(values[27]));
        colors.put(Color.z_Y_NSC, toDouble(values[28]));

        // DES
        colors.put(Color.i_z_DES, toDouble(values[27]));
        colors.put(Color.z_Y_DES, toDouble(values[28]));

        // SDSS
        colors.put(Color.i_z, toDouble(values[30]));

        // Gaia
        colors.put(Color.G_RP, toDouble(values[33]));
        colors.put(Color.BP_RP, toDouble(values[34]));
        colors.put(Color.BP_G, toDouble(values[35]));
        colors.put(Color.M_BP, toDouble(values[36]));
        colors.put(Color.M_G, toDouble(values[37]));
        colors.put(Color.M_RP, toDouble(values[38]));

        boolean photometricErrors = Boolean.parseBoolean(getUserSetting(PHOTOMETRIC_ERRORS, "false"));
        if (photometricErrors) {
            // PS1
            colors.put(Color.e_g_r_PS1, toDouble(values[12]));
            colors.put(Color.e_r_i_PS1, toDouble(values[13]));
            colors.put(Color.e_i_z_PS1, toDouble(values[14]));
            colors.put(Color.e_z_y_PS1, toDouble(values[15]));
            colors.put(Color.e_i_y_PS1, toDouble(values[22]));
            colors.put(Color.E_g_r_PS1, toDouble(values[12]));
            colors.put(Color.E_r_i_PS1, toDouble(values[13]));
            colors.put(Color.E_i_z_PS1, toDouble(values[14]));
            colors.put(Color.E_z_y_PS1, toDouble(values[15]));
            colors.put(Color.E_i_y_PS1, toDouble(values[22]));

            // 2MASS
            colors.put(Color.e_J_H, toDouble(values[17]));
            colors.put(Color.e_H_K, toDouble(values[18]));
            //colors.put(Color.e_J_K, toDouble(values[25]));
            colors.put(Color.E_J_H, toDouble(values[17]));
            colors.put(Color.E_H_K, toDouble(values[18]));
            //colors.put(Color.E_J_K, toDouble(values[25]));

            // WISE
            colors.put(Color.e_K_W1, toDouble(values[19]));
            colors.put(Color.e_W1_W2, toDouble(values[20]));
            colors.put(Color.E_K_W1, toDouble(values[19]));
            colors.put(Color.E_W1_W2, toDouble(values[20]));

            // NSC
            colors.put(Color.e_i_z_NSC, toDouble(values[27]));
            colors.put(Color.e_z_Y_NSC, toDouble(values[28]));
            colors.put(Color.E_i_z_NSC, toDouble(values[27]));
            colors.put(Color.E_z_Y_NSC, toDouble(values[28]));

            // DES
            colors.put(Color.e_i_z_DES, toDouble(values[27]));
            colors.put(Color.e_z_Y_DES, toDouble(values[28]));
            colors.put(Color.E_i_z_DES, toDouble(values[27]));
            colors.put(Color.E_z_Y_DES, toDouble(values[28]));

            // SDSS
            colors.put(Color.e_i_z, toDouble(values[30]));
            colors.put(Color.E_i_z, toDouble(values[30]));

            // Gaia
            colors.put(Color.e_G_RP, toDouble(values[33]));
            colors.put(Color.e_BP_RP, toDouble(values[34]));
            colors.put(Color.e_BP_G, toDouble(values[35]));
            colors.put(Color.e_M_BP, toDouble(values[36]));
            colors.put(Color.e_M_G, toDouble(values[37]));
            colors.put(Color.e_M_RP, toDouble(values[38]));
            colors.put(Color.E_G_RP, toDouble(values[33]));
            colors.put(Color.E_BP_RP, toDouble(values[34]));
            colors.put(Color.E_BP_G, toDouble(values[35]));
            colors.put(Color.E_M_BP, toDouble(values[36]));
            colors.put(Color.E_M_G, toDouble(values[37]));
            colors.put(Color.E_M_RP, toDouble(values[38]));
        }

        //------------------------------
        // Errors of absolute magnitude
        //------------------------------
        errors = new HashMap<>();
        errors.put(Band.g, toDouble(values[39]));
        errors.put(Band.r, toDouble(values[40]));
        errors.put(Band.i, toDouble(values[41]));
        errors.put(Band.z, toDouble(values[42]));
        errors.put(Band.y, toDouble(values[43]));
        errors.put(Band.J, toDouble(values[44]));
        errors.put(Band.H, toDouble(values[45]));
        errors.put(Band.K, toDouble(values[46]));
        errors.put(Band.W1, toDouble(values[47]));
        errors.put(Band.W2, toDouble(values[48]));
        errors.put(Band.W3, toDouble(values[49]));
        errors.put(Band.BP, toDouble(values[50]));
        errors.put(Band.G, toDouble(values[51]));
        errors.put(Band.RP, toDouble(values[52]));
    }

    @Override
    public String toString() {
        return "BrownDwarfLookupEntry{" + "spt=" + spt + ", bands=" + magnitudes + ", errors=" + errors + ", colors=" + colors + '}';
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

    public Map<Band, Double> getMagnitudes() {
        return magnitudes;
    }

    public Map<Band, Double> getErrors() {
        return errors;
    }

    @Override
    public Map<Color, Double> getColors() {
        return colors;
    }

}
