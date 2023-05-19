package astro.tool.box.catalog;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.util.Comparators.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.ServiceHelper.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.ABOffset;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SdssCatalogEntry implements CatalogEntry, Extinction {

    public static final String CATALOG_NAME = "SDSS DR17";

    // Unique object identifier
    private long objID;

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
    private LocalDateTime mjd;

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

    // Most likely spectral type
    private String spt;

    private boolean toVega;

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    private Map<String, Integer> columns;

    private String[] values;

    private static final Map<Integer, String> OBJECT_TYPES;

    static {
        OBJECT_TYPES = new HashMap<>();
        OBJECT_TYPES.put(0, "Unknown:");
        OBJECT_TYPES.put(1, "Cosmic-ray track");
        OBJECT_TYPES.put(2, "Defect");
        OBJECT_TYPES.put(3, "Galaxy");
        OBJECT_TYPES.put(4, "Ghost");
        OBJECT_TYPES.put(5, "Known object");
        OBJECT_TYPES.put(6, "Star");
        OBJECT_TYPES.put(7, "Satellite/Asteroid/Meteor trail");
        OBJECT_TYPES.put(8, "No objects in area");
        OBJECT_TYPES.put(9, "Not a type");
    }

    public SdssCatalogEntry() {
    }

    public SdssCatalogEntry(Map<String, Integer> columns, String[] values) {
        this.columns = columns;
        this.values = values;
        objID = toLong(values[columns.get("objid")]);
        ra = toDouble(values[columns.get("ra")]);
        dec = toDouble(values[columns.get("dec")]);
        raErr = toDouble(values[columns.get("raErr")]);
        decErr = toDouble(values[columns.get("decErr")]);
        type = toInteger(values[columns.get("type")]);
        clean = toInteger(values[columns.get("clean")]);
        mjd = convertMJDToDateTime(new BigDecimal(values[columns.get("mjd")]));
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
        return new SdssCatalogEntry(columns, values);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("source id", String.valueOf(objID), Alignment.LEFT, getLongComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("ra err (arcsec)", roundTo7DecNZ(raErr), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec err (arcsec)", roundTo7DecNZ(decErr), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("object type", OBJECT_TYPES.get(type), Alignment.LEFT, getStringComparator(), true));
        catalogElements.add(new CatalogElement("photometry flag", getSdssPhotometryFlag(clean), Alignment.LEFT, getStringComparator(), true));
        catalogElements.add(new CatalogElement("observation date", mjd.format(DATE_FORMATTER), Alignment.LEFT, getStringComparator()));
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
        final SdssCatalogEntry other = (SdssCatalogEntry) obj;
        return this.objID == other.objID;
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new SdssCatalogEntry(columns, values);
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
    public String getCatalogQueryUrl() {
        return createSdssUrl(ra, dec, searchRadius / ARCMIN_ARCSEC);
    }

    @Override
    public String[] getColumnValues() {
        String columnValues = roundTo3DecLZ(getTargetDistance()) + "," + objID + "," + roundTo7Dec(ra) + "," + roundTo7Dec(raErr) + "," + roundTo7Dec(dec) + "," + roundTo7Dec(decErr) + "," + OBJECT_TYPES.get(type) + "," + getSdssPhotometryFlag(clean) + "," + mjd.format(DATE_FORMATTER) + "," + specObjID + "," + roundTo3Dec(u_mag) + "," + roundTo3Dec(u_err) + "," + roundTo3Dec(g_mag) + "," + roundTo3Dec(g_err) + "," + roundTo3Dec(r_mag) + "," + roundTo3Dec(r_err) + "," + roundTo3Dec(i_mag) + "," + roundTo3Dec(i_err) + "," + roundTo3Dec(z_mag) + "," + roundTo3Dec(z_err) + "," + roundTo3Dec(get_u_g()) + "," + roundTo3Dec(get_g_r()) + "," + roundTo3Dec(get_r_i()) + "," + roundTo3Dec(get_i_z());
        return columnValues.split(",", -1);
    }

    @Override
    public String[] getColumnTitles() {
        String columnTitles = "dist (arcsec),source id,ra,ra err (arcsec),dec,dec err (arcsec),object type,photometry flag,observation date,spectrum pointer,u (mag),u err,g (mag),g err,r (mag),r err,i (mag),i err,z (mag),z err,u-g,g-r,r-i,i-z";
        return columnTitles.split(",", -1);
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
    public Map<Band, NumberPair> getBands() {
        Map<Band, NumberPair> bands = new LinkedHashMap<>();
        bands.put(Band.g, new NumberPair(g_mag, g_err));
        bands.put(Band.r, new NumberPair(r_mag, r_err));
        bands.put(Band.i, new NumberPair(i_mag, i_err));
        bands.put(Band.z, new NumberPair(z_mag, z_err));
        return bands;
    }

    @Override
    public Map<Color, Double> getColors(boolean toVega) {
        this.toVega = toVega;
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.u_g, get_u_g());
        colors.put(Color.g_r, get_g_r());
        colors.put(Color.r_i, get_r_i());
        colors.put(Color.i_z, get_i_z());
        colors.put(Color.e_u_g, get_u_g() - get_u_g_err());
        colors.put(Color.e_g_r, get_g_r() - get_g_r_err());
        colors.put(Color.e_r_i, get_r_i() - get_r_i_err());
        colors.put(Color.e_i_z, get_i_z() - get_i_z_err());
        colors.put(Color.E_u_g, get_u_g() + get_u_g_err());
        colors.put(Color.E_g_r, get_g_r() + get_g_r_err());
        colors.put(Color.E_r_i, get_r_i() + get_r_i_err());
        colors.put(Color.E_i_z, get_i_z() + get_i_z_err());
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
    public String getPhotometry() {
        StringBuilder mags = new StringBuilder();
        if (u_mag != 0) {
            mags.append(roundTo3DecNZ(u_mag)).append(",").append(roundTo3DecNZ(u_err)).append(",");
        } else {
            mags.append(",,");
        }
        if (g_mag != 0) {
            mags.append(roundTo3DecNZ(g_mag)).append(",").append(roundTo3DecNZ(g_err)).append(",");
        } else {
            mags.append(",,");
        }
        if (r_mag != 0) {
            mags.append(roundTo3DecNZ(r_mag)).append(",").append(roundTo3DecNZ(r_err)).append(",");
        } else {
            mags.append(",,");
        }
        if (i_mag != 0) {
            mags.append(roundTo3DecNZ(i_mag)).append(",").append(roundTo3DecNZ(i_err)).append(",");
        } else {
            mags.append(",,");
        }
        if (z_mag != 0) {
            mags.append(roundTo3DecNZ(z_mag)).append(",").append(roundTo3DecNZ(z_err)).append(",");
        } else {
            mags.append(",,");
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

    public String getSdssPhotometryFlag(int flag) {
        return flag == 1 ? "clean" : "unclean";
    }

    public LocalDateTime getObsDate() {
        return mjd;
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

    public double get_u_g_err() {
        if (u_err == 0 || g_err == 0) {
            return 0;
        } else {
            if (toVega) {
                return calculateAdditionError((u_err - ABOffset.u.val), (g_err - ABOffset.g.val));
            } else {
                return calculateAdditionError(u_err, g_err);
            }
        }
    }

    public double get_g_r_err() {
        if (g_err == 0 || r_err == 0) {
            return 0;
        } else {
            if (toVega) {
                return calculateAdditionError((g_err - ABOffset.g.val), (r_err - ABOffset.r.val));
            } else {
                return calculateAdditionError(g_err, r_err);
            }
        }
    }

    public double get_r_i_err() {
        if (r_err == 0 || i_err == 0) {
            return 0;
        } else {
            if (toVega) {
                return calculateAdditionError((r_err - ABOffset.r.val), (i_err - ABOffset.i.val));
            } else {
                return calculateAdditionError(r_err, i_err);
            }
        }
    }

    public double get_i_z_err() {
        if (i_err == 0 || z_err == 0) {
            return 0;
        } else {
            if (toVega) {
                return calculateAdditionError((i_err - ABOffset.i.val), (z_err - ABOffset.z.val));
            } else {
                return calculateAdditionError(i_err, z_err);
            }
        }
    }

}
