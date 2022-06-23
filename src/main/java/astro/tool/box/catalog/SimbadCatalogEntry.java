package astro.tool.box.catalog;

import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.function.AstrometricFunctions.*;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SimbadCatalogEntry implements CatalogEntry, Extinction {

    public static final String CATALOG_NAME = "SIMBAD";

    // Unique source identifier
    private String sourceId;

    // Object type
    private String objectType;

    // Spectral type
    private String spectralType;

    // Right ascension
    private double ra;

    // Declination
    private double dec;

    // Parallax
    private double plx;

    // Parallax error
    private double plx_err;

    // Proper motion in right ascension direction
    private double pmra;

    // Proper motion in declination direction
    private double pmdec;

    // Radial velocity
    private double radvel;

    // Redshift
    private double redshift;

    // Radial velocity type
    private String rvtype;

    // Johnson U magnitude
    private double Umag;

    // Johnson B magnitude
    private double Bmag;

    // Johnson V magnitude
    private double Vmag;

    // Johnson-Cousins R magnitude
    private double Rmag;

    // Johnson-Cousins I magnitude
    private double Imag;

    // Gaia G magnitude
    private double Gmag;

    // 2MASS J magnitude
    private double Jmag;

    // 2MASS H magnitude
    private double Hmag;

    // 2MASS K magnitude
    private double Kmag;

    // SDSS u magnitude
    private double u_mag;

    // SDSS g magnitude
    private double g_mag;

    // SDSS r magnitude
    private double r_mag;

    // SDSS i magnitude
    private double i_mag;

    // SDSS z magnitude
    private double z_mag;

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

    public SimbadCatalogEntry() {
    }

    public SimbadCatalogEntry(Map<String, Integer> columns, String[] values) {
        this.columns = columns;
        this.values = values;
        sourceId = values[columns.get("main_id")];
        objectType = values[columns.get("otype_longname")];
        spectralType = values[columns.get("sp_type")];
        ra = toDouble(values[columns.get("ra")]);
        dec = toDouble(values[columns.get("dec")]);
        plx = toDouble(values[columns.get("plx_value")]);
        plx_err = toDouble(values[columns.get("plx_err")]);
        pmra = toDouble(values[columns.get("pmra")]);
        pmdec = toDouble(values[columns.get("pmdec")]);
        radvel = toDouble(values[columns.get("rvz_radvel")]);
        redshift = toDouble(values[columns.get("rvz_redshift")]);
        rvtype = values[columns.get("rvz_type")];
        Umag = toDouble(values[columns.get("U")]);
        Bmag = toDouble(values[columns.get("B")]);
        Vmag = toDouble(values[columns.get("V")]);
        Rmag = toDouble(values[columns.get("R")]);
        Imag = toDouble(values[columns.get("I")]);
        Gmag = toDouble(values[columns.get("G")]);
        Jmag = toDouble(values[columns.get("J")]);
        Hmag = toDouble(values[columns.get("H")]);
        Kmag = toDouble(values[columns.get("K")]);
        u_mag = toDouble(values[columns.get("u_")]);
        g_mag = toDouble(values[columns.get("g_")]);
        r_mag = toDouble(values[columns.get("r_")]);
        i_mag = toDouble(values[columns.get("i_")]);
        z_mag = toDouble(values[columns.get("z_")]);
    }

    @Override
    public CatalogEntry copy() {
        return new SimbadCatalogEntry(columns, values);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("source id", sourceId, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("object type", objectType, Alignment.LEFT, getStringComparator(), true));
        catalogElements.add(new CatalogElement("spectral type", spectralType, Alignment.LEFT, getStringComparator(), true));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("plx (mas)", roundTo4DecNZ(plx), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("plx err", roundTo4DecNZ(plx_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmra (mas/yr)", roundTo3DecNZ(pmra), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("pmdec (mas/yr)", roundTo3DecNZ(pmdec), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("rad vel (km/s)", roundTo1DecNZ(radvel), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("redshift", roundTo6DecNZ(redshift), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("rv type", rvtype, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("U (mag)", roundTo3DecNZ(Umag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("B (mag)", roundTo3DecNZ(Bmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("V (mag)", roundTo3DecNZ(Vmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("R (mag)", roundTo3DecNZ(Rmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("I (mag)", roundTo3DecNZ(Imag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("G (mag)", roundTo3DecNZ(Gmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("J (mag)", roundTo3DecNZ(Jmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("H (mag)", roundTo3DecNZ(Hmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("K (mag)", roundTo3DecNZ(Kmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("u (mag)", roundTo3DecNZ(u_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("g (mag)", roundTo3DecNZ(g_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("r (mag)", roundTo3DecNZ(r_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("i (mag)", roundTo3DecNZ(i_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("z (mag)", roundTo3DecNZ(z_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("B-V", roundTo3DecNZ(getB_V()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("U-B", roundTo3DecNZ(getU_B()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("V-R", roundTo3DecNZ(getV_R()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("V-I", roundTo3DecNZ(getV_I()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("J-H", roundTo3DecNZ(getJ_H()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("H-K", roundTo3DecNZ(getH_K()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("J-K", roundTo3DecNZ(getJ_K()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("u-g", roundTo3DecNZ(get_u_g()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("g-r", roundTo3DecNZ(get_g_r()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("r-i", roundTo3DecNZ(get_r_i()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("i-z", roundTo3DecNZ(get_i_z()), Alignment.RIGHT, getDoubleComparator(), false, true));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + Objects.hashCode(this.sourceId);
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
        final SimbadCatalogEntry other = (SimbadCatalogEntry) obj;
        return Objects.equals(this.sourceId, other.sourceId);
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new SimbadCatalogEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return CATALOG_NAME;
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return java.awt.Color.RED;
    }

    @Override
    public String getCatalogQueryUrl() {
        return createSimbadUrl(ra, dec, searchRadius / DEG_ARCSEC);
    }

    @Override
    public String[] getColumnValues() {
        String columnValues = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + objectType + "," + spectralType + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + roundTo4Dec(plx) + "," + roundTo4Dec(plx_err) + "," + roundTo3Dec(pmra) + "," + roundTo3Dec(pmdec) + "," + roundTo1Dec(radvel) + "," + roundTo6Dec(redshift) + "," + rvtype + "," + roundTo3Dec(Umag) + "," + roundTo3Dec(Bmag) + "," + roundTo3Dec(Vmag) + "," + roundTo3Dec(Rmag) + "," + roundTo3Dec(Imag) + "," + roundTo3Dec(Gmag) + "," + roundTo3Dec(Jmag) + "," + roundTo3Dec(Hmag) + "," + roundTo3Dec(Kmag) + "," + roundTo3Dec(u_mag) + "," + roundTo3Dec(g_mag) + "," + roundTo3Dec(r_mag) + "," + roundTo3Dec(i_mag) + "," + roundTo3Dec(z_mag) + "," + roundTo3Dec(getB_V()) + "," + roundTo3Dec(getU_B()) + "," + roundTo3Dec(getV_R()) + "," + roundTo3Dec(getV_I()) + "," + roundTo3Dec(getJ_H()) + "," + roundTo3Dec(getH_K()) + "," + roundTo3Dec(getJ_K()) + "," + roundTo3Dec(get_u_g()) + "," + roundTo3Dec(get_g_r()) + "," + roundTo3Dec(get_r_i()) + "," + roundTo3Dec(get_i_z());
        return columnValues.split(",", -1);
    }

    @Override
    public String[] getColumnTitles() {
        String columnTitles = "dist (arcsec),source id,object type,spectral type,ra,dec,plx (mas),plx err,pmra (mas/yr),pmdec (mas/yr),rad vel (km/s),redshift,rv type,U (mag),B (mag),V (mag),R (mag),I (mag),G (mag),J (mag),H (mag),K (mag),u (mag),g (mag),r (mag),i (mag),z (mag),B-V,U-B,V-R,V-I,J-H,H-K,J-K,u-g,g-r,r-i,i-z";
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
    public Map<Band, NumberPair> getBands() {
        Map<Band, NumberPair> bands = new LinkedHashMap<>();
        bands.put(Band.J, new NumberPair(Jmag, 0));
        bands.put(Band.H, new NumberPair(Hmag, 0));
        bands.put(Band.K, new NumberPair(Kmag, 0));
        bands.put(Band.G, new NumberPair(Gmag, 0));
        return bands;
    }

    @Override
    public Map<Color, Double> getColors(boolean toVega) {
        this.toVega = toVega;
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.J_H, getJ_H());
        colors.put(Color.J_K, getJ_K());
        colors.put(Color.H_K, getH_K());
        colors.put(Color.u_g, get_u_g());
        colors.put(Color.g_r, get_g_r());
        colors.put(Color.r_i, get_r_i());
        colors.put(Color.i_z, get_i_z());
        colors.put(Color.M_G, getAbsoluteGmag());
        return colors;
    }

    @Override
    public String getMagnitudes() {
        StringBuilder mags = new StringBuilder();
        if (Umag != 0) {
            mags.append("U=").append(roundTo3DecNZ(Umag)).append(" ");
        }
        if (Bmag != 0) {
            mags.append("B=").append(roundTo3DecNZ(Bmag)).append(" ");
        }
        if (Vmag != 0) {
            mags.append("V=").append(roundTo3DecNZ(Vmag)).append(" ");
        }
        if (Rmag != 0) {
            mags.append("R=").append(roundTo3DecNZ(Rmag)).append(" ");
        }
        if (Imag != 0) {
            mags.append("I=").append(roundTo3DecNZ(Imag)).append(" ");
        }
        if (Gmag != 0) {
            mags.append("G=").append(roundTo3DecNZ(Gmag)).append(" ");
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

    public double getAbsoluteGmag() {
        return calculateAbsoluteMagnitudeFromParallax(Gmag, plx);
    }

    public String getObjectType() {
        return objectType;
    }

    public String getSpectralType() {
        return spectralType;
    }

    public double getB_V() {
        if (Bmag == 0 || Vmag == 0) {
            return 0;
        } else {
            return Bmag - Vmag;
        }
    }

    public double getU_B() {
        if (Umag == 0 || Bmag == 0) {
            return 0;
        } else {
            return Umag - Bmag;
        }
    }

    public double getV_R() {
        if (Vmag == 0 || Rmag == 0) {
            return 0;
        } else {
            return Vmag - Rmag;
        }
    }

    public double getV_I() {
        if (Vmag == 0 || Imag == 0) {
            return 0;
        } else {
            return Vmag - Imag;
        }
    }

    public double getV_J() {
        if (Vmag == 0 || Jmag == 0) {
            return 0;
        } else {
            return Vmag - Jmag;
        }
    }

    public double getR_I() {
        if (Rmag == 0 || Imag == 0) {
            return 0;
        } else {
            return Rmag - Imag;
        }
    }

    public double getJ_H() {
        if (Jmag == 0 || Hmag == 0) {
            return 0;
        } else {
            return Jmag - Hmag;
        }
    }

    public double getJ_K() {
        if (Jmag == 0 || Kmag == 0) {
            return 0;
        } else {
            return Jmag - Kmag;
        }
    }

    public double getH_K() {
        if (Hmag == 0 || Kmag == 0) {
            return 0;
        } else {
            return Hmag - Kmag;
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

}
