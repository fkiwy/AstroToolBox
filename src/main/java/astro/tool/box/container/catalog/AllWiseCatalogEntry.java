package astro.tool.box.container.catalog;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.util.Comparators.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import static astro.tool.box.util.Utils.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.TapProvider;
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

    // Instrumental profile-fit photometry S/N ratio, band 1
    private double W1_snr;

    // Instrumental profile-fit photometry S/N ratio, band 2
    private double W2_snr;

    // Instrumental profile-fit photometry S/N ratio, band 3
    private double W3_snr;

    // Instrumental profile-fit photometry S/N ratio, band 4
    private double W4_snr;

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
        if (TapProvider.IRSA.equals(getTapProvider())) {
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
            W1_snr = toDouble(values[columns.get("w1snr")]);
            W2_snr = toDouble(values[columns.get("w2snr")]);
            W3_snr = toDouble(values[columns.get("w3snr")]);
            W4_snr = toDouble(values[columns.get("w4snr")]);
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
        } else {
            sourceId = values[columns.get("AllWISE")];
            ra = toDouble(values[columns.get("RAJ2000")]);
            dec = toDouble(values[columns.get("DEJ2000")]);
            W1mag = toDouble(values[columns.get("W1mag")]);
            W1_err = toDouble(values[columns.get("e_W1mag")]);
            W2mag = toDouble(values[columns.get("W2mag")]);
            W2_err = toDouble(values[columns.get("e_W2mag")]);
            W3mag = toDouble(values[columns.get("W3mag")]);
            W3_err = toDouble(values[columns.get("e_W3mag")]);
            W4mag = toDouble(values[columns.get("W4mag")]);
            W4_err = toDouble(values[columns.get("e_W4mag")]);
            W1_snr = toDouble(values[columns.get("snr1")]);
            W2_snr = toDouble(values[columns.get("snr2")]);
            W3_snr = toDouble(values[columns.get("snr3")]);
            W4_snr = toDouble(values[columns.get("snr4")]);
            ra_pm = toDouble(values[columns.get("RA_pm")]);
            dec_pm = toDouble(values[columns.get("DE_pm")]);
            pmra = toDouble(values[columns.get("pmRA")]);
            pmra_err = toDouble(values[columns.get("e_pmRA")]);
            pmdec = toDouble(values[columns.get("pmDE")]);
            pmdec_err = toDouble(values[columns.get("e_pmDE")]);
            cc_flags = values[columns.get("ccf")];
            ext_flg = toInteger(values[columns.get("ex")]);
            var_flg = values[columns.get("var")];
            ph_qual = values[columns.get("qph")];
            Jmag = toDouble(values[columns.get("Jmag")]);
            J_err = toDouble(values[columns.get("e_Jmag")]);
            Hmag = toDouble(values[columns.get("Hmag")]);
            H_err = toDouble(values[columns.get("e_Hmag")]);
            Kmag = toDouble(values[columns.get("Kmag")]);
            K_err = toDouble(values[columns.get("e_Kmag")]);
        }
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
        catalogElements.add(new CatalogElement("W1 snr", roundTo1DecNZ(W1_snr), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W2 snr", roundTo1DecNZ(W2_snr), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W3 snr", roundTo1DecNZ(W3_snr), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W4 snr", roundTo1DecNZ(W4_snr), Alignment.RIGHT, getDoubleComparator()));
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
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.sourceId);
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
        return java.awt.Color.GREEN.darker();
    }

    @Override
    public String getCatalogUrl() {
        if (TapProvider.IRSA.equals(getTapProvider())) {
            return createIrsaUrl(ra, dec, searchRadius / DEG_ARCSEC, "allwise_p3as_psd");
        } else {
            return createVizieRUrl(ra, dec, searchRadius / DEG_ARCSEC, "II/328/allwise", "RAJ2000", "DEJ2000");
        }
    }

    @Override
    public String[] getColumnValues() {
        String columnValues = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + roundTo3Dec(W1mag) + "," + roundTo3Dec(W1_err) + "," + roundTo3Dec(W2mag) + "," + roundTo3Dec(W2_err) + "," + roundTo3Dec(W3mag) + "," + roundTo3Dec(W3_err) + "," + roundTo3Dec(W4mag) + "," + roundTo3Dec(W4_err) + "," + roundTo1Dec(W1_snr) + "," + roundTo1Dec(W2_snr) + "," + roundTo1Dec(W3_snr) + "," + roundTo1Dec(W4_snr) + "," + roundTo0Dec(pmra) + "," + roundTo0Dec(pmra_err) + "," + roundTo0Dec(pmdec) + "," + roundTo0Dec(pmdec_err) + "," + cc_flags + "," + ext_flg + "," + var_flg + "," + ph_qual + "," + roundTo3Dec(Jmag) + "," + roundTo3Dec(J_err) + "," + roundTo3Dec(Hmag) + "," + roundTo3Dec(H_err) + "," + roundTo3Dec(Kmag) + "," + roundTo3Dec(K_err) + "," + roundTo3Dec(getW1_W2()) + "," + roundTo3Dec(getW2_W3()) + "," + roundTo3Dec(getJ_W2()) + "," + roundTo3Dec(getJ_H()) + "," + roundTo3Dec(getH_K()) + "," + roundTo3Dec(getJ_K());
        return columnValues.split(",", -1);
    }

    @Override
    public String[] getColumnTitles() {
        String columnTitles = "dist (arcsec),source id,ra,dec,W1 (mag),W1 err,W2 (mag),W2 err,W3 (mag),W3 err,W4 (mag),W4 err,W1 snr,W2 snr,W3 snr,W4 snr,pmra (mas/yr),pmra err,pmdec (mas/yr),pmdec err,cc flags,ext. flag,var. flag,ph. qual.,J (mag),J err,H (mag),H err,K (mag),K err,W1-W2,W2-W3,J-W2,J-H,H-K,J-K";
        return columnTitles.split(",", -1);
    }

    @Override
    public void applyExtinctionCorrection(Map<String, Double> extinctionsByBand) {
        if (W1mag != 0) {
            W1mag = W1mag - extinctionsByBand.get(WISE_1);
        }
        if (W2mag != 0) {
            W2mag = W2mag - extinctionsByBand.get(WISE_2);
        }
        if (Jmag != 0) {
            Jmag = Jmag - extinctionsByBand.get(TWO_MASS_J);
        }
        if (Hmag != 0) {
            Hmag = Hmag - extinctionsByBand.get(TWO_MASS_H);
        }
        if (Kmag != 0) {
            Kmag = Kmag - extinctionsByBand.get(TWO_MASS_K);
        }
    }

    @Override
    public Map<Band, NumberPair> getBands() {
        Map<Band, NumberPair> bands = new LinkedHashMap<>();
        if (W1_err != 0) {
            bands.put(Band.W1, new NumberPair(W1mag, W1_err));
        }
        if (W2_err != 0) {
            bands.put(Band.W2, new NumberPair(W2mag, W2_err));
        }
        if (W3_err != 0) {
            bands.put(Band.W3, new NumberPair(W3mag, W3_err));
        }
        if (J_err != 0) {
            bands.put(Band.J, new NumberPair(Jmag, J_err));
        }
        if (H_err != 0) {
            bands.put(Band.H, new NumberPair(Hmag, H_err));
        }
        if (K_err != 0) {
            bands.put(Band.K, new NumberPair(Kmag, K_err));
        }
        return bands;
    }

    @Override
    public Map<Color, Double> getColors(boolean toVega) {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.W1_W2, getW1_W2());
        colors.put(Color.W1_W3, getW1_W3());
        colors.put(Color.W1_W4, getW1_W4());
        colors.put(Color.J_H, getJ_H());
        colors.put(Color.H_K, getH_K());
        colors.put(Color.J_K, getJ_K());
        colors.put(Color.K_W1, getK_W1());
        return colors;
    }

    @Override
    public String getMagnitudes() {
        StringBuilder mags = new StringBuilder();
        if (W1mag != 0) {
            mags.append("W1=").append(roundTo3DecNZ(W1mag)).append(" ");
        }
        if (W2mag != 0) {
            mags.append("W2=").append(roundTo3DecNZ(W2mag)).append(" ");
        }
        if (W3mag != 0) {
            mags.append("W3=").append(roundTo3DecNZ(W3mag)).append(" ");
        }
        if (W4mag != 0) {
            mags.append("W4=").append(roundTo3DecNZ(W4mag)).append(" ");
        }
        if (Jmag != 0) {
            mags.append("J=").append(roundTo3DecNZ(Jmag)).append(" ");
        }
        if (Hmag != 0) {
            mags.append("H=").append(roundTo3DecNZ(Hmag)).append(" ");
        }
        if (Kmag != 0) {
            mags.append("K=").append(roundTo3DecNZ(Kmag)).append(" ");
        }
        return mags.toString();
    }

    @Override
    public String getPhotometry() {
        StringBuilder mags = new StringBuilder();
        if (W1mag != 0) {
            mags.append(roundTo3DecNZ(W1mag)).append(",").append(roundTo3DecNZ(W1_err)).append(",");
        } else {
            mags.append(",,");
        }
        if (W2mag != 0) {
            mags.append(roundTo3DecNZ(W2mag)).append(",").append(roundTo3DecNZ(W2_err)).append(",");
        } else {
            mags.append(",,");
        }
        if (W3mag != 0) {
            mags.append(roundTo3DecNZ(W3mag)).append(",").append(roundTo3DecNZ(W3_err)).append(",");
        } else {
            mags.append(",,");
        }
        if (W4mag != 0) {
            mags.append(roundTo3DecNZ(W4mag)).append(",").append(roundTo3DecNZ(W4_err)).append(",");
        } else {
            mags.append(",,");
        }
        return mags.toString();
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

    public double getRa_pm() {
        return ra_pm;
    }

    public double getDec_pm() {
        return dec_pm;
    }

    public String getCc_flags() {
        return cc_flags;
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

    public double getW1mag() {
        return W1mag;
    }

    public double getW2mag() {
        return W2mag;
    }

    public double getW3mag() {
        return W3mag;
    }

    public double getW4mag() {
        return W4mag;
    }

    public double getJmag() {
        return Jmag;
    }

    public double getHmag() {
        return Hmag;
    }

    public double getKmag() {
        return Kmag;
    }

}
