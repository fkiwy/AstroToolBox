package astro.tool.box.container;

import java.time.LocalDateTime;

public class Epoch {

	private int band;
	private int epoch;
	private int forward;
	private double mjdmean;
	private transient LocalDateTime obsDate;

	public Epoch() {
	}

	public Epoch(int band, int epoch, int forward, double mjdmean) {
		this.band = band;
		this.epoch = epoch;
		this.forward = forward;
		this.mjdmean = mjdmean;
	}

	@Override
	public String toString() {
		return "Epoch{" + "band=" + band + ", epoch=" + epoch + ", forward=" + forward + ", mjdmean=" + mjdmean
				+ ", obsDate=" + obsDate + '}';
	}

	public int getBand() {
		return band;
	}

	public void setBand(int band) {
		this.band = band;
	}

	public int getEpoch() {
		return epoch;
	}

	public void setEpoch(int epoch) {
		this.epoch = epoch;
	}

	public int getForward() {
		return forward;
	}

	public void setForward(int forward) {
		this.forward = forward;
	}

	public double getMjdmean() {
		return mjdmean;
	}

	public void setMjdmean(double mjdmean) {
		this.mjdmean = mjdmean;
	}

	public LocalDateTime getObsDate() {
		return obsDate;
	}

	public void setObsDate(LocalDateTime obsDate) {
		this.obsDate = obsDate;
	}

}
