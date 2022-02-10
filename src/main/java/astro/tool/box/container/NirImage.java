package astro.tool.box.container;

import java.awt.image.BufferedImage;

public class NirImage {

    private final String filderId;

    private final String extNo;

    private final int year;

    private final String imageUrl;

    private BufferedImage image;

    public NirImage(String filderId, String extNo, int year, String imageUrl) {
        this.filderId = filderId;
        this.extNo = extNo;
        this.year = year;
        this.imageUrl = imageUrl;
    }

    public NirImage(int year, BufferedImage image) {
        this(null, null, year, null);
        this.image = image;
    }

    public NirImage(String filderId, int year, BufferedImage image) {
        this(filderId, null, year, null);
        this.image = image;
    }

    public String getFilderId() {
        return filderId;
    }

    public String getExtNo() {
        return extNo;
    }

    public int getYear() {
        return year;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

}
