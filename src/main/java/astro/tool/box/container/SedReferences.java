package astro.tool.box.container;

public class SedReferences {

    private final double zeropoint;

    private final double wavelenth;

    public SedReferences(double zeropoint, double wavelenth) {
        this.zeropoint = zeropoint;
        this.wavelenth = wavelenth;
    }

    public double getZeropoint() {
        return zeropoint;
    }

    public double getWavelenth() {
        return wavelenth;
    }

}
