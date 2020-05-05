package astro.tool.box.module;

import java.time.LocalDateTime;
import nom.tam.fits.Fits;

public class ImageContainer {

    private final LocalDateTime date;

    private final Fits image;

    public ImageContainer(LocalDateTime date, Fits image) {
        this.date = date;
        this.image = image;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Fits getImage() {
        return image;
    }

}
