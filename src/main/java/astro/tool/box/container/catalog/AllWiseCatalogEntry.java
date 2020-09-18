package astro.tool.box.container.catalog;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.util.Comparators.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AllWiseCatalogEntry implements CatalogEntry {

    public static final String CATALOG_NAME = "AllWISE";

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

    // Most likely spectral type
    private String spt;

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    private Map<String, Integer> columns;

    private String[] values;

    public AllWiseCatalogEntry() {
    }

    public AllWiseCatalogEntry(Map<String, Integer> columns, String[] values) {
        this.columns = columns;
        this.values = values;
        sourceId = values[columns.get("designation")];
        ra = toDouble(values[columns.get("ra")]);
        dec = toDouble(values[columns.get("dec")]);
        W1mag = toDouble(values[columns.get("w1mpro")]);
        W1_err = toDouble(values[columns.get("w1sigmpro")]);
        W2mag = toDouble(values[columns.get("w2mpro")]);
        W2_err = toDouble(values[columns.get("w2sigmpro")]);
        W3mag = toDouble(values[columns.get("w3mpro")]);
        W3_err = toDouble(values[columns.get("w3sigmpro")]);
        W4mag = toDouble(values[columns.get("w4mpro")]);
        W4_err = toDouble(values[columns.get("w4sigmpro")]);
        ra_pm = toDouble(values[columns.get("ra_pm")]);
        dec_pm = toDouble(values[columns.get("dec_pm")]);
        pmra = toDouble(values[columns.get("pmra")]);
        pmra_err = toDouble(values[columns.get("sigpmra")]);
        pmdec = toDouble(values[columns.get("pmdec")]);
        pmdec_err = toDouble(values[columns.get("sigpmdec")]);
        cc_flags = values[columns.get("cc_flags")];
        ext_flg = toInteger(values[columns.get("ext_flg")]);
        var_flg = values[columns.get("var_flg")];
        ph_qual = values[columns.get("ph_qual")];
        Jmag = toDouble(values[columns.get("j_m_2mass")]);
        J_err = toDouble(values[columns.get("j_msig_2mass")]);
        Hmag = toDouble(values[columns.get("h_m_2mass")]);
        H_err = toDouble(values[columns.get("h_msig_2mass")]);
        Kmag = toDouble(values[columns.get("k_m_2mass")]);
        K_err = toDouble(values[columns.get("k_msig_2mass")]);
    }

    @Override
    public CatalogEntry copy() {
        return new AllWiseCatalogEntry(columns, values);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("source id", sourceId, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W1 (mag)", roundTo3DecNZ(W1mag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("W1 err", roundTo3DecNZ(W1_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W2 (mag)", roundTo3DecNZ(W2mag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("W2 err", roundTo3DecNZ(W2_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W3 (mag)", roundTo3DecNZ(W3mag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("W3 err", roundTo3DecNZ(W3_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W4 (mag)", roundTo3DecNZ(W4mag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("W4 err", roundTo3DecNZ(W4_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmra (mas/yr)", roundTo0DecNZ(pmra), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmra err", roundTo0DecNZ(pmra_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmdec (mas/yr)", roundTo0DecNZ(pmdec), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmdec err", roundTo0DecNZ(pmdec_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("cc flags", cc_flags, Alignment.LEFT, getStringComparator(), createToolTip_cc_flags()));
        catalogElements.add(new CatalogElement("ext. flag", String.valueOf(ext_flg), Alignment.RIGHT, getIntegerComparator(), createToolTip_ext_flg()));
        catalogElements.add(new CatalogElement("var. flag", var_flg, Alignment.LEFT, getStringComparator(), createToolTip_var_flg()));
        catalogElements.add(new CatalogElement("ph. qual.", ph_qual, Alignment.LEFT, getStringComparator(), createToolTip_ph_qual()));
        catalogElements.add(new CatalogElement("J (mag)", roundTo3DecNZ(Jmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("J err", roundTo3DecNZ(J_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("H (mag)", roundTo3DecNZ(Hmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("H err", roundTo3DecNZ(H_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("K (mag)", roundTo3DecNZ(Kmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("K err", roundTo3DecNZ(K_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W1-W2", roundTo3DecNZ(getW1_W2()), Alignment.RIGHT, getDoubleComparator(), true, true));
        catalogElements.add(new CatalogElement("W2-W3", roundTo3DecNZ(getW2_W3()), Alignment.RIGHT, getDoubleComparator(), true, true));
        catalogElements.add(new CatalogElement("J-W2", roundTo3DecNZ(getJ_W2()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("J-H", roundTo3DecNZ(getJ_H()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("H-K", roundTo3DecNZ(getH_K()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("J-K", roundTo3DecNZ(getJ_K()), Alignment.RIGHT, getDoubleComparator(), false, true));
    }

    public static String createToolTip_cc_flags() {
        StringBuilder toolTip = new StringBuilder();
        toolTip.append("<b>Contamination and confusion flags (cc flags):</b>").append(LINE_BREAK);
        toolTip.append("D,d - Diffraction spike. Source may be a spurious detection of (D) or contaminated by (d) a diffraction spike from a nearby bright star on the same image, or").append(LINE_BREAK);
        toolTip.append("P,p - Persistence. Source may be a spurious detection of (P) or contaminated by (p) a short-term latent image left by a bright source, or").append(LINE_BREAK);
        toolTip.append("H,h - Halo. Source may be a spurious detection of (H) or contaminated by (h) the scattered light halo surrounding a nearby bright source, or").append(LINE_BREAK);
        toolTip.append("O,o (letter \"o\") - Optical ghost. Source may be a spurious detection of (O) or contaminated by (o) an optical ghost image caused by a nearby bright source, or").append(LINE_BREAK);
        toolTip.append("0 (number zero) - Source is unaffected by known artifacts. ");
        return toolTip.toString();
    }

    public static String createToolTip_ext_flg() {
        StringBuilder toolTip = new StringBuilder();
        toolTip.append("<b>Extended source flag (ext. flag):</b>").append(LINE_BREAK);
        toolTip.append("0 - The source shape is consistent with a point-source and the source is not associated with or superimposed on a 2MASS XSC source.").append(LINE_BREAK);
        toolTip.append("1 - The profile-fit photometry goodness-of-fit, w?rchi2, is &gt; 3.0 in one or more bands.").append(LINE_BREAK);
        toolTip.append("2 - The source falls within the extrapolated isophotal footprint of a 2MASS XSC source.").append(LINE_BREAK);
        toolTip.append("3 - The profile-fit photometry goodness-of-fit, w?rchi2, is &gt; 3.0 in one or more bands, and The source falls within the extrapolated isophotal footprint of a 2MASS XSC source.").append(LINE_BREAK);
        toolTip.append("4 - The source position falls within 5\" of a 2MASS XSC source.").append(LINE_BREAK);
        toolTip.append("5 - The profile-fit photometry goodness-of-fit, w?rchi2, is &gt; 3.0 in one or more bands, and the source position falls within 5\" of a 2MASS XSC source.");
        return toolTip.toString();
    }

    public static String createToolTip_var_flg() {
        StringBuilder toolTip = new StringBuilder();
        toolTip.append("<b>Variability flag (var. flag):</b>").append(LINE_BREAK);
        toolTip.append("A value of \"n\" in a band indicates insufficient or inadequate data to make a determination of possible variability.").append(LINE_BREAK);
        toolTip.append("Values of \"0\" through \"9\" indicate increasing probabilities of variation.").append(LINE_BREAK);
        toolTip.append("Values of \"0\" through \"5\" are most likely not variables.").append(LINE_BREAK);
        toolTip.append("Values of \"6\" and \"7\" are likely flux variables, but are the most susceptible to false-positive variability.").append(LINE_BREAK);
        toolTip.append("Values greater than \"7\" have the highest probability of being true flux variables in a band.");
        return toolTip.toString();
    }

    public static String createToolTip_ph_qual() {
        StringBuilder toolTip = new StringBuilder();
        toolTip.append("<b>Photometric quality flag (ph. qual.):</b>").append(LINE_BREAK);
        toolTip.append("A - Source is detected in this band with a flux signal-to-noise ratio w?snr &gt; 10.").append(LINE_BREAK);
        toolTip.append("B - Source is detected in this band with a flux signal-to-noise ratio 3 &lt; w?snr &lt; 10.").append(LINE_BREAK);
        toolTip.append("C - Source is detected in this band with a flux signal-to-noise ratio 2 &lt; w?snr &lt; 3.").append(LINE_BREAK);
        toolTip.append("U - Upper limit on magnitude. Source measurement has w?snr &lt; 2. The profile-fit magnitude w?mpro is a 95% confidence upper limit.").append(LINE_BREAK);
        toolTip.append("X - A profile-fit measurement was not possible at this location in this band. The value of w?mpro and w?sigmpro will be \"null\" in this band.").append(LINE_BREAK);
        toolTip.append("Z - A profile-fit source flux measurement was made at this location, but the flux uncertainty could not be measured.");
        return toolTip.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AllWiseCatalogEntry{sourceId=").append(sourceId);
        sb.append(", ra=").append(ra);
        sb.append(", dec=").append(dec);
        sb.append(", W1mag=").append(W1mag);
        sb.append(", W1_err=").append(W1_err);
        sb.append(", W2mag=").append(W2mag);
        sb.append(", W2_err=").append(W2_err);
        sb.append(", W3mag=").append(W3mag);
        sb.append(", W3_err=").append(W3_err);
        sb.append(", W4mag=").append(W4mag);
        sb.append(", W4_err=").append(W4_err);
        sb.append(", pmra=").append(pmra);
        sb.append(", pmra_err=").append(pmra_err);
        sb.append(", pmdec=").append(pmdec);
        sb.append(", pmdec_err=").append(pmdec_err);
        sb.append(", cc_flags=").append(cc_flags);
        sb.append(", ext_flg=").append(ext_flg);
        sb.append(", var_flg=").append(var_flg);
        sb.append(", ph_qual=").append(ph_qual);
        sb.append(", Jmag=").append(Jmag);
        sb.append(", J_err=").append(J_err);
        sb.append(", Hmag=").append(Hmag);
        sb.append(", H_err=").append(H_err);
        sb.append(", Kmag=").append(Kmag);
        sb.append(", K_err=").append(K_err);
        sb.append(", ra_pm=").append(ra_pm);
        sb.append(", dec_pm=").append(dec_pm);
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
        hash = 17 * hash + Objects.hashCode(this.sourceId);
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
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new AllWiseCatalogEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return CATALOG_NAME;
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
        String columnValues = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + roundTo3Dec(W1mag) + "," + roundTo3Dec(W1_err) + "," + roundTo3Dec(W2mag) + "," + roundTo3Dec(W2_err) + "," + roundTo3Dec(W3mag) + "," + roundTo3Dec(W3_err) + "," + roundTo3Dec(W4mag) + "," + roundTo3Dec(W4_err) + "," + roundTo0Dec(pmra) + "," + roundTo0Dec(pmra_err) + "," + roundTo0Dec(pmdec) + "," + roundTo0Dec(pmdec_err) + "," + cc_flags + "," + ext_flg + "," + var_flg + "," + ph_qual + "," + roundTo3Dec(Jmag) + "," + roundTo3Dec(J_err) + "," + roundTo3Dec(Hmag) + "," + roundTo3Dec(H_err) + "," + roundTo3Dec(Kmag) + "," + roundTo3Dec(K_err) + "," + roundTo3Dec(getW1_W2()) + "," + roundTo3Dec(getW2_W3()) + "," + roundTo3Dec(getJ_W2()) + "," + roundTo3Dec(getJ_H()) + "," + roundTo3Dec(getH_K()) + "," + roundTo3Dec(getJ_K());
        return columnValues.split(",", 32);
    }

    @Override
    public String[] getColumnTitles() {
        String columnTitles = "dist (arcsec),source id,ra,dec,W1 (mag),W1 err,W2 (mag),W2 err,W3 (mag),W3 err,W4 (mag),W4 err,pmra (mas/yr),pmra err,pmdec (mas/yr),pmdec err,cc flags,ext. flag,var. flag,ph. qual.,J (mag),J err,H (mag),H err,K (mag),K err,W1-W2,W2-W3,J-W2,J-H,H-K,J-K";
        return columnTitles.split(",", 32);
    }

    @Override
    public void applyExtinctionCorrection(Map<String, Double> extinctionsByBand) {
        if (Jmag != 0) {
            Jmag = Jmag - extinctionsByBand.get(TWO_MASS_J);
        }
        if (Hmag != 0) {
            Hmag = Hmag - extinctionsByBand.get(TWO_MASS_H);
        }
        if (Kmag != 0) {
            Kmag = Kmag - extinctionsByBand.get(TWO_MASS_K);
        }
        if (W1mag != 0) {
            W1mag = W1mag - extinctionsByBand.get(WISE_1);
        }
        if (W2mag != 0) {
            W2mag = W2mag - extinctionsByBand.get(WISE_2);
        }
    }

    @Override
    public Map<Band, Double> getBands() {
        Map<Band, Double> bands = new LinkedHashMap<>();
        bands.put(Band.J, Jmag);
        bands.put(Band.H, Hmag);
        bands.put(Band.K, Kmag);
        bands.put(Band.W1, W1mag);
        bands.put(Band.W2, W2mag);
        return bands;
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
    public String getMagnitudes() {
        return String.format("W1=%s; W2=%s; W3=%s; W4=%s", roundTo3DecNZ(W1mag), roundTo3DecNZ(W2mag), roundTo3DecNZ(W3mag), roundTo3DecNZ(W4mag));
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
