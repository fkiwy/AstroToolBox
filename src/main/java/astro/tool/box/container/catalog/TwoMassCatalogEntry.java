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
import astro.tool.box.enumeration.JColor;
import astro.tool.box.enumeration.TapProvider;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TwoMassCatalogEntry implements CatalogEntry {

    public static final String CATALOG_NAME = "2MASS";

    public static final String NEW_LINE = "<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";

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

    // Most likely spectral type
    private String spt;

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    private Map<String, Integer> columns;

    private String[] values;

    public TwoMassCatalogEntry() {
    }

    public TwoMassCatalogEntry(Map<String, Integer> columns, String[] values) {
        this.columns = columns;
        this.values = values;
        if (TapProvider.IRSA.equals(getTapProvider())) {
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
        } else {
            sourceId = values[columns.get("2MASS")].trim();
            ra = toDouble(values[columns.get("RAJ2000")]);
            dec = toDouble(values[columns.get("DEJ2000")]);
            Jmag = toDouble(values[columns.get("Jmag")]);
            J_err = toDouble(values[columns.get("e_Jmag")]);
            Hmag = toDouble(values[columns.get("Hmag")]);
            H_err = toDouble(values[columns.get("e_Hmag")]);
            Kmag = toDouble(values[columns.get("Kmag")]);
            K_err = toDouble(values[columns.get("e_Kmag")]);
            xdate = values[columns.get("Date")];
            ph_qual = values[columns.get("Qflg")];
            rd_flg = values[columns.get("Rflg")];
            bl_flg = values[columns.get("Bflg")];
            cc_flg = values[columns.get("Cflg")];
            gal_contam = toInteger(values[columns.get("Xflg")]);
            mp_flg = toInteger(values[columns.get("Aflg")]);
        }
    }

    @Override
    public CatalogEntry copy() {
        return new TwoMassCatalogEntry(columns, values);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("source id", sourceId, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("observation date", xdate, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("ph. qual.", ph_qual, Alignment.LEFT, getStringComparator(), createToolTip_ph_qual()));
        catalogElements.add(new CatalogElement("read flag", rd_flg, Alignment.LEFT, getStringComparator(), createToolTip_rd_flg()));
        catalogElements.add(new CatalogElement("blend flag", bl_flg, Alignment.LEFT, getStringComparator(), createToolTip_bl_flg()));
        catalogElements.add(new CatalogElement("cc flags", cc_flg, Alignment.LEFT, getStringComparator(), createToolTip_cc_flg()));
        catalogElements.add(new CatalogElement("ext. flag", String.valueOf(gal_contam), Alignment.RIGHT, getIntegerComparator(), createToolTip_gal_contam()));
        catalogElements.add(new CatalogElement("minor planet flag", String.valueOf(mp_flg), Alignment.RIGHT, getIntegerComparator(), createToolTip_mp_flg()));
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

    public static String createToolTip_ph_qual() {
        StringBuilder toolTip = new StringBuilder();
        toolTip.append("<b>Photometric quality flag (ph. qual.):</b>").append(LINE_BREAK);
        toolTip.append("\"X\" - There is a detection at this location, but no valid brightness estimate can be extracted using any algorithm. rd_flg=\"9\" and default magnitude is null.").append(LINE_BREAK);
        toolTip.append("\"U\" - Upper limit on magnitude. Source is not detected in this band (rd_flg=\"0\"), or it is detected, but not resolved in a consistent fashion with other bands (rd_flg=\"6\").")
                .append(NEW_LINE).append("A value of ph_qual=\"U\" does not necessarily mean that there is no flux detected in this band at the location.")
                .append(NEW_LINE).append("Whether or not flux has been detected can be determined from the value of rd_flg. When rd_flg=\"0\", no flux has been detected.")
                .append(NEW_LINE).append("When rd_flg=\"6\", flux has been detected at the location where the images were not deblended consistently in all three bands (JHKs).").append(LINE_BREAK);
        toolTip.append("\"F\" - This category includes rd_flg=\"1\" or rd_flg=\"3\" sources where a reliable estimate of the photometric error, [jhk]_cmsig, could not be determined.")
                .append(NEW_LINE).append("The uncertainties reported for these sources in [jhk]_cmsig and [jhk]_msigcom are flags and have numeric values &gt; 8.0.").append(LINE_BREAK);
        toolTip.append("\"E\" - This category includes detections where the goodness-of-fit quality of the profile-fit photometry was very poor (rd_flg=2 and [jhk]psf_chi &gt; 10.0),")
                .append(NEW_LINE).append("or detections where psf fit photometry did not converge and an aperture magnitude is reported (rd_flg=4),")
                .append(NEW_LINE).append("or detections where the number of frames was too small in relation to the number of frames in which a detection was geometrically possible (rd_flg=\"1\" or rd_flg=\"2\").").append(LINE_BREAK);
        toolTip.append("\"A\" - Detections in any brightness regime where valid measurements were made (rd_flg=\"1\",\"2\" or \"3\") with [jhk]_snr &gt; 10 AND [jhk]_cmsig &lt; 0.10857.").append(LINE_BREAK);
        toolTip.append("\"B\" - Detections in any brightness regime where valid measurements were made (rd_flg=\"1\",\"2\" or \"3\") with [jhk]_snr &gt; 7 AND [jhk]_cmsig &lt; 0.15510.").append(LINE_BREAK);
        toolTip.append("\"C\" - Detections in any brightness regime where valid measurements were made (rd_flg=\"1\",\"2\" or \"3\") with [jhk]_snr &gt; 5 AND [jhk]_cmsig &lt; 0.21714.").append(LINE_BREAK);
        toolTip.append("\"D\" - Detections in any brightness regime where valid measurements were made (rd_flg=\"1\",\"2\" or \"3\") with no [jhk]_snr or [jhk]_cmsig requirement.");
        return toolTip.toString();
    }

    public static String createToolTip_rd_flg() {
        StringBuilder toolTip = new StringBuilder();
        toolTip.append("<b>Read flag:</b>").append(LINE_BREAK);
        toolTip.append("\"0\" - Source is not detected in this band. The default magnitude is the 95% confidence upper limit derived from a 4\" radius aperture measurement taken at the position")
                .append(NEW_LINE).append(" of the source on the Atlas Image. The sky background is estimated in an annular region with inner radius of 14\" and outer radius of 20\".").append(LINE_BREAK);
        toolTip.append("\"1\" - The default magnitude is derived from aperture photometry measurements on the 51 ms \"Read_1\" exposures.")
                .append(NEW_LINE).append("The aperture radius is 4\", with the sky background measured in an annulus with an inner radius of 14\" and an outer radius of 20\".")
                .append(NEW_LINE).append("Used for sources that saturate one or more of the 1.3s \"Read_2\" exposures, but are not saturated on at least one of the 51 ms \"Read_1\" frames.").append(LINE_BREAK);
        toolTip.append("\"2\" - The default magnitude is derived from a profile-fitting measurement made on the 1.3 sec \"Read_2\" exposures.")
                .append(NEW_LINE).append("The profile-fit magnitudes are normalized to curve-of-growth-corrected aperture magnitudes.")
                .append(NEW_LINE).append("This is the most common type in the PSC, and is used for sources that have no saturated pixels in any of the 1.3 sec exposures.").append(LINE_BREAK);
        toolTip.append("\"3\" - The default magnitude is derived from a 1-d radial profile fitting measurement made on the 51 ms \"Read_1\" exposures.")
                .append(NEW_LINE).append("Used for very bright sources that saturate all of the 51 ms \"Read 1\" exposures.").append(LINE_BREAK);
        toolTip.append("\"4\" - The default magnitude is derived from curve-of-growth-corrected 4\" radius aperture photometry measurements on the 1.3 s \"Read_2\" exposures.")
                .append(NEW_LINE).append("This is used for sources that are not saturated in any of the Read_2 frames,")
                .append(NEW_LINE).append("but where the profile-fitting measurements fail to converge to a solution. These magnitudes are the same as the standard aperture magnitudes (j_m_stdap, h_m_stdap, k_m_stdap),")
                .append(NEW_LINE).append("but when they are the default magnitudes, it generally implies that they are low quality measurements.").append(LINE_BREAK);
        toolTip.append("\"6\" - The default magnitude is the 95% confidence upper limit derived from a 4\" radius aperture measurement taken at the position of the source on the Atlas Image.")
                .append(NEW_LINE).append("The sky background is estimated in an annular region with inner radius of 14\" and outer radius of 20\".")
                .append(NEW_LINE).append("This is used for pairs of sources which are detected and resolved in another band, but are detected and not resolved in this band.")
                .append(NEW_LINE).append("This differs from a rd_flg=\"0\" because in this case there is a detection of the source in this band, but it is not consistently resolved across all bands.").append(LINE_BREAK);
        toolTip.append("\"9\" - The default magnitude is the 95% confidence upper limit derived from a 4\" radius aperture measurement taken at the position of the source on the Atlas Image.")
                .append(NEW_LINE).append("The sky background is estimated in an annular region with inner radius of 14\" and outer radius of 20\".")
                .append(NEW_LINE).append("This is used for sources that were nominally detected in this band, but which could not have a useful brightness measurement from either profile fitting or aperture photometry.")
                .append(NEW_LINE).append("This often occurs in highly confused regions, or very near Tile edges where a significant fraction of the measurement aperture of sky annulus falls off the focal plane.");
        return toolTip.toString();
    }

    public static String createToolTip_bl_flg() {
        StringBuilder toolTip = new StringBuilder();
        toolTip.append("<b>Blend flag:</b>").append(LINE_BREAK);
        toolTip.append("\"0\" - Source is not detected, or is inconsistently deblended in that band.").append(LINE_BREAK);
        toolTip.append("\"1\" - One component was fit to the source in R_2 profile-fitting photometry (rd_flg=\"2\"), or default magnitudes are from aperture photometry (rd_flg=\"1\" or \"4\")")
                .append(NEW_LINE).append("or saturated star 1-d radial profile-fitting (rd_flg=\"3\").").append(LINE_BREAK);
        toolTip.append("\"&gt;1\" - More than one component was fit simultaneously during R2 profile-fit photometry, where the value of the field is the number of components simultaneously fit.")
                .append(NEW_LINE).append("The maximum number of components is 7 in any band for the PSC, so this bl_flg is always a three character flag.")
                .append(NEW_LINE).append("Multi-component fitting occurs only for profile-fitting, and only when more than one detection is found within ~5\".")
                .append(NEW_LINE).append("Single detections that are not well-fit by a single PSF are not split.");
        return toolTip.toString();
    }

    public static String createToolTip_cc_flg() {
        StringBuilder toolTip = new StringBuilder();
        toolTip.append("<b>Contamination and confusion flags (cc flags):</b>").append(LINE_BREAK);
        toolTip.append("\"p\" = Persistence. Source may be contaminated by a latent image left by a nearby bright star.").append(LINE_BREAK);
        toolTip.append("\"c\" = Photometric Confusion. Source photometry is biased by a nearby star that has contaminated the background estimation.")
                .append(NEW_LINE).append("This is very common in high source density regions.").append(LINE_BREAK);
        toolTip.append("\"d\" = Diffraction spike confusion. Source may be contaminated by a diffraction spike from a nearby star.").append(LINE_BREAK);
        toolTip.append("\"s\" = Electronic stripe. Source measurement may be contaminated by a stripe from a nearby bright star.").append(LINE_BREAK);
        toolTip.append("\"b\" = Bandmerge confusion. In the process of merging detections in the different bands for this source,")
                .append(NEW_LINE).append("there was more than one possible match between the different band components.")
                .append(NEW_LINE).append("This occurs in regions of very high source density, or when multiple sources were split in one band but not another.").append(LINE_BREAK);
        toolTip.append("\"0\" = Source is unaffected by known artifacts, or is not detected in the band.");
        return toolTip.toString();
    }

    public static String createToolTip_gal_contam() {
        StringBuilder toolTip = new StringBuilder();
        toolTip.append("<b>Extended source flag (ext. flag):</b>").append(LINE_BREAK);
        toolTip.append("\"0\" - Source does not fall within the elliptical profile of an extended source with semi-major axis &gt; 10'',")
                .append(NEW_LINE).append("or it is not identified exactly with an XSC source with semi-major axis &gt; 10''.")
                .append(NEW_LINE).append("However, the source may correspond exactly to a smaller XSC source.").append(LINE_BREAK);
        toolTip.append("\"1\" - Source is resolved by 2MASS, and is equivalent to a source in the XSC that has a semi-major axis &gt; 10'' in size.").append(LINE_BREAK);
        toolTip.append("\"2\" - Source falls within the elliptical boundary of an XSC source that has a semi-major axis &gt; 10'' in size.");
        return toolTip.toString();
    }

    public static String createToolTip_mp_flg() {
        StringBuilder toolTip = new StringBuilder();
        toolTip.append("<b>Minor planet flag:</b>").append(LINE_BREAK);
        toolTip.append("\"0\" - Source is not associated with a known solar system object.").append(LINE_BREAK);
        toolTip.append("\"1\" - Source is associated with the predicted position of a known solar system object.");
        return toolTip.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.sourceId);
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
        return JColor.ORANGE.val;
    }

    @Override
    public String getCatalogUrl() {
        if (TapProvider.IRSA.equals(getTapProvider())) {
            return createIrsaUrl(ra, dec, searchRadius / DEG_ARCSEC, "fp_psc");
        } else {
            return createVizieRUrl(ra, dec, searchRadius / DEG_ARCSEC, "II/246/out", "RAJ2000", "DEJ2000");
        }
    }

    @Override
    public String[] getColumnValues() {
        String columnValues = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + xdate + "," + ph_qual + "," + rd_flg + "," + bl_flg + "," + cc_flg + "," + gal_contam + "," + mp_flg + "," + roundTo3Dec(Jmag) + "," + roundTo3Dec(J_err) + "," + roundTo3Dec(Hmag) + "," + roundTo3Dec(H_err) + "," + roundTo3Dec(Kmag) + "," + roundTo3Dec(K_err) + "," + roundTo3Dec(getJ_H()) + "," + roundTo3Dec(getH_K()) + "," + roundTo3Dec(getJ_K());
        return columnValues.split(",", -1);
    }

    @Override
    public String[] getColumnTitles() {
        String columnTitles = "dist (arcsec),source id,ra,dec,observation date,ph. qual.,read flag,blend flag,cc flags,ext. flag,minor planet flag,J (mag),J err,H (mag),H err,K (mag),K err,J-H,H-K,J-K";
        return columnTitles.split(",", -1);
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
    }

    @Override
    public Map<Band, Double> getBands() {
        Map<Band, Double> bands = new LinkedHashMap<>();
        bands.put(Band.J, Jmag);
        bands.put(Band.H, Hmag);
        bands.put(Band.K, Kmag);
        return bands;
    }

    @Override
    public Map<Color, Double> getColors(boolean toVega) {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.J_H, getJ_H());
        colors.put(Color.H_K, getH_K());
        colors.put(Color.J_K, getJ_K());
        return colors;
    }

    @Override
    public String getMagnitudes() {
        StringBuilder mags = new StringBuilder();
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

    public LocalDateTime getObsDate() {
        return LocalDateTime.parse(xdate + "T00:00:00");
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
