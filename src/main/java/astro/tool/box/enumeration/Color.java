package astro.tool.box.enumeration;

public enum Color {

    M_G("M_G"),
    M_RP("M_RP"),
    M_BP("M_BP"),
    G_RP("G-RP"),
    BP_RP("BP-RP"),
    BP_G("BP-G"),
    U_B("U-B"),
    B_V("B-V"),
    V_R("V-R"),
    V_I("V-I"),
    V_J("V-J"),
    R_I("R-I"),
    J_H("J-H"),
    J_K("J-K"),
    H_K("H-K"),
    K_W1("K-W1"),
    W1_W2("W1-W2"),
    W1_W3("W1-W3"),
    W1_W4("W1-W4"),
    u_g("u-g"),
    g_r("g-r"),
    r_i("r-i"),
    i_z("i-z"),
    g_r_PS1("g-r"),
    r_i_PS1("r-i"),
    i_z_PS1("i-z"),
    i_y_PS1("i-y"),
    z_y_PS1("z-y"),
    g_r_NSC("g-r"),
    r_i_NSC("r-i"),
    i_z_NSC("i-z"),
    z_Y_NSC("z-Y");

    public String val;

    private Color(String val) {
        this.val = val;
    }

}
