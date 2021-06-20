package astro.tool.box.container;

public class ClassifierData {

    private final String catalog;

    private final String colorKey;

    private final String colorValue;

    private final String spectralType;

    private final String referenceColor;

    private final String sourceId;

    private Double sptNum;

    public ClassifierData(String spectralType, String sourceId) {
        this("", "", "", spectralType, "", sourceId);
    }

    public ClassifierData(String catalog, String colorKey, String colorValue, String spectralType, String referenceColor, String sourceId) {
        this.catalog = catalog;
        this.colorKey = colorKey;
        this.colorValue = colorValue;
        this.spectralType = spectralType;
        this.referenceColor = referenceColor;
        this.sourceId = sourceId;
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

    public String getReferenceColor() {
        return referenceColor;
    }

    public String getSourceId() {
        return sourceId;
    }

    public Double getSptNum() {
        return sptNum;
    }

    public void setSptNum(Double sptNum) {
        this.sptNum = sptNum;
    }

}
