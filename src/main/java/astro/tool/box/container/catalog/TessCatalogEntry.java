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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TessCatalogEntry implements CatalogEntry, WhiteDwarf {

    public static final String CATALOG_NAME = "TESS Input Catalog";

    // TESS Input Catalog identifier
    private long sourceId;

    // Right ascension
    private double ra;

    // Declination
    private double dec;

    // Parallax
    private double plx;

    // Error in parallax
    private double plx_err;

    // Proper motion in right ascension direction
    private double pmra;

    // Proper motion error in right ascension
    private double pmra_err;

    // Proper motion in declination direction
    private double pmdec;

    // Proper motion error in declination
    private double pmdec_err;

    // Effective temperature
    private double teff;

    // Uncertainty in teff
    private double teff_err;

    // Surface Gravity
    private double logg;

    // Uncertainty in logg
    private double logg_err;

    // Radius
    private double rad;

    // Uncertainty in rad
    private double rad_err;

    // Mass
    private double mass;

    // Uncertainty in mass
    private double mass_err;

    // Stellar Luminosity
    private double lum;

    // Uncertainty in lum
    private double lum_err;

    // Distance 
    private double dist;

    // Uncertainty in dist 
    private double dist_err;

    // Magnitude in G band
    private double Gmag;

    // Error in G magnitude
    private double G_err;

    // Magnitude in BP band
    private double BPmag;

    // Error in BP magnitude
    private double BP_err;

    // Magnitude in RP band
    private double RPmag;

    // Error in RP magnitude
    private double RP_err;

    // Magnitude in B band
    private double Bmag;

    // Error in B magnitude
    private double B_err;

    // Magnitude in V band
    private double Vmag;

    // Error in V magnitude
    private double V_err;

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

    // Magnitude in W1 band
    private double W1mag;

    // Error in W1 magnitude
    private double W1_err;

    // Magnitude in W2 band
    private double W2mag;

    // Error in W2 magnitude
    private double W2_err;

    // Magnitude in W3 band
    private double W3mag;

    // Error in W3 magnitude
    private double W3_err;

    // Magnitude in W4 band
    private double W4mag;

    // Error in W4 magnitude
    private double W4_err;

    // Magnitude in J band
    private double Jmag;

    // Error in J magnitude
    private double J_err;

    // Magnitude in H band
    private double Hmag;

    // Error in H magnitude
    private double H_err;

    // Magnitude in K band
    private double Kmag;

    // Error in K magnitude
    private double K_err;

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

    public TessCatalogEntry() {
    }

    public TessCatalogEntry(Map<String, Integer> columns, String[] values) {
        this.columns = columns;
        this.values = values;
        sourceId = toLong(values[columns.get("TIC")]);
        ra = toDouble(values[columns.get("RAJ2000")]);
        dec = toDouble(values[columns.get("DEJ2000")]);
        plx = toDouble(values[columns.get("Plx")]);
        plx_err = toDouble(values[columns.get("e_Plx")]);
        pmra = toDouble(values[columns.get("pmRA")]);
        pmra_err = toDouble(values[columns.get("e_pmRA")]);
        pmdec = toDouble(values[columns.get("pmDE")]);
        pmdec_err = toDouble(values[columns.get("e_pmDE")]);
        teff = toDouble(values[columns.get("Teff")]);
        teff_err = toDouble(values[columns.get("s_Teff")]);
        logg = toDouble(values[columns.get("logg")]);
        logg_err = toDouble(values[columns.get("s_logg")]);
        rad = toDouble(values[columns.get("Rad")]);
        rad_err = toDouble(values[columns.get("s_Rad")]);
        mass = toDouble(values[columns.get("Mass")]);
        mass_err = toDouble(values[columns.get("s_Mass")]);
        lum = toDouble(values[columns.get("Lum")]);
        lum_err = toDouble(values[columns.get("s_Lum")]);
        dist = toDouble(values[columns.get("Dist")]);
        dist_err = toDouble(values[columns.get("s_Dist")]);
        Gmag = toDouble(values[columns.get("Gmag")]);
        G_err = toDouble(values[columns.get("e_Gmag")]);
        BPmag = toDouble(values[columns.get("BPmag")]);
        BP_err = toDouble(values[columns.get("e_BPmag")]);
        RPmag = toDouble(values[columns.get("RPmag")]);
        RP_err = toDouble(values[columns.get("e_RPmag")]);
        Bmag = toDouble(values[columns.get("Bmag")]);
        B_err = toDouble(values[columns.get("e_Bmag")]);
        Vmag = toDouble(values[columns.get("Vmag")]);
        V_err = toDouble(values[columns.get("e_Vmag")]);
        u_mag = toDouble(values[columns.get("umag")]);
        u_err = toDouble(values[columns.get("e_umag")]);
        g_mag = toDouble(values[columns.get("gmag")]);
        g_err = toDouble(values[columns.get("e_gmag")]);
        r_mag = toDouble(values[columns.get("rmag")]);
        r_err = toDouble(values[columns.get("e_rmag")]);
        i_mag = toDouble(values[columns.get("imag")]);
        i_err = toDouble(values[columns.get("e_imag")]);
        z_mag = toDouble(values[columns.get("zmag")]);
        z_err = toDouble(values[columns.get("e_zmag")]);
        W1mag = toDouble(values[columns.get("W1mag")]);
        W1_err = toDouble(values[columns.get("e_W1mag")]);
        W2mag = toDouble(values[columns.get("W2mag")]);
        W2_err = toDouble(values[columns.get("e_W2mag")]);
        W3mag = toDouble(values[columns.get("W3mag")]);
        W3_err = toDouble(values[columns.get("e_W3mag")]);
        W4mag = toDouble(values[columns.get("W4mag")]);
        W4_err = toDouble(values[columns.get("e_W4mag")]);
        Jmag = toDouble(values[columns.get("Jmag")]);
        J_err = toDouble(values[columns.get("e_Jmag")]);
        Hmag = toDouble(values[columns.get("Hmag")]);
        H_err = toDouble(values[columns.get("e_Hmag")]);
        Kmag = toDouble(values[columns.get("Kmag")]);
        K_err = toDouble(values[columns.get("e_Kmag")]);
    }

    @Override
    public CatalogEntry copy() {
        return new TessCatalogEntry(columns, values);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("TIC id", String.valueOf(sourceId), Alignment.LEFT, getLongComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("plx (mas)", roundTo4DecNZ(plx), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("plx err", roundTo4DecNZ(plx_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmra (mas/yr)", roundTo3DecNZ(pmra), Alignment.RIGHT, getDoubleComparator(), true, false, isProperMotionFaulty(pmra, pmra_err)));
        catalogElements.add(new CatalogElement("pmra err", roundTo3DecNZ(pmra_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmdec (mas/yr)", roundTo3DecNZ(pmdec), Alignment.RIGHT, getDoubleComparator(), true, false, isProperMotionFaulty(pmdec, pmdec_err)));
        catalogElements.add(new CatalogElement("pmdec err", roundTo3DecNZ(pmdec_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("teff (K)", roundTo3DecNZ(teff), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("teff err", roundTo3DecNZ(teff_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("logg [cm/s2]", roundTo3DecNZ(logg), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("logg err", roundTo3DecNZ(logg_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("radius (Rsun)", roundTo3DecNZ(rad), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("radius err", roundTo3DecNZ(rad_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("mass (Msun)", roundTo3DecNZ(mass), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("mass err", roundTo3DecNZ(mass_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("luminosity (Lsun)", roundTo3DecNZ(lum), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("luminosity err", roundTo3DecNZ(lum_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("distance (pc)", roundTo3DecNZ(dist), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("distance err", roundTo3DecNZ(dist_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("G (mag)", roundTo3DecNZ(Gmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("G err", roundTo3DecNZ(G_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("BP (mag)", roundTo3DecNZ(BPmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("BP err", roundTo3DecNZ(BP_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("RP (mag)", roundTo3DecNZ(RPmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("RP err", roundTo3DecNZ(RP_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("B (mag)", roundTo3DecNZ(Bmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("B err", roundTo3DecNZ(B_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("V (mag)", roundTo3DecNZ(Vmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("V err", roundTo3DecNZ(V_err), Alignment.RIGHT, getDoubleComparator()));
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
        catalogElements.add(new CatalogElement("W1 (mag)", roundTo3DecNZ(W1mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W1 err", roundTo3DecNZ(W1_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W2 (mag)", roundTo3DecNZ(W2mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W2 err", roundTo3DecNZ(W2_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W3 (mag)", roundTo3DecNZ(W3mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W3 err", roundTo3DecNZ(W3_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W4 (mag)", roundTo3DecNZ(W4mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W4 err", roundTo3DecNZ(W4_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("J (mag)", roundTo3DecNZ(Jmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("J err", roundTo3DecNZ(J_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("H (mag)", roundTo3DecNZ(Hmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("H err", roundTo3DecNZ(H_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("K (mag)", roundTo3DecNZ(Kmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("K err", roundTo3DecNZ(K_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("G-RP", roundTo3DecNZ(getG_RP()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("BP-RP", roundTo3DecNZ(getBP_RP()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("B-V", roundTo3DecNZ(getB_V()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("u-g", roundTo3DecNZ(get_u_g()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("g-r", roundTo3DecNZ(get_g_r()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("r-i", roundTo3DecNZ(get_r_i()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("i-z", roundTo3DecNZ(get_i_z()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("W1-W2", roundTo3DecNZ(getW1_W2()), Alignment.RIGHT, getDoubleComparator(), true, true));
        catalogElements.add(new CatalogElement("W2-W3", roundTo3DecNZ(getW2_W3()), Alignment.RIGHT, getDoubleComparator(), true, true));
        catalogElements.add(new CatalogElement("J-W2", roundTo3DecNZ(getJ_W2()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("J-H", roundTo3DecNZ(getJ_H()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("H-K", roundTo3DecNZ(getH_K()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("J-K", roundTo3DecNZ(getJ_K()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("dist (1/plx)", roundTo3DecNZ(getParallacticDistance()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("tpm (mas/yr)", roundTo3DecNZ(getTotalProperMotion()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("Absolute G (mag)", roundTo3DecNZ(getAbsoluteGmag()), Alignment.RIGHT, getDoubleComparator(), false, true));
    }

    @Override
    public int hashCode() {
        int hash = 3;
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
        final TessCatalogEntry other = (TessCatalogEntry) obj;
        return this.sourceId == other.sourceId;
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new TessCatalogEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return CATALOG_NAME;
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.LILAC.val;
    }

    @Override
    public String getCatalogUrl() {
        return createVizieRUrl(ra, dec, searchRadius / DEG_ARCSEC, "IV/38/tic", "RAJ2000", "DEJ2000");
    }

    @Override
    public String[] getColumnValues() {
        String columnValues = roundTo3DecLZ(getTargetDistance()) + ","
                + sourceId + ","
                + roundTo7Dec(ra) + ","
                + roundTo7Dec(dec) + ","
                + roundTo4Dec(plx) + ","
                + roundTo4Dec(plx_err) + ","
                + roundTo3Dec(pmra) + ","
                + roundTo3Dec(pmra_err) + ","
                + roundTo3Dec(pmdec) + ","
                + roundTo3Dec(pmdec_err) + ","
                + roundTo3Dec(teff) + ","
                + roundTo3Dec(teff_err) + ","
                + roundTo3Dec(logg) + ","
                + roundTo3Dec(logg_err) + ","
                + roundTo3Dec(rad) + ","
                + roundTo3Dec(rad_err) + ","
                + roundTo3Dec(mass) + ","
                + roundTo3Dec(mass_err) + ","
                + roundTo3Dec(lum) + ","
                + roundTo3Dec(lum_err) + ","
                + roundTo3Dec(dist) + ","
                + roundTo3Dec(dist_err) + ","
                + roundTo3Dec(Gmag) + ","
                + roundTo3Dec(G_err) + ","
                + roundTo3Dec(BPmag) + ","
                + roundTo3Dec(BP_err) + ","
                + roundTo3Dec(RPmag) + ","
                + roundTo3Dec(RP_err) + ","
                + roundTo3Dec(Bmag) + ","
                + roundTo3Dec(B_err) + ","
                + roundTo3Dec(Vmag) + ","
                + roundTo3Dec(V_err) + ","
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
                + roundTo3Dec(W1mag) + ","
                + roundTo3Dec(W1_err) + ","
                + roundTo3Dec(W2mag) + ","
                + roundTo3Dec(W2_err) + ","
                + roundTo3Dec(W3mag) + ","
                + roundTo3Dec(W3_err) + ","
                + roundTo3Dec(W4mag) + ","
                + roundTo3Dec(W4_err) + ","
                + roundTo3Dec(Jmag) + ","
                + roundTo3Dec(J_err) + ","
                + roundTo3Dec(Hmag) + ","
                + roundTo3Dec(H_err) + ","
                + roundTo3Dec(Kmag) + ","
                + roundTo3Dec(K_err) + ","
                + roundTo3Dec(getB_V()) + ","
                + roundTo3Dec(getG_RP()) + ","
                + roundTo3Dec(getBP_RP()) + ","
                + roundTo3Dec(get_u_g()) + ","
                + roundTo3Dec(get_g_r()) + ","
                + roundTo3Dec(get_r_i()) + ","
                + roundTo3Dec(get_i_z()) + ","
                + roundTo3Dec(getW1_W2()) + ","
                + roundTo3Dec(getW2_W3()) + ","
                + roundTo3Dec(getJ_W2()) + ","
                + roundTo3Dec(getJ_H()) + ","
                + roundTo3Dec(getH_K()) + ","
                + roundTo3Dec(getJ_K()) + ","
                + roundTo3Dec(getParallacticDistance()) + ","
                + roundTo3Dec(getTotalProperMotion()) + ","
                + roundTo3Dec(getAbsoluteGmag());
        return columnValues.split(",", 72);
    }

    @Override
    public String[] getColumnTitles() {
        String columnTitles = "dist (arcsec),"
                + "TIC id,"
                + "ra,"
                + "dec,"
                + "plx (mas),"
                + "plx err,"
                + "pmra (mas/yr),"
                + "pmra err,"
                + "pmdec (mas/yr),"
                + "pmdec err,"
                + "teff (K),"
                + "teff err,"
                + "logg [cm/s2],"
                + "logg err,"
                + "radius (Rsun),"
                + "radius err,"
                + "mass (Msun),"
                + "mass err,"
                + "luminosity (Lsun),"
                + "luminosity err,"
                + "distance (pc),"
                + "distance err,"
                + "G (mag),"
                + "G err,"
                + "BP (mag),"
                + "BP err,"
                + "RP (mag),"
                + "RP err,"
                + "B (mag),"
                + "B err,"
                + "V (mag),"
                + "V err,"
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
                + "W1 (mag),"
                + "W1 err,"
                + "W2 (mag),"
                + "W2 err,"
                + "W3 (mag),"
                + "W3 err,"
                + "W4 (mag),"
                + "W4 err,"
                + "J (mag),"
                + "J err,"
                + "H (mag),"
                + "H err,"
                + "K (mag),"
                + "K err,"
                + "G-RP,"
                + "BP-RP,"
                + "B-V,"
                + "u-g,"
                + "g-r,"
                + "r-i,"
                + "i-z,"
                + "W1-W2,"
                + "W2-W3,"
                + "J-W2,"
                + "J-H,"
                + "H-K,"
                + "J-K,"
                + "dist (1/plx),"
                + "tpm (mas/yr),"
                + "Absolute G (mag)";
        return columnTitles.split(",", 72);
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
        if (W1mag != 0) {
            W1mag = W1mag - extinctionsByBand.get(WISE_1);
        }
        if (W2mag != 0) {
            W2mag = W2mag - extinctionsByBand.get(WISE_2);
        }
        if (Jmag != 0) {
            Jmag = Jmag - extinctionsByBand.get(TWO_MASS_J);
        }
        if (Hmag != 0) {
            Hmag = Hmag - extinctionsByBand.get(TWO_MASS_H);
        }
        if (Kmag != 0) {
            Kmag = Kmag - extinctionsByBand.get(TWO_MASS_K);
        }
    }

    @Override
    public Map<Band, Double> getBands() {
        Map<Band, Double> bands = new LinkedHashMap<>();
        bands.put(Band.W1, W1mag);
        bands.put(Band.W2, W2mag);
        bands.put(Band.W3, W3mag);
        bands.put(Band.J, Jmag);
        bands.put(Band.H, Hmag);
        bands.put(Band.K, Kmag);
        bands.put(Band.G, Gmag);
        return bands;
    }

    @Override
    public Map<Color, Double> getColors(boolean toVega) {
        this.toVega = toVega;
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.M_G, getAbsoluteGmag());
        colors.put(Color.G_RP, getG_RP());
        colors.put(Color.BP_RP, getBP_RP());
        colors.put(Color.B_V, getB_V());
        colors.put(Color.u_g, get_u_g());
        colors.put(Color.g_r, get_g_r());
        colors.put(Color.r_i, get_r_i());
        colors.put(Color.i_z, get_i_z());
        colors.put(Color.W1_W2, getW1_W2());
        colors.put(Color.W1_W3, getW1_W3());
        colors.put(Color.W1_W4, getW1_W4());
        colors.put(Color.J_H, getJ_H());
        colors.put(Color.H_K, getH_K());
        colors.put(Color.J_K, getJ_K());
        colors.put(Color.K_W1, getK_W1());
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
        if (Bmag != 0) {
            mags.append("B=").append(roundTo3DecNZ(Bmag)).append(" ");
        }
        if (Vmag != 0) {
            mags.append("V=").append(roundTo3DecNZ(Vmag)).append(" ");
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
        if (W1mag != 0) {
            mags.append("W1=").append(roundTo3DecNZ(W1mag)).append(" ");
        }
        if (W2mag != 0) {
            mags.append("W2=").append(roundTo3DecNZ(W2mag)).append(" ");
        }
        if (W3mag != 0) {
            mags.append("W3=").append(roundTo3DecNZ(W3mag)).append(" ");
        }
        if (W4mag != 0) {
            mags.append("W4=").append(roundTo3DecNZ(W4mag)).append(" ");
        }
        if (Jmag != 0) {
            mags.append("J=").append(roundTo3DecNZ(Jmag)).append(" ");
        }
        if (Hmag != 0) {
            mags.append("H=").append(roundTo3DecNZ(Hmag)).append(" ");
        }
        if (Kmag != 0) {
            mags.append("K=").append(roundTo3DecNZ(Kmag)).append(" ");
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

    @Override
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

    @Override
    public double getBP_RP() {
        if (BPmag == 0 || RPmag == 0) {
            return 0;
        } else {
            return BPmag - RPmag;
        }
    }

    public double getB_V() {
        if (Bmag == 0 || Vmag == 0) {
            return 0;
        } else {
            return Bmag - Vmag;
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

    public double getW1_W2() {
        if (W1mag == 0 || W2mag == 0) {
            return 0;
        } else {
            return W1mag - W2mag;
        }
    }

    public double getW1_W3() {
        if (W1mag == 0 || W3mag == 0) {
            return 0;
        } else {
            return W1mag - W3mag;
        }
    }

    public double getW1_W4() {
        if (W1mag == 0 || W4mag == 0) {
            return 0;
        } else {
            return W1mag - W4mag;
        }
    }

    public double getW2_W3() {
        if (W2mag == 0 || W3mag == 0) {
            return 0;
        } else {
            return W2mag - W3mag;
        }
    }

    public double getW3_W4() {
        if (W3mag == 0 || W4mag == 0) {
            return 0;
        } else {
            return W3mag - W4mag;
        }
    }

    public double getJ_H() {
        if (Jmag == 0 || Hmag == 0) {
            return 0;
        } else {
            return Jmag - Hmag;
        }
    }

    public double getH_K() {
        if (Hmag == 0 || Kmag == 0) {
            return 0;
        } else {
            return Hmag - Kmag;
        }
    }

    public double getJ_K() {
        if (Jmag == 0 || Kmag == 0) {
            return 0;
        } else {
            return Jmag - Kmag;
        }
    }

    public double getK_W1() {
        if (Kmag == 0 || W1mag == 0) {
            return 0;
        } else {
            return Kmag - W1mag;
        }
    }

    public double getJ_W2() {
        if (Jmag == 0 || W2mag == 0) {
            return 0;
        } else {
            return Jmag - W2mag;
        }
    }

}