package astro.tool.box.catalog;

import static astro.tool.box.util.Constants.LINE_SEP;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import astro.tool.box.exception.ExtinctionException;
import java.util.List;
import java.util.Map;

public interface CatalogEntry {

    default String getEntryData() {
        StringBuilder entryData = new StringBuilder(getCatalogName()).append(":");
        List<CatalogElement> catalogElements = getCatalogElements();
        for (int i = 1; i < catalogElements.size(); i++) {
            CatalogElement catalogElement = catalogElements.get(i);
            entryData.append(LINE_SEP).append(catalogElement.getName()).append(" = ").append(catalogElement.getValue());
        }
        return entryData.toString();
    }

    CatalogEntry copy();

    void applyExtinctionCorrection(Map<String, Double> extinctionsByBand) throws ExtinctionException;

    CatalogEntry getInstance(Map<String, Integer> columns, String[] values);

    void loadCatalogElements();

    String getCatalogName();

    java.awt.Color getCatalogColor();

    String getCatalogQueryUrl();

    String[] getColumnValues();

    String[] getColumnTitles();

    Map<Band, NumberPair> getBands();

    Map<Color, Double> getColors(boolean toVega);

    String getMagnitudes();

    default String getPhotometry() {
        return null;
    }

    String getSourceId();

    double getRa();

    void setRa(double ra);

    double getDec();

    void setDec(double dec);

    double getSearchRadius();

    void setSearchRadius(double searchRadius);

    double getTargetRa();

    void setTargetRa(double targetRa);

    double getTargetDec();

    void setTargetDec(double targetDec);

    double getPixelRa();

    void setPixelRa(double pixelRa);

    double getPixelDec();

    void setPixelDec(double pixelDec);

    String getSpt();

    void setSpt(String spt);

    List<CatalogElement> getCatalogElements();

    double getPlx();

    double getPmra();

    double getPmdec();

    double getTargetDistance();

    double getParallacticDistance();

    double getTotalProperMotion();

}
