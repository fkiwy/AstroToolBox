package astro.tool.box.container;

import java.util.List;

import astro.tool.box.enumeration.Band;

public class SedBestMatch {

	private final String spt;

	private final double medianDiffMag;

	private final double meanDiffMag;

	private final double medianPhotDist;

	private final double stdPhotDist;

	private final List<Band> photBands;

	public SedBestMatch(String spt, double medianDiffMag, double meanDiffMag, double medianPhotDist, double stdPhotDist,
			List<Band> photBands) {
		this.spt = spt;
		this.medianDiffMag = medianDiffMag;
		this.meanDiffMag = meanDiffMag;
		this.medianPhotDist = medianPhotDist;
		this.stdPhotDist = stdPhotDist;
		this.photBands = photBands;
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

	public double getMedianPhotDist() {
		return medianPhotDist;
	}

	public double getStdPhotDist() {
		return stdPhotDist;
	}

	public List<Band> getPhotBands() {
		return photBands;
	}

}
