package astro.tool.box.enumeration;

public enum Color {

	// Gaia
	M_G("M_G"), M_RP("M_RP"), M_BP("M_BP"), G_RP("G-RP"), BP_RP("BP-RP"), BP_G("BP-G"), e_M_G("M_G (-err)"),
	e_M_RP("M_RP (-err)"), e_M_BP("M_BP (-err)"), e_G_RP("G-RP (-err)"), e_BP_RP("BP-RP (-err)"), e_BP_G("BP-G (-err)"),
	E_M_G("M_G (+err)"), E_M_RP("M_RP (+err)"), E_M_BP("M_BP (+err)"), E_G_RP("G-RP (+err)"), E_BP_RP("BP-RP (+err)"),
	E_BP_G("BP-G (+err)"),
	// 2MASS
	J_H("J-H"), J_K("J-K"), H_K("H-K"), e_J_H("J-H (-err)"), e_J_K("J-K (-err)"), e_H_K("H-K (-err)"),
	E_J_H("J-H (+err)"), E_J_K("J-K (+err)"), E_H_K("H-K (+err)"),
	// WISE
	W1_W2("W1-W2"), W1_W3("W1-W3"), W1_W4("W1-W4"), K_W1("K-W1"), e_W1_W2("W1-W2 (-err)"), e_W1_W3("W1-W3 (-err)"),
	e_W1_W4("W1-W4 (-err)"), e_K_W1("K-W1 (-err)"), E_W1_W2("W1-W2 (+err)"), E_W1_W3("W1-W3 (+err)"),
	E_W1_W4("W1-W4 (+err)"), E_K_W1("K-W1 (+err)"),
	// SDSS
	u_g("u-g"), g_r("g-r"), r_i("r-i"), i_z("i-z"), e_u_g("u-g (-err)"), e_g_r("g-r (-err)"), e_r_i("r-i (-err)"),
	e_i_z("i-z (-err)"), E_u_g("u-g (+err)"), E_g_r("g-r (+err)"), E_r_i("r-i (+err)"), E_i_z("i-z (+err)"),
	// PS1
	g_r_PS1("g-r"), r_i_PS1("r-i"), i_z_PS1("i-z"), i_y_PS1("i-y"), z_y_PS1("z-y"), e_g_r_PS1("g-r (-err)"),
	e_r_i_PS1("r-i (-err)"), e_i_z_PS1("i-z (-err)"), e_i_y_PS1("i-y (-err)"), e_z_y_PS1("z-y (-err)"),
	E_g_r_PS1("g-r (+err)"), E_r_i_PS1("r-i (+err)"), E_i_z_PS1("i-z (+err)"), E_i_y_PS1("i-y (+err)"),
	E_z_y_PS1("z-y (+err)"),
	// DES
	g_r_DES("g-r"), r_i_DES("r-i"), i_z_DES("i-z"), z_Y_DES("z-Y"), e_g_r_DES("g-r (-err)"), e_r_i_DES("r-i (-err)"),
	e_i_z_DES("i-z (-err)"), e_z_Y_DES("z-Y (-err)"), E_g_r_DES("g-r (+err)"), E_r_i_DES("r-i (+err)"),
	E_i_z_DES("i-z (+err)"), E_z_Y_DES("z-Y (+err)"),
	// NSC
	g_r_NSC("g-r"), r_i_NSC("r-i"), i_z_NSC("i-z"), z_Y_NSC("z-Y"), e_g_r_NSC("g-r (-err)"), e_r_i_NSC("r-i (-err)"),
	e_i_z_NSC("i-z (-err)"), e_z_Y_NSC("z-Y (-err)"), E_g_r_NSC("g-r (+err)"), E_r_i_NSC("r-i (+err)"),
	E_i_z_NSC("i-z (+err)"), E_z_Y_NSC("z-Y (+err)");

	public String val;

	private Color(String val) {
		this.val = val;
	}

}
