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
import astro.tool.box.enumeration.ABOffset;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GaiaWDCatalogEntry implements CatalogEntry {

    public static final String CATALOG_NAME = "Gaia EDR3 WD";

    // Unique source identifier (unique within a particular Data Release)
    private long sourceId;

    // WD name
    private String wdId;

    // Right ascension
    private double ra;

    // Declination
    private double dec;

    // Parallax
    private double plx;

    // Proper motion in right ascension direction
    private double pmra;

    // Proper motion in declination direction
    private double pmdec;

    // G-band mean magnitude
    private double Gmag;

    // Integrated BP mean magnitude
    private double BPmag;

    // Integrated RP mean magnitude
    private double RPmag;

    // SDSS object name if available
    private String sdssId;

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

    // The probability of being a white dwarf
    private double pwd;

    // Effective temperature from fitting the dereddened G, GBP and GRP absolute fluxes with pure-H model atmospheres
    private double teffH;

    // Surface gravity from fitting the dereddened G, GBP and GRP absolute fluxes with pure-H model atmospheres
    private double loggH;

    // Stellar mass resulting from the adopted mass-radius relation
    private double massH;

    // Effective temperature from fitting the dereddened G, GBP and GRP absolute fluxes with pure-He model atmospheres
    private double teffHe;

    // Surface gravity from fitting the dereddened G, GBP and GRP absolute fluxes with pure-He model atmospheres
    private double loggHe;

    // Stellar mass resulting from the adopted mass-radius relation
    private double massHe;

    // Uncertainty on teff H
    private double teffH_err;

    // Uncertainty on log g H
    private double loggH_err;

    // Uncertainty on mass H
    private double massH_err;

    // Uncertainty on teff He
    private double teffHe_err;

    // Uncertainty on log g He
    private double loggHe_err;

    // Uncertainty on mass He
    private double massHe_err;

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

    public GaiaWDCatalogEntry() {
    }

    public GaiaWDCatalogEntry(Map<String, Integer> columns, String[] values) {
        this.columns = columns;
        this.values = values;
        sourceId = toLong(values[columns.get("GaiaEDR3")]);
        wdId = values[columns.get("WDJname")];
        ra = toDouble(values[columns.get("RA_ICRS")]);
        dec = toDouble(values[columns.get("DE_ICRS")]);
        plx = toDouble(values[columns.get("Plx")]);
        pmra = toDouble(values[columns.get("pmRA")]);
        pmdec = toDouble(values[columns.get("pmDE")]);
        Gmag = toDouble(values[columns.get("Gmag")]);
        BPmag = toDouble(values[columns.get("BPmag")]);
        RPmag = toDouble(values[columns.get("RPmag")]);
        sdssId = values[columns.get("SDSS12")];
        u_mag = toDouble(values[columns.get("umag")]);
        g_mag = toDouble(values[columns.get("gmag")]);
        r_mag = toDouble(values[columns.get("rmag")]);
        i_mag = toDouble(values[columns.get("imag")]);
        z_mag = toDouble(values[columns.get("zmag")]);
        pwd = toDouble(values[columns.get("Pwd")]);
        teffH = toDouble(values[columns.get("TeffH")]);
        loggH = toDouble(values[columns.get("loggH")]);
        massH = toDouble(values[columns.get("MassH")]);
        teffHe = toDouble(values[columns.get("TeffHe")]);
        loggHe = toDouble(values[columns.get("loggHe")]);
        massHe = toDouble(values[columns.get("MassHe")]);
        teffH_err = toDouble(values[columns.get("e_TeffH")]);
        loggH_err = toDouble(values[columns.get("e_loggH")]);
        massH_err = toDouble(values[columns.get("e_MassH")]);
        teffHe_err = toDouble(values[columns.get("e_TeffHe")]);
        loggHe_err = toDouble(values[columns.get("e_loggHe")]);
        massHe_err = toDouble(values[columns.get("e_MassHe")]);
    }

    @Override
    public CatalogEntry copy() {
        return new GaiaWDCatalogEntry(columns, values);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("source id", String.valueOf(sourceId), Alignment.LEFT, getLongComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("WD id", wdId, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("plx (mas)", roundTo4DecNZ(plx), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("pmra (mas/yr)", roundTo3DecNZ(pmra), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("pmdec (mas/yr)", roundTo3DecNZ(pmdec), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("G (mag)", roundTo3DecNZ(Gmag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("BP (mag)", roundTo3DecNZ(BPmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("RP (mag)", roundTo3DecNZ(RPmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("u (mag)", roundTo3DecNZ(u_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("g (mag)", roundTo3DecNZ(g_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("r (mag)", roundTo3DecNZ(r_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("i (mag)", roundTo3DecNZ(i_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("z (mag)", roundTo3DecNZ(z_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("teff H (K)", roundTo3DecNZ(teffH), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("teff H err", roundTo3DecNZ(teffH_err), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("teff He (K)", roundTo3DecNZ(teffHe), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("teff He err", roundTo3DecNZ(teffHe_err), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("logg H", roundTo3DecNZ(loggH), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("logg H err", roundTo3DecNZ(loggH_err), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("logg He", roundTo3DecNZ(loggHe), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("logg He err", roundTo3DecNZ(loggHe_err), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("mass H (Msun)", roundTo3DecNZ(massH), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("mass H err", roundTo3DecNZ(massH_err), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("mass He (Msun)", roundTo3DecNZ(massHe), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("mass He err", roundTo3DecNZ(massHe_err), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("SDSS id", sdssId, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("probability of being a WD", roundTo3DecNZ(pwd), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dist (1/plx)", roundTo3DecNZ(getParallacticDistance()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("tpm (mas/yr)", roundTo3DecNZ(getTotalProperMotion()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("Absolute G (mag)", roundTo3DecNZ(getAbsoluteGmag()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("G-RP", roundTo3DecNZ(getG_RP()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("BP-RP", roundTo3DecNZ(getBP_RP()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("u-g", roundTo3DecNZ(get_u_g()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("g-r", roundTo3DecNZ(get_g_r()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("r-i", roundTo3DecNZ(get_r_i()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("i-z", roundTo3DecNZ(get_i_z()), Alignment.RIGHT, getDoubleComparator(), false, true));
    }

    @Override
    public int hashCode() {
        int hash = 3;
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
        final GaiaWDCatalogEntry other = (GaiaWDCatalogEntry) obj;
        return this.sourceId == other.sourceId;
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new GaiaWDCatalogEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return CATALOG_NAME;
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.PURPLE.val;
    }

    @Override
    public String getCatalogQueryUrl() {
        return createVizieRUrl(ra, dec, searchRadius / DEG_ARCSEC, "J/MNRAS/508/3877/maincat", "RA_ICRS", "DE_ICRS");
    }

    @Override
    public String[] getColumnValues() {
        String columnValues = roundTo3DecLZ(getTargetDistance()) + ","
                + sourceId + ","
                + roundTo7Dec(ra) + ","
                + roundTo7Dec(dec) + ","
                + wdId + ","
                + roundTo4Dec(plx) + ","
                + roundTo3Dec(pmra) + ","
                + roundTo3Dec(pmdec) + ","
                + roundTo3Dec(Gmag) + ","
                + roundTo3Dec(BPmag) + ","
                + roundTo3Dec(RPmag) + ","
                + roundTo3Dec(u_mag) + ","
                + roundTo3Dec(g_mag) + ","
                + roundTo3Dec(r_mag) + ","
                + roundTo3Dec(i_mag) + ","
                + roundTo3Dec(z_mag) + ","
                + roundTo3Dec(teffH) + ","
                + roundTo3Dec(teffH_err) + ","
                + roundTo3Dec(teffHe) + ","
                + roundTo3Dec(teffHe_err) + ","
                + roundTo3Dec(loggH) + ","
                + roundTo3Dec(loggH_err) + ","
                + roundTo3Dec(loggHe) + ","
                + roundTo3Dec(loggHe_err) + ","
                + roundTo3Dec(massH) + ","
                + roundTo3Dec(massH_err) + ","
                + roundTo3Dec(massHe) + ","
                + roundTo3Dec(massHe_err) + ","
                + sdssId + ","
                + roundTo3Dec(pwd) + ","
                + roundTo3Dec(getParallacticDistance()) + ","
                + roundTo3Dec(getTotalProperMotion()) + ","
                + roundTo3Dec(getAbsoluteGmag()) + ","
                + roundTo3Dec(getG_RP()) + ","
                + roundTo3Dec(getBP_RP()) + ","
                + roundTo3Dec(get_u_g()) + ","
                + roundTo3Dec(get_g_r()) + ","
                + roundTo3Dec(get_r_i()) + ","
                + roundTo3Dec(get_i_z());
        return columnValues.split(",", -1);
    }

    @Override
    public String[] getColumnTitles() {
        String columnTitles = "dist (arcsec),"
                + "source id,"
                + "ra,"
                + "dec,"
                + "WD id,"
                + "plx (mas),"
                + "pmra (mas/yr),"
                + "pmdec (mas/yr),"
                + "G (mag),"
                + "BP (mag),"
                + "RP (mag),"
                + "u (mag),"
                + "g (mag),"
                + "r (mag),"
                + "i (mag),"
                + "z (mag),"
                + "teff H (K),"
                + "teff H err,"
                + "teff He (K),"
                + "teff He err,"
                + "logg H,"
                + "logg H err,"
                + "logg He,"
                + "logg He err,"
                + "mass H (Msun),"
                + "mass H err,"
                + "mass He (Msun),"
                + "mass He err,"
                + "SDSS id,"
                + "probability of being a WD,"
                + "dist (1/plx),"
                + "tpm (mas/yr),"
                + "Absolute G (mag),"
                + "G-RP,"
                + "BP-RP,"
                + "u-g,"
                + "g-r,"
                + "r-i,"
                + "i-z";
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
        bands.put(Band.G, new NumberPair(Gmag, 0));
        bands.put(Band.BP, new NumberPair(BPmag, 0));
        bands.put(Band.RP, new NumberPair(RPmag, 0));
        return bands;
    }

    @Override
    public Map<Color, Double> getColors(boolean toVega) {
        this.toVega = toVega;
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.M_G, getAbsoluteGmag());
        colors.put(Color.M_RP, getAbsoluteRPmag());
        colors.put(Color.M_BP, getAbsoluteBPmag());
        colors.put(Color.G_RP, getG_RP());
        colors.put(Color.BP_RP, getBP_RP());
        colors.put(Color.BP_G, getBP_G());
        colors.put(Color.u_g, get_u_g());
        colors.put(Color.g_r, get_g_r());
        colors.put(Color.r_i, get_r_i());
        colors.put(Color.i_z, get_i_z());
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

    public double getAbsoluteRPmag() {
        return calculateAbsoluteMagnitudeFromParallax(RPmag, plx);
    }

    public double getAbsoluteBPmag() {
        return calculateAbsoluteMagnitudeFromParallax(BPmag, plx);
    }

    public double getAbsoluteGmag() {
        return calculateAbsoluteMagnitudeFromParallax(Gmag, plx);
    }

    public double getG_RP() {
        if (Gmag == 0 || RPmag == 0) {
            return 0;
        } else {
            return Gmag - RPmag;
        }
    }

    public double getBP_RP() {
        if (BPmag == 0 || RPmag == 0) {
            return 0;
        } else {
            return BPmag - RPmag;
        }
    }

    public double getBP_G() {
        if (BPmag == 0 || Gmag == 0) {
            return 0;
        } else {
            return BPmag - Gmag;
        }
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

    public double getPwd() {
        return pwd;
    }

    public double getTeffH() {
        return teffH;
    }

    public double getLoggH() {
        return loggH;
    }

    public double getMassH() {
        return massH;
    }

    public double getTeffHe() {
        return teffHe;
    }

    public double getLoggHe() {
        return loggHe;
    }

    public double getMassHe() {
        return massHe;
    }

    public double getTeffH_err() {
        return teffH_err;
    }

    public double getLoggH_err() {
        return loggH_err;
    }

    public double getMassH_err() {
        return massH_err;
    }

    public double getTeffHe_err() {
        return teffHe_err;
    }

    public double getLoggHe_err() {
        return loggHe_err;
    }

    public double getMassHe_err() {
        return massHe_err;
    }

}
