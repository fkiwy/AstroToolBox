package astro.tool.box.container.catalog;

import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.module.ServiceProviderUtils.*;
import static astro.tool.box.function.PhotometricFunctions.*;
import static astro.tool.box.util.Comparators.*;
import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SimbadCatalogEntry implements CatalogEntry {

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

    // Catalog number
    private int catalogNumber;

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    public SimbadCatalogEntry() {
    }

    public SimbadCatalogEntry(String[] values) {
        sourceId = values[0];
        objectType = values[1];
        spectralType = values[2];
        ra = toDouble(values[3]);
        dec = toDouble(values[4]);
        plx = toDouble(values[5]);
        plx_err = toDouble(values[6]);
        pmra = toDouble(values[7]);
        pmdec = toDouble(values[8]);
        radvel = toDouble(values[9]);
        redshift = toDouble(values[10]);
        rvtype = values[11];
        Umag = toDouble(values[12]);
        Bmag = toDouble(values[13]);
        Vmag = toDouble(values[14]);
        Rmag = toDouble(values[15]);
        Imag = toDouble(values[16]);
        Gmag = toDouble(values[17]);
        Jmag = toDouble(values[18]);
        Hmag = toDouble(values[19]);
        Kmag = toDouble(values[20]);
        u_mag = toDouble(values[21]);
        g_mag = toDouble(values[22]);
        r_mag = toDouble(values[23]);
        i_mag = toDouble(values[24]);
        z_mag = toDouble(values[25]);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("sourceId", sourceId, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("objectType", objectType, Alignment.LEFT, getStringComparator(), true));
        catalogElements.add(new CatalogElement("spectralType", spectralType, Alignment.LEFT, getStringComparator(), true));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("plx (mas)", roundTo4DecNZ(plx), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("plx err", roundTo4DecNZ(plx), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmra (mas/yr)", roundTo3DecNZ(pmra), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("pmdec (mas/yr)", roundTo3DecNZ(pmdec), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("rad vel (km/s)", roundTo1DecNZ(radvel), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("redshift", roundTo6DecNZ(redshift), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("rv type", rvtype, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("Umag", roundTo3DecNZ(Umag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Bmag", roundTo3DecNZ(Bmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Vmag", roundTo3DecNZ(Vmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Rmag", roundTo3DecNZ(Rmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Imag", roundTo3DecNZ(Imag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Gmag", roundTo3DecNZ(Gmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Jmag", roundTo3DecNZ(Jmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Hmag", roundTo3DecNZ(Hmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("Kmag", roundTo3DecNZ(Kmag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("u_mag", roundTo3DecNZ(u_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("g_mag", roundTo3DecNZ(g_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("r_mag", roundTo3DecNZ(r_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("i_mag", roundTo3DecNZ(i_mag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("z_mag", roundTo3DecNZ(z_mag), Alignment.RIGHT, getDoubleComparator()));
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
    public String toString() {
        return "SimbadCatalogEntry{" + "sourceId=" + sourceId + ", objectType=" + objectType + ", spectralType=" + spectralType + ", ra=" + ra + ", dec=" + dec + ", plx=" + plx + ", plx_err=" + plx_err + ", pmra=" + pmra + ", pmdec=" + pmdec + ", radvel=" + radvel + ", redshift=" + redshift + ", rvtype=" + rvtype + ", Umag=" + Umag + ", Bmag=" + Bmag + ", Vmag=" + Vmag + ", Rmag=" + Rmag + ", Imag=" + Imag + ", Gmag=" + Gmag + ", Jmag=" + Jmag + ", Hmag=" + Hmag + ", Kmag=" + Kmag + ", u_mag=" + u_mag + ", g_mag=" + g_mag + ", r_mag=" + r_mag + ", i_mag=" + i_mag + ", z_mag=" + z_mag + ", targetRa=" + targetRa + ", targetDec=" + targetDec + ", searchRadius=" + searchRadius + ", catalogNumber=" + catalogNumber + ", catalogElements=" + catalogElements + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.sourceId);
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
        if (!Objects.equals(this.sourceId, other.sourceId)) {
            return false;
        }
        return true;
    }

    @Override
    public CatalogEntry getInstance(String[] values) {
        return new SimbadCatalogEntry(values);
    }

    @Override
    public String getCatalogName() {
        return "SIMBAD";
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.LIGHT_RED.val;
    }

    @Override
    public String getCatalogUrl() {
        return createSimbadUrl(ra, dec, searchRadius / DEG_ARCSEC);
    }

    @Override
    public String[] getColumnValues() {
        String values = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + objectType + "," + spectralType + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + roundTo4Dec(plx) + "," + roundTo4Dec(plx_err) + "," + roundTo3Dec(pmra) + "," + roundTo3Dec(pmdec) + "," + roundTo1Dec(radvel) + "," + roundTo6Dec(redshift) + "," + rvtype + "," + roundTo3Dec(Umag) + "," + roundTo3Dec(Bmag) + "," + roundTo3Dec(Vmag) + "," + roundTo3Dec(Rmag) + "," + roundTo3Dec(Imag) + "," + roundTo3Dec(Gmag) + "," + roundTo3Dec(Jmag) + "," + roundTo3Dec(Hmag) + "," + roundTo3Dec(Kmag) + "," + roundTo3Dec(u_mag) + "," + roundTo3Dec(g_mag) + "," + roundTo3Dec(r_mag) + "," + roundTo3Dec(i_mag) + "," + roundTo3Dec(z_mag) + "," + roundTo3Dec(getB_V()) + "," + roundTo3Dec(getU_B()) + "," + roundTo3Dec(getV_R()) + "," + roundTo3Dec(getV_I()) + "," + roundTo3Dec(getJ_H()) + "," + roundTo3Dec(getH_K()) + "," + roundTo3Dec(getJ_K()) + "," + roundTo3Dec(get_u_g()) + "," + roundTo3Dec(get_g_r()) + "," + roundTo3Dec(get_r_i()) + "," + roundTo3Dec(get_i_z());
        return values.split(",", 38);
    }

    @Override
    public String[] getColumnTitles() {
        String titles = "dist (arcsec),sourceId,type,spt,ra,dec,plx,plx err,pmra,pmdec,rad vel,redshift,rv type,Umag,Bmag,Vmag,Rmag,Imag,Gmag,Jmag,Hmag,Kmag,u_mag,g_mag,r_mag,i_mag,z_mag,B-V,U-B,V-R,V-I,J-H,H-K,J-K,u-g,g-r,r-i,i-z";
        return titles.split(",", 38);
    }

    @Override
    public Map<Color, Double> getColors() {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.U_B, getU_B());
        colors.put(Color.B_V, getB_V());
        colors.put(Color.V_R, getV_R());
        colors.put(Color.V_I, getV_I());
        colors.put(Color.V_J, getV_J());
        colors.put(Color.R_I, getR_I());
        colors.put(Color.J_H, getJ_H());
        colors.put(Color.J_K, getJ_K());
        colors.put(Color.H_K, getH_K());
        colors.put(Color.u_g, get_u_g());
        colors.put(Color.g_r, get_g_r());
        colors.put(Color.r_i, get_r_i());
        colors.put(Color.r_J, get_r_J());
        colors.put(Color.i_z, get_i_z());
        colors.put(Color.M_G, getAbsoluteGmag());
        return colors;
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

    public double get_r_J() {
        if (r_mag == 0 || Jmag == 0) {
            return 0;
        } else {
            return r_mag - Jmag;
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
