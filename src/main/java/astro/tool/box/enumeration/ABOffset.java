package astro.tool.box.enumeration;

public enum ABOffset {

	// Blanton+2006
	// u(0.91),
	// g(-0.08),
	// r(0.16),
	// i(0.37),
	// z(0.54);

	// Hewett+2006
	u(0.927), g(-0.103), r(0.146), i(0.366), z(0.533);

	public double val;

	private ABOffset(double val) {
		this.val = val;
	}

}
