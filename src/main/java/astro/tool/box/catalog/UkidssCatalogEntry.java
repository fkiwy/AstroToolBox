package astro.tool.box.catalog;

import static astro.tool.box.function.AstrometricFunctions.calculateAdditionError;
import static astro.tool.box.function.AstrometricFunctions.calculateAngularDistance;
import static astro.tool.box.function.AstrometricFunctions.calculateTotalProperMotion;
import static astro.tool.box.function.AstrometricFunctions.isProperMotionSpurious;
import static astro.tool.box.function.NumericFunctions.roundTo3Dec;
import static astro.tool.box.function.NumericFunctions.roundTo3DecLZ;
import static astro.tool.box.function.NumericFunctions.roundTo3DecNZ;
import static astro.tool.box.function.NumericFunctions.roundTo3DecNZLZ;
import static astro.tool.box.function.NumericFunctions.roundTo4DecNZ;
import static astro.tool.box.function.NumericFunctions.roundTo7Dec;
import static astro.tool.box.function.NumericFunctions.roundTo7DecNZ;
import static astro.tool.box.function.NumericFunctions.toDouble;
import static astro.tool.box.function.NumericFunctions.toInteger;
import static astro.tool.box.function.NumericFunctions.toLong;
import static astro.tool.box.util.Comparators.getDoubleComparator;
import static astro.tool.box.util.Comparators.getLongComparator;
import static astro.tool.box.util.Comparators.getStringComparator;
import static astro.tool.box.util.Constants.NOIRLAB_TAP_URL;
import static astro.tool.box.util.ConversionFactors.DEG_ARCSEC;
import static astro.tool.box.util.MiscUtils.addRow;
import static astro.tool.box.util.MiscUtils.encodeQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;

public class UkidssCatalogEntry implements CatalogEntry, ProperMotionQuery, ProperMotionCatalog {

	public static final String CATALOG_NAME = "UKIDSS DR11";

	// Unique identifier of this merged detection as assigned by merge algorithm
	private long sourceId;

	// Right ascension
	private double ra;

	// Error in right ascension
	private double ra_err;

	// Declination
	private double dec;

	// Error in declination
	private double dec_err;

	// Proper motion in right ascension direction
	private double pmra;

	// Standard error of proper motion in right ascension direction
	private double pmra_err;

	// Proper motion in declination direction
	private double pmdec;

	// Standard error of proper motion in declination direction
	private double pmdec_err;

	// Object type
	private int objectType;

	// Epoch of position measurement
	private double epoch;

	// Default point source Y aperture corrected mag
	private double y_ap3;

	// Error in default point/extended source Y mag
	private double y_ap3_err;

	// Default point source J aperture corrected mag
	private double j_ap3;

	// Error in default point/extended source J mag
	private double j_ap3_err;

	// Default point source H aperture corrected mag
	private double h_ap3;

	// Error in default point/extended source H mag
	private double h_ap3_err;

	// Default point source Ks aperture corrected mag
	private double ks_ap3;

	// Error in default point/extended source Ks mag
	private double ks_ap3_err;

	// Point source colour Y-J
	private double y_j_pnt;

	// Point source colour J-H
	private double j_h_pnt;

	// Point source colour H-Ks
	private double h_ks_pnt;

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

	private Survey survey;

	public enum Survey {
		LAS, GCS, GPS, DXS, UDS;

		public static Survey[] motionSurveys() {
			return new Survey[] { LAS, GCS, GPS };
		}
	}

	private static final Map<Integer, String> TYPE_TABLE = new HashMap<>();

	static {
		TYPE_TABLE.put(1, "Galaxy");
		TYPE_TABLE.put(0, "Noise");
		TYPE_TABLE.put(-1, "Star");
		TYPE_TABLE.put(-2, "Probable star");
		TYPE_TABLE.put(-3, "Probable galaxy");
		TYPE_TABLE.put(-9, "Saturated");
	}

	private static final Map<Survey, String> SURVEY_LABEL = new HashMap<>();

	static {
		SURVEY_LABEL.put(Survey.LAS, "Large Area Survey");
		SURVEY_LABEL.put(Survey.GCS, "Galactic Clusters Survey");
		SURVEY_LABEL.put(Survey.GPS, "Galactic Plane Survey");
		SURVEY_LABEL.put(Survey.DXS, "Deep Extragalactic Survey");
		SURVEY_LABEL.put(Survey.UDS, "Ultra Deep Survey");
	}

	public UkidssCatalogEntry() {
	}

	public UkidssCatalogEntry(Map<String, Integer> columns, String[] values) {
		this.columns = columns;
		this.values = values;
		sourceId = toLong(values[columns.get("sourceid")]);
		ra = toDouble(values[columns.get("ra")]);
		dec = toDouble(values[columns.get("dec")]);
		if (columns.get("sigra") != null) {
			ra_err = fixValue(toDouble(values[columns.get("sigra")]));
			dec_err = fixValue(toDouble(values[columns.get("sigdec")]));
		}
		if (columns.get("mura") != null) {
			pmra = fixValue(toDouble(values[columns.get("mura")]));
			pmra_err = fixValue(toDouble(values[columns.get("sigmura")]));
			pmdec = fixValue(toDouble(values[columns.get("mudec")]));
			pmdec_err = fixValue(toDouble(values[columns.get("sigmudec")]));
			epoch = toDouble(values[columns.get("epoch")]);
		}
		objectType = toInteger(values[columns.get("mergedclass")]);
		if (columns.get("yapermag3") != null) {
			y_ap3 = fixValue(toDouble(values[columns.get("yapermag3")]));
			y_ap3_err = fixValue(toDouble(values[columns.get("yapermag3err")]));
			y_j_pnt = fixValue(toDouble(values[columns.get("ymjpnt")]));
		}
		j_ap3 = fixValue(toDouble(values[columns.get("japermag3")]));
		j_ap3_err = fixValue(toDouble(values[columns.get("japermag3err")]));
		h_ap3 = fixValue(toDouble(values[columns.get("hapermag3")]));
		h_ap3_err = fixValue(toDouble(values[columns.get("hapermag3err")]));
		ks_ap3 = fixValue(toDouble(values[columns.get("kapermag3")]));
		ks_ap3_err = fixValue(toDouble(values[columns.get("kapermag3err")]));
		j_h_pnt = fixValue(toDouble(values[columns.get("jmhpnt")]));
		h_ks_pnt = fixValue(toDouble(values[columns.get("hmkpnt")]));
	}

	private double fixValue(double value) {
		return value < -999999 ? 0 : value;
	}

	@Override
	public CatalogEntry copy() {
		return new UkidssCatalogEntry(columns, values);
	}

	@Override
	public void loadCatalogElements() {
		catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT,
				getDoubleComparator()));
		catalogElements
				.add(new CatalogElement("source id", String.valueOf(sourceId), Alignment.LEFT, getLongComparator()));
		catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
		catalogElements.add(new CatalogElement("ra err", roundTo7DecNZ(ra_err), Alignment.LEFT, getDoubleComparator()));
		catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
		catalogElements
				.add(new CatalogElement("dec err", roundTo7DecNZ(dec_err), Alignment.LEFT, getDoubleComparator()));
		catalogElements
				.add(new CatalogElement("pmra (mas/yr)", roundTo3DecNZ(pmra), Alignment.RIGHT, getDoubleComparator()));
		catalogElements.add(new CatalogElement("pmra err", roundTo3DecNZ(pmra_err), Alignment.RIGHT,
				getDoubleComparator(), false, false, isProperMotionSpurious(pmra, pmra_err)));
		catalogElements.add(
				new CatalogElement("pmdec (mas/yr)", roundTo3DecNZ(pmdec), Alignment.RIGHT, getDoubleComparator()));
		catalogElements.add(new CatalogElement("pmdec err", roundTo3DecNZ(pmdec_err), Alignment.RIGHT,
				getDoubleComparator(), false, false, isProperMotionSpurious(pmdec, pmdec_err)));
		catalogElements.add(
				new CatalogElement("UKIDSS survey", SURVEY_LABEL.get(survey), Alignment.LEFT, getStringComparator()));
		catalogElements.add(new CatalogElement("object type", TYPE_TABLE.get(objectType), Alignment.LEFT,
				getStringComparator(), true));
		catalogElements.add(new CatalogElement("epoch", roundTo3DecNZ(epoch), Alignment.LEFT, getStringComparator()));
		catalogElements
				.add(new CatalogElement("Y (mag)", roundTo3DecNZ(y_ap3), Alignment.RIGHT, getDoubleComparator(), true));
		catalogElements
				.add(new CatalogElement("Y err", roundTo3DecNZ(y_ap3_err), Alignment.RIGHT, getDoubleComparator()));
		catalogElements
				.add(new CatalogElement("J (mag)", roundTo3DecNZ(j_ap3), Alignment.RIGHT, getDoubleComparator(), true));
		catalogElements
				.add(new CatalogElement("J err", roundTo3DecNZ(j_ap3_err), Alignment.RIGHT, getDoubleComparator()));
		catalogElements
				.add(new CatalogElement("H (mag)", roundTo3DecNZ(h_ap3), Alignment.RIGHT, getDoubleComparator(), true));
		catalogElements
				.add(new CatalogElement("H err", roundTo3DecNZ(h_ap3_err), Alignment.RIGHT, getDoubleComparator()));
		catalogElements.add(
				new CatalogElement("Ks (mag)", roundTo3DecNZ(ks_ap3), Alignment.RIGHT, getDoubleComparator(), true));
		catalogElements
				.add(new CatalogElement("Ks err", roundTo3DecNZ(ks_ap3_err), Alignment.RIGHT, getDoubleComparator()));
		catalogElements.add(new CatalogElement("Y-J", roundTo3DecNZ(y_j_pnt), Alignment.RIGHT, getDoubleComparator()));
		catalogElements.add(new CatalogElement("J-H", roundTo3DecNZ(j_h_pnt), Alignment.RIGHT, getDoubleComparator()));
		catalogElements
				.add(new CatalogElement("H-Ks", roundTo3DecNZ(h_ks_pnt), Alignment.RIGHT, getDoubleComparator()));
		catalogElements
				.add(new CatalogElement("J-Ks", roundTo3DecNZ(getJ_K()), Alignment.RIGHT, getDoubleComparator()));
		catalogElements.add(new CatalogElement("tpm (mas/yr)", roundTo3DecNZ(getTotalProperMotion()), Alignment.RIGHT,
				getDoubleComparator(), false, true));
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 59 * hash + (int) (this.sourceId ^ (this.sourceId >>> 32));
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		final UkidssCatalogEntry other = (UkidssCatalogEntry) obj;
		return this.sourceId == other.sourceId;
	}

	@Override
	public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
		UkidssCatalogEntry entry = new UkidssCatalogEntry(columns, values);
		entry.setSurvey(survey);
		return entry;
	}

	@Override
	public String getCatalogName() {
		return CATALOG_NAME;
	}

	@Override
	public java.awt.Color getCatalogColor() {
		return JColor.BLOOD.val;
	}

	@Override
	public String getCatalogQueryUrl() {
		String catalogQuery = "";
		switch (survey) {
		case LAS -> catalogQuery = createCatalogQueryLAS();
		case GCS -> catalogQuery = createCatalogQueryGCS();
		case GPS -> catalogQuery = createCatalogQueryGPS();
		case DXS -> catalogQuery = createCatalogQueryDXS();
		case UDS -> catalogQuery = createCatalogQueryUDS();
		}
		return NOIRLAB_TAP_URL + encodeQuery(catalogQuery);
	}

	@Override
	public String getMotionQueryUrl() {
		StringBuilder queryBuilder = new StringBuilder();
		String catalogQuery = "";
		switch (survey) {
		case LAS -> {
			catalogQuery = createCatalogQueryLAS();
			addRow(queryBuilder, catalogQuery);
			catalogQuery = createProperMotionQuery(queryBuilder);
		}
		case GCS -> {
			catalogQuery = createCatalogQueryGCS();
			addRow(queryBuilder, catalogQuery);
			catalogQuery = createProperMotionQuery(queryBuilder);
		}
		case GPS -> {
			catalogQuery = createCatalogQueryGPS();
			addRow(queryBuilder, catalogQuery);
			catalogQuery = createProperMotionQuery(queryBuilder);
		}
		default -> throw new IllegalArgumentException("Unexpected value: " + survey);
		}
		return NOIRLAB_TAP_URL + encodeQuery(catalogQuery);
	}

	private String createCatalogQueryLAS() {
		StringBuilder query = new StringBuilder();
		addRow(query, "SELECT sourceid,");
		addRow(query, "       ra,");
		addRow(query, "       sigra,");
		addRow(query, "       dec,");
		addRow(query, "       sigdec,");
		addRow(query, "       mura,");
		addRow(query, "       sigmura,");
		addRow(query, "       mudec,");
		addRow(query, "       sigmudec,");
		addRow(query, "       mergedclass,");
		addRow(query, "       epoch,");
		addRow(query, "       yapermag3,");
		addRow(query, "       yapermag3err,");
		addRow(query, "       japermag3,");
		addRow(query, "       japermag3err,");
		addRow(query, "       hapermag3,");
		addRow(query, "       hapermag3err,");
		addRow(query, "       kapermag3,");
		addRow(query, "       kapermag3err,");
		addRow(query, "       ymjpnt,");
		addRow(query, "       jmhpnt,");
		addRow(query, "       hmkpnt");
		addRow(query, "FROM   ukidss_dr11plus.lassource");
		addRow(query,
				"WHERE  't'=q3c_radial_query(ra, dec, " + ra + ", " + dec + ", " + searchRadius / DEG_ARCSEC + ")");
		return query.toString();
	}

	private String createCatalogQueryGCS() {
		StringBuilder query = new StringBuilder();
		addRow(query, "SELECT sourceid,");
		addRow(query, "       ra,");
		addRow(query, "       sigra,");
		addRow(query, "       dec,");
		addRow(query, "       sigdec,");
		addRow(query, "       mura,");
		addRow(query, "       sigmura,");
		addRow(query, "       mudec,");
		addRow(query, "       sigmudec,");
		addRow(query, "       mergedclass,");
		addRow(query, "       epoch,");
		addRow(query, "       yapermag3,");
		addRow(query, "       yapermag3err,");
		addRow(query, "       japermag3,");
		addRow(query, "       japermag3err,");
		addRow(query, "       hapermag3,");
		addRow(query, "       hapermag3err,");
		addRow(query, "       kapermag3,");
		addRow(query, "       kapermag3err,");
		addRow(query, "       ymjpnt,");
		addRow(query, "       jmhpnt,");
		addRow(query, "       hmkpnt");
		addRow(query, "FROM   ukidss_dr11plus.gcssource");
		addRow(query,
				"WHERE  't'=q3c_radial_query(ra, dec, " + ra + ", " + dec + ", " + searchRadius / DEG_ARCSEC + ")");
		return query.toString();
	}

	private String createCatalogQueryGPS() {
		StringBuilder query = new StringBuilder();
		addRow(query, "SELECT sourceid,");
		addRow(query, "       ra,");
		addRow(query, "       sigra,");
		addRow(query, "       dec,");
		addRow(query, "       sigdec,");
		addRow(query, "       mura,");
		addRow(query, "       sigmura,");
		addRow(query, "       mudec,");
		addRow(query, "       sigmudec,");
		addRow(query, "       mergedclass,");
		addRow(query, "       epoch,");
		addRow(query, "       japermag3,");
		addRow(query, "       japermag3err,");
		addRow(query, "       hapermag3,");
		addRow(query, "       hapermag3err,");
		addRow(query, "       kapermag3,");
		addRow(query, "       kapermag3err,");
		addRow(query, "       jmhpnt,");
		addRow(query, "       hmkpnt");
		addRow(query, "FROM   ukidss_dr11plus.gpssource");
		addRow(query,
				"WHERE  't'=q3c_radial_query(ra, dec, " + ra + ", " + dec + ", " + searchRadius / DEG_ARCSEC + ")");
		return query.toString();
	}

	private String createCatalogQueryDXS() {
		StringBuilder query = new StringBuilder();
		addRow(query, "SELECT sourceid,");
		addRow(query, "       ra,");
		addRow(query, "       dec,");
		addRow(query, "       mergedclass,");
		addRow(query, "       japermag3,");
		addRow(query, "       japermag3err,");
		addRow(query, "       hapermag3,");
		addRow(query, "       hapermag3err,");
		addRow(query, "       kapermag3,");
		addRow(query, "       kapermag3err,");
		addRow(query, "       jmhpnt,");
		addRow(query, "       hmkpnt");
		addRow(query, "FROM   ukidss_dr11plus.dxssource");
		addRow(query,
				"WHERE  't'=q3c_radial_query(ra, dec, " + ra + ", " + dec + ", " + searchRadius / DEG_ARCSEC + ")");
		return query.toString();
	}

	private String createCatalogQueryUDS() {
		StringBuilder query = new StringBuilder();
		addRow(query, "SELECT sourceid,");
		addRow(query, "       ra,");
		addRow(query, "       dec,");
		addRow(query, "       mergedclass,");
		addRow(query, "       japermag3,");
		addRow(query, "       japermag3err,");
		addRow(query, "       hapermag3,");
		addRow(query, "       hapermag3err,");
		addRow(query, "       kapermag3,");
		addRow(query, "       kapermag3err,");
		addRow(query, "       jmhpnt,");
		addRow(query, "       hmkpnt");
		addRow(query, "FROM   ukidss_dr11plus.udssource");
		addRow(query,
				"WHERE  't'=q3c_radial_query(ra, dec, " + ra + ", " + dec + ", " + searchRadius / DEG_ARCSEC + ")");
		return query.toString();
	}

	private String createProperMotionQuery(StringBuilder query) {
		addRow(query, "AND    mura > -999999 AND mudec > -999999");
		addRow(query, "AND    SQRT(mura * mura + mudec * mudec) >= " + tpm);
		return query.toString();
	}

	@Override
	public void setTpm(double tpm) {
		this.tpm = tpm;
	}

	@Override
	public String[] getColumnValues() {
		String columnValues = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + roundTo7Dec(ra) + ","
				+ roundTo7Dec(ra_err) + "," + roundTo7Dec(dec) + "," + roundTo7Dec(dec_err) + "," + roundTo3Dec(pmra)
				+ "," + roundTo3Dec(pmra_err) + "," + roundTo3Dec(pmdec) + "," + roundTo3Dec(pmdec_err) + ","
				+ SURVEY_LABEL.get(survey) + "," + TYPE_TABLE.get(objectType) + "," + roundTo3Dec(epoch) + ","
				+ roundTo3Dec(y_ap3) + "," + roundTo3Dec(y_ap3_err) + "," + roundTo3Dec(j_ap3) + ","
				+ roundTo3Dec(j_ap3_err) + "," + roundTo3Dec(h_ap3) + "," + roundTo3Dec(h_ap3_err) + ","
				+ roundTo3Dec(ks_ap3) + "," + roundTo3Dec(ks_ap3_err) + "," + roundTo3Dec(y_j_pnt) + ","
				+ roundTo3Dec(j_h_pnt) + "," + roundTo3Dec(h_ks_pnt) + "," + roundTo3Dec(getJ_K()) + ","
				+ roundTo3Dec(getTotalProperMotion());
		return columnValues.split(",", -1);
	}

	@Override
	public String[] getColumnTitles() {
		String columnTitles = """
				dist (arcsec),\
				source id,\
				ra,\
				ra err,\
				dec,\
				dec err,\
				pmra (mas/yr),\
				pmra err,\
				pmdec (mas/yr),\
				pmdec err,\
				UKIDSS survey,\
				object type,\
				epoch,\
				Y (mag),\
				Y err,\
				J (mag),\
				J err,\
				H (mag),\
				H err,\
				Ks (mag),\
				Ks err,\
				Y-J,\
				J-H,\
				H-Ks,\
				J-Ks,\
				tpm (mas/yr)\
				""";
		return columnTitles.split(",", -1);
	}

	@Override
	public Map<Band, NumberPair> getBands() {
		Map<Band, NumberPair> bands = new LinkedHashMap<>();
		bands.put(Band.J, new NumberPair(j_ap3, j_ap3_err));
		bands.put(Band.H, new NumberPair(h_ap3, h_ap3_err));
		bands.put(Band.K, new NumberPair(ks_ap3, ks_ap3_err));
		return bands;
	}

	@Override
	public Map<Color, Double> getColors(boolean toVega) {
		Map<Color, Double> colors = new LinkedHashMap<>();
		colors.put(Color.J_H, j_h_pnt);
		colors.put(Color.H_K, h_ks_pnt);
		colors.put(Color.J_K, getJ_K());
		colors.put(Color.e_J_H, getJ_H() - getJ_H_err());
		colors.put(Color.e_H_K, getH_K() - getH_K_err());
		colors.put(Color.e_J_K, getJ_K() - getJ_K_err());
		colors.put(Color.E_J_H, getJ_H() + getJ_H_err());
		colors.put(Color.E_H_K, getH_K() + getH_K_err());
		colors.put(Color.E_J_K, getJ_K() + getJ_K_err());
		return colors;
	}

	@Override
	public String getMagnitudes() {
		StringBuilder mags = new StringBuilder();
		if (y_ap3 != 0) {
			mags.append("Y=").append(roundTo4DecNZ(y_ap3)).append(" ");
		}
		if (j_ap3 != 0) {
			mags.append("J=").append(roundTo4DecNZ(j_ap3)).append(" ");
		}
		if (h_ap3 != 0) {
			mags.append("H=").append(roundTo4DecNZ(h_ap3)).append(" ");
		}
		if (ks_ap3 != 0) {
			mags.append("K=").append(roundTo4DecNZ(ks_ap3)).append(" ");
		}
		return mags.toString();
	}

	@Override
	public String getPhotometry() {
		StringBuilder mags = new StringBuilder();
		if (y_ap3 != 0) {
			mags.append(roundTo4DecNZ(y_ap3)).append(",").append(roundTo4DecNZ(y_ap3_err)).append(",");
		} else {
			mags.append(",,");
		}
		if (j_ap3 != 0) {
			mags.append(roundTo4DecNZ(j_ap3)).append(",").append(roundTo4DecNZ(j_ap3_err)).append(",");
		} else {
			mags.append(",,");
		}
		if (h_ap3 != 0) {
			mags.append(roundTo4DecNZ(h_ap3)).append(",").append(roundTo4DecNZ(h_ap3_err)).append(",");
		} else {
			mags.append(",,");
		}
		if (ks_ap3 != 0) {
			mags.append(roundTo4DecNZ(ks_ap3)).append(",").append(roundTo4DecNZ(ks_ap3_err)).append(",");
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
		return epoch;
	}

	public void setSurvey(Survey survey) {
		this.survey = survey;
	}

	public double getJ_H() {
		return j_h_pnt;
	}

	public double getH_K() {
		return h_ks_pnt;
	}

	public double getJ_K() {
		if (j_ap3 == 0 || ks_ap3 == 0) {
			return 0;
		} else {
			return j_ap3 - ks_ap3;
		}
	}

	public double getJ_H_err() {
		if (j_ap3_err == 0 || h_ap3_err == 0) {
			return 0;
		} else {
			return calculateAdditionError(j_ap3_err, h_ap3_err);
		}
	}

	public double getH_K_err() {
		if (h_ap3_err == 0 || ks_ap3_err == 0) {
			return 0;
		} else {
			return calculateAdditionError(h_ap3_err, ks_ap3_err);
		}
	}

	public double getJ_K_err() {
		if (j_ap3_err == 0 || ks_ap3_err == 0) {
			return 0;
		} else {
			return calculateAdditionError(j_ap3_err, ks_ap3_err);
		}
	}

	public double getJmag() {
		return j_ap3;
	}

	public double getHmag() {
		return h_ap3;
	}

	public double getKmag() {
		return ks_ap3;
	}

	public double getJ_err() {
		return j_ap3_err;
	}

	public double getH_err() {
		return h_ap3_err;
	}

	public double getK_err() {
		return ks_ap3_err;
	}

}
