package astro.tool.box.container;

import nom.tam.fits.Fits;

public class ImageContainer {

    private final int epoch;

    private final Fits image;

    private final boolean skip;

    public ImageContainer(int epoch, Fits image, boolean skip) {
        this.epoch = epoch;
        this.image = image;
        this.skip = skip;
    }

    public int getEpoch() {
        return epoch;
    }

    public Fits getImage() {
        return image;
    }

    public boolean isSkip() {
        return skip;
    }

}
