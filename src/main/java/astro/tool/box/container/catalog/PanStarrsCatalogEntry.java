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
import java.math.BigDecimal;

public class PanStarrsCatalogEntry implements CatalogEntry {

    // Unique object identifier
    private long objID;

    // IAU name for this object
    private String objName;

    // Information flag bitmask indicating details of the photometry
    private int objInfoFlag;

    // Right ascension from single epoch detections (weighted mean) in equinox J2000 at the mean epoch given by epochMean
    private double raMean;

    // Declination from single epoch detections (weighted mean) in equinox J2000 at the mean epoch given by epochMean
    private double decMean;

    // Right ascension standard deviation from single epoch detections
    private double raMeanErr;

    // Declination standard deviation from single epoch detections
    private double decMeanErr;

    // Modified Julian Date of the mean epoch corresponding to raMean, decMean (equinox J2000)
    private double epochMean;

    // Number of single epoch detections in all filters
    private int nDetections;

    // Mean PSF magnitude from g filter detections
    private double gMeanPSFMag;

    // Error in mean PSF magnitude from g filter detections
    private double gMeanPSFMagErr;

    // Mean PSF magnitude from r filter detections
    private double rMeanPSFMag;

    // Error in mean PSF magnitude from r filter detections
    private double rMeanPSFMagErr;

    // Mean PSF magnitude from i filter detections
    private double iMeanPSFMag;

    // Error in mean PSF magnitude from i filter detections
    private double iMeanPSFMagErr;

    // Mean PSF magnitude from z filter detections
    private double zMeanPSFMag;

    // Error in mean PSF magnitude from z filter detections
    private double zMeanPSFMagErr;

    // Mean PSF magnitude from y filter detections
    private double yMeanPSFMag;

    // Error in mean PSF magnitude from y filter detections
    private double yMeanPSFMagErr;

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

    public PanStarrsCatalogEntry() {
    }

    public PanStarrsCatalogEntry(String[] values) {
        for (int i = 0; i < values.length; i++) {
            System.out.println(">" + values[i] + "<");
            if (values[i].equals("-999.0")) {
                values[i] = "0";
            }
        }
        objID = toLong(values[1]);
        objName = values[0];
        objInfoFlag = toInteger(values[2]);
        raMean = toDouble(values[3]);
        decMean = toDouble(values[4]);
        raMeanErr = toDouble(values[5]);
        decMeanErr = toDouble(values[6]);
        epochMean = toDouble(values[7]);
        nDetections = toInteger(values[8]);
        gMeanPSFMag = toDouble(values[9]);
        gMeanPSFMagErr = toDouble(values[10]);
        rMeanPSFMag = toDouble(values[11]);
        rMeanPSFMagErr = toDouble(values[12]);
        iMeanPSFMag = toDouble(values[13]);
        iMeanPSFMagErr = toDouble(values[14]);
        zMeanPSFMag = toDouble(values[15]);
        zMeanPSFMagErr = toDouble(values[16]);
        yMeanPSFMag = toDouble(values[17]);
        yMeanPSFMagErr = toDouble(values[18]);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("object ID", String.valueOf(objID), Alignment.LEFT, getLongComparator()));
        catalogElements.add(new CatalogElement("object name", objName, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("object info flag", String.valueOf(objInfoFlag), Alignment.RIGHT, getIntegerComparator()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(raMean), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("ra err", roundTo4DecNZ(raMeanErr), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(decMean), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec err", roundTo4DecNZ(decMeanErr), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("observation time", convertMJDToDateTime(new BigDecimal(Double.toString(epochMean))).format(DATE_TIME_FORMATTER), Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("detections", String.valueOf(nDetections), Alignment.RIGHT, getIntegerComparator()));
        catalogElements.add(new CatalogElement("g_mag", roundTo3DecNZ(gMeanPSFMag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("g_mag err", roundTo3DecNZ(gMeanPSFMagErr), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("r_mag", roundTo3DecNZ(rMeanPSFMag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("r_mag err", roundTo3DecNZ(rMeanPSFMagErr), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("i_mag", roundTo3DecNZ(iMeanPSFMag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("i_mag err", roundTo3DecNZ(iMeanPSFMagErr), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("z_mag", roundTo3DecNZ(zMeanPSFMag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("z_mag err", roundTo3DecNZ(zMeanPSFMagErr), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("y_mag", roundTo3DecNZ(yMeanPSFMag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("y_mag err", roundTo3DecNZ(yMeanPSFMagErr), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("g-r", roundTo3DecNZ(get_g_r()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("r-i", roundTo3DecNZ(get_r_i()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("i-z", roundTo3DecNZ(get_i_z()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("z-y", roundTo3DecNZ(get_i_z()), Alignment.RIGHT, getDoubleComparator(), false, true));
    }

    @Override
    public String toString() {
        return "PanStarrsCatalogEntry{" + "objID=" + objID + ", objName=" + objName + ", objInfoFlag=" + objInfoFlag + ", raMean=" + raMean + ", decMean=" + decMean + ", raMeanErr=" + raMeanErr + ", decMeanErr=" + decMeanErr + ", epochMean=" + epochMean + ", nDetections=" + nDetections + ", gMeanPSFMag=" + gMeanPSFMag + ", gMeanPSFMagErr=" + gMeanPSFMagErr + ", rMeanPSFMag=" + rMeanPSFMag + ", rMeanPSFMagErr=" + rMeanPSFMagErr + ", iMeanPSFMag=" + iMeanPSFMag + ", iMeanPSFMagErr=" + iMeanPSFMagErr + ", zMeanPSFMag=" + zMeanPSFMag + ", zMeanPSFMagErr=" + zMeanPSFMagErr + ", yMeanPSFMag=" + yMeanPSFMag + ", yMeanPSFMagErr=" + yMeanPSFMagErr + ", targetRa=" + targetRa + ", targetDec=" + targetDec + ", pixelRa=" + pixelRa + ", pixelDec=" + pixelDec + ", searchRadius=" + searchRadius + ", catalogNumber=" + catalogNumber + ", catalogElements=" + catalogElements + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (int) (this.objID ^ (this.objID >>> 32));
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
        final PanStarrsCatalogEntry other = (PanStarrsCatalogEntry) obj;
        return this.objID == other.objID;
    }

    @Override
    public CatalogEntry getInstance(String[] values) {
        return new PanStarrsCatalogEntry(values);
    }

    @Override
    public String getCatalogName() {
        return "Pan-STARRS";
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.LIGHT_BROWN.val;
    }

    @Override
    public String getCatalogUrl() {
        return createPanStarrsUrl(raMean, decMean, searchRadius / DEG_ARCSEC);
    }

    @Override
    public String[] getColumnValues() {
        String values = roundTo3DecLZ(getTargetDistance()) + "," + objID + "," + objName + "," + objInfoFlag + "," + roundTo7Dec(raMean) + "," + roundTo4Dec(raMeanErr) + "," + roundTo7Dec(decMean) + "," + roundTo4Dec(decMeanErr) + "," + convertMJDToDateTime(new BigDecimal(Double.toString(epochMean))).format(DATE_TIME_FORMATTER) + "," + nDetections + "," + roundTo3DecNZ(gMeanPSFMag) + "," + roundTo3DecNZ(gMeanPSFMagErr) + "," + roundTo3DecNZ(rMeanPSFMag) + "," + roundTo3DecNZ(rMeanPSFMagErr) + "," + roundTo3DecNZ(iMeanPSFMag) + "," + roundTo3DecNZ(iMeanPSFMagErr) + "," + roundTo3DecNZ(zMeanPSFMag) + "," + roundTo3DecNZ(zMeanPSFMagErr) + "," + roundTo3DecNZ(yMeanPSFMag) + "," + roundTo3DecNZ(yMeanPSFMagErr) + "," + roundTo3Dec(get_g_r()) + "," + roundTo3Dec(get_r_i()) + "," + roundTo3Dec(get_i_z()) + "," + roundTo3Dec(get_z_y());
        return values.split(",", 24);
    }

    @Override
    public String[] getColumnTitles() {
        String titles = "dist (arcsec),object ID,object name,object info flag,ra,ra err,dec,dec err,observation time,detections,g_mag,g_mag err,r_mag,r_mag err,i_mag,i_mag err,z_mag,z_mag err,y_mag,y_mag err,g-r,r-i,i-z,z-y";
        return titles.split(",", 24);
    }

    @Override
    public Map<Color, Double> getColors() {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.g_r, get_g_r());
        colors.put(Color.r_i, get_r_i());
        colors.put(Color.i_z, get_i_z());
        colors.put(Color.z_y, get_z_y());
        return colors;
    }

    @Override
    public String getSourceId() {
        return String.valueOf(objID);
    }

    @Override
    public double getRa() {
        return raMean;
    }

    @Override
    public void setRa(double ra) {
        this.raMean = ra;
    }

    @Override
    public double getDec() {
        return decMean;
    }

    @Override
    public void setDec(double dec) {
        this.decMean = dec;
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
        return calculateAngularDistance(new NumberPair(targetRa, targetDec), new NumberPair(raMean, decMean), DEG_ARCSEC);
    }

    public double get_g_r() {
        if (gMeanPSFMag == 0 || rMeanPSFMag == 0) {
            return 0;
        } else {
            return gMeanPSFMag - rMeanPSFMag;
        }
    }

    public double get_r_i() {
        if (rMeanPSFMag == 0 || iMeanPSFMag == 0) {
            return 0;
        } else {
            return rMeanPSFMag - iMeanPSFMag;
        }
    }

    public double get_i_z() {
        if (iMeanPSFMag == 0 || zMeanPSFMag == 0) {
            return 0;
        } else {
            return iMeanPSFMag - zMeanPSFMag;
        }
    }

    public double get_z_y() {
        if (zMeanPSFMag == 0 || yMeanPSFMag == 0) {
            return 0;
        } else {
            return zMeanPSFMag - yMeanPSFMag;
        }
    }

}
