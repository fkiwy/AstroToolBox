package astro.tool.box.container.catalog;

import static astro.tool.box.function.AstrometricFunctions.*;
import static astro.tool.box.function.NumericFunctions.*;
import static astro.tool.box.util.Comparators.*;
import static astro.tool.box.util.ConversionFactors.*;
import static astro.tool.box.util.Constants.*;
import static astro.tool.box.util.ServiceProviderUtils.*;
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

public class CatWiseRejectedEntry implements CatalogEntry {

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

    // Parallax from PM desc-asce elon
    private double par_pm;

    // One-sigma uncertainty in par_pm
    private double par_pmsig;

    // Parallax estimate from stationary solution
    private double par_stat;

    // One-sigma uncertainty in par_stat
    private double par_sigma;

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

    // Pixel RA position
    private double pixelRa;

    // Pixel declination position
    private double pixelDec;

    // Search radius
    private double searchRadius;

    // Catalog number
    private int catalogNumber;

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    public CatWiseRejectedEntry() {
    }

    public CatWiseRejectedEntry(Map<String, Integer> columns, String[] values) {
        sourceId = values[columns.get("source_name")];
        ra = toDouble(values[columns.get("ra")]);
        dec = toDouble(values[columns.get("dec")]);
        W1mag = toDouble(values[columns.get("w1mpro")]);
        W1_err = toDouble(values[columns.get("w1sigmpro")]);
        W2mag = toDouble(values[columns.get("w2mpro")]);
        W2_err = toDouble(values[columns.get("w2sigmpro")]);
        meanObsMJD = toDouble(values[columns.get("meanobsmjd")]);
        ra_pm = toDouble(values[columns.get("ra_pm")]);
        dec_pm = toDouble(values[columns.get("dec_pm")]);
        pmra = toDouble(values[columns.get("pmra")]) * ARCSEC_MAS;
        pmdec = toDouble(values[columns.get("pmdec")]) * ARCSEC_MAS;
        pmra_err = toDouble(values[columns.get("sigpmra")]) * ARCSEC_MAS;
        pmdec_err = toDouble(values[columns.get("sigpmdec")]) * ARCSEC_MAS;
        par_pm = toDouble(values[columns.get("par_pm")]) * ARCSEC_MAS;
        par_pmsig = toDouble(values[columns.get("par_pmsig")]) * ARCSEC_MAS;
        par_stat = toDouble(values[columns.get("par_stat")]) * ARCSEC_MAS;
        par_sigma = toDouble(values[columns.get("par_sigma")]) * ARCSEC_MAS;
        cc_flags = values[columns.get("cc_flags")];
        ab_flags = values[columns.get("ab_flags")];
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("source id", sourceId, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W1 (mag)", roundTo3DecNZ(W1mag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("W1 err", roundTo3DecNZ(W1_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("W2 (mag)", roundTo3DecNZ(W2mag), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("W2 err", roundTo3DecNZ(W2_err), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("pmra (mas/yr)", roundTo2DecNZ(pmra), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("pmra err", roundTo2DecNZ(pmra_err), Alignment.RIGHT, getDoubleComparator(), false, false, isProperMotionFaulty(pmra, pmra_err)));
        catalogElements.add(new CatalogElement("pmdec (mas/yr)", roundTo2DecNZ(pmdec), Alignment.RIGHT, getDoubleComparator(), true));
        catalogElements.add(new CatalogElement("pmdec err", roundTo2DecNZ(pmdec_err), Alignment.RIGHT, getDoubleComparator(), false, false, isProperMotionFaulty(pmdec, pmdec_err)));
        catalogElements.add(new CatalogElement("plx PM desc-asc (mas)", roundTo1DecNZ(par_pm), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("plx PM desc-asc err", roundTo1DecNZ(par_pmsig), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("plx stat. sol. (mas)", roundTo1DecNZ(par_stat), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("plx stat. sol. err", roundTo1DecNZ(par_sigma), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("cc flags", cc_flags, Alignment.LEFT, getStringComparator(), AllWiseCatalogEntry.createToolTip_cc_flags()));
        catalogElements.add(new CatalogElement("ab flags", ab_flags, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("W1-W2", roundTo3DecNZ(getW1_W2()), Alignment.RIGHT, getDoubleComparator(), true, true));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CatWiseRejectedEntry{sourceId=").append(sourceId);
        sb.append(", ra=").append(ra);
        sb.append(", dec=").append(dec);
        sb.append(", W1mag=").append(W1mag);
        sb.append(", W1_err=").append(W1_err);
        sb.append(", W2mag=").append(W2mag);
        sb.append(", W2_err=").append(W2_err);
        sb.append(", pmra=").append(pmra);
        sb.append(", pmra_err=").append(pmra_err);
        sb.append(", pmdec=").append(pmdec);
        sb.append(", pmdec_err=").append(pmdec_err);
        sb.append(", par_pm=").append(par_pm);
        sb.append(", par_pmsig=").append(par_pmsig);
        sb.append(", par_stat=").append(par_stat);
        sb.append(", par_sigma=").append(par_sigma);
        sb.append(", cc_flags=").append(cc_flags);
        sb.append(", ab_flags=").append(ab_flags);
        sb.append(", meanObsMJD=").append(meanObsMJD);
        sb.append(", ra_pm=").append(ra_pm);
        sb.append(", dec_pm=").append(dec_pm);
        sb.append(", targetRa=").append(targetRa);
        sb.append(", targetDec=").append(targetDec);
        sb.append(", pixelRa=").append(pixelRa);
        sb.append(", pixelDec=").append(pixelDec);
        sb.append(", searchRadius=").append(searchRadius);
        sb.append(", catalogNumber=").append(catalogNumber);
        sb.append(", catalogElements=").append(catalogElements);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.sourceId);
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
        final CatWiseRejectedEntry other = (CatWiseRejectedEntry) obj;
        return Objects.equals(this.sourceId, other.sourceId);
    }

    @Override
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new CatWiseRejectedEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return "CatWISE 2020 Reject Table";
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.LIGHT_GRAY.val;
    }

    @Override
    public String getCatalogUrl() {
        return createIrsaUrl(CATWISE_REJECTED_ID, ra, dec, searchRadius / DEG_ARCSEC);
    }

    @Override
    public String[] getColumnValues() {
        String values = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + roundTo7Dec(ra) + "," + roundTo7Dec(dec) + "," + roundTo3Dec(W1mag) + "," + roundTo3Dec(W1_err) + "," + roundTo3Dec(W2mag) + "," + roundTo3Dec(W2_err) + "," + roundTo2Dec(pmra) + "," + roundTo2Dec(pmra_err) + "," + roundTo2Dec(pmdec) + "," + roundTo2Dec(pmdec_err) + "," + roundTo1Dec(par_pm) + "," + roundTo1Dec(par_pmsig) + "," + roundTo1Dec(par_stat) + "," + roundTo1Dec(par_sigma) + "," + cc_flags + "," + ab_flags + "," + roundTo3Dec(getW1_W2());
        return values.split(",", 19);
    }

    @Override
    public String[] getColumnTitles() {
        String titles = "dist (arcsec),source id,ra,dec,W1 (mag),W1 err,W2 (mag),W2 err,pmra,pmra err,pmdec,pmdec err,plx PM desc-asc (mas),plx PM desc-asc err,plx stat. sol. (mas),plx stat. sol. err,cc flags,ab flags,W1-W2";
        return titles.split(",", 19);
    }

    @Override
    public Map<Color, Double> getColors() {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.W1_W2, getW1_W2());
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
        return par_pm;
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

    public String getCc_flags() {
        return cc_flags;
    }

    public String getAb_flags() {
        return ab_flags;
    }

}
