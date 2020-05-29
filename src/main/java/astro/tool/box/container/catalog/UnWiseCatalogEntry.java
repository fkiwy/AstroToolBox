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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UnWiseCatalogEntry implements CatalogEntry {

    public static final String CATALOG_NAME = "unWISE";

    // Unique object id
    private String unwise_objid;

    // W1 position, if available; otherwise W2 position
    private double ra;

    // W1 position, if available; otherwise W2 position
    private double dec;

    // W1 magnitude (Vega)
    private double mag_w1_vg;

    // W2 magnitude (Vega)
    private double mag_w2_vg;

    // W1-W2 color (Vega)
    private double w1_w2_vg;

    // quality factor for W1
    private double qf_w1;

    // quality factor for W2
    private double qf_w2;

    // unWISE Coadd flags at central pixel for W1
    private int flags_unwise_w1;

    // unWISE Coadd flags at central pixel for W2
    private int flags_unwise_w2;

    // Additional informational flags at central pixel for W1
    private int flags_info_w1;

    // Additional informational flags at central pixel for W2
    private int flags_info_w2;

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

    // Total proper motion
    private double tpm;

    // Catalog number
    private int catalogNumber;

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    public UnWiseCatalogEntry() {
    }

    public UnWiseCatalogEntry(Map<String, Integer> columns, String[] values) {
        unwise_objid = values[columns.get("unwise_objid")];
        ra = toDouble(values[columns.get("ra")]);
        dec = toDouble(values[columns.get("dec")]);
        mag_w1_vg = toDouble(values[columns.get("mag_w1_vg")]);
        mag_w2_vg = toDouble(values[columns.get("mag_w2_vg")]);
        w1_w2_vg = toDouble(values[columns.get("w1_w2_vg")]);
        qf_w1 = toDouble(values[columns.get("qf_w1")]);
        qf_w2 = toDouble(values[columns.get("qf_w2")]);
        flags_unwise_w1 = toInteger(values[columns.get("flags_unwise_w1")]);
        flags_unwise_w2 = toInteger(values[columns.get("flags_unwise_w2")]);
        flags_info_w1 = toInteger(values[columns.get("flags_info_w1")]);
        flags_info_w2 = toInteger(values[columns.get("flags_info_w2")]);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("source id", unwise_objid, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W1 (mag)", roundTo3DecNZ(mag_w1_vg), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("W2 (mag)", roundTo3DecNZ(mag_w2_vg), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("qual. fact. W1", roundTo3DecNZ(qf_w1), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("qual. fact. W2", roundTo3DecNZ(qf_w2), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("coadd flags W1", String.valueOf(flags_unwise_w1), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("coadd flags W2", String.valueOf(flags_unwise_w2), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("info flags W1", String.valueOf(flags_info_w1), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("info flags W2", String.valueOf(flags_info_w2), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W1-W2", roundTo3DecNZ(w1_w2_vg), Alignment.RIGHT, getDoubleComparator(), true));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("UnWiseCatalogEntry{unwise_objid=").append(unwise_objid);
        sb.append(", ra=").append(ra);
        sb.append(", dec=").append(dec);
        sb.append(", mag_w1_vg=").append(mag_w1_vg);
        sb.append(", mag_w2_vg=").append(mag_w2_vg);
        sb.append(", w1_w2_vg=").append(w1_w2_vg);
        sb.append(", qf_w1=").append(qf_w1);
        sb.append(", qf_w2=").append(qf_w2);
        sb.append(", flags_unwise_w1=").append(flags_unwise_w1);
        sb.append(", flags_unwise_w2=").append(flags_unwise_w2);
        sb.append(", flags_info_w1=").append(flags_info_w1);
        sb.append(", flags_info_w2=").append(flags_info_w2);
        sb.append(", targetRa=").append(targetRa);
        sb.append(", targetDec=").append(targetDec);
        sb.append(", pixelRa=").append(pixelRa);
        sb.append(", pixelDec=").append(pixelDec);
        sb.append(", searchRadius=").append(searchRadius);
        sb.append(", tpm=").append(tpm);
        sb.append(", catalogNumber=").append(catalogNumber);
        sb.append(", catalogElements=").append(catalogElements);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.unwise_objid);
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
        final UnWiseCatalogEntry other = (UnWiseCatalogEntry) obj;
        if (!Objects.equals(this.unwise_objid, other.unwise_objid)) {
            return false;
        }
        return true;
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new UnWiseCatalogEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return CATALOG_NAME;
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.LIGHT_GRAY.val;
    }

    @Override
    public String getCatalogUrl() {
        return createUnWiseUrl(ra, dec, searchRadius / DEG_ARCSEC);
    }

    @Override
    public String[] getColumnValues() {
        String values = roundTo3DecLZ(getTargetDistance()) + "," + unwise_objid + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + roundTo3Dec(mag_w1_vg) + "," + roundTo3Dec(mag_w2_vg) + "," + roundTo3Dec(qf_w1) + "," + roundTo3Dec(qf_w2) + "," + flags_unwise_w1 + "," + flags_unwise_w2 + "," + flags_info_w1 + "," + flags_info_w2 + "," + roundTo3Dec(w1_w2_vg);
        return values.split(",", 13);
    }

    @Override
    public String[] getColumnTitles() {
        String titles = "dist (arcsec),source id,ra,dec,W1 (mag),W2 (mag),qual. fact. W1,qual. fact. W2,coadd flags W1,coadd flags W2,info flags W1,info flags W2,W1-W2";
        return titles.split(",", 13);
    }

    @Override
    public Map<Color, Double> getColors() {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.W1_W2, w1_w2_vg);
        return colors;
    }

    @Override
    public String getMagnitudes() {
        return String.format("W1=%s; W2=%s", roundTo3DecNZ(mag_w1_vg), roundTo3DecNZ(mag_w2_vg));
    }

    @Override
    public String getSourceId() {
        return unwise_objid;
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
