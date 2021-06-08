package astro.tool.box.container;

public class ClassifierData {

    private final String catalog;

    private final String colorKey;

    private final String colorValue;

    private final String spectralType;

    private Double sptNum;

    public ClassifierData(String spectralType) {
        this("", "", "", spectralType);
    }

    public ClassifierData(String catalog, String colorKey, String colorValue, String spectralType) {
        this.catalog = catalog;
        this.colorKey = colorKey;
        this.colorValue = colorValue;
        this.spectralType = spectralType;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getColorKey() {
        return colorKey;
    }

    public String getColorValue() {
        return colorValue;
    }

    public String getSpectralType() {
        return spectralType;
    }

    public Double getSptNum() {
        return sptNum;
    }

    public void setSptNum(Double sptNum) {
        this.sptNum = sptNum;
    }

}
