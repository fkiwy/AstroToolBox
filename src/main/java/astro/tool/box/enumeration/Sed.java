package astro.tool.box.enumeration;

public enum Sed {

	// =================================================================================================================================//
	// All wavelengths (λref) and zero points (ZPν) are from the SVO Filter Profile
	// Service (http://svo2.cab.inta-csic.es/theory/fps/) //
	// =================================================================================================================================//
	GAIA_BP(3552.01, 0.510971), GAIA_G(3228.75, 0.621759), GAIA_RP(2554.95, 0.776902), PS1_G(3631, 0.484911),
	PS1_R(3631, 0.62012), PS1_I(3631, 0.753496), PS1_Z(3631, 0.86742), PS1_Y(3631, 0.962779), MASS_J(1594, 1.235),
	MASS_H(1024, 1.662), MASS_K(666.8, 2.159), WISE_W1(309.54, 3.3526), WISE_W2(171.79, 4.6028),
	WISE_W3(31.67, 11.5608), WISE_W4(8.36, 22.0883), DECAM_G(3631, 0.480849), // http://svo2.cab.inta-csic.es/theory/fps/index.php?mode=browse&gname=CTIO&gname2=DECam
	DECAM_R(3631, 0.641765), DECAM_I(3631, 0.781458), DECAM_Z(3631, 0.916885), DECAM_Y(3631, 0.989611),
	VHS_J(1533.54, 1.252483), // http://svo2.cab.inta-csic.es/theory/fps/index.php?mode=browse&gname=Paranal&gname2=VISTA
	VHS_H(1015.62, 1.643245), VHS_K(659.1, 2.152152), UKIDSS_J(1534.75, 1.2483), UKIDSS_H(1022.87, 1.6313),
	UKIDSS_K(632.56, 2.201);

	public double zeropoint;
	public double wavelenth;

	private Sed(double zeropoint, double wavelenth) {
		this.zeropoint = zeropoint;
		this.wavelenth = wavelenth;
	}

}
