package astro.tool.box.catalog;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.util.Comparators.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.ServiceHelper.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VhsCatalogEntry implements CatalogEntry {

    public static final String CATALOG_NAME = "VHS DR5";

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

    // Most likely spectral type
    private String spt;

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    private Map<String, Integer> columns;

    private String[] values;

    private static final Map<Integer, String> TYPE_TABLE = new HashMap<>();

    static {
        TYPE_TABLE.put(1, "Galaxy");
        TYPE_TABLE.put(0, "Noise");
        TYPE_TABLE.put(-1, "Star");
        TYPE_TABLE.put(-2, "Probable star");
        TYPE_TABLE.put(-3, "Probable galaxy");
    }

    public VhsCatalogEntry() {
    }

    public VhsCatalogEntry(Map<String, Integer> columns, String[] values) {
        this.columns = columns;
        this.values = values;
        sourceId = toLong(values[columns.get("SrcID")]);
        ra = toDouble(values[columns.get("RAJ2000")]);
        dec = toDouble(values[columns.get("DEJ2000")]);
        objectType = toInteger(values[columns.get("Mclass")]);
        y_ap3 = toDouble(values[columns.get("Yap3")]);
        y_ap3_err = toDouble(values[columns.get("e_Yap3")]);
        j_ap3 = toDouble(values[columns.get("Jap3")]);
        j_ap3_err = toDouble(values[columns.get("e_Jap3")]);
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
    public CatalogEntry copy() {
        return new UnWiseCatalogEntry(columns, values);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("source id", String.valueOf(sourceId), Alignment.LEFT, getLongComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo6DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo6DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Y (mag)", roundTo4DecNZ(y_ap3), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("Y err", roundTo4DecNZ(y_ap3_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("J (mag)", roundTo4DecNZ(j_ap3), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("J err", roundTo4DecNZ(j_ap3_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("H (mag)", roundTo4DecNZ(h_ap3), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("H err", roundTo4DecNZ(h_ap3_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Ks (mag)", roundTo4DecNZ(ks_ap3), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("Ks err", roundTo4DecNZ(ks_ap3_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Y-J", roundTo4DecNZ(y_j_pnt), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("J-H", roundTo4DecNZ(j_h_pnt), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("H-Ks", roundTo4DecNZ(h_ks_pnt), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("J-Ks", roundTo4DecNZ(j_ks_pnt), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("object type", TYPE_TABLE.get(objectType), Alignment.LEFT, getStringComparator(), true));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (int) (this.sourceId ^ (this.sourceId >>> 32));
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
        final VhsCatalogEntry other = (VhsCatalogEntry) obj;
        return this.sourceId == other.sourceId;
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new VhsCatalogEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return CATALOG_NAME;
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.PINK.val;
    }

    @Override
    public String getCatalogUrl() {
        return createVizieRUrl(ra, dec, searchRadius / DEG_ARCSEC, "II/367/vhs_dr5", "RAJ2000", "DEJ2000");
    }

    @Override
    public String[] getColumnValues() {
        String columnValues = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + roundTo6Dec(ra) + "," + roundTo6Dec(dec) + "," + roundTo4Dec(y_ap3) + "," + roundTo4Dec(y_ap3_err) + "," + roundTo4Dec(j_ap3) + "," + roundTo4Dec(j_ap3_err) + "," + roundTo4Dec(h_ap3) + "," + roundTo4Dec(h_ap3_err) + "," + roundTo4Dec(ks_ap3) + "," + roundTo4Dec(ks_ap3_err) + "," + roundTo4Dec(y_j_pnt) + "," + roundTo4Dec(j_h_pnt) + "," + roundTo4Dec(h_ks_pnt) + "," + roundTo4Dec(j_ks_pnt) + "," + TYPE_TABLE.get(objectType);
        return columnValues.split(",", -1);
    }

    @Override
    public String[] getColumnTitles() {
        String columnTitles = "dist (arcsec),source id,ra,dec,Y (mag),Y err,J (mag),J err,H (mag),H err,Ks (mag),Ks err,Y-J,J-H,H-Ks,J-Ks,object type";
        return columnTitles.split(",", -1);
    }

    @Override
    public void applyExtinctionCorrection(Map<String, Double> extinctionsByBand) {
        if (j_ap3 != 0) {
            j_ap3 = j_ap3 - extinctionsByBand.get(TWO_MASS_J);
        }
        if (h_ap3 != 0) {
            h_ap3 = h_ap3 - extinctionsByBand.get(TWO_MASS_H);
        }
        if (ks_ap3 != 0) {
            ks_ap3 = ks_ap3 - extinctionsByBand.get(TWO_MASS_K);
        }
    }

    @Override
    public Map<Band, NumberPair> getBands() {
        Map<Band, NumberPair> bands = new LinkedHashMap<>();
        bands.put(Band.J, new NumberPair(j_ap3, j_ap3_err));
        bands.put(Band.H, new NumberPair(h_ap3, h_ap3_err));
        bands.put(Band.K, new NumberPair(ks_ap3, ks_ap3_err));
        return bands;
    }

    @Override
    public Map<Color, Double> getColors(boolean toVega) {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.J_H, j_h_pnt);
        colors.put(Color.H_K, h_ks_pnt);
        colors.put(Color.J_K, j_ks_pnt);
        return colors;
    }

    @Override
    public String getMagnitudes() {
        StringBuilder mags = new StringBuilder();
        if (y_ap3 != 0) {
            mags.append("Y=").append(roundTo4DecNZ(y_ap3)).append(" ");
        }
        if (j_ap3 != 0) {
            mags.append("J=").append(roundTo4DecNZ(j_ap3)).append(" ");
        }
        if (h_ap3 != 0) {
            mags.append("H=").append(roundTo4DecNZ(h_ap3)).append(" ");
        }
        if (ks_ap3 != 0) {
            mags.append("K=").append(roundTo4DecNZ(ks_ap3)).append(" ");
        }
        return mags.toString();
    }

    @Override
    public String getPhotometry() {
        StringBuilder mags = new StringBuilder();
        if (y_ap3 != 0) {
            mags.append(roundTo4DecNZ(y_ap3)).append(",").append(roundTo4DecNZ(y_ap3_err)).append(",");
        } else {
            mags.append(",,");
        }
        if (j_ap3 != 0) {
            mags.append(roundTo4DecNZ(j_ap3)).append(",").append(roundTo4DecNZ(j_ap3_err)).append(",");
        } else {
            mags.append(",,");
        }
        if (h_ap3 != 0) {
            mags.append(roundTo4DecNZ(h_ap3)).append(",").append(roundTo4DecNZ(h_ap3_err)).append(",");
        } else {
            mags.append(",,");
        }
        if (ks_ap3 != 0) {
            mags.append(roundTo4DecNZ(ks_ap3)).append(",").append(roundTo4DecNZ(ks_ap3_err)).append(",");
        } else {
            mags.append(",,");
        }
        return mags.toString();
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
    public String getSpt() {
        return spt;
    }

    @Override
    public void setSpt(String spt) {
        this.spt = spt;
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

    @Override
    public double getParallacticDistance() {
        return 0;
    }

    @Override
    public double getTotalProperMotion() {
        return 0;
    }

    public double getJmag() {
        return j_ap3;
    }

    public double getHmag() {
        return h_ap3;
    }

    public double getKmag() {
        return ks_ap3;
    }

}