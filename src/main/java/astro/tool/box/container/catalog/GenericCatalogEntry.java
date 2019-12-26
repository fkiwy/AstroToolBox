package astro.tool.box.container.catalog;

import astro.tool.box.container.CatalogElement;
import astro.tool.box.enumeration.Alignment;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GenericCatalogEntry implements CatalogEntry {

    private String[] titles;

    private String[] values;

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    public GenericCatalogEntry() {
    }

    public GenericCatalogEntry(String[] titles, String[] values) {
        this.titles = titles;
        this.values = values;
    }

    @Override
    public CatalogEntry getInstance(String[] values) {
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
        return "";
    }

    @Override
    public Color getCatalogColor() {
        return Color.BLACK;
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
    public Map<astro.tool.box.enumeration.Color, Double> getColors() {
        return null;
    }

    @Override
    public String getSourceId() {
        return "";
    }

    @Override
    public double getRa() {
        return 0;
    }

    @Override
    public void setRa(double ra) {
    }

    @Override
    public double getDec() {
        return 0;
    }

    @Override
    public void setDec(double dec) {
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
        return 0;
    }

    @Override
    public void setTargetRa(double targetRa) {
    }

    @Override
    public double getTargetDec() {
        return 0;
    }

    @Override
    public void setTargetDec(double targetDec) {
    }

    @Override
    public double getPixelRa() {
        return 0;
    }

    @Override
    public void setPixelRa(double pixelRa) {
    }

    @Override
    public double getPixelDec() {
        return 0;
    }

    @Override
    public void setPixelDec(double pixelDec) {
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
