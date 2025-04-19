package astro.tool.box.container;

import java.awt.image.BufferedImage;

import nom.tam.fits.Fits;

public class FlipbookComponent {

	private final Fits fits1;

	private final Fits fits2;

	private final String band;

	private final String obsDate;

	private final boolean firstEpoch;

	private BufferedImage image;

	public FlipbookComponent(Fits fits1, Fits fits2, String band, String obsDate, boolean firstEpoch) {
		this.fits1 = fits1;
		this.fits2 = fits2;
		this.band = band;
		this.obsDate = obsDate;
		this.firstEpoch = firstEpoch;
	}

	public String getTitle() {
		return band + "  " + obsDate;
	}

	public Fits getFits1() {
		return fits1;
	}

	public Fits getFits2() {
		return fits2;
	}

	public boolean isFirstEpoch() {
		return firstEpoch;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

}
