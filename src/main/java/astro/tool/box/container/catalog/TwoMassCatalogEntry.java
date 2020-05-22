package astro.tool.box.container.catalog;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.util.Comparators.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.Constants.*;
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

public class TwoMassCatalogEntry implements CatalogEntry {

    public static final String CATALOG_NAME = "2MASS";

    // Source designation formed from sexigesimal coordinates
    private String sourceId;

    // Right ascension (J2000)
    private double ra;

    // Declination (J2000)
    private double dec;

    // J band selected "default" magnitude
    private double Jmag;

    // Corrected J band photometric uncertainty
    private double J_err;

    // H band selected "default" magnitude
    private double Hmag;

    // Corrected H band photometric uncertainty
    private double H_err;

    // K band selected "default" magnitude
    private double Kmag;

    // Corrected K band photometric uncertainty
    private double K_err;

    // observation reference date
    private String xdate;

    // Flag indicating photometric quality of source
    private String ph_qual;

    // Source of JHK "default" mags (AKA "read flag")
    private String rd_flg;

    // Indicates # JHK components fit simultaneously to source
    private String bl_flg;

    // Indicates JHK artifact contamination and/or confusion
    private String cc_flg;

    // Flag indicating if src is contaminated by extended source
    private int gal_contam;

    // Src is positionally associated with an asteroid, comet, etc
    private int mp_flg;

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

    public TwoMassCatalogEntry() {
    }

    public TwoMassCatalogEntry(Map<String, Integer> columns, String[] values) {
        sourceId = values[columns.get("designation")];
        ra = toDouble(values[columns.get("ra")]);
        dec = toDouble(values[columns.get("dec")]);
        Jmag = toDouble(values[columns.get("j_m")]);
        J_err = toDouble(values[columns.get("j_cmsig")]);
        Hmag = toDouble(values[columns.get("h_m")]);
        H_err = toDouble(values[columns.get("h_cmsig")]);
        Kmag = toDouble(values[columns.get("k_m")]);
        K_err = toDouble(values[columns.get("k_cmsig")]);
        xdate = values[columns.get("xdate")];
        ph_qual = values[columns.get("ph_qual")];
        rd_flg = values[columns.get("rd_flg")];
        bl_flg = values[columns.get("bl_flg")];
        cc_flg = values[columns.get("cc_flg")];
        gal_contam = toInteger(values[columns.get("gal_contam")]);
        mp_flg = toInteger(values[columns.get("mp_flg")]);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("source id", sourceId, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("observation date", xdate, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("ph. qual.", ph_qual, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("read flag", rd_flg, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("blend flag", bl_flg, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("cc flags", cc_flg, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("ext. flag", String.valueOf(gal_contam), Alignment.RIGHT, getIntegerComparator()));
        catalogElements.add(new CatalogElement("assoc. flag", String.valueOf(mp_flg), Alignment.RIGHT, getIntegerComparator()));
        catalogElements.add(new CatalogElement("J (mag)", roundTo3DecNZ(Jmag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("J err", roundTo3DecNZ(J_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("H (mag)", roundTo3DecNZ(Hmag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("H err", roundTo3DecNZ(H_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("K (mag)", roundTo3DecNZ(Kmag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("K err", roundTo3DecNZ(K_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("J-H", roundTo3DecNZ(getJ_H()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("H-K", roundTo3DecNZ(getH_K()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("J-K", roundTo3DecNZ(getJ_K()), Alignment.RIGHT, getDoubleComparator(), false, true));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TwoMassCatalogEntry{sourceId=").append(sourceId);
        sb.append(", ra=").append(ra);
        sb.append(", dec=").append(dec);
        sb.append(", Jmag=").append(Jmag);
        sb.append(", J_err=").append(J_err);
        sb.append(", Hmag=").append(Hmag);
        sb.append(", H_err=").append(H_err);
        sb.append(", Kmag=").append(Kmag);
        sb.append(", K_err=").append(K_err);
        sb.append(", xdate=").append(xdate);
        sb.append(", ph_qual=").append(ph_qual);
        sb.append(", rd_flg=").append(rd_flg);
        sb.append(", bl_flg=").append(bl_flg);
        sb.append(", cc_flg=").append(cc_flg);
        sb.append(", gal_contam=").append(gal_contam);
        sb.append(", mp_flg=").append(mp_flg);
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
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.sourceId);
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
        final TwoMassCatalogEntry other = (TwoMassCatalogEntry) obj;
        return Objects.equals(this.sourceId, other.sourceId);
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new TwoMassCatalogEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return CATALOG_NAME;
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.LIGHT_ORANGE.val;
    }

    @Override
    public String getCatalogUrl() {
        return createIrsaUrl(TWO_MASS_CATALOG_ID, ra, dec, searchRadius / DEG_ARCSEC);
    }

    @Override
    public String[] getColumnValues() {
        String values = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + xdate + "," + ph_qual + "," + rd_flg + "," + bl_flg + "," + cc_flg + "," + gal_contam + "," + mp_flg + "," + roundTo3Dec(Jmag) + "," + roundTo3Dec(J_err) + "," + roundTo3Dec(Hmag) + "," + roundTo3Dec(H_err) + "," + roundTo3Dec(Kmag) + "," + roundTo3Dec(K_err) + "," + roundTo3Dec(getJ_H()) + "," + roundTo3Dec(getH_K()) + "," + roundTo3Dec(getJ_K());
        return values.split(",", 20);
    }

    @Override
    public String[] getColumnTitles() {
        String titles = "dist (arcsec),source id,ra,dec,observation date,ph. qual.,read flag,blend flag,cc flags,ext. flag,assoc. flag,J (mag),J err,H (mag),H err,K (mag),K err,J-H,H-K,J-K";
        return titles.split(",", 20);
    }

    @Override
    public Map<Color, Double> getColors() {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.J_H, getJ_H());
        colors.put(Color.H_K, getH_K());
        colors.put(Color.J_K, getJ_K());
        return colors;
    }

    @Override
    public String getMagnitudes() {
        return String.format("J=%s; H=%s; K=%s", roundTo3DecNZ(Jmag), roundTo3DecNZ(Hmag), roundTo3DecNZ(Kmag));
    }

    @Override
    public String getSourceId() {
        return sourceId;
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

    public double getJ_H() {
        if (Jmag == 0 || Hmag == 0) {
            return 0;
        } else {
            return Jmag - Hmag;
        }
    }

    public double getH_K() {
        if (Hmag == 0 || Kmag == 0) {
            return 0;
        } else {
            return Hmag - Kmag;
        }
    }

    public double getJ_K() {
        if (Jmag == 0 || Kmag == 0) {
            return 0;
        } else {
            return Jmag - Kmag;
        }
    }

}
