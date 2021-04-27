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
import astro.tool.box.enumeration.ABOffset;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SDSSCatalogEntry implements CatalogEntry {

    public static final String CATALOG_NAME = "SDSS DR16";

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

    // J2000 Right Ascension (r-band)
    private double ra;

    // J2000 Declination (r-band)
    private double dec;

    // Error in RA (* cos(Dec), that is, proper units)
    private double raErr;

    // Error in Dec
    private double decErr;

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

    // Error in u magnitude
    private double u_err;

    // Magnitude in g band
    private double g_mag;

    // Error in g magnitude
    private double g_err;

    // Magnitude in r band
    private double r_mag;

    // Error in r magnitude
    private double r_err;

    // Magnitude in i band
    private double i_mag;

    // Error in i magnitude
    private double i_err;

    // Magnitude in z band
    private double z_mag;

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

    // Most likely spectral type
    private String spt;

    private boolean toVega;

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    private Map<String, Integer> columns;

    private String[] values;

    public SDSSCatalogEntry() {
    }

    public SDSSCatalogEntry(Map<String, Integer> columns, String[] values) {
        this.columns = columns;
        this.values = values;
        objID = toLong(values[columns.get("objid")]);
        run = toInteger(values[columns.get("run")]);
        rerun = toInteger(values[columns.get("rerun")]);
        camcol = toInteger(values[columns.get("camcol")]);
        field = toInteger(values[columns.get("field")]);
        obj = toInteger(values[columns.get("obj")]);
        ra = toDouble(values[columns.get("ra")]);
        dec = toDouble(values[columns.get("dec")]);
        raErr = toDouble(values[columns.get("raErr")]);
        decErr = toDouble(values[columns.get("decErr")]);
        type = toInteger(values[columns.get("type")]);
        clean = toInteger(values[columns.get("clean")]);
        mjd = toInteger(values[columns.get("mjd")]);
        specObjID = new BigInteger(values[columns.get("specObjID")]);
        u_mag = toDouble(values[columns.get("u")]);
        u_err = toDouble(values[columns.get("Err_u")]);
        g_mag = toDouble(values[columns.get("g")]);
        g_err = toDouble(values[columns.get("Err_g")]);
        r_mag = toDouble(values[columns.get("r")]);
        r_err = toDouble(values[columns.get("Err_r")]);
        i_mag = toDouble(values[columns.get("i")]);
        i_err = toDouble(values[columns.get("Err_i")]);
        z_mag = toDouble(values[columns.get("z")]);
        z_err = toDouble(values[columns.get("Err_z")]);
    }

    @Override
    public CatalogEntry copy() {
        return new SDSSCatalogEntry(columns, values);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("source id", String.valueOf(objID), Alignment.LEFT, getLongComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("ra err", roundTo7DecNZ(raErr), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec err", roundTo7DecNZ(decErr), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("object type", getSdssObjectType(type), Alignment.LEFT, getStringComparator(), true));
        catalogElements.add(new CatalogElement("photometry flag", getSdssPhotometryFlag(clean), Alignment.LEFT, getStringComparator(), true));
        catalogElements.add(new CatalogElement("observation date", convertMJDToDateTime(new BigDecimal(Double.toString(mjd))).format(DATE_FORMATTER), Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("spectrum pointer", String.valueOf(specObjID), Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("u (mag)", roundTo3DecNZ(u_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("u err", roundTo3DecNZ(u_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("g (mag)", roundTo3DecNZ(g_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("g err", roundTo3DecNZ(g_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("r (mag)", roundTo3DecNZ(r_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("r err", roundTo3DecNZ(r_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("i (mag)", roundTo3DecNZ(i_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("i err", roundTo3DecNZ(i_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("z (mag)", roundTo3DecNZ(z_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("z err", roundTo3DecNZ(z_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("u-g", roundTo3DecNZ(get_u_g()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("g-r", roundTo3DecNZ(get_g_r()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("r-i", roundTo3DecNZ(get_r_i()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("i-z", roundTo3DecNZ(get_i_z()), Alignment.RIGHT, getDoubleComparator(), false, true));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (int) (this.objID ^ (this.objID >>> 32));
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
        return this.objID == other.objID;
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new SDSSCatalogEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return CATALOG_NAME;
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.STEEL.val;
    }

    @Override
    public String getCatalogUrl() {
        return createSdssUrl(ra, dec, searchRadius / ARCMIN_ARCSEC);
    }

    @Override
    public String[] getColumnValues() {
        String columnValues = roundTo3DecLZ(getTargetDistance()) + "," + objID + "," + roundTo7Dec(ra) + "," + roundTo7Dec(raErr) + "," + roundTo7Dec(dec) + "," + roundTo7Dec(decErr) + "," + getSdssObjectType(type) + "," + getSdssPhotometryFlag(clean) + "," + convertMJDToDateTime(new BigDecimal(Double.toString(mjd))).format(DATE_FORMATTER) + "," + specObjID + "," + roundTo3Dec(u_mag) + "," + roundTo3Dec(u_err) + "," + roundTo3Dec(g_mag) + "," + roundTo3Dec(g_err) + "," + roundTo3Dec(r_mag) + "," + roundTo3Dec(r_err) + "," + roundTo3Dec(i_mag) + "," + roundTo3Dec(i_err) + "," + roundTo3Dec(z_mag) + "," + roundTo3Dec(z_err) + "," + roundTo3Dec(get_u_g()) + "," + roundTo3Dec(get_g_r()) + "," + roundTo3Dec(get_r_i()) + "," + roundTo3Dec(get_i_z());
        return columnValues.split(",", 24);
    }

    @Override
    public String[] getColumnTitles() {
        String columnTitles = "dist (arcsec),source id,ra,ra err,dec,dec err,object type,photometry flag,observation date,spectrum pointer,u (mag),u err,g (mag),g err,r (mag),r err,i (mag),i err,z (mag),z err,u-g,g-r,r-i,i-z";
        return columnTitles.split(",", 24);
    }

    @Override
    public void applyExtinctionCorrection(Map<String, Double> extinctionsByBand) {
        if (u_mag != 0) {
            u_mag = u_mag - extinctionsByBand.get(SDSS_U);
        }
        if (g_mag != 0) {
            g_mag = g_mag - extinctionsByBand.get(SDSS_G);
        }
        if (r_mag != 0) {
            r_mag = r_mag - extinctionsByBand.get(SDSS_R);
        }
        if (i_mag != 0) {
            i_mag = i_mag - extinctionsByBand.get(SDSS_I);
        }
        if (z_mag != 0) {
            z_mag = z_mag - extinctionsByBand.get(SDSS_Z);
        }
    }

    @Override
    public Map<Band, Double> getBands() {
        return new LinkedHashMap<>();
    }

    @Override
    public Map<Color, Double> getColors(boolean toVega) {
        this.toVega = toVega;
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.u_g, get_u_g());
        colors.put(Color.g_r, get_g_r());
        colors.put(Color.r_i, get_r_i());
        colors.put(Color.i_z, get_i_z());
        return colors;
    }

    @Override
    public String getMagnitudes() {
        StringBuilder mags = new StringBuilder();
        if (u_mag != 0) {
            mags.append("u=").append(roundTo3DecNZ(u_mag)).append(" ");
        }
        if (g_mag != 0) {
            mags.append("g=").append(roundTo3DecNZ(g_mag)).append(" ");
        }
        if (r_mag != 0) {
            mags.append("r=").append(roundTo3DecNZ(r_mag)).append(" ");
        }
        if (i_mag != 0) {
            mags.append("i=").append(roundTo3DecNZ(i_mag)).append(" ");
        }
        if (z_mag != 0) {
            mags.append("z=").append(roundTo3DecNZ(z_mag)).append(" ");
        }
        return mags.toString();
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

    public BigInteger getSpecObjID() {
        return specObjID;
    }

    public double get_u_g() {
        if (u_mag == 0 || g_mag == 0) {
            return 0;
        } else {
            if (toVega) {
                return (u_mag - ABOffset.u.val) - (g_mag - ABOffset.g.val);
            } else {
                return u_mag - g_mag;
            }
        }
    }

    public double get_g_r() {
        if (g_mag == 0 || r_mag == 0) {
            return 0;
        } else {
            if (toVega) {
                return (g_mag - ABOffset.g.val) - (r_mag - ABOffset.r.val);
            } else {
                return g_mag - r_mag;
            }
        }
    }

    public double get_r_i() {
        if (r_mag == 0 || i_mag == 0) {
            return 0;
        } else {
            if (toVega) {
                return (r_mag - ABOffset.r.val) - (i_mag - ABOffset.i.val);
            } else {
                return r_mag - i_mag;
            }
        }
    }

    public double get_i_z() {
        if (i_mag == 0 || z_mag == 0) {
            return 0;
        } else {
            if (toVega) {
                return (i_mag - ABOffset.i.val) - (z_mag - ABOffset.z.val);
            } else {
                return i_mag - z_mag;
            }
        }
    }

}
