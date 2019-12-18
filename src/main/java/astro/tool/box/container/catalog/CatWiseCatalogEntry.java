package astro.tool.box.container.catalog;

import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.module.ServiceProviderUtils.*;
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
import static astro.tool.box.function.PhotometricFunctions.calculateAbsoluteMagnitudeFromParallax;

public class CatWiseCatalogEntry implements CatalogEntry {

    // Unique WISE source designation
    private String sourceId;

    // Right ascension (J2000)
    private double ra;

    // Declination (J2000)
    private double dec;

    // Instrumental profile-fit photometry magnitude, band 1
    private double W1mag;

    // Instrumental profile-fit photometry flux uncertainty in mag units, band 1
    private double W1_err;

    // Instrumental profile-fit photometry magnitude, band 2
    private double W2mag;

    // Instrumental profile-fit photometry flux uncertainty in mag units, band 2
    private double W2_err;

    // Apparent motion in RA
    private double pmra;

    // Uncertainty in the RA motion estimate
    private double pmra_err;

    // Apparent motion in Dec
    private double pmdec;

    // Uncertainty in the Dec motion estimate
    private double pmdec_err;

    // Parallax
    private double plx;

    // Prioritized artifacts affecting the source in each band
    private String cc_flags;

    // UnWISE artifact bitmask contamination flags
    private String ab_flags;

    // Mean observation epoch
    private double meanObsMJD;

    // Right ascension at epoch MJD=56700.0 (2014.118) from pff model incl. motion
    private double ra_pm;

    // Declination at epoch MJD=56700.0 (2014.118) from pff model incl. motion
    private double dec_pm;

    // Right ascension used for distance calculation
    private double targetRa;

    // Declination used for distance calculation
    private double targetDec;

    // Search radius
    private double searchRadius;

    // Catalog number
    private int catalogNumber;

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    public CatWiseCatalogEntry() {
    }

    public CatWiseCatalogEntry(String[] values) {
        sourceId = values[0];
        ra = toDouble(values[2]);
        dec = toDouble(values[3]);
        W1mag = toDouble(values[23]);
        W1_err = toDouble(values[24]);
        W2mag = toDouble(values[26]);
        W2_err = toDouble(values[27]);
        meanObsMJD = toDouble(values[119]);
        ra_pm = toDouble(values[120]);
        dec_pm = toDouble(values[121]);
        pmra = toDouble(values[125]) * ARCSEC_MAS;
        pmdec = toDouble(values[126]) * ARCSEC_MAS;
        pmra_err = toDouble(values[127]) * ARCSEC_MAS;
        pmdec_err = toDouble(values[128]) * ARCSEC_MAS;
        //parallax from PM desc-asce elon
        plx = toDouble(values[166]) * ARCSEC_MAS;
        //parallax estimate from stationary solution
        //plx = toDouble(values[168]) * ARCSEC_MAS;
        cc_flags = values[171];
        ab_flags = values[177];
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("sourceId", sourceId, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W1mag", roundTo3DecNZ(W1mag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("W1 err", roundTo3DecNZ(W1_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W2mag", roundTo3DecNZ(W2mag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("W2 err", roundTo3DecNZ(W2_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmra (mas/yr)", roundTo2DecNZ(pmra), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("pmra err", roundTo2DecNZ(pmra_err), Alignment.RIGHT, getDoubleComparator(), false, false, isProperMotionFaulty(pmra, pmra_err)));
        catalogElements.add(new CatalogElement("pmdec (mas/yr)", roundTo2DecNZ(pmdec), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("pmdec err", roundTo2DecNZ(pmdec_err), Alignment.RIGHT, getDoubleComparator(), false, false, isProperMotionFaulty(pmdec, pmdec_err)));
        catalogElements.add(new CatalogElement("plx (mas)", roundTo1DecNZ(plx), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("cc_flags", cc_flags, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("ab_flags", ab_flags, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("W1-W2", roundTo3DecNZ(getW1_W2()), Alignment.RIGHT, getDoubleComparator(), true, true));
        //catalogElements.add(new CatalogElement("M W1mag", roundTo3DecNZ(getAbsoluteW1mag()), Alignment.RIGHT, getDoubleComparator(), false, true));
    }

    @Override
    public String toString() {
        return "CatWiseCatalogEntry{" + "sourceId=" + sourceId + ", ra=" + ra + ", dec=" + dec + ", W1mag=" + W1mag + ", W1_err=" + W1_err + ", W2mag=" + W2mag + ", W2_err=" + W2_err + ", pmra=" + pmra + ", pmra_err=" + pmra_err + ", pmdec=" + pmdec + ", pmdec_err=" + pmdec_err + ", plx=" + plx + ", cc_flags=" + cc_flags + ", ab_flags=" + ab_flags + ", meanObsMJD=" + meanObsMJD + ", ra_pm=" + ra_pm + ", dec_pm=" + dec_pm + ", targetRa=" + targetRa + ", targetDec=" + targetDec + ", searchRadius=" + searchRadius + ", catalogNumber=" + catalogNumber + ", catalogElements=" + catalogElements + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.sourceId);
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
        final CatWiseCatalogEntry other = (CatWiseCatalogEntry) obj;
        return Objects.equals(this.sourceId, other.sourceId);
    }

    @Override
    public CatalogEntry getInstance(String[] values) {
        return new CatWiseCatalogEntry(values);
    }

    @Override
    public String getCatalogName() {
        return "CatWISE";
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.LIGHT_YELLOW.val;
    }

    @Override
    public String getCatalogUrl() {
        return createIrsaUrl(CATWISE_CATALOG_ID, ra, dec, searchRadius / DEG_ARCSEC);
    }

    @Override
    public String[] getColumnValues() {
        String values = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + roundTo3Dec(W1mag) + "," + roundTo3Dec(W1_err) + "," + roundTo3Dec(W2mag) + "," + roundTo3Dec(W2_err) + "," + roundTo2Dec(pmra) + "," + roundTo2Dec(pmra_err) + "," + roundTo2Dec(pmdec) + "," + roundTo2Dec(pmdec_err) + "," + roundTo1Dec(plx) + "," + cc_flags + "," + ab_flags + "," + roundTo3Dec(getW1_W2()) /*+ "," + roundTo3Dec(getAbsoluteW1mag())*/;
        return values.split(",", 16);
    }

    @Override
    public String[] getColumnTitles() {
        String titles = "dist (arcsec),sourceId,ra,dec,W1mag,W1 err,W2mag,W2 err,pmra,pmra err,pmdec,pmdec err,plx,cc_flags,ab_flags,W1-W2" /*,M W1mag"*/;
        return titles.split(",", 16);
    }

    @Override
    public Map<Color, Double> getColors() {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.W1_W2, getW1_W2());
        colors.put(Color.M_W1, getAbsoluteW1mag());
        return colors;
    }

    @Override
    public String getSourceId() {
        return sourceId;
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

    public double getActualDistance() {
        return calculateActualDistance(Math.abs(plx));
    }

    public double getAbsoluteW1mag() {
        return calculateAbsoluteMagnitudeFromParallax(W1mag, Math.abs(plx));
    }

    public double getMeanObsMJD() {
        return meanObsMJD;
    }

    public double getRa_pm() {
        return ra_pm;
    }

    public double getDec_pm() {
        return dec_pm;
    }

    public double getW1_W2() {
        if (W1mag == 0 || W2mag == 0) {
            return 0;
        } else {
            return W1mag - W2mag;
        }
    }

}
