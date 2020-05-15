package astro.tool.box.container.catalog;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.util.Comparators.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VHSCatalogEntry implements CatalogEntry {

    // Unique identifier of this merged detection as assigned by merge algorithm
    private long sourceId;

    // Celestial Right Ascension (J2000)
    private double ra;

    // Celestial Declination (J2000)
    private double dec;

    // Object type
    private int objectType;

    // Default point source Y aperture corrected mag 
    private double y_ap3;

    // Error in default point/extended source Y mag
    private double y_ap3_err;

    // Default point source J aperture corrected mag 
    private double j_ap3;

    // Error in default point/extended source J mag
    private double j_ap3_err;

    // Default point source H aperture corrected mag 
    private double h_ap3;

    // Error in default point/extended source H mag
    private double h_ap3_err;

    // Default point source Ks aperture corrected mag 
    private double ks_ap3;

    // Error in default point/extended source Ks mag
    private double ks_ap3_err;

    // Point source colour Y-J
    private double y_j_pnt;

    // Point source colour J-H
    private double j_h_pnt;

    // Point source colour H-Ks
    private double h_ks_pnt;

    // Point source colour J-Ks
    private double j_ks_pnt;

    // Right ascension used for distance calculation
    private double targetRa;

    // Declination used for distance calculation
    private double targetDec;

    // Pixel RA position
    private double pixelRa;

    // Pixel declination position
    private double pixelDec;

    // Search radius
    private double searchRadius;

    // Catalog number
    private int catalogNumber;

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    private static final Map<Integer, String> TYPE_TABLE = new HashMap<>();

    static {
        TYPE_TABLE.put(1, "Galaxy");
        TYPE_TABLE.put(0, "Noise");
        TYPE_TABLE.put(-1, "Star");
        TYPE_TABLE.put(-2, "Probable star");
        TYPE_TABLE.put(-3, "Probable galaxy");
    }

    public VHSCatalogEntry() {
    }

    public VHSCatalogEntry(Map<String, Integer> columns, String[] values) {
        sourceId = toLong(values[columns.get("SrcID")]);
        ra = toDouble(values[columns.get("RAJ2000")]);
        dec = toDouble(values[columns.get("DEJ2000")]);
        objectType = toInteger(values[columns.get("Mclass")]);
        y_ap3 = toDouble(values[columns.get("Yap3")]);
        y_ap3_err = toDouble(values[columns.get("e_Yap3")]);
        j_ap3 = toDouble(values[columns.get("Jap3")]);
        j_ap3_err = toDouble(values[columns.get("e_Yap3")]);
        h_ap3 = toDouble(values[columns.get("Hap3")]);
        h_ap3_err = toDouble(values[columns.get("e_Hap3")]);
        ks_ap3 = toDouble(values[columns.get("Ksap3")]);
        ks_ap3_err = toDouble(values[columns.get("e_Ksap3")]);
        y_j_pnt = toDouble(values[columns.get("Y-Jpnt")]);
        j_h_pnt = toDouble(values[columns.get("J-Hpnt")]);
        h_ks_pnt = toDouble(values[columns.get("H-Kspnt")]);
        j_ks_pnt = toDouble(values[columns.get("J-Kspnt")]);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("source id", String.valueOf(sourceId), Alignment.LEFT, getLongComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Y (mag)", roundTo3DecNZ(y_ap3), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("Y err", roundTo3DecNZ(y_ap3_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("J (mag)", roundTo3DecNZ(j_ap3), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("J err", roundTo3DecNZ(j_ap3_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("H (mag)", roundTo3DecNZ(h_ap3), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("H err", roundTo3DecNZ(h_ap3_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Ks (mag)", roundTo3DecNZ(ks_ap3), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("Ks err", roundTo3DecNZ(ks_ap3_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Y-J", roundTo3DecNZ(y_j_pnt), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("J-H", roundTo3DecNZ(j_h_pnt), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("H-Ks", roundTo3DecNZ(h_ks_pnt), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("J-Ks", roundTo3DecNZ(j_ks_pnt), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("object type", TYPE_TABLE.get(objectType), Alignment.LEFT, getStringComparator(), true));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VHSCatalogEntry{sourceId=").append(sourceId);
        sb.append(", ra=").append(ra);
        sb.append(", dec=").append(dec);
        sb.append(", objectType=").append(objectType);
        sb.append(", y_ap3=").append(y_ap3);
        sb.append(", y_ap3_err=").append(y_ap3_err);
        sb.append(", j_ap3=").append(j_ap3);
        sb.append(", j_ap3_err=").append(j_ap3_err);
        sb.append(", h_ap3=").append(h_ap3);
        sb.append(", h_ap3_err=").append(h_ap3_err);
        sb.append(", ks_ap3=").append(ks_ap3);
        sb.append(", ks_ap3_err=").append(ks_ap3_err);
        sb.append(", y_j_pnt=").append(y_j_pnt);
        sb.append(", j_h_pnt=").append(j_h_pnt);
        sb.append(", h_ks_pnt=").append(h_ks_pnt);
        sb.append(", j_ks_pnt=").append(j_ks_pnt);
        sb.append(", targetRa=").append(targetRa);
        sb.append(", targetDec=").append(targetDec);
        sb.append(", pixelRa=").append(pixelRa);
        sb.append(", pixelDec=").append(pixelDec);
        sb.append(", searchRadius=").append(searchRadius);
        sb.append(", catalogNumber=").append(catalogNumber);
        sb.append(", catalogElements=").append(catalogElements);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (int) (this.sourceId ^ (this.sourceId >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final VHSCatalogEntry other = (VHSCatalogEntry) obj;
        return this.sourceId == other.sourceId;
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new VHSCatalogEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return "VISTA-VHS";
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.LIGHT_PINK.val;
    }

    @Override
    public String getCatalogUrl() {
        return createVizierUrl(ra, dec, searchRadius / DEG_ARCSEC);
    }

    @Override
    public String[] getColumnValues() {
        String values = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + roundTo3Dec(y_ap3) + "," + roundTo3Dec(y_ap3_err) + "," + roundTo3Dec(j_ap3) + "," + roundTo3Dec(j_ap3_err) + "," + roundTo3Dec(h_ap3) + "," + roundTo3Dec(h_ap3_err) + "," + roundTo3Dec(ks_ap3) + "," + roundTo3Dec(ks_ap3_err) + "," + roundTo3Dec(y_j_pnt) + "," + roundTo3Dec(j_h_pnt) + "," + roundTo3Dec(h_ks_pnt) + "," + roundTo3Dec(j_ks_pnt) + "," + TYPE_TABLE.get(objectType);
        return values.split(",", 17);
    }

    @Override
    public String[] getColumnTitles() {
        String titles = "dist (arcsec),source id,ra,dec,Y (mag),Y err,J (mag),J err,H (mag),H err,Ks (mag),Ks err,Y-J,J-H,H-Ks,J-Ks,object type";
        return titles.split(",", 17);
    }

    @Override
    public Map<Color, Double> getColors() {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.J_H, j_h_pnt);
        colors.put(Color.H_K, h_ks_pnt);
        colors.put(Color.J_K, j_ks_pnt);
        return colors;
    }

    @Override
    public String getMagnitudes() {
        return String.format("Y=%s; J=%s; H=%s; K=%s", roundTo3DecNZ(y_ap3), roundTo3DecNZ(j_ap3), roundTo3DecNZ(h_ap3), roundTo3DecNZ(ks_ap3));
    }

    @Override
    public String getSourceId() {
        return String.valueOf(sourceId);
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
        return searchRadius;
    }

    @Override
    public void setSearchRadius(double searchRadius) {
        this.searchRadius = searchRadius;
    }

    @Override
    public int getCatalogNumber() {
        return catalogNumber;
    }

    @Override
    public void setCatalogNumber(int catalogNumber) {
        this.catalogNumber = catalogNumber;
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
        return calculateAngularDistance(new NumberPair(targetRa, targetDec), new NumberPair(ra, dec), DEG_ARCSEC);
    }

}
