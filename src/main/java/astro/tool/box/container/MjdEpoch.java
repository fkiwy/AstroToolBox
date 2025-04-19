package astro.tool.box.container;

public class MjdEpoch {

	private final String band;

	private final int epoch;

	public MjdEpoch(String band, int epoch) {
		this.band = band;
		this.epoch = epoch;
	}

	public String getBand() {
		return band;
	}

	public int getEpoch() {
		return epoch;
	}

}
