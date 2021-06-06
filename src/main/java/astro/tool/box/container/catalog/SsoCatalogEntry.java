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
import astro.tool.box.exception.NoExtinctionValuesException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SsoCatalogEntry implements CatalogEntry {

    public static final String CATALOG_NAME = "Solar System Objects";

    // Solar System object identifier
    private String objectID;

    // Type of Solar System object
    private String type;

    // Right Ascension (J2000) of SSO at time of NEOWISE observation (deg)
    private double ra;

    // Declination (J2000) of SSO at time of NEOWISE observation (deg)
    private double dec;

    // Predicted Right Ascension (J2000) of SSO at time of NEOWISE observation (deg)
    private double pra;

    // Predicted Declination (J2000) of SSO at time of NEOWISE observation (deg)
    private double pdec;

    // Predicted proper motion of SSO at time of NEOWISE observation (arcsec/sec)
    private double ppm;

    // Direction of predicted proper motion (E of N) of SSO at time of NEOWISE observation (deg)
    private double theta;

    // SSO heliocentric distance at the time of the NEOWISE observation (AU)
    private double rhelio;

    // SSO absolute magnitude (mag)
    private double amag;

    // SSO predicted visual magnitude at the time of the NEOWISE observation (mag)
    private double vmag;

    // SSO perihelion distance at the time of the NEOWISE observation (AU)
    private double perdist;

    // SSO orbital eccentricity
    private double ecc;

    // SSO orbital inclination (deg)
    private double incl;

    // SSO orbit perihelion passage time, modified Julian date (mjdate)
    private double pertime;

    // Modified Julian date of the mid-point of the observation of the frameset within which the FOV the SSO is predicted to fall (mjdate)
    private double mjd;

    // RA distance between associated NEOWISE detection and predicted SSO position (NEOWISE - SSO)
    private double dra;

    // Declination distance between associated NEOWISE extraction and predicted SSO position (NEOWISE - SSO)
    private double ddec;

    // Single-exposure W1 profile-fit magnitude or magnitude upper limit for the NEOWISE detection spatially associated with the SSO
    private double W1mag;

    // Single-exposure W1 profile-fit photometric measurement uncertainty for the associated NEOWISE detection
    private double W1_err;

    // Single-exposure W2 profile-fit magnitude or magnitude upper limit for the NEOWISE detection spatially associated with the SSO
    private double W2mag;

    // Single-exposure W2 profile-fit photometric measurement uncertainty for the associated NEOWISE detection
    private double W2_err;

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

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    private Map<String, Integer> columns;

    private String[] values;

    private static final Map<String, String> TYPE_TABLE = new HashMap<>();

    static {
        TYPE_TABLE.put("A", "Asteroid, numbered");
        TYPE_TABLE.put("U", "Asteroid, unnumbered");
        TYPE_TABLE.put("C", "Comet, periodic");
        TYPE_TABLE.put("N", "Comet, non-periodic");
        TYPE_TABLE.put("P", "Planet");
        TYPE_TABLE.put("S", "Planetary Satellite");
    }

    public SsoCatalogEntry() {
    }

    public SsoCatalogEntry(Map<String, Integer> columns, String[] values) {
        this.columns = columns;
        this.values = values;
        objectID = values[columns.get("objid")].replaceAll("\\s+", " ");
        type = values[columns.get("t")];
        pra = toDouble(values[columns.get("ra")]);
        pdec = toDouble(values[columns.get("dec")]);
        ppm = toDouble(values[columns.get("mu")]);
        theta = toDouble(values[columns.get("theta")]);
        rhelio = toDouble(values[columns.get("rhelio")]);
        amag = toDouble(values[columns.get("amag")]);
        vmag = toDouble(values[columns.get("vmag")]);
        perdist = toDouble(values[columns.get("perdist")]);
        ecc = toDouble(values[columns.get("ecc")]);
        incl = toDouble(values[columns.get("incl")]);
        pertime = toDouble(values[columns.get("pertime")]);
        mjd = toDouble(values[columns.get("mjd")]);
        dra = toDouble(values[columns.get("dra")]);
        ddec = toDouble(values[columns.get("ddec")]);
        W1mag = toDouble(values[columns.get("w1mpro")]);
        W1_err = toDouble(values[columns.get("w1sgmp")]);
        W2mag = toDouble(values[columns.get("w2mpro")]);
        W2_err = toDouble(values[columns.get("w2sgmp")]);
        //ra = pra + dra / DEG_ARCSEC;
        //dec = pdec + ddec / DEG_ARCSEC;
        ra = pra;
        dec = pdec;
    }

    @Override
    public CatalogEntry copy() {
        return new SsoCatalogEntry(columns, values);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("object id", objectID, Alignment.LEFT, getStringComparator(), true));
        catalogElements.add(new CatalogElement("type", TYPE_TABLE.get(type), Alignment.LEFT, getStringComparator(), true));
        catalogElements.add(new CatalogElement("predicted ra", roundTo7DecNZ(pra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("predicted dec", roundTo7DecNZ(pdec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("predicted pm (arcsec/sec)", roundTo3DecNZ(ppm), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("pm direction (deg)", roundTo3DecNZ(theta), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("heliocentric dist. (AU)", roundTo3DecNZ(rhelio), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("absolute mag", roundTo3DecNZ(amag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("visual mag", roundTo3DecNZ(vmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("perihelion dist. (AU)", roundTo3DecNZ(perdist), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("orbital ecc.", roundTo3DecNZ(ecc), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("orbital incl. (deg)", roundTo3DecNZ(incl), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("perih. passage time", convertMJDToDateTime(new BigDecimal(Double.toString(pertime))).format(DATE_TIME_FORMATTER), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("observation time", convertMJDToDateTime(new BigDecimal(Double.toString(mjd))).format(DATE_TIME_FORMATTER), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dist. to predict ra (arcsec)", roundTo3DecNZ(dra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dist. to predict dec (arcsec)", roundTo3DecNZ(ddec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W1 (mag)", roundTo3DecNZ(W1mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W1 err", roundTo3DecNZ(W1_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W2 (mag)", roundTo3DecNZ(W2mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W2 err", roundTo3DecNZ(W2_err), Alignment.RIGHT, getDoubleComparator()));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.objectID);
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
        final SsoCatalogEntry other = (SsoCatalogEntry) obj;
        return Objects.equals(this.objectID, other.objectID);
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new SsoCatalogEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return CATALOG_NAME;
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return java.awt.Color.BLUE;
    }

    @Override
    public String getCatalogUrl() {
        return createIrsaUrl(ra, dec, searchRadius / DEG_ARCSEC, "neowiser_p1ba_mch");
    }

    @Override
    public String[] getColumnValues() {
        String columnValues = roundTo3DecLZ(getTargetDistance()) + "," + objectID + "," + TYPE_TABLE.get(type) + "," + roundTo7Dec(pra) + "," + roundTo7Dec(pdec) + "," + roundTo3Dec(ppm) + "," + roundTo3Dec(theta) + "," + roundTo3Dec(rhelio) + "," + roundTo3Dec(amag) + "," + roundTo3Dec(vmag) + "," + roundTo3Dec(perdist) + "," + roundTo3Dec(ecc) + "," + roundTo3Dec(incl) + "," + roundTo3Dec(pertime) + "," + roundTo3Dec(mjd) + "," + roundTo3Dec(dra) + "," + roundTo3Dec(ddec) + "," + roundTo3Dec(W1mag) + "," + roundTo3Dec(W1_err) + "," + roundTo3Dec(W2mag) + "," + roundTo3Dec(W2_err);
        return columnValues.split(",", -1);
    }

    @Override
    public String[] getColumnTitles() {
        String columnTitles = "dist (arcsec),object id,type,predicted ra,predicted dec,predicted pm (arcsec/sec),pm direction (deg),heliocentric dist. (AU),absolute mag,visual mag,perihelion dist. (AU),orbital ecc.,orbital incl. (deg),perih. passage time,observation time,dist. to predict ra (arcsec),dist. to predict dec (arcsec),W1 (mag),W1 err,W2 (mag),W2 err";
        return columnTitles.split(",", -1);
    }

    @Override
    public void applyExtinctionCorrection(Map<String, Double> extinctionsByBand) throws NoExtinctionValuesException {
        throw new NoExtinctionValuesException();
    }

    @Override
    public Map<Band, Double> getBands() {
        return new LinkedHashMap<>();
    }

    @Override
    public Map<Color, Double> getColors(boolean toVega) {
        return new LinkedHashMap<>();
    }

    @Override
    public String getMagnitudes() {
        return "";
    }

    @Override
    public String getSourceId() {
        return objectID;
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
        return null;
    }

    @Override
    public void setSpt(String spt) {
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

}
