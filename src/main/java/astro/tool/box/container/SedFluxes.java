package astro.tool.box.container;

public class SedFluxes {

	private final double magnitude;

	private final double fluxDensity;

	private final double fluxJansky;

	private final double fluxLambda;

	public SedFluxes(double magnitude, double fluxDensity, double fluxJansky, double fluxLambda) {
		this.magnitude = magnitude;
		this.fluxDensity = fluxDensity;
		this.fluxJansky = fluxJansky;
		this.fluxLambda = fluxLambda;
	}

	public double getMagnitude() {
		return magnitude;
	}

	public double getFluxDensity() {
		return fluxDensity;
	}

	public double getFluxJansky() {
		return fluxJansky;
	}

	public double getFluxLambda() {
		return fluxLambda;
	}

}
