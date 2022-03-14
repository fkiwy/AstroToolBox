package astro.tool.box.container;

import java.awt.image.BufferedImage;
import nom.tam.fits.Fits;

public class FlipbookComponent {

    private final Fits fits1;

    private final Fits fits2;

    private final String band;

    private final String scan;

    private final int epoch;

    private final int totalEpochs;

    private final boolean firstEpoch;

    private BufferedImage image;

    public FlipbookComponent(Fits fits1, Fits fits2, String band, String scan, int epoch, int totalEpochs, boolean firstEpoch) {
        this.fits1 = fits1;
        this.fits2 = fits2;
        this.band = band;
        this.scan = scan;
        this.epoch = epoch;
        this.totalEpochs = totalEpochs;
        this.firstEpoch = firstEpoch;
    }

    public String getTitle() {
        return band + "  " + epoch + "  " + scan;
    }

    public Fits getFits1() {
        return fits1;
    }

    public Fits getFits2() {
        return fits2;
    }

    public int getTotalEpochs() {
        return totalEpochs;
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
