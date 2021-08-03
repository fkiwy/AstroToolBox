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

public class CatWiseCatalogEntry implements CatalogEntry, ProperMotionQuery, ProperMotionCatalog, Artifact {

    public static final String CATALOG_NAME = "CatWISE2020";

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

    // Instrumental profile-fit photometry S/N ratio, band 1
    private double W1_snr;

    // Instrumental profile-fit photometry S/N ratio, band 2
    private double W2_snr;

    // Apparent motion in RA
    private double pmra;

    // Uncertainty in the RA motion estimate
    private double pmra_err;

    // Apparent motion in Dec
    private double pmdec;

    // Uncertainty in the Dec motion estimate
    private double pmdec_err;

    // Parallax from PM desc-asce elon
    private double par_pm;

    // One-sigma uncertainty in par_pm
    private double par_pmsig;

    // Parallax estimate from stationary solution
    private double par_stat;

    // One-sigma uncertainty in par_stat
    private double par_sigma;

    // Prioritized artifacts affecting the source in each band
    private String cc_flags;

    // UnWISE artifact bitmask contamination flags
    private String ab_flags;

    // Mean observation epoch
    private double meanObsMJD;

    // Right ascension at epoch MJD=56700.0 (2014.118) from pff model incl. motion
    private double ra_pm;

    // Declination at epoch MJD=56700.0 (2014.118) from pff model incl. motion
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

    // Total proper motion
    private double tpm;

    // Most likely spectral type
    private String spt;

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    private Map<String, Integer> columns;

    private String[] values;

    public CatWiseCatalogEntry() {
    }

    public CatWiseCatalogEntry(Map<String, Integer> columns, String[] values) {
        this.columns = columns;
        this.values = values;
        if (TapProvider.IRSA.equals(getTapProvider())) {
            sourceId = values[columns.get("source_name")];
            ra = toDouble(values[columns.get("ra")]);
            dec = toDouble(values[columns.get("dec")]);
            W1mag = toDouble(values[columns.get("w1mpro_pm")]);
            W1_err = toDouble(values[columns.get("w1sigmpro_pm")]);
            W2mag = toDouble(values[columns.get("w2mpro_pm")]);
            W2_err = toDouble(values[columns.get("w2sigmpro_pm")]);
            W1_snr = toDouble(values[columns.get("w1snr_pm")]);
            W2_snr = toDouble(values[columns.get("w2snr_pm")]);
            meanObsMJD = toDouble(values[columns.get("meanobsmjd")]);
            ra_pm = toDouble(values[columns.get("ra_pm")]);
            dec_pm = toDouble(values[columns.get("dec_pm")]);
            pmra = toDouble(values[columns.get("pmra")]) * ARCSEC_MAS;
            pmdec = toDouble(values[columns.get("pmdec")]) * ARCSEC_MAS;
            pmra_err = toDouble(values[columns.get("sigpmra")]) * ARCSEC_MAS;
            pmdec_err = toDouble(values[columns.get("sigpmdec")]) * ARCSEC_MAS;
            par_pm = toDouble(values[columns.get("par_pm")]) * ARCSEC_MAS;
            par_pmsig = toDouble(values[columns.get("par_pmsig")]) * ARCSEC_MAS;
            par_stat = toDouble(values[columns.get("par_stat")]) * ARCSEC_MAS;
            par_sigma = toDouble(values[columns.get("par_sigma")]) * ARCSEC_MAS;
            cc_flags = values[columns.get("cc_flags")];
            ab_flags = values[columns.get("ab_flags")];
        } else {
            sourceId = values[columns.get("Name")].trim();
            ra = toDouble(values[columns.get("RA_ICRS")]);
            dec = toDouble(values[columns.get("DE_ICRS")]);
            W1mag = toDouble(values[columns.get("W1mproPM")]);
            W1_err = toDouble(values[columns.get("e_W1mproPM")]);
            W2mag = toDouble(values[columns.get("W2mproPM")]);
            W2_err = toDouble(values[columns.get("e_W2mproPM")]);
            W1_snr = toDouble(values[columns.get("snrW1pm")]);
            W2_snr = toDouble(values[columns.get("snrW2pm")]);
            meanObsMJD = toDouble(values[columns.get("MJD")]);
            ra_pm = toDouble(values[columns.get("RAPMdeg")]);
            dec_pm = toDouble(values[columns.get("DEPMdeg")]);
            pmra = toDouble(values[columns.get("pmRA")]) * ARCSEC_MAS;
            pmdec = toDouble(values[columns.get("pmDE")]) * ARCSEC_MAS;
            pmra_err = toDouble(values[columns.get("e_pmRA")]) * ARCSEC_MAS;
            pmdec_err = toDouble(values[columns.get("e_pmDE")]) * ARCSEC_MAS;
            par_pm = toDouble(values[columns.get("plx1")]) * ARCSEC_MAS;
            par_pmsig = toDouble(values[columns.get("e_plx1")]) * ARCSEC_MAS;
            par_stat = toDouble(values[columns.get("plx2")]) * ARCSEC_MAS;
            par_sigma = toDouble(values[columns.get("e_plx2")]) * ARCSEC_MAS;
            cc_flags = values[columns.get("ccf")];
            ab_flags = values[columns.get("abf")];
        }
    }

    @Override
    public CatalogEntry copy() {
        return new CatWiseCatalogEntry(columns, values);
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
        catalogElements.add(new CatalogElement("W1 snr", roundTo1DecNZ(W1_snr), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W2 snr", roundTo1DecNZ(W2_snr), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmra (mas/yr)", roundTo2DecNZ(pmra), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("pmra err", roundTo2DecNZ(pmra_err), Alignment.RIGHT, getDoubleComparator(), false, false, isProperMotionFaulty(pmra, pmra_err)));
        catalogElements.add(new CatalogElement("pmdec (mas/yr)", roundTo2DecNZ(pmdec), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("pmdec err", roundTo2DecNZ(pmdec_err), Alignment.RIGHT, getDoubleComparator(), false, false, isProperMotionFaulty(pmdec, pmdec_err)));
        catalogElements.add(new CatalogElement("plx PM desc-asc (mas)", roundTo1DecNZ(par_pm), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("plx PM desc-asc err", roundTo1DecNZ(par_pmsig), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("plx stat. sol. (mas)", roundTo1DecNZ(par_stat), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("plx stat. sol. err", roundTo1DecNZ(par_sigma), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("cc flags", cc_flags, Alignment.LEFT, getStringComparator(), AllWiseCatalogEntry.createToolTip_cc_flags()));
        catalogElements.add(new CatalogElement("ab flags", ab_flags, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("tpm (mas/yr)", roundTo3DecNZ(getTotalProperMotion()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("W1-W2", roundTo3DecNZ(getW1_W2()), Alignment.RIGHT, getDoubleComparator(), true, true));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.sourceId);
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
        final CatWiseCatalogEntry other = (CatWiseCatalogEntry) obj;
        return Objects.equals(this.sourceId, other.sourceId);
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new CatWiseCatalogEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return CATALOG_NAME;
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return java.awt.Color.MAGENTA;
    }

    @Override
    public String getCatalogUrl() {
        if (TapProvider.IRSA.equals(getTapProvider())) {
            return createIrsaUrl(ra, dec, searchRadius / DEG_ARCSEC, "catwise_2020");
        } else {
            return createVizieRUrl(ra, dec, searchRadius / DEG_ARCSEC, "II/365/catwise", "RA_ICRS", "DE_ICRS");
        }
    }

    @Override
    public String getProperMotionQueryUrl() {
        if (TapProvider.IRSA.equals(getTapProvider())) {
            return IRSA_TAP_URL + "/sync?query=" + createProperMotionQuery() + "&format=csv";
        } else {
            return VIZIER_TAP_URL + createProperMotionQuery();
        }
    }

    private String createProperMotionQuery() {
        StringBuilder query = new StringBuilder();
        if (TapProvider.IRSA.equals(getTapProvider())) {
            addRow(query, "SELECT source_name,");
            addRow(query, "       ra,");
            addRow(query, "       dec,");
            addRow(query, "       w1mpro,");
            addRow(query, "       w1sigmpro,");
            addRow(query, "       w2mpro,");
            addRow(query, "       w2sigmpro,");
            addRow(query, "       w1snr,");
            addRow(query, "       w2snr,");
            addRow(query, "       meanobsmjd,");
            addRow(query, "       ra_pm,");
            addRow(query, "       dec_pm,");
            addRow(query, "       pmra,");
            addRow(query, "       pmdec,");
            addRow(query, "       sigpmra,");
            addRow(query, "       sigpmdec,");
            addRow(query, "       par_pm,");
            addRow(query, "       par_pmsig,");
            addRow(query, "       par_stat,");
            addRow(query, "       par_sigma,");
            addRow(query, "       cc_flags,");
            addRow(query, "       ab_flags");
            addRow(query, "FROM   catwise_2020");
            addRow(query, "WHERE  1=CONTAINS(POINT('ICRS', ra, dec), CIRCLE('ICRS', " + ra + ", " + dec + ", " + searchRadius / DEG_ARCSEC + "))");
            addRow(query, "AND   (SQRT(pmra * pmra + pmdec * pmdec) >= " + tpm / ARCSEC_MAS + ")");
        } else {
            addRow(query, "SELECT Name,");
            addRow(query, "       RA_ICRS,");
            addRow(query, "       DE_ICRS,");
            addRow(query, "       W1mproPM,");
            addRow(query, "       e_W1mproPM,");
            addRow(query, "       W2mproPM,");
            addRow(query, "       e_W2mproPM,");
            addRow(query, "       snrW1pm,");
            addRow(query, "       snrW2pm,");
            addRow(query, "       MJD,");
            addRow(query, "       RAPMdeg,");
            addRow(query, "       DEPMdeg,");
            addRow(query, "       pmRA,");
            addRow(query, "       pmDE,");
            addRow(query, "       e_pmRA,");
            addRow(query, "       e_pmDE,");
            addRow(query, "       plx1,");
            addRow(query, "       e_plx1,");
            addRow(query, "       plx2,");
            addRow(query, "       e_plx2,");
            addRow(query, "       ccf,");
            addRow(query, "       abf");
            addRow(query, "FROM   \"II/365/catwise\"");
            addRow(query, "WHERE  1=CONTAINS(POINT('ICRS', RA_ICRS, DE_ICRS), CIRCLE('ICRS', " + ra + ", " + dec + ", " + searchRadius / DEG_ARCSEC + "))");
            addRow(query, "AND   (SQRT(pmRA * pmRA + pmDE * pmDE) >= " + tpm / ARCSEC_MAS + ")");
        }
        return encodeQuery(query.toString());
    }

    @Override
    public void setTpm(double tpm) {
        this.tpm = tpm;
    }

    @Override
    public String[] getColumnValues() {
        String columnValues = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + roundTo3Dec(W1mag) + "," + roundTo3Dec(W1_err) + "," + roundTo3Dec(W2mag) + "," + roundTo3Dec(W2_err) + "," + roundTo1Dec(W1_snr) + "," + roundTo1Dec(W2_snr) + "," + roundTo2Dec(pmra) + "," + roundTo2Dec(pmra_err) + "," + roundTo2Dec(pmdec) + "," + roundTo2Dec(pmdec_err) + "," + roundTo1Dec(par_pm) + "," + roundTo1Dec(par_pmsig) + "," + roundTo1Dec(par_stat) + "," + roundTo1Dec(par_sigma) + "," + cc_flags + "," + ab_flags + "," + roundTo3Dec(getTotalProperMotion()) + "," + roundTo3Dec(getW1_W2());
        return columnValues.split(",", -1);
    }

    @Override
    public String[] getColumnTitles() {
        String columnTitles = "dist (arcsec),source id,ra,dec,W1 (mag),W1 err,W2 (mag),W2 err,W1 snr,W2 snr,pmra,pmra err,pmdec,pmdec err,plx PM desc-asc (mas),plx PM desc-asc err,plx stat. sol. (mas),plx stat. sol. err,cc flags,ab flags,tpm (mas/yr),W1-W2";
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
    }

    @Override
    public Map<Band, Double> getBands() {
        Map<Band, Double> bands = new LinkedHashMap<>();
        bands.put(Band.W1, W1mag);
        bands.put(Band.W2, W2mag);
        return bands;
    }

    @Override
    public Map<Color, Double> getColors(boolean toVega) {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.W1_W2, getW1_W2());
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
        return pmra;
    }

    @Override
    public double getPmdec() {
        return pmdec;
    }

    @Override
    public double getPmraErr() {
        return pmra_err;
    }

    @Override
    public double getPmdecErr() {
        return pmdec_err;
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
        return calculateTotalProperMotion(pmra, pmdec);
    }

    public double getMeanObsMJD() {
        return meanObsMJD;
    }

    public double getRa_pm() {
        return ra_pm;
    }

    public double getDec_pm() {
        return dec_pm;
    }

    @Override
    public String getCc_flags() {
        return cc_flags;
    }

    @Override
    public String getAb_flags() {
        return ab_flags;
    }

    public double getW1_W2() {
        if (W1mag == 0 || W2mag == 0) {
            return 0;
        } else {
            return W1mag - W2mag;
        }
    }

}
