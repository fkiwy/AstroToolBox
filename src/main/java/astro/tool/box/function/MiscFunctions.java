package astro.tool.box.function;

public class MiscFunctions {

	/**
	 * Calculate the binding energy between 2 stars
	 *
	 * @param pMass      (primary mass in solar masses)
	 * @param sMass      (secondary mass in solar masses)
	 * @param separation (projected separation in AU)
	 * @return the binding energy between 2 stars
	 */
	public static double calculateBindingEnergy(double pMass, double sMass, double separation) {
		double G = 6.6743E-11;
		pMass *= 1.989E+30; // Convert from solar mass to kilogram
		sMass *= 1.989E+30; // Convert from solar mass to kilogram
		separation *= 1.49598E+11; // Convert from AU to meter
		separation *= 1.26; // To account for inclination angle and eccentricity of the binary orbits
		double eBin = -G * pMass * sMass / separation;
		return eBin * 1E7; // Convert from Joule to erg
	}

}
