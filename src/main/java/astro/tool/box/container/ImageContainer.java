package astro.tool.box.container;

import nom.tam.fits.Fits;

public class ImageContainer {

    private final int epoch;

    private final Fits image;

    public ImageContainer(int epoch, Fits image) {
        this.epoch = epoch;
        this.image = image;
    }

    public int getEpoch() {
        return epoch;
    }

    public Fits getImage() {
        return image;
    }

}
