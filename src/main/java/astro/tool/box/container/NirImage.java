package astro.tool.box.container;

public class NirImage {

    private final String filderId;

    private final String extNo;

    private final String imageUrl;

    public NirImage(String filderId, String extNo, String imageUrl) {
        this.filderId = filderId;
        this.extNo = extNo;
        this.imageUrl = imageUrl;
    }

    public String getFilderId() {
        return filderId;
    }

    public String getExtNo() {
        return extNo;
    }

    public String getImageUrl() {
        return imageUrl;
    }

}
