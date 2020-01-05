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

public class AllWiseCatalogEntry implements CatalogEntry {

    // Unique WISE source designation
    private String sourceId;

    // Right ascension (J2000)
    private double ra;

    // Declination (J2000)
    private double dec;

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

    // Apparent motion in RA
    private double pmra;

    // Uncertainty in the RA motion estimate
    private double pmra_err;

    // Apparent motion in Dec
    private double pmdec;

    // Uncertainty in the Dec motion estimate
    private double pmdec_err;

    // Prioritized artifacts affecting the source in each band
    private String cc_flags;

    // Probability that source morphology is not consistent with single PSF
    private int ext_flg;

    // Probability that flux varied in any band greater than amount expected from unc.s
    private String var_flg;

    // Photometric quality of each band (A=highest, U=upper limit)
    private String ph_qual;

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

    // Right ascension at epoch MJD=55400.0 (2010.5589) from pff model incl. motion
    private double ra_pm;

    // Declination at epoch MJD=55400.0 (2010.5589) from pff model incl. motion
    private double dec_pm;

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

    public AllWiseCatalogEntry() {
    }

    public AllWiseCatalogEntry(String[] values) {
        sourceId = values[0];
        ra = toDouble(values[1]);
        dec = toDouble(values[2]);
        W1mag = toDouble(values[16]);
        W1_err = toDouble(values[17]);
        W2mag = toDouble(values[20]);
        W2_err = toDouble(values[21]);
        W3mag = toDouble(values[24]);
        W3_err = toDouble(values[25]);
        W4mag = toDouble(values[28]);
        W4_err = toDouble(values[29]);
        ra_pm = toDouble(values[40]);
        dec_pm = toDouble(values[41]);
        pmra = toDouble(values[45]);
        pmra_err = toDouble(values[46]);
        pmdec = toDouble(values[47]);
        pmdec_err = toDouble(values[48]);
        cc_flags = values[55];
        ext_flg = toInteger(values[57]);
        var_flg = values[58];
        ph_qual = values[59];
        Jmag = toDouble(values[288]);
        J_err = toDouble(values[289]);
        Hmag = toDouble(values[290]);
        H_err = toDouble(values[291]);
        Kmag = toDouble(values[292]);
        K_err = toDouble(values[293]);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("sourceId", sourceId, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W1mag", roundTo3DecNZ(W1mag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("W1 err", roundTo3DecNZ(W1_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W2mag", roundTo3DecNZ(W2mag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("W2 err", roundTo3DecNZ(W2_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W3mag", roundTo3DecNZ(W3mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W3 err", roundTo3DecNZ(W3_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W4mag", roundTo3DecNZ(W4mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W4 err", roundTo3DecNZ(W4_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmra (mas/yr)", roundTo0DecNZ(pmra), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmra err", roundTo0DecNZ(pmra_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmdec (mas/yr)", roundTo0DecNZ(pmdec), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmdec err", roundTo0DecNZ(pmdec_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("cc_flags", cc_flags, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("ext_flg", String.valueOf(ext_flg), Alignment.RIGHT, getIntegerComparator()));
        catalogElements.add(new CatalogElement("var_flg", var_flg, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("ph_qual", ph_qual, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("Jmag", roundTo3DecNZ(Jmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("J err", roundTo3DecNZ(J_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Hmag", roundTo3DecNZ(Hmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("H err", roundTo3DecNZ(H_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Kmag", roundTo3DecNZ(Kmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("K err", roundTo3DecNZ(K_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W1-W2", roundTo3DecNZ(getW1_W2()), Alignment.RIGHT, getDoubleComparator(), true, true));
        catalogElements.add(new CatalogElement("W2-W3", roundTo3DecNZ(getW2_W3()), Alignment.RIGHT, getDoubleComparator(), true, true));
        catalogElements.add(new CatalogElement("J-W2", roundTo3DecNZ(getJ_W2()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("J-H", roundTo3DecNZ(getJ_H()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("H-K", roundTo3DecNZ(getH_K()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("J-K", roundTo3DecNZ(getJ_K()), Alignment.RIGHT, getDoubleComparator(), false, true));
    }

    @Override
    public String toString() {
        return "AllWiseCatalogEntry{" + "sourceId=" + sourceId + ", ra=" + ra + ", dec=" + dec + ", W1mag=" + W1mag + ", W1_err=" + W1_err + ", W2mag=" + W2mag + ", W2_err=" + W2_err + ", W3mag=" + W3mag + ", W3_err=" + W3_err + ", W4mag=" + W4mag + ", W4_err=" + W4_err + ", pmra=" + pmra + ", pmra_err=" + pmra_err + ", pmdec=" + pmdec + ", pmdec_err=" + pmdec_err + ", cc_flags=" + cc_flags + ", ext_flg=" + ext_flg + ", var_flg=" + var_flg + ", ph_qual=" + ph_qual + ", Jmag=" + Jmag + ", J_err=" + J_err + ", Hmag=" + Hmag + ", H_err=" + H_err + ", Kmag=" + Kmag + ", K_err=" + K_err + ", ra_pm=" + ra_pm + ", dec_pm=" + dec_pm + ", targetRa=" + targetRa + ", targetDec=" + targetDec + ", searchRadius=" + searchRadius + ", catalogNumber=" + catalogNumber + ", catalogElements=" + catalogElements + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.sourceId);
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
        final AllWiseCatalogEntry other = (AllWiseCatalogEntry) obj;
        return Objects.equals(this.sourceId, other.sourceId);
    }

    @Override
    public CatalogEntry getInstance(String[] values) {
        return new AllWiseCatalogEntry(values);
    }

    @Override
    public String getCatalogName() {
        return "AllWISE";
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.LIGHT_GREEN.val;
    }

    @Override
    public String getCatalogUrl() {
        return createIrsaUrl(ALLWISE_CATALOG_ID, ra, dec, searchRadius / DEG_ARCSEC);
    }

    @Override
    public String[] getColumnValues() {
        String values = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + roundTo3Dec(W1mag) + "," + roundTo3Dec(W1_err) + "," + roundTo3Dec(W2mag) + "," + roundTo3Dec(W2_err) + "," + roundTo3Dec(W3mag) + "," + roundTo3Dec(W3_err) + "," + roundTo3Dec(W4mag) + "," + roundTo3Dec(W4_err) + "," + roundTo0Dec(pmra) + "," + roundTo0Dec(pmra_err) + "," + roundTo0Dec(pmdec) + "," + roundTo0Dec(pmdec_err) + "," + cc_flags + "," + ext_flg + "," + var_flg + "," + ph_qual + "," + roundTo3Dec(Jmag) + "," + roundTo3Dec(J_err) + "," + roundTo3Dec(Hmag) + "," + roundTo3Dec(H_err) + "," + roundTo3Dec(Kmag) + "," + roundTo3Dec(K_err) + "," + roundTo3Dec(getW1_W2()) + "," + roundTo3Dec(getW2_W3()) + "," + roundTo3Dec(getW3_W4()) + "," + roundTo3Dec(getJ_H()) + "," + roundTo3Dec(getH_K()) + "," + roundTo3Dec(getJ_K());
        return values.split(",", 32);
    }

    @Override
    public String[] getColumnTitles() {
        String titles = "dist (arcsec),sourceId,ra,dec,W1mag,W1 err,W2mag,W2 err,W3mag,W3 err,W4mag,W4 err,pmra,pmra err,pmdec,pmdec err,cc_flags,ext_flg,var_flg,ph_qual,Jmag,J err,Hmag,H err,Kmag,K err,W1-W2,W2-W3,W3-W4,J-H,H-K,J-K";
        return titles.split(",", 32);
    }

    @Override
    public Map<Color, Double> getColors() {
        Map<Color, Double> colors = new LinkedHashMap<>();
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
        return pmra;
    }

    @Override
    public double getPmdec() {
        return pmdec;
    }

    @Override
    public double getTargetDistance() {
        return calculateAngularDistance(new NumberPair(targetRa, targetDec), new NumberPair(ra, dec), DEG_ARCSEC);
    }

    public double getRa_pm() {
        return ra_pm;
    }

    public double getDec_pm() {
        return dec_pm;
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
