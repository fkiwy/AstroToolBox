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

public class SpitzerCatalogEntry implements CatalogEntry {

    public static final String CATALOG_NAME = "Spitzer/WISE photometry";
    public static final String CATALOG_SHORT_NAME = "Spitzer/WISE";

    // Unique source identifier
    private String sourceId;

    // Right ascension
    private double ra;

    // Declination
    private double dec;

    // Spitzer/IRAC 3.6µm Vega magnitude
    private double CH1mag;

    // Uncertainty in [3.6]
    private double CH1_err;

    // Spitzer/IRAC 4.5µm Vega magnitude
    private double CH2mag;

    // Uncertainty in [4.5]
    private double CH2_err;

    // SExtractor Star/Galaxy separator measured at 3.6µm
    private double extCH1;

    // SExtractor Star/Galaxy separator measured at 4.5µm
    private double extCH2;

    // Instrumental profile-fit photometry magnitude, band 1
    private double W1mag;

    // Instrumental profile-fit photometry flux uncertainty in mag units, band 1
    private double W1_err;

    // Instrumental profile-fit photometry magnitude, band 2
    private double W2mag;

    // Instrumental profile-fit photometry flux uncertainty in mag units, band 2
    private double W2_err;

    // Instrumental profile-fit photometry magnitude, band 3
    private double W3mag;

    // Instrumental profile-fit photometry flux uncertainty in mag units, band 3
    private double W3_err;

    // Instrumental profile-fit photometry magnitude, band 4
    private double W4mag;

    // Instrumental profile-fit photometry flux uncertainty in mag units, band 4
    private double W4_err;

    // J magnitude entry of the associated 2MASS All-Sky PSC source
    private double Jmag;

    // J photometric uncertainty of the associated 2MASS All-Sky PSC source
    private double J_err;

    // H magnitude entry of the associated 2MASS All-Sky PSC source
    private double Hmag;

    // H photometric uncertainty of the associated 2MASS All-Sky PSC source
    private double H_err;

    // K magnitude entry of the associated 2MASS All-Sky PSC source
    private double Kmag;

    // K photometric uncertainty of the associated 2MASS All-Sky PSC source
    private double K_err;

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

    public SpitzerCatalogEntry() {
    }

    public SpitzerCatalogEntry(Map<String, Integer> columns, String[] values) {
        //RAJ2000,DEJ2000,[3_6],e_[3_6],[4_5],e_[4_5],S/G1,S/G2,[3_4],e_[3_4],[4_6],e_[4_6],[12],e_[12],[22],e_[22],Jmag,e_Jmag,Hmag,e_Hmag,Kmag,e_Kmag
        ra = toDouble(values[columns.get("RAJ2000")]);
        dec = toDouble(values[columns.get("DEJ2000")]);
        sourceId = roundTo2DecNZ(ra) + addPlusSign(roundDouble(dec, PATTERN_2DEC_NZ));
        CH1mag = toDouble(values[columns.get("[3_6]")]);
        CH1_err = toDouble(values[columns.get("e_[3_6]")]);
        CH2mag = toDouble(values[columns.get("[4_5]")]);
        CH2_err = toDouble(values[columns.get("e_[4_5]")]);
        extCH1 = toDouble(values[columns.get("S/G1")]);
        extCH2 = toDouble(values[columns.get("S/G2")]);
        W1mag = toDouble(values[columns.get("[3_4]")]);
        W1_err = toDouble(values[columns.get("e_[3_4]")]);
        W2mag = toDouble(values[columns.get("[4_6]")]);
        W2_err = toDouble(values[columns.get("e_[4_6]")]);
        W3mag = toDouble(values[columns.get("[12]")]);
        W3_err = toDouble(values[columns.get("e_[12]")]);
        W4mag = toDouble(values[columns.get("[22]")]);
        W4_err = toDouble(values[columns.get("e_[22]")]);
        Jmag = toDouble(values[columns.get("Jmag")]);
        J_err = toDouble(values[columns.get("e_Jmag")]);
        Hmag = toDouble(values[columns.get("Hmag")]);
        H_err = toDouble(values[columns.get("e_Hmag")]);
        Kmag = toDouble(values[columns.get("Kmag")]);
        K_err = toDouble(values[columns.get("e_Kmag")]);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("source id", String.valueOf(sourceId), Alignment.LEFT, getLongComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("CH1 (mag)", roundTo3DecNZ(CH1mag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("CH1 err", roundTo3DecNZ(CH1_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("CH2 (mag)", roundTo3DecNZ(CH2mag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("CH2 err", roundTo3DecNZ(CH2_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Galaxy-Star (0-1) CH1", roundTo3DecNZ(extCH1), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Galaxy-Star (0-1) CH2", roundTo3DecNZ(extCH2), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W1 (mag)", roundTo3DecNZ(W1mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W1 err", roundTo3DecNZ(W1_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W2 (mag)", roundTo3DecNZ(W2mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W2 err", roundTo3DecNZ(W2_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W3 (mag)", roundTo3DecNZ(W3mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W3 err", roundTo3DecNZ(W3_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W4 (mag)", roundTo3DecNZ(W4mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W4 err", roundTo3DecNZ(W4_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("J (mag)", roundTo3DecNZ(Jmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("J err", roundTo3DecNZ(J_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("H (mag)", roundTo3DecNZ(Hmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("H err", roundTo3DecNZ(H_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("K (mag)", roundTo3DecNZ(Kmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("K err", roundTo3DecNZ(K_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("CH1-CH2", roundTo3DecNZ(getCH1_CH2()), Alignment.RIGHT, getDoubleComparator(), true, true));
        catalogElements.add(new CatalogElement("W1-W2", roundTo3DecNZ(getW1_W2()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("W2-W3", roundTo3DecNZ(getW2_W3()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("J-W2", roundTo3DecNZ(getJ_W2()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("J-H", roundTo3DecNZ(getJ_H()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("H-K", roundTo3DecNZ(getH_K()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("J-K", roundTo3DecNZ(getJ_K()), Alignment.RIGHT, getDoubleComparator(), false, true));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SpitzerCatalogEntry{sourceId=").append(sourceId);
        sb.append(", ra=").append(ra);
        sb.append(", dec=").append(dec);
        sb.append(", CH1mag=").append(CH1mag);
        sb.append(", CH1_err=").append(CH1_err);
        sb.append(", CH2mag=").append(CH2mag);
        sb.append(", CH2_err=").append(CH2_err);
        sb.append(", extCH1=").append(extCH1);
        sb.append(", extCH2=").append(extCH2);
        sb.append(", W1mag=").append(W1mag);
        sb.append(", W1_err=").append(W1_err);
        sb.append(", W2mag=").append(W2mag);
        sb.append(", W2_err=").append(W2_err);
        sb.append(", W3mag=").append(W3mag);
        sb.append(", W3_err=").append(W3_err);
        sb.append(", W4mag=").append(W4mag);
        sb.append(", W4_err=").append(W4_err);
        sb.append(", Jmag=").append(Jmag);
        sb.append(", J_err=").append(J_err);
        sb.append(", Hmag=").append(Hmag);
        sb.append(", H_err=").append(H_err);
        sb.append(", Kmag=").append(Kmag);
        sb.append(", K_err=").append(K_err);
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
        hash = 29 * hash + Objects.hashCode(this.sourceId);
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
        final SpitzerCatalogEntry other = (SpitzerCatalogEntry) obj;
        return Objects.equals(this.sourceId, other.sourceId);
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new SpitzerCatalogEntry(columns, values);
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
        return createSpitzerUrl(ra, dec, searchRadius / DEG_ARCSEC);
    }

    @Override
    public String[] getColumnValues() {
        String values = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + roundTo3Dec(CH1mag) + "," + roundTo3Dec(CH1_err) + "," + roundTo3Dec(CH2mag) + "," + roundTo3Dec(CH2_err) + "," + roundTo3Dec(extCH1) + "," + roundTo3Dec(extCH2) + "," + roundTo3Dec(W1mag) + "," + roundTo3Dec(W1_err) + "," + roundTo3Dec(W2mag) + "," + roundTo3Dec(W2_err) + "," + roundTo3Dec(W3mag) + "," + roundTo3Dec(W3_err) + "," + roundTo3Dec(W4mag) + "," + roundTo3Dec(W4_err) + "," + roundTo3Dec(Jmag) + "," + roundTo3Dec(J_err) + "," + roundTo3Dec(Hmag) + "," + roundTo3Dec(H_err) + "," + roundTo3Dec(Kmag) + "," + roundTo3Dec(K_err) + "," + roundTo3Dec(getCH1_CH2()) + "," + roundTo3Dec(getW1_W2()) + "," + roundTo3Dec(getW2_W3()) + "," + roundTo3Dec(getJ_W2()) + "," + roundTo3Dec(getJ_H()) + "," + roundTo3Dec(getH_K()) + "," + roundTo3Dec(getJ_K());
        return values.split(",", 31);
    }

    @Override
    public String[] getColumnTitles() {
        String titles = "dist (arcsec),source id,ra,dec,CH1 (mag),CH1 err,CH2 (mag),CH2 err,Galaxy-Star (0-1) CH1,Galaxy-Star (0-1) CH2,W1 (mag),W1 err,W2 (mag),W2 err,W3 (mag),W3 err,W4 (mag),W4 err,J (mag),J err,H (mag),H err,K (mag),K err,CH1-CH2,W1-W2,W2-W3,J-W2,J-H,H-K,J-K";
        return titles.split(",", 31);
    }

    @Override
    public Map<Color, Double> getColors() {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.CH1_CH2, getCH1_CH2());
        colors.put(Color.J_H, getJ_H());
        colors.put(Color.H_K, getH_K());
        colors.put(Color.J_K, getJ_K());
        colors.put(Color.W1_W2, getW1_W2());
        colors.put(Color.W1_W3, getW1_W3());
        colors.put(Color.W1_W4, getW1_W4());
        colors.put(Color.J_W2, getJ_W2());
        colors.put(Color.K_W1, getK_W1());
        return colors;
    }

    @Override
    public String getMagnitudes() {
        return String.format("CH1=%s; CH2=%s", roundTo3DecNZ(CH1mag), roundTo3DecNZ(CH2mag));
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

    public double getCH1_CH2() {
        if (CH1mag == 0 || CH2mag == 0) {
            return 0;
        } else {
            return CH1mag - CH2mag;
        }
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

    public double getK_W1() {
        if (Kmag == 0 || W1mag == 0) {
            return 0;
        } else {
            return Kmag - W1mag;
        }
    }

    public double getJ_W2() {
        if (Jmag == 0 || W2mag == 0) {
            return 0;
        } else {
            return Jmag - W2mag;
        }
    }

    public double getW1_W2() {
        if (W1mag == 0 || W2mag == 0) {
            return 0;
        } else {
            return W1mag - W2mag;
        }
    }

    public double getW1_W3() {
        if (W1mag == 0 || W3mag == 0) {
            return 0;
        } else {
            return W1mag - W3mag;
        }
    }

    public double getW1_W4() {
        if (W1mag == 0 || W4mag == 0) {
            return 0;
        } else {
            return W1mag - W4mag;
        }
    }

    public double getW2_W3() {
        if (W2mag == 0 || W3mag == 0) {
            return 0;
        } else {
            return W2mag - W3mag;
        }
    }

    public double getW3_W4() {
        if (W3mag == 0 || W4mag == 0) {
            return 0;
        } else {
            return W3mag - W4mag;
        }
    }

}
