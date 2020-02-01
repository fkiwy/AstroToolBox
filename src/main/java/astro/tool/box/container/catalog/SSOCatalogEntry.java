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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SSOCatalogEntry implements CatalogEntry {

    // Solar System object identifier
    private String objectId;

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

    // Catalog number
    private int catalogNumber;

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    private static final Map<String, String> TYPE_TABLE = new HashMap<>();

    static {
        TYPE_TABLE.put("A", "Asteroid, numbered");
        TYPE_TABLE.put("U", "Asteroid, unnumbered");
        TYPE_TABLE.put("C", "Comet, periodic");
        TYPE_TABLE.put("N", "Comet, non-periodic");
        TYPE_TABLE.put("P", "Planet");
        TYPE_TABLE.put("S", "Planetary Satellite");
    }

    public SSOCatalogEntry() {
    }

    public SSOCatalogEntry(String[] values) {
        objectId = values[1];
        type = values[2];
        pra = toDouble(values[3]);
        pdec = toDouble(values[4]);
        ppm = toDouble(values[5]);
        theta = toDouble(values[6]);
        rhelio = toDouble(values[10]);
        amag = toDouble(values[13]);
        vmag = toDouble(values[14]);
        perdist = toDouble(values[16]);
        ecc = toDouble(values[17]);
        incl = toDouble(values[18]);
        pertime = toDouble(values[25]);
        mjd = toDouble(values[27]);
        dra = toDouble(values[34]);
        ddec = toDouble(values[35]);
        W1mag = toDouble(values[36]);
        W1_err = toDouble(values[37]);
        W2mag = toDouble(values[38]);
        W2_err = toDouble(values[39]);
        ra = pra + dra;
        dec = pdec + ddec;
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("objectId", objectId, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("type", TYPE_TABLE.get(type), Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("predicted ra", roundTo7DecNZ(pra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("predicted dec", roundTo7DecNZ(pdec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("predicted pm (arcsec/sec)", roundTo3DecNZ(ppm), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pm direction (deg)", roundTo3DecNZ(theta), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("heliocentric dist. (AU)", roundTo3DecNZ(rhelio), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("absolute mag", roundTo3DecNZ(amag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("visual mag", roundTo3DecNZ(vmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("perihelion dist. (AU)", roundTo3DecNZ(perdist), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("orbital ecc.", roundTo3DecNZ(ecc), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("orbital incl. (deg)", roundTo3DecNZ(incl), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("perih. passage time (mjd)", roundTo3DecNZ(pertime), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("observation time (mjd)", roundTo3DecNZ(mjd), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dist. to prediced ra (arcsec)", roundTo3DecNZ(dra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dist. to prediced dec (arcsec)", roundTo3DecNZ(ddec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W1mag", roundTo3DecNZ(W1mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W1 err", roundTo3DecNZ(W1_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W2mag", roundTo3DecNZ(W2mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W2 err", roundTo3DecNZ(W2_err), Alignment.RIGHT, getDoubleComparator()));
    }

    @Override
    public String toString() {
        return "SSOCatalogEntry{" + "objectId=" + objectId + ", type=" + type + ", ra=" + ra + ", dec=" + dec + ", pra=" + pra + ", pdec=" + pdec + ", ppm=" + ppm + ", theta=" + theta + ", rhelio=" + rhelio + ", amag=" + amag + ", vmag=" + vmag + ", perdist=" + perdist + ", ecc=" + ecc + ", incl=" + incl + ", pertime=" + pertime + ", mjd=" + mjd + ", dra=" + dra + ", ddec=" + ddec + ", W1mag=" + W1mag + ", W1_err=" + W1_err + ", W2mag=" + W2mag + ", W2_err=" + W2_err + ", targetRa=" + targetRa + ", targetDec=" + targetDec + ", pixelRa=" + pixelRa + ", pixelDec=" + pixelDec + ", searchRadius=" + searchRadius + ", catalogNumber=" + catalogNumber + ", catalogElements=" + catalogElements + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.objectId);
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
        final SSOCatalogEntry other = (SSOCatalogEntry) obj;
        if (!Objects.equals(this.objectId, other.objectId)) {
            return false;
        }
        return true;
    }

    @Override
    public CatalogEntry getInstance(String[] values) {
        return new SSOCatalogEntry(values);
    }

    @Override
    public String getCatalogName() {
        return "Solar System Objects";
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.LIGHT_GRAY.val;
    }

    @Override
    public String getCatalogUrl() {
        return createIrsaUrl(SSO_CATALOG_ID, ra, dec, searchRadius / DEG_ARCSEC);
    }

    @Override
    public String[] getColumnValues() {
        String values = roundTo3DecLZ(getTargetDistance()) + "," + objectId + "," + TYPE_TABLE.get(type) + "," + roundTo7Dec(pra) + "," + roundTo7Dec(pdec) + "," + roundTo3DecNZ(ppm) + "," + roundTo3DecNZ(theta) + "," + roundTo3DecNZ(rhelio) + "," + roundTo3DecNZ(amag) + "," + roundTo3DecNZ(vmag) + "," + roundTo3DecNZ(perdist) + "," + roundTo3DecNZ(ecc) + "," + roundTo3DecNZ(incl) + "," + roundTo3DecNZ(pertime) + "," + roundTo3DecNZ(mjd) + "," + roundTo3Dec(dra) + "," + roundTo3Dec(ddec) + "," + roundTo3Dec(W1mag) + "," + roundTo3Dec(W1_err) + "," + roundTo3Dec(W2mag) + "," + roundTo3Dec(W2_err);
        return values.split(",", 21);
    }

    @Override
    public String[] getColumnTitles() {
        String titles = "dist (arcsec),objectId,type,predicted ra,predicted dec,predicted pm (arcsec/sec),pm direction (deg),heliocentric dist. (AU),absolute mag,visual mag,perihelion dist. (AU),orbital ecc.,orbital incl. (deg),perih. passage time (mjd),observation time (mjd),dist. to prediced ra (arcsec),dist. to prediced dec (arcsec),W1mag,W1 err,W2mag,W2 err";
        return titles.split(",", 21);
    }

    @Override
    public Map<Color, Double> getColors() {
        return new LinkedHashMap<>();
    }

    @Override
    public String getSourceId() {
        return objectId;
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

}
