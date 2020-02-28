package astro.tool.box.container.catalog;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.util.Comparators.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SDSSCatalogEntry implements CatalogEntry {

    // Unique object identifier
    private long objID;

    // Run number
    private int run;

    // Rerun number
    private int rerun;

    // Camera column
    private int camcol;

    // Field number
    private int field;

    // Object number within a field (usually changes between reruns of the same field)
    private int obj;

    // J2000 right ascension
    private double ra;

    // J2000 declination
    private double dec;

    // Type of object
    private int type;

    // Clean photometry flag (1=clean, 0=unclean)
    private int clean;

    // Date of observation
    private int mjd;

    // Pointer to the spectrum of object, if exists, else 0
    private BigInteger specObjID;

    // Magnitude in u band
    private double u_mag;

    // Magnitude in g band
    private double g_mag;

    // Magnitude in r band
    private double r_mag;

    // Magnitude in i band
    private double i_mag;

    // Magnitude in z band
    private double z_mag;

    // Error in u magnitude
    private double u_err;

    // Error in g magnitude
    private double g_err;

    // Error in r magnitude
    private double r_err;

    // Error in i magnitude
    private double i_err;

    // Error in z magnitude
    private double z_err;

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

    public SDSSCatalogEntry() {
    }

    public SDSSCatalogEntry(String[] values) {
        objID = toLong(values[0]);
        run = toInteger(values[1]);
        rerun = toInteger(values[2]);
        camcol = toInteger(values[3]);
        field = toInteger(values[4]);
        obj = toInteger(values[5]);
        ra = toDouble(values[6]);
        dec = toDouble(values[7]);
        type = toInteger(values[8]);
        clean = toInteger(values[9]);
        mjd = toInteger(values[10]);
        specObjID = new BigInteger(values[11]);
        u_mag = toDouble(values[12]);
        g_mag = toDouble(values[13]);
        r_mag = toDouble(values[14]);
        i_mag = toDouble(values[15]);
        z_mag = toDouble(values[16]);
        u_err = toDouble(values[17]);
        g_err = toDouble(values[18]);
        r_err = toDouble(values[19]);
        i_err = toDouble(values[20]);
        z_err = toDouble(values[21]);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("sourceId", String.valueOf(objID), Alignment.LEFT, getLongComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("object type", getSdssObjectType(type), Alignment.LEFT, getStringComparator(), true));
        catalogElements.add(new CatalogElement("photometry flag", getSdssPhotometryFlag(clean), Alignment.LEFT, getStringComparator(), true));
        catalogElements.add(new CatalogElement("observation date", convertMJDToDateTime(new BigDecimal(Double.toString(mjd))).format(DATE_FORMATTER), Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("spectrum pointer", String.valueOf(specObjID), Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("u_mag", roundTo3DecNZ(u_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("u_mag err", roundTo3DecNZ(u_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("g_mag", roundTo3DecNZ(g_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("g_mag err", roundTo3DecNZ(g_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("r_mag", roundTo3DecNZ(r_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("r_mag err", roundTo3DecNZ(r_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("i_mag", roundTo3DecNZ(i_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("i_mag err", roundTo3DecNZ(i_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("z_mag", roundTo3DecNZ(z_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("z_mag err", roundTo3DecNZ(z_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("u-g", roundTo3DecNZ(get_u_g()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("g-r", roundTo3DecNZ(get_g_r()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("r-i", roundTo3DecNZ(get_r_i()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("i-z", roundTo3DecNZ(get_i_z()), Alignment.RIGHT, getDoubleComparator(), false, true));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SDSSCatalogEntry{objID=").append(objID);
        sb.append(", run=").append(run);
        sb.append(", rerun=").append(rerun);
        sb.append(", camcol=").append(camcol);
        sb.append(", field=").append(field);
        sb.append(", obj=").append(obj);
        sb.append(", ra=").append(ra);
        sb.append(", dec=").append(dec);
        sb.append(", type=").append(type);
        sb.append(", clean=").append(clean);
        sb.append(", mjd=").append(mjd);
        sb.append(", specObjID=").append(specObjID);
        sb.append(", u_mag=").append(u_mag);
        sb.append(", g_mag=").append(g_mag);
        sb.append(", r_mag=").append(r_mag);
        sb.append(", i_mag=").append(i_mag);
        sb.append(", z_mag=").append(z_mag);
        sb.append(", u_err=").append(u_err);
        sb.append(", g_err=").append(g_err);
        sb.append(", r_err=").append(r_err);
        sb.append(", i_err=").append(i_err);
        sb.append(", z_err=").append(z_err);
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
        int hash = 5;
        hash = 43 * hash + (int) (this.objID ^ (this.objID >>> 32));
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
        final SDSSCatalogEntry other = (SDSSCatalogEntry) obj;
        if (this.objID != other.objID) {
            return false;
        }
        return true;
    }

    @Override
    public CatalogEntry getInstance(String[] values) {
        return new SDSSCatalogEntry(values);
    }

    @Override
    public String getCatalogName() {
        return "SDSS DR16";
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.LIGHT_STEEL.val;
    }

    @Override
    public String getCatalogUrl() {
        return createSdssUrl(ra, dec, searchRadius / ARCMIN_ARCSEC);
    }

    @Override
    public String[] getColumnValues() {
        String values = roundTo3DecLZ(getTargetDistance()) + "," + objID + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + getSdssObjectType(type) + "," + getSdssPhotometryFlag(type) + "," + convertMJDToDateTime(new BigDecimal(Double.toString(mjd))).format(DATE_FORMATTER) + "," + specObjID + "," + roundTo3Dec(u_mag) + "," + roundTo3Dec(u_err) + "," + roundTo3Dec(g_mag) + "," + roundTo3Dec(g_err) + "," + roundTo3Dec(r_mag) + "," + roundTo3Dec(r_err) + "," + roundTo3Dec(i_mag) + "," + roundTo3Dec(i_err) + "," + roundTo3Dec(z_mag) + "," + roundTo3Dec(z_err) + "," + roundTo3Dec(get_u_g()) + "," + roundTo3Dec(get_g_r()) + "," + roundTo3Dec(get_r_i()) + "," + roundTo3Dec(get_i_z());
        return values.split(",", 22);
    }

    @Override
    public String[] getColumnTitles() {
        String titles = "dist (arcsec),sourceId,ra,dec,object type,photometry flag,observation date,spectrum pointer,u_mag,u_mag err,g_mag,g_mag err,r_mag,r_mag err,i_mag,i_mag err,z_mag,z_mag err,u-g,g-r,r-i,i-z";
        return titles.split(",", 22);
    }

    @Override
    public Map<Color, Double> getColors() {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.u_g, get_u_g());
        colors.put(Color.g_r, get_g_r());
        colors.put(Color.r_i, get_r_i());
        colors.put(Color.i_z, get_i_z());
        return colors;
    }

    @Override
    public String getSourceId() {
        return String.valueOf(objID);
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

    public BigInteger getSpecObjID() {
        return specObjID;
    }

    public double get_u_g() {
        if (u_mag == 0 || g_mag == 0) {
            return 0;
        } else {
            return u_mag - g_mag;
        }
    }

    public double get_g_r() {
        if (g_mag == 0 || r_mag == 0) {
            return 0;
        } else {
            return g_mag - r_mag;
        }
    }

    public double get_r_i() {
        if (r_mag == 0 || i_mag == 0) {
            return 0;
        } else {
            return r_mag - i_mag;
        }
    }

    public double get_i_z() {
        if (i_mag == 0 || z_mag == 0) {
            return 0;
        } else {
            return i_mag - z_mag;
        }
    }

}
