package astro.tool.box.container;

import nom.tam.fits.Fits;

public record ImageContainer(int epoch, Fits image, boolean skip) {

}
