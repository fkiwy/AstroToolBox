package astro.tool.box.container.catalog;

import astro.tool.box.container.CatalogElement;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GenericCatalogEntry implements CatalogEntry {

    private String[] titles;

    private String[] values;

    // Right ascension
    private double ra;

    // Declination
    private double dec;

    // Right ascension used for distance calculation
    private double targetRa;

    // Declination used for distance calculation
    private double targetDec;

    // Pixel RA position
    private double pixelRa;

    // Pixel declination position
    private double pixelDec;

    // Catalog name
    private String catalogName;

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    public GenericCatalogEntry() {
    }

    public GenericCatalogEntry(String[] titles, String[] values) {
        this.titles = titles;
        this.values = values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GenericCatalogEntry{ra=").append(ra);
        sb.append(", dec=").append(dec);
        sb.append(", targetRa=").append(targetRa);
        sb.append(", targetDec=").append(targetDec);
        sb.append(", pixelRa=").append(pixelRa);
        sb.append(", pixelDec=").append(pixelDec);
        sb.append(", catalogName=").append(catalogName);
        sb.append(", catalogElements=").append(catalogElements);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new GenericCatalogEntry();
    }

    @Override
    public void loadCatalogElements() {
        for (int i = 0; i < titles.length; i++) {
            catalogElements.add(new CatalogElement(titles[i], values[i], Alignment.LEFT, null));
        }
    }

    @Override
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.LIGHT_GRAY.val;
    }

    @Override
    public String getCatalogUrl() {
        return "";
    }

    @Override
    public String[] getColumnValues() {
        return new String[0];
    }

    @Override
    public String[] getColumnTitles() {
        return new String[0];
    }

    @Override
    public Map<Band, Double> getBands() {
        return new LinkedHashMap<>();
    }

    @Override
    public Map<Color, Double> getColors() {
        return new LinkedHashMap<>();
    }

    @Override
    public String getMagnitudes() {
        return "";
    }

    @Override
    public String getSourceId() {
        return "";
    }

    @Override
    public double getRa() {
        return ra;
    }

    @Override
    public void setRa(double ra) {
        this.ra = ra;
    }

    @Override
    public double getDec() {
        return dec;
    }

    @Override
    public void setDec(double dec) {
        this.dec = dec;
    }

    @Override
    public double getSearchRadius() {
        return 0;
    }

    @Override
    public void setSearchRadius(double searchRadius) {
    }

    @Override
    public int getCatalogNumber() {
        return 0;
    }

    @Override
    public void setCatalogNumber(int catalogNumber) {
    }

    @Override
    public double getTargetRa() {
        return targetRa;
    }

    @Override
    public void setTargetRa(double targetRa) {
        this.targetRa = targetRa;
    }

    @Override
    public double getTargetDec() {
        return targetDec;
    }

    @Override
    public void setTargetDec(double targetDec) {
        this.targetDec = targetDec;
    }

    @Override
    public double getPixelRa() {
        return pixelRa;
    }

    @Override
    public void setPixelRa(double pixelRa) {
        this.pixelRa = pixelRa;
    }

    @Override
    public double getPixelDec() {
        return pixelDec;
    }

    @Override
    public void setPixelDec(double pixelDec) {
        this.pixelDec = pixelDec;
    }

    @Override
    public List<CatalogElement> getCatalogElements() {
        return catalogElements;
    }

    @Override
    public double getPlx() {
        return 0;
    }

    @Override
    public double getPmra() {
        return 0;
    }

    @Override
    public double getPmdec() {
        return 0;
    }

    @Override
    public double getTargetDistance() {
        return 0;
    }

}
