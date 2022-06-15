package astro.tool.box.container;

import java.time.LocalDateTime;
import nom.tam.fits.Fits;

public class ImageContainer {

    private final int epoch;

    private final LocalDateTime date;

    private final Fits image;

    public ImageContainer(int epoch, LocalDateTime date, Fits image) {
        this.epoch = epoch;
        this.date = date;
        this.image = image;
    }

    public int getEpoch() {
        return epoch;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Fits getImage() {
        return image;
    }

}
