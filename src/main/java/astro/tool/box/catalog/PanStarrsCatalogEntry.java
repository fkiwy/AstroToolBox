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
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.exception.NoExtinctionValuesException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;

public class PanStarrsCatalogEntry implements CatalogEntry {

    public static final String CATALOG_NAME = "Pan-STARRS";

    // Unique object identifier
    private long objID;

    // IAU name for this object
    private String objName;

    // Subset of objInfoFlag denoting whether this object is real or a likely false positive
    private int qualityFlag;

    // Right ascension from single epoch detections (weighted mean) in equinox J2000 at the mean epoch given by epochMean
    private double raMean;

    // Declination from single epoch detections (weighted mean) in equinox J2000 at the mean epoch given by epochMean
    private double decMean;

    // Right ascension standard deviation from single epoch detections
    private double raMeanErr;

    // Declination standard deviation from single epoch detections
    private double decMeanErr;

    // Modified Julian Date of the mean epoch corresponding to raMean, decMean (equinox J2000)
    private LocalDateTime epochMean;

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

    // Most likely spectral type
    private String spt;

    private final List<CatalogElement> catalogElements = new ArrayList<>();

    private Map<String, Integer> columns;

    private String[] values;

    private static final Map<Integer, String> QUALITY_FLAGS;

    static {
        QUALITY_FLAGS = new HashMap<>();
        QUALITY_FLAGS.put(1, "extended in Pan-STARRS data");
        QUALITY_FLAGS.put(2, "extended in external data (2MASS)");
        QUALITY_FLAGS.put(4, "good-quality measurement in Pan-STARRS data");
        QUALITY_FLAGS.put(8, "good-quality measurement in external data (2MASS)");
        QUALITY_FLAGS.put(16, "good-quality object in the stack");
        QUALITY_FLAGS.put(32, "the primary stack measurements are the best");
        QUALITY_FLAGS.put(64, "suspect object in the stack (no more than 1 good measurement)");
        QUALITY_FLAGS.put(128, "poor-quality stack object (no more than 1 good or suspect measurement)");
    }

    public PanStarrsCatalogEntry() {
    }

    public PanStarrsCatalogEntry(Map<String, Integer> columns, String[] values) {
        this.columns = columns;
        this.values = values;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals("-999.0")) {
                values[i] = "0";
            }
        }
        objName = values[columns.get("objName")];
        objID = toLong(values[columns.get("objID")]);
        qualityFlag = toInteger(values[columns.get("qualityFlag")]);
        raMean = toDouble(values[columns.get("raMean")]);
        decMean = toDouble(values[columns.get("decMean")]);
        raMeanErr = toDouble(values[columns.get("raMeanErr")]);
        decMeanErr = toDouble(values[columns.get("decMeanErr")]);
        epochMean = convertMJDToDateTime(new BigDecimal(values[columns.get("epochMean")]));
        nDetections = toInteger(values[columns.get("nDetections")]);
        gMeanPSFMag = toDouble(values[columns.get("gMeanPSFMag")]);
        gMeanPSFMagErr = toDouble(values[columns.get("gMeanPSFMagErr")]);
        rMeanPSFMag = toDouble(values[columns.get("rMeanPSFMag")]);
        rMeanPSFMagErr = toDouble(values[columns.get("rMeanPSFMagErr")]);
        iMeanPSFMag = toDouble(values[columns.get("iMeanPSFMag")]);
        iMeanPSFMagErr = toDouble(values[columns.get("iMeanPSFMagErr")]);
        zMeanPSFMag = toDouble(values[columns.get("zMeanPSFMag")]);
        zMeanPSFMagErr = toDouble(values[columns.get("zMeanPSFMagErr")]);
        yMeanPSFMag = toDouble(values[columns.get("yMeanPSFMag")]);
        yMeanPSFMagErr = toDouble(values[columns.get("yMeanPSFMagErr")]);
    }

    @Override
    public CatalogEntry copy() {
        return new PanStarrsCatalogEntry(columns, values);
    }

    @Override
    public void loadCatalogElements() {
        catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("source id", String.valueOf(objID), Alignment.LEFT, getLongComparator()));
        catalogElements.add(new CatalogElement("object name", objName, Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("quality flag sum", String.valueOf(qualityFlag), Alignment.RIGHT, getIntegerComparator(), createToolTipQualityFlag()));
        catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(raMean), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("ra err (arcsec)", roundTo4DecNZ(raMeanErr), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(decMean), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("dec err (arcsec)", roundTo4DecNZ(decMeanErr), Alignment.LEFT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("mean observ. time", epochMean.format(DATE_TIME_FORMATTER), Alignment.LEFT, getStringComparator()));
        catalogElements.add(new CatalogElement("detections", String.valueOf(nDetections), Alignment.RIGHT, getIntegerComparator()));
        catalogElements.add(new CatalogElement("g (mag)", roundTo3DecNZ(gMeanPSFMag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("g err", roundTo3DecNZ(gMeanPSFMagErr), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("r (mag)", roundTo3DecNZ(rMeanPSFMag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("r err", roundTo3DecNZ(rMeanPSFMagErr), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("i (mag)", roundTo3DecNZ(iMeanPSFMag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("i err", roundTo3DecNZ(iMeanPSFMagErr), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("z (mag)", roundTo3DecNZ(zMeanPSFMag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("z err", roundTo3DecNZ(zMeanPSFMagErr), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("y (mag)", roundTo3DecNZ(yMeanPSFMag), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("y err", roundTo3DecNZ(yMeanPSFMagErr), Alignment.RIGHT, getDoubleComparator()));
        catalogElements.add(new CatalogElement("g-r", roundTo3DecNZ(get_g_r()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("r-i", roundTo3DecNZ(get_r_i()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("i-z", roundTo3DecNZ(get_i_z()), Alignment.RIGHT, getDoubleComparator(), false, true));
        catalogElements.add(new CatalogElement("z-y", roundTo3DecNZ(get_z_y()), Alignment.RIGHT, getDoubleComparator(), false, true));
    }

    public String createToolTipQualityFlag() {
        StringBuilder toolTip = new StringBuilder();
        toolTip.append("<b>Quality flag details:</b>");
        getFlagLabels(qualityFlag, QUALITY_FLAGS).forEach((flag) -> {
            toolTip.append(LINE_BREAK).append(flag.getS1()).append(" = ").append(flag.getS2());
        });
        return toolTip.toString();
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
    public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
        return new PanStarrsCatalogEntry(columns, values);
    }

    @Override
    public String getCatalogName() {
        return CATALOG_NAME;
    }

    @Override
    public java.awt.Color getCatalogColor() {
        return JColor.BROWN.val;
    }

    @Override
    public String getCatalogQueryUrl() {
        return createPanStarrsUrl(raMean, decMean, searchRadius / DEG_ARCSEC);
    }

    @Override
    public String[] getColumnValues() {
        String columnValues = roundTo3DecLZ(getTargetDistance()) + "," + objID + "," + objName + "," + qualityFlag + "," + roundTo7Dec(raMean) + "," + roundTo4Dec(raMeanErr) + "," + roundTo7Dec(decMean) + "," + roundTo4Dec(decMeanErr) + "," + epochMean.format(DATE_TIME_FORMATTER) + "," + nDetections + "," + roundTo3Dec(gMeanPSFMag) + "," + roundTo3Dec(gMeanPSFMagErr) + "," + roundTo3Dec(rMeanPSFMag) + "," + roundTo3Dec(rMeanPSFMagErr) + "," + roundTo3Dec(iMeanPSFMag) + "," + roundTo3Dec(iMeanPSFMagErr) + "," + roundTo3Dec(zMeanPSFMag) + "," + roundTo3Dec(zMeanPSFMagErr) + "," + roundTo3Dec(yMeanPSFMag) + "," + roundTo3Dec(yMeanPSFMagErr) + "," + roundTo3Dec(get_g_r()) + "," + roundTo3Dec(get_r_i()) + "," + roundTo3Dec(get_i_z()) + "," + roundTo3Dec(get_z_y());
        return columnValues.split(",", -1);
    }

    @Override
    public String[] getColumnTitles() {
        String columnTitles = "dist (arcsec),source id,object name,quality flag,ra,ra err (arcsec),dec,dec err (arcsec),mean observ. time,detections,g (mag),g err,r (mag),r err,i (mag),i err,z (mag),z err,y (mag),y err,g-r,r-i,i-z,z-y";
        return columnTitles.split(",", -1);
    }

    @Override
    public void applyExtinctionCorrection(Map<String, Double> extinctionsByBand) throws NoExtinctionValuesException {
        throw new NoExtinctionValuesException();
    }

    @Override
    public Map<Band, NumberPair> getBands() {
        Map<Band, NumberPair> bands = new LinkedHashMap<>();
        bands.put(Band.g, new NumberPair(gMeanPSFMag, gMeanPSFMagErr));
        bands.put(Band.r, new NumberPair(rMeanPSFMag, rMeanPSFMagErr));
        bands.put(Band.i, new NumberPair(iMeanPSFMag, iMeanPSFMagErr));
        bands.put(Band.z, new NumberPair(zMeanPSFMag, zMeanPSFMagErr));
        bands.put(Band.y, new NumberPair(yMeanPSFMag, yMeanPSFMagErr));
        return bands;
    }

    @Override
    public Map<Color, Double> getColors(boolean toVega) {
        Map<Color, Double> colors = new LinkedHashMap<>();
        colors.put(Color.g_r_PS1, get_g_r());
        colors.put(Color.r_i_PS1, get_r_i());
        colors.put(Color.i_z_PS1, get_i_z());
        colors.put(Color.i_y_PS1, get_i_y());
        colors.put(Color.z_y_PS1, get_z_y());
        return colors;
    }

    @Override
    public String getMagnitudes() {
        StringBuilder mags = new StringBuilder();
        if (gMeanPSFMag != 0) {
            mags.append("g=").append(roundTo3DecNZ(gMeanPSFMag)).append(" ");
        }
        if (rMeanPSFMag != 0) {
            mags.append("r=").append(roundTo3DecNZ(rMeanPSFMag)).append(" ");
        }
        if (iMeanPSFMag != 0) {
            mags.append("i=").append(roundTo3DecNZ(iMeanPSFMag)).append(" ");
        }
        if (zMeanPSFMag != 0) {
            mags.append("z=").append(roundTo3DecNZ(zMeanPSFMag)).append(" ");
        }
        if (yMeanPSFMag != 0) {
            mags.append("y=").append(roundTo3DecNZ(yMeanPSFMag)).append(" ");
        }
        return mags.toString();
    }

    @Override
    public String getPhotometry() {
        StringBuilder mags = new StringBuilder();
        if (gMeanPSFMag != 0) {
            mags.append(roundTo3DecNZ(gMeanPSFMag)).append(",").append(roundTo3DecNZ(gMeanPSFMagErr)).append(",");
        } else {
            mags.append(",,");
        }
        if (rMeanPSFMag != 0) {
            mags.append(roundTo3DecNZ(rMeanPSFMag)).append(",").append(roundTo3DecNZ(rMeanPSFMagErr)).append(",");
        } else {
            mags.append(",,");
        }
        if (iMeanPSFMag != 0) {
            mags.append(roundTo3DecNZ(iMeanPSFMag)).append(",").append(roundTo3DecNZ(iMeanPSFMagErr)).append(",");
        } else {
            mags.append(",,");
        }
        if (zMeanPSFMag != 0) {
            mags.append(roundTo3DecNZ(zMeanPSFMag)).append(",").append(roundTo3DecNZ(zMeanPSFMagErr)).append(",");
        } else {
            mags.append(",,");
        }
        if (yMeanPSFMag != 0) {
            mags.append(roundTo3DecNZ(yMeanPSFMag)).append(",").append(roundTo3DecNZ(yMeanPSFMagErr)).append(",");
        } else {
            mags.append(",,");
        }
        return mags.toString();
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
        return calculateAngularDistance(new NumberPair(targetRa, targetDec), new NumberPair(raMean, decMean), DEG_ARCSEC);
    }

    @Override
    public double getParallacticDistance() {
        return 0;
    }

    @Override
    public double getTotalProperMotion() {
        return 0;
    }

    public LocalDateTime getObsDate() {
        return epochMean;
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

    public double get_i_y() {
        if (iMeanPSFMag == 0 || yMeanPSFMag == 0) {
            return 0;
        } else {
            return iMeanPSFMag - yMeanPSFMag;
        }
    }

    public double get_z_y() {
        if (zMeanPSFMag == 0 || yMeanPSFMag == 0) {
            return 0;
        } else {
            return zMeanPSFMag - yMeanPSFMag;
        }
    }

    public double get_g_mag() {
        return gMeanPSFMag;
    }

    public double get_r_mag() {
        return rMeanPSFMag;
    }

    public double get_i_mag() {
        return iMeanPSFMag;
    }

    public double get_z_mag() {
        return zMeanPSFMag;
    }

    public double get_y_mag() {
        return yMeanPSFMag;
    }

}
