package astro.tool.box.container;

public class SedFluxes {

    private final double magnitude;
    
    private final double flux;

    private final double fluxDensity;

    private final double fluxLambda;

    public SedFluxes(double magnitude, double flux, double fluxDensity, double fluxLambda) {
        this.magnitude = magnitude;
        this.flux = flux;
        this.fluxDensity = fluxDensity;
        this.fluxLambda = fluxLambda;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public double getFlux() {
        return flux;
    }

    public double getFluxDensity() {
        return fluxDensity;
    }

    public double getFluxLambda() {
        return fluxLambda;
    }

}
