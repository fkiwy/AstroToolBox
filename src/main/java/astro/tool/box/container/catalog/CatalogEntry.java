package astro.tool.box.container.catalog;

import astro.tool.box.container.CatalogElement;
import astro.tool.box.enumeration.Color;
import java.util.List;
import java.util.Map;

public interface CatalogEntry {

    CatalogEntry getInstance(Map<String, Integer> columns, String[] values);

    void loadCatalogElements();

    String getCatalogName();

    java.awt.Color getCatalogColor();

    String getCatalogUrl();

    String[] getColumnValues();

    String[] getColumnTitles();

    Map<Color, Double> getColors();

    String getSourceId();

    double getRa();

    void setRa(double ra);

    double getDec();

    void setDec(double dec);

    double getSearchRadius();

    void setSearchRadius(double searchRadius);

    int getCatalogNumber();

    void setCatalogNumber(int catalogNumber);

    double getTargetRa();

    void setTargetRa(double targetRa);

    double getTargetDec();

    void setTargetDec(double targetDec);

    double getPixelRa();

    void setPixelRa(double pixelRa);

    double getPixelDec();

    void setPixelDec(double pixelDec);

    List<CatalogElement> getCatalogElements();

    double getPlx();

    double getPmra();

    double getPmdec();

    double getTargetDistance();

}
