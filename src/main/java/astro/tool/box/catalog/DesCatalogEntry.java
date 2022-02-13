package astro.tool.box.catalog;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.util.Comparators.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.ServiceHelper.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.container.StringPair;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.exception.NoExtinctionValuesException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DesCatalogEntry implements CatalogEntry {

    public static final String CATALOG_NAME = "DES DR1";

    // Identifier based on IAU format
    private String sourceId;

    // Right ascension (J2000)
    private double ra;

    // Declination (J2000)
    private double dec;

    // Extended source flag for g-band (0=galaxy; 1=star)
    private double g_ext;

    // Extended source flag for r-band (0=galaxy; 1=star)
    private double r_ext;

    // Extended source flag for i-band (0=galaxy; 1=star)
    private double i_ext;

    // Extended source flag for z-band (0=galaxy; 1=star)
    private double z_ext;

    // Extended source flag for Y-band (0=galaxy; 1=star)
    private double y_ext;

    // Cautionary flag for g-band (<4=well behaved objects)
    private int g_caut;

    // Cautionary flag for r-band (<4=well behaved objects)
    private int r_caut;

    // Cautionary flag for i-band (<4=well behaved objects)
    private int i_caut;

    // Cautionary flag for z-band (<4=well behaved objects)
    private int z_caut;

    // Cautionary flag for y-band (<4=well behaved objects)
    private int y_caut;

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

    // Magnitude in Y band
    private double y_mag;

    // Error in Y band
    private double y_err;

    // Galactic longitude
    private double glon;

    // Galactic latitude
    private double glat;

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

    private static final Map<Integer, String> CAUTIONARY_FLAGS;

    static {
        CAUTIONARY_FLAGS = new HashMap<>();
        CAUTIONARY_FLAGS.put(1, "The object has neighbors, bright and close enough to significantly bias the photometry, or bad pixels (more than 10% of the integrated area affected).");
        CAUTIONARY_FLAGS.put(2, "The object was originally blended with another one.");
        CAUTIONARY_FLAGS.put(4, "At least one pixel of the object is saturated (or very close to).");
        CAUTIONARY_FLAGS.put(8, "The object is truncated (too close to an image boundary).");
        CAUTIONARY_FLAGS.put(16, "Object's aperture data are incomplete or corrupted.");
        CAUTIONARY_FLAGS.put(32, "Object's isophotal data are incomplete or corrupted.");
        CAUTIONARY_FLAGS.put(64, "A memory overflow occurred during deblending.");
        CAUTIONARY_FLAGS.put(128, "A memory overflow occurred during extraction.");
    }

    public DesCatalogEntry() {
    }

    public DesCatalogEntry(Map<String, Integer> columns, String[] values) {
        this.columns = columns;
        this.values = values;
        sourceId = values[columns.get("DES")];
        ra = toDouble(values[columns.get("RAJ2000")]);
        dec = toDouble(values[columns.get("DEJ2000")]);
        g_ext = toDouble(values[columns.get("S/Gg")]);
        r_ext = toDouble(values[columns.get("S/Gr")]);
        i_ext = toDouble(values[columns.get("S/Gi")]);
        z_ext = toDouble(values[columns.get("S/Gz")]);
        y_ext = toDouble(values[columns.get("S/GY")]);
        g_caut = toInteger(values[columns.get("gFlag")]);
        r_caut = toInteger(values[columns.get("rFlag")]);
        i_caut = toInteger(values[columns.get("iFlag")]);
        z_caut = toInteger(values[columns.get("zFlag")]);
        y_caut = toInteger(values[columns.get("YFlag")]);
        g_mag = toDouble(values[columns.get("gmag")]);
        g_err = toDouble(values[columns.get("e_gmag")]);
        r_mag = toDouble(values[columns.get("rmag")]);
        r_err = toDouble(values[columns.get("e_rmag")]);
        i_mag = toDouble(values[columns.get("imag")]);
        i_err = toDouble(values[columns.get("e_imag")]);
        z_mag = toDouble(values[columns.get("zmag")]);
        z_err = toDouble(values[columns.get("e_zmag")]);
        y_mag = toDouble(values[columns.get("Ymag")]);
        y_err = toDouble(values[columns.get("e_Ymag")]);
        glon = toDouble(values[columns.get("GLON")]);
        glat = toDouble(values[columns.get("GLAT")]);
    }

    @Override
    public CatalogEntry copy() {
        return new DesCatalogEntry(columns, values);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("source id", String.valueOf(sourceId), Alignment.LEFT, getLongComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("g Galaxy-Star (0-1)", roundTo2DecNZ(g_ext), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("r Galaxy-Star (0-1)", roundTo2DecNZ(r_ext), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("i Galaxy-Star (0-1)", roundTo2DecNZ(i_ext), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("z Galaxy-Star (0-1)", roundTo2DecNZ(z_ext), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Y Galaxy-Star (0-1)", roundTo2DecNZ(y_ext), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("g cautionary flag", String.valueOf(g_caut), Alignment.RIGHT, getIntegerComparator(), createToolTipCautionaryFlag(g_caut)));
        catalogElements.add(new CatalogElement("r cautionary flag", String.valueOf(r_caut), Alignment.RIGHT, getIntegerComparator(), createToolTipCautionaryFlag(r_caut)));
        catalogElements.add(new CatalogElement("i cautionary flag", String.valueOf(i_caut), Alignment.RIGHT, getIntegerComparator(), createToolTipCautionaryFlag(i_caut)));
        catalogElements.add(new CatalogElement("z cautionary flag", String.valueOf(z_caut), Alignment.RIGHT, getIntegerComparator(), createToolTipCautionaryFlag(z_caut)));
        catalogElements.add(new CatalogElement("Y cautionary flag", String.valueOf(y_caut), Alignment.RIGHT, getIntegerComparator(), createToolTipCautionaryFlag(y_caut)));
        catalogElements.add(new CatalogElement("g (mag)", roundTo3DecNZ(g_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("g err", roundTo3DecNZ(g_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("r (mag)", roundTo3DecNZ(r_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("r err", roundTo3DecNZ(r_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("i (mag)", roundTo3DecNZ(i_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("i err", roundTo3DecNZ(i_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("z (mag)", roundTo3DecNZ(z_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("z err", roundTo3DecNZ(z_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Y (mag)", roundTo3DecNZ(y_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Y err", roundTo3DecNZ(y_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("g-r", roundTo3DecNZ(get_g_r()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("r-i", roundTo3DecNZ(get_r_i()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("i-z", roundTo3DecNZ(get_i_z()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("z-Y", roundTo3DecNZ(get_z_y()), Alignment.RIGHT, getDoubleComparator(), false, true));
    }

    public String createToolTipCautionaryFlag(Integer cautionaryFlag) {
        StringBuilder toolTip = new StringBuilder();
        toolTip.append("<b>Cautionary flag details:</b>");
        List<StringPair> flagLabels = getFlagLabels(cautionaryFlag, CAUTIONARY_FLAGS);
        if (flagLabels.isEmpty()) {
            toolTip.append(LINE_BREAK).append("No warnings.");
        } else {
            flagLabels.forEach((flag) -> {
                toolTip.append(LINE_BREAK).append(flag.getS1()).append(" = ").append(flag.getS2());
            });
        }
        return toolTip.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.sourceId);
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
        final DesCatalogEntry other = (DesCatalogEntry) obj;
        return Objects.equals(this.sourceId, other.sourceId);
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new DesCatalogEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return CATALOG_NAME;
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.SAND.val;
    }

    @Override
    public String getCatalogQueryUrl() {
        return createVizieRUrl(ra, dec, searchRadius / DEG_ARCSEC, "II/357/des_dr1", "RAJ2000", "DEJ2000");
    }

    @Override
    public String[] getColumnValues() {
        String columnValues = roundTo3DecLZ(getTargetDistance()) + ","
                + sourceId + ","
                + roundTo7Dec(ra) + ","
                + roundTo7Dec(dec) + ","
                + roundTo2Dec(g_ext) + ","
                + roundTo2Dec(r_ext) + ","
                + roundTo2Dec(i_ext) + ","
                + roundTo2Dec(z_ext) + ","
                + roundTo2Dec(y_ext) + ","
                + g_caut + ","
                + r_caut + ","
                + i_caut + ","
                + z_caut + ","
                + y_caut + ","
                + roundTo3Dec(g_mag) + ","
                + roundTo3Dec(g_err) + ","
                + roundTo3Dec(r_mag) + ","
                + roundTo3Dec(r_err) + ","
                + roundTo3Dec(i_mag) + ","
                + roundTo3Dec(i_err) + ","
                + roundTo3Dec(z_mag) + ","
                + roundTo3Dec(z_err) + ","
                + roundTo3Dec(y_mag) + ","
                + roundTo3Dec(y_err) + ","
                + roundTo3Dec(get_g_r()) + ","
                + roundTo3Dec(get_r_i()) + ","
                + roundTo3Dec(get_i_z()) + ","
                + roundTo3Dec(get_z_y());
        return columnValues.split(",", -1);
    }

    @Override
    public String[] getColumnTitles() {
        String columnTitles = "dist (arcsec),"
                + "source id,"
                + "ra,"
                + "dec,"
                + "g Galaxy-Star (0-1),"
                + "r Galaxy-Star (0-1),"
                + "i Galaxy-Star (0-1),"
                + "z Galaxy-Star (0-1),"
                + "Y Galaxy-Star (0-1),"
                + "g cautionary flag,"
                + "r cautionary flag,"
                + "i cautionary flag,"
                + "z cautionary flag,"
                + "Y cautionary flag,"
                + "u (mag),"
                + "u err,"
                + "g (mag),"
                + "g err,"
                + "r (mag),"
                + "r err,"
                + "i (mag),"
                + "i err,"
                + "z (mag),"
                + "z err,"
                + "Y (mag),"
                + "Y err,"
                + "u-g,"
                + "g-r,"
                + "r-i,"
                + "i-z,"
                + "z-Y";
        return columnTitles.split(",", -1);
    }

    @Override
    public void applyExtinctionCorrection(Map<String, Double> extinctionsByBand) throws NoExtinctionValuesException {
        throw new NoExtinctionValuesException();
    }

    @Override
    public Map<Band, NumberPair> getBands() {
        Map<Band, NumberPair> bands = new LinkedHashMap<>();
        bands.put(Band.g, new NumberPair(g_mag, g_err));
        bands.put(Band.r, new NumberPair(r_mag, r_err));
        bands.put(Band.i, new NumberPair(i_mag, i_err));
        bands.put(Band.z, new NumberPair(z_mag, z_err));
        //bands.put(Band.y, new NumberPair(y_mag, y_err));
        return bands;
    }

    @Override
    public Map<Color, Double> getColors(boolean toVega) {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.g_r_DES, get_g_r());
        colors.put(Color.r_i_DES, get_r_i());
        colors.put(Color.i_z_DES, get_i_z());
        colors.put(Color.z_Y_DES, get_z_y());
        return colors;
    }

    @Override
    public String getMagnitudes() {
        StringBuilder mags = new StringBuilder();
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
        if (y_mag != 0) {
            mags.append("y=").append(roundTo3DecNZ(y_mag)).append(" ");
        }
        return mags.toString();
    }

    @Override
    public String getPhotometry() {
        StringBuilder mags = new StringBuilder();
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
        if (y_mag != 0) {
            mags.append(roundTo3DecNZ(y_mag)).append(",").append(roundTo3DecNZ(y_err)).append(",");
        } else {
            mags.append(",,");
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

    public double getGlon() {
        return glon;
    }

    public double getGlat() {
        return glat;
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

    public double get_z_y() {
        if (z_mag == 0 || y_mag == 0) {
            return 0;
        } else {
            return z_mag - y_mag;
        }
    }

    public double get_g_mag() {
        return g_mag;
    }

    public double get_r_mag() {
        return r_mag;
    }

    public double get_i_mag() {
        return i_mag;
    }

    public double get_z_mag() {
        return z_mag;
    }

    public double get_y_mag() {
        return y_mag;
    }

}
