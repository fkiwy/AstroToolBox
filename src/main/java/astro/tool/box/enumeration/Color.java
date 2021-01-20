package astro.tool.box.enumeration;

public enum Color {

    M_G("MGmag"),
    G_RP("G-RP"),
    BP_RP("BP-RP"),
    U_B("U-B"),
    B_V("B-V"),
    V_R("V-R"),
    V_I("V-I"),
    V_J("V-J"),
    R_I("R-I"),
    J_H("J-H"),
    J_K("J-K"),
    J_W2("J-W2"),
    H_K("H-K"),
    K_W1("K-W1"),
    W1_W2("W1-W2"),
    W1_W3("W1-W3"),
    W1_W4("W1-W4"),
    W2_W3("W2-W3"),
    CH1_CH2("CH1-CH2"),
    u_g("u-g"),
    g_r("g-r"),
    r_i("r-i"),
    r_J("r-J"),
    i_z("i-z"),
    i_y("i-y"),
    z_y("z-y"),
    z_Y("z-Y");

    public String val;

    private Color(String val) {
        this.val = val;
    }

}
