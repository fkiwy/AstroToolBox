package astro.tool.box.catalog;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.util.Comparators.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.MiscUtils.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import astro.tool.box.exception.NoExtinctionValuesException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GaiaDR3CatalogEntry implements CatalogEntry, ProperMotionQuery, ProperMotionCatalog, WhiteDwarf, GaiaCmd {

    public static final String CATALOG_NAME = "Gaia eDR3";

    // Unique source identifier (unique within a particular Data Release)
    private long sourceId;

    // Right ascension
    private double ra;

    // Declination
    private double dec;

    // Parallax
    private double plx;

    // Standard error of parallax
    private double plx_err;

    // Proper motion in right ascension direction
    private double pmra;

    // Standard error of proper motion in right ascension direction
    private double pmra_err;

    // Proper motion in declination direction
    private double pmdec;

    // Standard error of proper motion in declination direction
    private double pmdec_err;

    // G-band mean magnitude
    private double Gmag;

    // Integrated BP mean magnitude
    private double BPmag;

    // Integrated RP mean magnitude
    private double RPmag;

    // BP - RP colour
    private double BP_RP;

    // BP - G colour
    private double BP_G;

    // G - RP colour
    private double G_RP;

    // Renormalised unit weight error (RUWE)
    private double ruwe;

    // Radial velocity
    private double radvel;

    // Radial velocity error
    private double radvel_err;

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

    public GaiaDR3CatalogEntry() {
    }

    public GaiaDR3CatalogEntry(Map<String, Integer> columns, String[] values) {
        this.columns = columns;
        this.values = values;
        sourceId = toLong(values[columns.get("source_id")]);
        ra = toDouble(values[columns.get("ra")]);
        dec = toDouble(values[columns.get("dec")]);
        plx = toDouble(values[columns.get("parallax")]);
        plx_err = toDouble(values[columns.get("parallax_error")]);
        pmra = toDouble(values[columns.get("pmra")]);
        pmra_err = toDouble(values[columns.get("pmra_error")]);
        pmdec = toDouble(values[columns.get("pmdec")]);
        pmdec_err = toDouble(values[columns.get("pmdec_error")]);
        Gmag = toDouble(values[columns.get("phot_g_mean_mag")]);
        BPmag = toDouble(values[columns.get("phot_bp_mean_mag")]);
        RPmag = toDouble(values[columns.get("phot_rp_mean_mag")]);
        BP_RP = toDouble(values[columns.get("bp_rp")]);
        BP_G = toDouble(values[columns.get("bp_g")]);
        G_RP = toDouble(values[columns.get("g_rp")]);
        ruwe = toDouble(values[columns.get("ruwe")]);
        radvel = toDouble(values[columns.get("dr2_radial_velocity")]);
        radvel_err = toDouble(values[columns.get("dr2_radial_velocity_error")]);
    }

    @Override
    public CatalogEntry copy() {
        return new GaiaDR3CatalogEntry(columns, values);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("source id", String.valueOf(sourceId), Alignment.LEFT, getLongComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("plx (mas)", roundTo4DecNZ(plx), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("plx err", roundTo4DecNZ(plx_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmra (mas/yr)", roundTo3DecNZ(pmra), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("pmra err", roundTo3DecNZ(pmra_err), Alignment.RIGHT, getDoubleComparator(), false, false, isProperMotionFaulty(pmra, pmra_err)));
        catalogElements.add(new CatalogElement("pmdec (mas/yr)", roundTo3DecNZ(pmdec), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("pmdec err", roundTo3DecNZ(pmdec_err), Alignment.RIGHT, getDoubleComparator(), false, false, isProperMotionFaulty(pmdec, pmdec_err)));
        catalogElements.add(new CatalogElement("G (mag)", roundTo3DecNZ(Gmag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("BP (mag)", roundTo3DecNZ(BPmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("RP (mag)", roundTo3DecNZ(RPmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("BP-RP", roundTo3DecNZ(BP_RP), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("BP-G", roundTo3DecNZ(BP_G), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("G-RP", roundTo3DecNZ(G_RP), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("RUWE", roundTo3DecNZ(ruwe), Alignment.RIGHT, getDoubleComparator(), false));
        catalogElements.add(new CatalogElement("rad vel (km/s)", roundTo3DecNZ(radvel), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("rad vel err", roundTo3DecNZ(radvel_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dist (1/plx)", roundTo3DecNZ(getParallacticDistance()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("Absolute G (mag)", roundTo3DecNZ(getAbsoluteGmag()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("tpm (mas/yr)", roundTo3DecNZ(getTotalProperMotion()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("tang vel (km/s)", roundTo3DecNZ(getTangentialVelocity()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("tot vel (km/s)", roundTo3DecNZ(getTotalVelocity()), Alignment.RIGHT, getDoubleComparator(), false, true));
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
        final GaiaDR3CatalogEntry other = (GaiaDR3CatalogEntry) obj;
        return this.sourceId == other.sourceId;
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new GaiaDR3CatalogEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return CATALOG_NAME;
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return java.awt.Color.CYAN.darker();
    }

    @Override
    public String getCatalogUrl() {
        return ESAC_TAP_URL + encodeQuery(createCatalogQuery());
    }

    @Override
    public String getProperMotionQueryUrl() {
        return ESAC_TAP_URL + encodeQuery(createProperMotionQuery());
    }

    private String createCatalogQuery() {
        StringBuilder query = new StringBuilder();
        addRow(query, "SELECT source_id,");
        addRow(query, "       ra,");
        addRow(query, "       dec,");
        addRow(query, "       parallax,");
        addRow(query, "       parallax_error,");
        addRow(query, "       pmra,");
        addRow(query, "       pmra_error,");
        addRow(query, "       pmdec,");
        addRow(query, "       pmdec_error,");
        addRow(query, "       phot_g_mean_mag,");
        addRow(query, "       phot_bp_mean_mag,");
        addRow(query, "       phot_rp_mean_mag,");
        addRow(query, "       bp_rp,");
        addRow(query, "       bp_g,");
        addRow(query, "       g_rp,");
        addRow(query, "       ruwe,");
        addRow(query, "       dr2_radial_velocity,");
        addRow(query, "       dr2_radial_velocity_error");
        addRow(query, "FROM   gaiaedr3.gaia_source");
        addRow(query, "WHERE  1=CONTAINS(POINT('ICRS', ra, dec), CIRCLE('ICRS', " + ra + ", " + dec + ", " + searchRadius / DEG_ARCSEC + "))");
        return query.toString();
    }

    private String createProperMotionQuery() {
        StringBuilder query = new StringBuilder();
        addRow(query, createCatalogQuery());
        addRow(query, "AND    SQRT(pmra * pmra + pmdec * pmdec) >= " + tpm);
        return query.toString();
    }

    @Override
    public void setTpm(double tpm) {
        this.tpm = tpm;
    }

    @Override
    public String[] getColumnValues() {
        String columnValues = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + roundTo4Dec(plx) + "," + roundTo4Dec(plx_err) + "," + roundTo3Dec(pmra) + "," + roundTo3Dec(pmra_err) + "," + roundTo3Dec(pmdec) + "," + roundTo3Dec(pmdec_err) + "," + roundTo3Dec(Gmag) + "," + roundTo3Dec(BPmag) + "," + roundTo3Dec(RPmag) + "," + roundTo3Dec(BP_RP) + "," + roundTo3Dec(BP_G) + "," + roundTo3Dec(G_RP) + "," + roundTo3Dec(ruwe) + "," + roundTo3Dec(radvel) + "," + roundTo3Dec(radvel_err) + "," + roundTo3Dec(getParallacticDistance()) + "," + roundTo3Dec(getAbsoluteGmag()) + "," + roundTo3Dec(getTotalProperMotion()) + "," + roundTo3Dec(getTangentialVelocity()) + "," + roundTo3Dec(getTotalVelocity());
        return columnValues.split(",", -1);
    }

    @Override
    public String[] getColumnTitles() {
        String columnTitles = "dist (arcsec),source id,ra,dec,plx (mas),plx err,pmra (mas/yr),pmra err,pmdec (mas/yr),pmdec err,G (mag),BP (mag),RP (mag),BP-RP,BP-G,G-RP,RUWE,rad vel (km/s),rad vel err,dist (1/plx),Absolute G (mag),tpm (mas/yr),tang vel (km/s),tot vel (km/s)";
        return columnTitles.split(",", -1);
    }

    @Override
    public void applyExtinctionCorrection(Map<String, Double> extinctionsByBand) throws NoExtinctionValuesException {
        throw new NoExtinctionValuesException();
    }

    @Override
    public Map<Band, NumberPair> getBands() {
        Map<Band, NumberPair> bands = new LinkedHashMap<>();
        bands.put(Band.G, new NumberPair(Gmag, 0));
        bands.put(Band.BP, new NumberPair(BPmag, 0));
        bands.put(Band.RP, new NumberPair(RPmag, 0));
        return bands;
    }

    @Override
    public Map<Color, Double> getColors(boolean toVega) {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.M_G, getAbsoluteGmag());
        colors.put(Color.M_BP, getAbsoluteBPmag());
        colors.put(Color.M_RP, getAbsoluteRPmag());
        colors.put(Color.G_RP, G_RP);
        colors.put(Color.BP_RP, BP_RP);
        colors.put(Color.BP_G, BP_G);
        return colors;
    }

    @Override
    public String getMagnitudes() {
        StringBuilder mags = new StringBuilder();
        if (Gmag != 0) {
            mags.append("G=").append(roundTo3DecNZ(Gmag)).append(" ");
        }
        if (BPmag != 0) {
            mags.append("BP=").append(roundTo3DecNZ(BPmag)).append(" ");
        }
        if (RPmag != 0) {
            mags.append("RP=").append(roundTo3DecNZ(RPmag)).append(" ");
        }
        return mags.toString();
    }

    @Override
    public String getPhotometry() {
        StringBuilder mags = new StringBuilder();
        if (Gmag != 0) {
            mags.append(roundTo3DecNZ(Gmag)).append(",");
        } else {
            mags.append(",");
        }
        if (BPmag != 0) {
            mags.append(roundTo3DecNZ(BPmag)).append(",");
        } else {
            mags.append(",");
        }
        if (RPmag != 0) {
            mags.append(roundTo3DecNZ(RPmag)).append(",");
        } else {
            mags.append(",");
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
        return plx;
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

    @Override
    public double getParallacticDistance() {
        return calculateParallacticDistance(plx);
    }

    @Override
    public double getTotalProperMotion() {
        return calculateTotalProperMotion(pmra, pmdec);
    }

    // Needed to fill the TYGO form
    public double getPlxErr() {
        return plx_err;
    }

    @Override
    public double getPmraErr() {
        return pmra_err;
    }

    @Override
    public double getPmdecErr() {
        return pmdec_err;
    }

    public double getRadvel() {
        return radvel;
    }

    public double getRadvelErr() {
        return radvel_err;
    }
    //

    public double getRuwe() {
        return ruwe;
    }

    public double getTangentialVelocity() {
        return calculateTangentialVelocityFromParallax(pmra, pmdec, plx);
    }

    public double getTotalVelocity() {
        return calculateTotalVelocity(radvel, getTangentialVelocity());
    }

    public double getAbsoluteRPmag() {
        return calculateAbsoluteMagnitudeFromParallax(RPmag, plx);
    }

    public double getAbsoluteBPmag() {
        return calculateAbsoluteMagnitudeFromParallax(BPmag, plx);
    }

    @Override
    public double getAbsoluteGmag() {
        return calculateAbsoluteMagnitudeFromParallax(Gmag, plx);
    }

    @Override
    public double getBP_RP() {
        return BP_RP;
    }

    @Override
    public double getG_RP() {
        return G_RP;
    }

    public double getGmag() {
        return Gmag;
    }

    public double getBPmag() {
        return BPmag;
    }

    public double getRPmag() {
        return RPmag;
    }

}