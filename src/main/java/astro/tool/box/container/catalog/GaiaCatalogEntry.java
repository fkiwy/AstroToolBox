package astro.tool.box.container.catalog;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.util.Comparators.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import static astro.tool.box.util.Utils.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GaiaCatalogEntry implements CatalogEntry, ProperMotionQuery {

    public static final String CATALOG_NAME = "Gaia DR2";

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

    // Radial velocity
    private double radvel;

    // Radial velocity error
    private double radvel_err;

    // Stellar effective temperature
    private double teff;

    // Stellar radius
    private double radsun;

    // Stellar luminosity
    private double lumsun;

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

    public GaiaCatalogEntry() {
    }

    public GaiaCatalogEntry(Map<String, Integer> columns, String[] values) {
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
        radvel = toDouble(values[columns.get("radial_velocity")]);
        radvel_err = toDouble(values[columns.get("radial_velocity_error")]);
        teff = toDouble(values[columns.get("teff_val")]);
        radsun = toDouble(values[columns.get("radius_val")]);
        lumsun = toDouble(values[columns.get("lum_val")]);
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
        catalogElements.add(new CatalogElement("rad vel (km/s)", roundTo3DecNZ(radvel), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("rad vel err", roundTo3DecNZ(radvel_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("teff (K)", roundTo2DecNZ(teff), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("sol rad", roundTo2DecNZ(radsun), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("sol lum", roundTo3DecNZ(lumsun), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dist (1/plx)", roundTo3DecNZ(getActualDistance()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("Absolute G (mag)", roundTo3DecNZ(getAbsoluteGmag()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("tpm (mas/yr)", roundTo3DecNZ(getTotalProperMotion()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("tang vel (km/s)", roundTo3DecNZ(getTansverseVelocity()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("tot vel (km/s)", roundTo3DecNZ(getTotalVelocity()), Alignment.RIGHT, getDoubleComparator(), false, true));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GaiaCatalogEntry{sourceId=").append(sourceId);
        sb.append(", ra=").append(ra);
        sb.append(", dec=").append(dec);
        sb.append(", plx=").append(plx);
        sb.append(", plx_err=").append(plx_err);
        sb.append(", pmra=").append(pmra);
        sb.append(", pmra_err=").append(pmra_err);
        sb.append(", pmdec=").append(pmdec);
        sb.append(", pmdec_err=").append(pmdec_err);
        sb.append(", Gmag=").append(Gmag);
        sb.append(", BPmag=").append(BPmag);
        sb.append(", RPmag=").append(RPmag);
        sb.append(", BP_RP=").append(BP_RP);
        sb.append(", BP_G=").append(BP_G);
        sb.append(", G_RP=").append(G_RP);
        sb.append(", radvel=").append(radvel);
        sb.append(", radvel_err=").append(radvel_err);
        sb.append(", teff=").append(teff);
        sb.append(", radsun=").append(radsun);
        sb.append(", lumsun=").append(lumsun);
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
        hash = 29 * hash + (int) (this.sourceId ^ (this.sourceId >>> 32));
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
        final GaiaCatalogEntry other = (GaiaCatalogEntry) obj;
        return this.sourceId == other.sourceId;
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new GaiaCatalogEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return CATALOG_NAME;
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.LIGHT_BLUE.val;
    }

    @Override
    public String getCatalogUrl() {
        return createIrsaUrl(GAIA_CATALOG_ID, ra, dec, searchRadius / DEG_ARCSEC);
    }

    @Override
    public String getProperMotionQueryUrl() {
        return IRSA_TAP_URL + "/sync?query=" + createProperMotionQuery() + "&format=csv";
    }

    private String createProperMotionQuery() {
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
        addRow(query, "       radial_velocity,");
        addRow(query, "       radial_velocity_error,");
        addRow(query, "       teff_val,");
        addRow(query, "       radius_val,");
        addRow(query, "       lum_val");
        addRow(query, "FROM   " + GAIA_CATALOG_ID);
        addRow(query, "WHERE  1=CONTAINS(POINT('ICRS', ra, dec), CIRCLE('ICRS', " + ra + ", " + dec + ", " + searchRadius / DEG_ARCSEC + "))");
        addRow(query, "AND   (SQRT(pmra * pmra + pmdec * pmdec) >= " + tpm + ")");
        return encodeQuery(query.toString());
    }

    @Override
    public void setTpm(double tpm) {
        this.tpm = tpm;
    }

    @Override
    public String[] getColumnValues() {
        String values = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + roundTo4Dec(plx) + "," + roundTo4Dec(plx_err) + "," + roundTo3Dec(pmra) + "," + roundTo3Dec(pmra_err) + "," + roundTo3Dec(pmdec) + "," + roundTo3Dec(pmdec_err) + "," + roundTo3Dec(Gmag) + "," + roundTo3Dec(BPmag) + "," + roundTo3Dec(RPmag) + "," + roundTo3Dec(BP_RP) + "," + roundTo3Dec(BP_G) + "," + roundTo3Dec(G_RP) + "," + roundTo3Dec(radvel) + "," + roundTo3Dec(radvel_err) + "," + roundTo2Dec(teff) + "," + roundTo2Dec(radsun) + "," + roundTo3Dec(lumsun) + "," + roundTo3Dec(getActualDistance()) + "," + roundTo3Dec(getAbsoluteGmag()) + "," + roundTo3Dec(getTotalProperMotion()) + "," + roundTo3Dec(getTansverseVelocity()) + "," + roundTo3Dec(getTotalVelocity());
        return values.split(",", 26);
    }

    @Override
    public String[] getColumnTitles() {
        String titles = "dist (arcsec),source id,ra,dec,plx (mas),plx err,pmra (mas/yr),pmra err,pmdec (mas/yr),pmdec err,G (mag),BP (mag),RP (mag),BP-RP,BP-G,G-RP,rad vel (km/s),rad vel err,teff (K),sol rad,sol lum,dist (1/plx),Absolute G (mag),tpm (mas/yr),tang vel (km/s),tot vel (km/s)";
        return titles.split(",", 26);
    }

    @Override
    public Map<Color, Double> getColors() {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.BP_RP, BP_RP);
        colors.put(Color.G_RP, G_RP);
        colors.put(Color.M_G, getAbsoluteGmag());
        return colors;
    }

    @Override
    public String getMagnitudes() {
        return String.format("G=%s; BP=%s; RP=%s", roundTo3DecNZ(Gmag), roundTo3DecNZ(BPmag), roundTo3DecNZ(RPmag));
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

    public double getActualDistance() {
        return calculateActualDistance(plx);
    }

    public double getAbsoluteGmag() {
        return calculateAbsoluteMagnitudeFromParallax(Gmag, plx);
    }

    public double getTotalProperMotion() {
        return calculateTotalProperMotion(pmra, pmdec);
    }

    public double getTansverseVelocity() {
        return calculateTransverseVelocityFromParallax(pmra, pmdec, plx);
    }

    public double getTotalVelocity() {
        return calculateTotalVelocity(radvel, getTansverseVelocity());
    }

    public double getBP_RP() {
        return BP_RP;
    }

}
