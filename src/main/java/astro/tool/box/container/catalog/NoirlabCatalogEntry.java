package astro.tool.box.container.catalog;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.util.Comparators.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.Utils.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.exception.NoExtinctionValuesException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NoirlabCatalogEntry implements CatalogEntry, ProperMotionQuery {

    public static final String CATALOG_NAME = "NOIRLab DR2";

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
    private double mean_mjd;

    // Number of detections in all bands
    private int ndet;

    // Range of Modified Julian Date
    private double delta_mjd;

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

    // Magnitude in VR band
    private double vr_mag;

    // Magnitude in Y band
    private double y_mag;

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

    // Error in VR band
    private double vr_err;

    // Error in Y band
    private double y_err;

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
        pmra = toDouble(getFixedPM(values[columns.get("pmra")]));
        pmra_err = toDouble(getFixedPM(values[columns.get("pmraerr")]));
        pmdec = toDouble(getFixedPM(values[columns.get("pmdec")]));
        pmdec_err = toDouble(getFixedPM(values[columns.get("pmdecerr")]));
        type = toDouble(values[columns.get("class_star")]);
        mean_mjd = toDouble(values[columns.get("mjd")]);
        ndet = toInteger(values[columns.get("ndet")]);
        delta_mjd = toDouble(values[columns.get("deltamjd")]);
        u_mag = getFixedMag(toDouble(values[columns.get("umag")]));
        g_mag = getFixedMag(toDouble(values[columns.get("gmag")]));
        r_mag = getFixedMag(toDouble(values[columns.get("rmag")]));
        i_mag = getFixedMag(toDouble(values[columns.get("imag")]));
        z_mag = getFixedMag(toDouble(values[columns.get("zmag")]));
        vr_mag = getFixedMag(toDouble(values[columns.get("vrmag")]));
        y_mag = getFixedMag(toDouble(values[columns.get("ymag")]));
        u_err = getFixedErr(toDouble(values[columns.get("uerr")]));
        g_err = getFixedErr(toDouble(values[columns.get("gerr")]));
        r_err = getFixedErr(toDouble(values[columns.get("rerr")]));
        i_err = getFixedErr(toDouble(values[columns.get("ierr")]));
        z_err = getFixedErr(toDouble(values[columns.get("zerr")]));
        vr_err = getFixedErr(toDouble(values[columns.get("vrerr")]));
        y_err = getFixedErr(toDouble(values[columns.get("yerr")]));
    }

    private String getFixedPM(String pm) {
        return "NaN".equals(pm) ? "0" : pm;
    }

    private double getFixedMag(double mag) {
        return mag > 99.9 ? 0 : mag;
    }

    private double getFixedErr(double err) {
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
        catalogElements.add(new CatalogElement("ra err", roundTo7DecNZ(ra_err), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec err", roundTo7DecNZ(dec_err), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmra (mas/yr)", roundTo3DecNZ(pmra), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmra err", roundTo3DecNZ(pmra_err), Alignment.RIGHT, getDoubleComparator(), false, false, isProperMotionFaulty(pmra, pmra_err)));
        catalogElements.add(new CatalogElement("pmdec (mas/yr)", roundTo3DecNZ(pmdec), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmdec err", roundTo3DecNZ(pmdec_err), Alignment.RIGHT, getDoubleComparator(), false, false, isProperMotionFaulty(pmdec, pmdec_err)));
        catalogElements.add(new CatalogElement("Galaxy-Star (0-1)", roundTo2DecNZ(type), Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("mean mjd", convertMJDToDateTime(new BigDecimal(Double.toString(mean_mjd))).format(DATE_TIME_FORMATTER), Alignment.LEFT, getStringComparator()));
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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NoirlabCatalogEntry{sourceId=").append(sourceId);
        sb.append(", ra=").append(ra);
        sb.append(", ra_err=").append(ra_err);
        sb.append(", dec=").append(dec);
        sb.append(", dec_err=").append(dec_err);
        sb.append(", type=").append(type);
        sb.append(", pmra=").append(pmra);
        sb.append(", pmra_err=").append(pmra_err);
        sb.append(", pmdec=").append(pmdec);
        sb.append(", pmdec_err=").append(pmdec_err);
        sb.append(", mean_mjd=").append(mean_mjd);
        sb.append(", ndet=").append(ndet);
        sb.append(", delta_mjd=").append(delta_mjd);
        sb.append(", u_mag=").append(u_mag);
        sb.append(", g_mag=").append(g_mag);
        sb.append(", r_mag=").append(r_mag);
        sb.append(", i_mag=").append(i_mag);
        sb.append(", z_mag=").append(z_mag);
        sb.append(", vr_mag=").append(vr_mag);
        sb.append(", y_mag=").append(y_mag);
        sb.append(", u_err=").append(u_err);
        sb.append(", g_err=").append(g_err);
        sb.append(", r_err=").append(r_err);
        sb.append(", i_err=").append(i_err);
        sb.append(", z_err=").append(z_err);
        sb.append(", vr_err=").append(vr_err);
        sb.append(", y_err=").append(y_err);
        sb.append(", targetRa=").append(targetRa);
        sb.append(", targetDec=").append(targetDec);
        sb.append(", pixelRa=").append(pixelRa);
        sb.append(", pixelDec=").append(pixelDec);
        sb.append(", searchRadius=").append(searchRadius);
        sb.append(", tpm=").append(tpm);
        sb.append(", catalogNumber=").append(catalogNumber);
        sb.append(", spt=").append(spt);
        sb.append(", catalogElements=").append(catalogElements);
        sb.append(", columns=").append(columns);
        sb.append(", values=").append(values);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.sourceId);
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
        return JColor.LIGHT_NAVY.val;
    }

    @Override
    public String getCatalogUrl() {
        return NOAO_TAP_URL + encodeQuery(createCatalogQuery());
    }

    @Override
    public String getProperMotionQueryUrl() {
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
        addRow(query, "       gmag,");
        addRow(query, "       rmag,");
        addRow(query, "       imag,");
        addRow(query, "       zmag,");
        addRow(query, "       vrmag,");
        addRow(query, "       ymag,");
        addRow(query, "       uerr,");
        addRow(query, "       gerr,");
        addRow(query, "       rerr,");
        addRow(query, "       ierr,");
        addRow(query, "       zerr,");
        addRow(query, "       vrerr,");
        addRow(query, "       yerr");
        addRow(query, "FROM   nsc_dr2.object");
        addRow(query, "WHERE  't'=q3c_radial_query(ra, dec, " + ra + ", " + dec + ", " + searchRadius / DEG_ARCSEC + ")");
        return query.toString();
    }

    private String createProperMotionQuery() {
        StringBuilder query = new StringBuilder();
        addRow(query, createCatalogQuery());
        addRow(query, "AND   pmra <> 'NaN' AND pmdec <> 'NaN'");
        addRow(query, "AND   deltamjd >= 180"); // -> at least 6 month time baseline
        //addRow(query, "AND   POWER(pmra / pmraerr, 2) + POWER(pmdec / pmdecerr, 2) > 27"); // -> chi2_motion significance of motion metric
        addRow(query, "AND   SQRT(pmra * pmra + pmdec * pmdec) >= " + tpm);
        return query.toString();
    }

    @Override
    public void setTpm(double tpm) {
        this.tpm = tpm;
    }

    @Override
    public String[] getColumnValues() {
        String columnValues = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + roundTo7Dec(ra) + "," + roundTo7Dec(ra_err) + "," + roundTo7Dec(dec) + "," + roundTo7Dec(dec_err) + "," + roundTo3Dec(pmra) + "," + roundTo3Dec(pmra_err) + "," + roundTo3Dec(pmdec) + "," + roundTo3Dec(pmdec_err) + "," + roundTo2DecNZ(type) + "," + convertMJDToDateTime(new BigDecimal(Double.toString(mean_mjd))).format(DATE_TIME_FORMATTER) + "," + ndet + "," + roundTo3Dec(delta_mjd) + "," + roundTo3Dec(u_mag) + "," + roundTo3Dec(u_err) + "," + roundTo3Dec(g_mag) + "," + roundTo3Dec(g_err) + "," + roundTo3Dec(r_mag) + "," + roundTo3Dec(r_err) + "," + roundTo3Dec(i_mag) + "," + roundTo3Dec(i_err) + "," + roundTo3Dec(z_mag) + "," + roundTo3Dec(z_err) + "," + roundTo3Dec(y_mag) + "," + roundTo3Dec(y_err) + "," + roundTo3Dec(vr_mag) + "," + roundTo3Dec(vr_err) + "," + roundTo3Dec(get_u_g()) + "," + roundTo3Dec(get_g_r()) + "," + roundTo3Dec(get_r_i()) + "," + roundTo3Dec(get_i_z()) + "," + roundTo3Dec(get_z_y()) + "," + roundTo3Dec(getTotalProperMotion());
        return columnValues.split(",", 34);
    }

    @Override
    public String[] getColumnTitles() {
        String columnTitles = "dist (arcsec),source id,ra,ra err,dec,dec err,pmra (mas/yr),pmra err,pmdec (mas/yr),pmdec err,Galaxy-Star (0-1),mean mjd,detections,delta mjd,u (mag),u err,g (mag),g err,r (mag),r err,i (mag),i err,z (mag),z err,Y (mag),Y err,VR (mag),VR err,u-g,g-r,r-i,i-z,z-Y,tpm (mas/yr)";
        return columnTitles.split(",", 34);
    }

    @Override
    public void applyExtinctionCorrection(Map<String, Double> extinctionsByBand) throws NoExtinctionValuesException {
        throw new NoExtinctionValuesException();
    }

    @Override
    public Map<Band, Double> getBands() {
        Map<Band, Double> bands = new LinkedHashMap<>();
        bands.put(Band.r, r_mag);
        bands.put(Band.i, i_mag);
        bands.put(Band.z, z_mag);
        return bands;
    }

    @Override
    public Map<Color, Double> getColors() {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.u_g, get_u_g());
        colors.put(Color.g_r, get_g_r());
        colors.put(Color.r_i, get_r_i());
        colors.put(Color.i_z, get_i_z());
        colors.put(Color.z_Y, get_z_y());
        return colors;
    }

    @Override
    public String getMagnitudes() {
        return String.format("u=%s; g=%s; r=%s; i=%s; z=%s, VR=%s, Y=%s", roundTo3DecNZ(u_mag), roundTo3DecNZ(g_mag), roundTo3DecNZ(r_mag), roundTo3DecNZ(i_mag), roundTo3DecNZ(z_mag), roundTo3DecNZ(vr_mag), roundTo3DecNZ(y_mag));
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
    public double getTargetDistance() {
        return calculateAngularDistance(new NumberPair(targetRa, targetDec), new NumberPair(ra, dec), DEG_ARCSEC);
    }

    public double getMeanEpoch() {
        LocalDate date = convertMJDToDateTime(new BigDecimal(Double.toString(mean_mjd))).toLocalDate();
        long days = ChronoUnit.DAYS.between(LocalDate.of(0, Month.JANUARY, 1), date);
        return days / 365.2425;
    }

    public double getTotalProperMotion() {
        return calculateTotalProperMotion(pmra, pmdec);
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

}
