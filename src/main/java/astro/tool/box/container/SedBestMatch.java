package astro.tool.box.container;

public class SedBestMatch {

    private final String spt;

    private final double medianDiffMag;

    private final double meanDiffMag;

    public SedBestMatch(String spt, double medianDiffMag, double meanDiffMag) {
        this.spt = spt;
        this.medianDiffMag = medianDiffMag;
        this.meanDiffMag = meanDiffMag;
    }

    public String getSpt() {
        return spt;
    }

    public double getMedianDiffMag() {
        return medianDiffMag;
    }

    public double getMeanDiffMag() {
        return meanDiffMag;
    }

}
