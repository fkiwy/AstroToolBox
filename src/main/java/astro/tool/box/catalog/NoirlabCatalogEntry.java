package astro.tool.box.catalog;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.util.Comparators.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.MiscUtils.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.exception.ExtinctionException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NoirlabCatalogEntry implements CatalogEntry, ProperMotionQuery, ProperMotionCatalog {

    public static final String CATALOG_NAME = "NSC DR2";

    // Unique source identifier (unique within a particular Data Release)
    private String sourceId;

    // Right ascension
    private double ra;

    // Error in right ascension
    private double ra_err;

    // Declination
    private double dec;

    // Error in declination
    private double dec_err;

    // Galaxy-Star (0-1)
    private double type;

    // Proper motion in right ascension direction
    private double pmra;

    // Standard error of proper motion in right ascension direction
    private double pmra_err;

    // Proper motion in declination direction
    private double pmdec;

    // Standard error of proper motion in declination direction
    private double pmdec_err;

    // Mean Modified Julian Date
    private LocalDateTime mean_mjd;

    // Number of detections in all bands
    private int ndet;

    // Range of Modified Julian Date
    private double delta_mjd;

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

    // Magnitude in Y band
    private double y_mag;

    // Error in Y band
    private double y_err;

    // Magnitude in VR band
    private double vr_mag;

    // Error in VR band
    private double vr_err;

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

    // Total proper motion
    private double tpm;

    // Most likely spectral type
    private String spt;

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    private Map<String, Integer> columns;

    private String[] values;

    public NoirlabCatalogEntry() {
    }

    public NoirlabCatalogEntry(Map<String, Integer> columns, String[] values) {
        this.columns = columns;
        this.values = values;
        sourceId = values[columns.get("id")];
        ra = toDouble(values[columns.get("ra")]);
        ra_err = toDouble(values[columns.get("raerr")]);
        dec = toDouble(values[columns.get("dec")]);
        dec_err = toDouble(values[columns.get("decerr")]);
        pmra = toDouble(fixPmVal(values[columns.get("pmra")]));
        pmra_err = toDouble(fixPmVal(values[columns.get("pmraerr")]));
        pmdec = toDouble(fixPmVal(values[columns.get("pmdec")]));
        pmdec_err = toDouble(fixPmVal(values[columns.get("pmdecerr")]));
        type = toDouble(values[columns.get("class_star")]);
        mean_mjd = convertMJDToDateTime(new BigDecimal(values[columns.get("mjd")]));
        ndet = toInteger(values[columns.get("ndet")]);
        delta_mjd = toDouble(values[columns.get("deltamjd")]);
        u_mag = fixMagVal(toDouble(values[columns.get("umag")]));
        u_err = fixMagErr(toDouble(values[columns.get("uerr")]));
        g_mag = fixMagVal(toDouble(values[columns.get("gmag")]));
        g_err = fixMagErr(toDouble(values[columns.get("gerr")]));
        r_mag = fixMagVal(toDouble(values[columns.get("rmag")]));
        r_err = fixMagErr(toDouble(values[columns.get("rerr")]));
        i_mag = fixMagVal(toDouble(values[columns.get("imag")]));
        i_err = fixMagErr(toDouble(values[columns.get("ierr")]));
        z_mag = fixMagVal(toDouble(values[columns.get("zmag")]));
        z_err = fixMagErr(toDouble(values[columns.get("zerr")]));
        y_mag = fixMagVal(toDouble(values[columns.get("ymag")]));
        y_err = fixMagErr(toDouble(values[columns.get("yerr")]));
        vr_mag = fixMagVal(toDouble(values[columns.get("vrmag")]));
        vr_err = fixMagErr(toDouble(values[columns.get("vrerr")]));
        glon = toDouble(values[columns.get("glon")]);
        glat = toDouble(values[columns.get("glat")]);
    }

    private String fixPmVal(String pm) {
        return "NaN".equals(pm) ? "0" : pm;
    }

    private double fixMagVal(double mag) {
        return mag > 99.9 ? 0 : mag;
    }

    private double fixMagErr(double err) {
        return err > 9.9 ? 0 : err;
    }

    @Override
    public CatalogEntry copy() {
        return new NoirlabCatalogEntry(columns, values);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("source id", String.valueOf(sourceId), Alignment.LEFT, getLongComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("ra err (arcsec)", roundTo7DecNZ(ra_err), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec err (arcsec)", roundTo7DecNZ(dec_err), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmra (mas/yr)", roundTo3DecNZ(pmra), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmra err", roundTo3DecNZ(pmra_err), Alignment.RIGHT, getDoubleComparator(), false, false, isProperMotionFaulty(pmra, pmra_err)));
        catalogElements.add(new CatalogElement("pmdec (mas/yr)", roundTo3DecNZ(pmdec), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmdec err", roundTo3DecNZ(pmdec_err), Alignment.RIGHT, getDoubleComparator(), false, false, isProperMotionFaulty(pmdec, pmdec_err)));
        catalogElements.add(new CatalogElement("Galaxy-Star (0-1)", roundTo2DecNZ(type), Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("mean mjd", mean_mjd.format(DATE_TIME_FORMATTER), Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("detections", String.valueOf(ndet), Alignment.RIGHT, getIntegerComparator()));
        catalogElements.add(new CatalogElement("delta mjd", roundTo3DecNZ(delta_mjd), Alignment.RIGHT, getDoubleComparator()));
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
        catalogElements.add(new CatalogElement("Y (mag)", roundTo3DecNZ(y_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Y err", roundTo3DecNZ(y_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("VR (mag)", roundTo3DecNZ(vr_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("VR err", roundTo3DecNZ(vr_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("u-g", roundTo3DecNZ(get_u_g()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("g-r", roundTo3DecNZ(get_g_r()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("r-i", roundTo3DecNZ(get_r_i()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("i-z", roundTo3DecNZ(get_i_z()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("z-Y", roundTo3DecNZ(get_z_y()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("tpm (mas/yr)", roundTo3DecNZ(getTotalProperMotion()), Alignment.RIGHT, getDoubleComparator(), false, true));
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
        final NoirlabCatalogEntry other = (NoirlabCatalogEntry) obj;
        return Objects.equals(this.sourceId, other.sourceId);
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new NoirlabCatalogEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return CATALOG_NAME;
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.NAVY.val;
    }

    @Override
    public String getCatalogQueryUrl() {
        return NOAO_TAP_URL + encodeQuery(createCatalogQuery());
    }

    @Override
    public String getMotionQueryUrl() {
        return NOAO_TAP_URL + encodeQuery(createProperMotionQuery());
    }

    private String createCatalogQuery() {
        StringBuilder query = new StringBuilder();
        addRow(query, "SELECT id,");
        addRow(query, "       ra,");
        addRow(query, "       raerr,");
        addRow(query, "       dec,");
        addRow(query, "       decerr,");
        addRow(query, "       pmra,");
        addRow(query, "       pmraerr,");
        addRow(query, "       pmdec,");
        addRow(query, "       pmdecerr,");
        addRow(query, "       class_star,");
        addRow(query, "       mjd,");
        addRow(query, "       ndet,");
        addRow(query, "       deltamjd,");
        addRow(query, "       umag,");
        addRow(query, "       uerr,");
        addRow(query, "       gmag,");
        addRow(query, "       gerr,");
        addRow(query, "       rmag,");
        addRow(query, "       rerr,");
        addRow(query, "       imag,");
        addRow(query, "       ierr,");
        addRow(query, "       zmag,");
        addRow(query, "       zerr,");
        addRow(query, "       ymag,");
        addRow(query, "       yerr,");
        addRow(query, "       vrmag,");
        addRow(query, "       vrerr,");
        addRow(query, "       glat,");
        addRow(query, "       glon");
        addRow(query, "FROM   nsc_dr2.object");
        addRow(query, "WHERE  't'=q3c_radial_query(ra, dec, " + ra + ", " + dec + ", " + searchRadius / DEG_ARCSEC + ")");
        return query.toString();
    }

    private String createProperMotionQuery() {
        StringBuilder query = new StringBuilder();
        addRow(query, createCatalogQuery());
        addRow(query, "AND    class_star > 0.7");
        addRow(query, "AND    ndet > 3");
        addRow(query, "AND    deltamjd > 180");
        addRow(query, "AND    SQRT(pmra * pmra + pmdec * pmdec) >= " + tpm);
        return query.toString();
    }

    @Override
    public void setTpm(double tpm) {
        this.tpm = tpm;
    }

    @Override
    public String[] getColumnValues() {
        String columnValues = roundTo3DecLZ(getTargetDistance()) + ","
                + sourceId + ","
                + roundTo7Dec(ra) + ","
                + roundTo7Dec(ra_err) + ","
                + roundTo7Dec(dec) + ","
                + roundTo7Dec(dec_err) + ","
                + roundTo3Dec(pmra) + ","
                + roundTo3Dec(pmra_err) + ","
                + roundTo3Dec(pmdec) + ","
                + roundTo3Dec(pmdec_err) + ","
                + roundTo2DecNZ(type) + ","
                + mean_mjd.format(DATE_TIME_FORMATTER) + ","
                + ndet + ","
                + roundTo3Dec(delta_mjd) + ","
                + roundTo3Dec(u_mag) + ","
                + roundTo3Dec(u_err) + ","
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
                + roundTo3Dec(vr_mag) + ","
                + roundTo3Dec(vr_err) + ","
                + roundTo3Dec(get_u_g()) + ","
                + roundTo3Dec(get_g_r()) + ","
                + roundTo3Dec(get_r_i()) + ","
                + roundTo3Dec(get_i_z()) + ","
                + roundTo3Dec(get_z_y()) + ","
                + roundTo3Dec(getTotalProperMotion());
        return columnValues.split(",", -1);
    }

    @Override
    public String[] getColumnTitles() {
        String columnTitles = "dist (arcsec),"
                + "source id,"
                + "ra,"
                + "ra err (arcsec),"
                + "dec,"
                + "dec err (arcsec),"
                + "pmra (mas/yr),"
                + "pmra err,"
                + "pmdec (mas/yr),"
                + "pmdec err,"
                + "Galaxy-Star (0-1),"
                + "mean mjd,"
                + "detections,"
                + "delta mjd,"
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
                + "VR (mag),"
                + "VR err,"
                + "u-g,"
                + "g-r,"
                + "r-i,"
                + "i-z,"
                + "z-Y,"
                + "tpm (mas/yr)";
        return columnTitles.split(",", -1);
    }

    @Override
    public void applyExtinctionCorrection(Map<String, Double> extinctionsByBand) throws ExtinctionException {
        throw new ExtinctionException();
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
        colors.put(Color.g_r_NSC, get_g_r());
        colors.put(Color.r_i_NSC, get_r_i());
        colors.put(Color.i_z_NSC, get_i_z());
        colors.put(Color.z_Y_NSC, get_z_y());
        colors.put(Color.e_g_r_NSC, get_g_r() - get_g_r_err());
        colors.put(Color.e_r_i_NSC, get_r_i() - get_r_i_err());
        colors.put(Color.e_i_z_NSC, get_i_z() - get_i_z_err());
        colors.put(Color.e_z_Y_NSC, get_z_y() - get_z_y_err());
        colors.put(Color.E_g_r_NSC, get_g_r() + get_g_r_err());
        colors.put(Color.E_r_i_NSC, get_r_i() + get_r_i_err());
        colors.put(Color.E_i_z_NSC, get_i_z() + get_i_z_err());
        colors.put(Color.E_z_Y_NSC, get_z_y() + get_z_y_err());
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
        if (y_mag != 0) {
            mags.append("y=").append(roundTo3DecNZ(y_mag)).append(" ");
        }
        if (vr_mag != 0) {
            mags.append("vr=").append(roundTo3DecNZ(vr_mag)).append(" ");
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

    public double getMeanEpoch() {
        return convertDateToYear(mean_mjd);
    }

    public int getNdet() {
        return ndet;
    }

    public double getDelta_mjd() {
        return delta_mjd;
    }

    public double getGlon() {
        return glon;
    }

    public double getGlat() {
        return glat;
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

    public double get_z_y() {
        if (z_mag == 0 || y_mag == 0) {
            return 0;
        } else {
            return z_mag - y_mag;
        }
    }

    public double get_u_g_err() {
        if (u_err == 0 || g_err == 0) {
            return 0;
        } else {
            return calculateAddSubError(u_err, g_err);
        }
    }

    public double get_g_r_err() {
        if (g_err == 0 || r_err == 0) {
            return 0;
        } else {
            return calculateAddSubError(g_err, r_err);
        }
    }

    public double get_r_i_err() {
        if (r_err == 0 || i_err == 0) {
            return 0;
        } else {
            return calculateAddSubError(r_err, i_err);
        }
    }

    public double get_i_z_err() {
        if (i_err == 0 || z_err == 0) {
            return 0;
        } else {
            return calculateAddSubError(i_err, z_err);
        }
    }

    public double get_z_y_err() {
        if (z_err == 0 || y_err == 0) {
            return 0;
        } else {
            return calculateAddSubError(z_err, y_err);
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
