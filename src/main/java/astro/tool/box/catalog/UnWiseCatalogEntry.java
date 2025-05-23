package astro.tool.box.catalog;

import static astro.tool.box.function.AstrometricFunctions.calculateAngularDistance;
import static astro.tool.box.function.NumericFunctions.roundTo3Dec;
import static astro.tool.box.function.NumericFunctions.roundTo3DecLZ;
import static astro.tool.box.function.NumericFunctions.roundTo3DecNZ;
import static astro.tool.box.function.NumericFunctions.roundTo3DecNZLZ;
import static astro.tool.box.function.NumericFunctions.roundTo7Dec;
import static astro.tool.box.function.NumericFunctions.roundTo7DecNZ;
import static astro.tool.box.function.NumericFunctions.toDouble;
import static astro.tool.box.function.NumericFunctions.toInteger;
import static astro.tool.box.util.Comparators.getDoubleComparator;
import static astro.tool.box.util.Comparators.getStringComparator;
import static astro.tool.box.util.Constants.NOIRLAB_TAP_URL;
import static astro.tool.box.util.Constants.WISE_1;
import static astro.tool.box.util.Constants.WISE_2;
import static astro.tool.box.util.ConversionFactors.DEG_ARCSEC;
import static astro.tool.box.util.MiscUtils.addRow;
import static astro.tool.box.util.MiscUtils.encodeQuery;
import static astro.tool.box.util.MiscUtils.replaceNanValuesByZero;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;

public class UnWiseCatalogEntry implements CatalogEntry, Extinction {

	public static final String CATALOG_NAME = "unWISE";

	// Unique object id
	private String unwise_objid;

	// W1 position, if available; otherwise W2 position
	private double ra;

	// W1 position, if available; otherwise W2 position
	private double dec;

	// W1 magnitude (Vega)
	private double mag_w1_vg;

	// W2 magnitude (Vega)
	private double mag_w2_vg;

	// W1-W2 color (Vega)
	private double w1_w2_vg;

	// quality factor for W1
	private double qf_w1;

	// quality factor for W2
	private double qf_w2;

	// unWISE Coadd flags at central pixel for W1
	private int flags_unwise_w1;

	// unWISE Coadd flags at central pixel for W2
	private int flags_unwise_w2;

	// Additional informational flags at central pixel for W1
	private int flags_info_w1;

	// Additional informational flags at central pixel for W2
	private int flags_info_w2;

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

	public UnWiseCatalogEntry() {
	}

	public UnWiseCatalogEntry(Map<String, Integer> columns, String[] values) {
		this.columns = columns;
		this.values = values;
		replaceNanValuesByZero(values);
		unwise_objid = values[columns.get("unwise_objid")];
		ra = toDouble(values[columns.get("ra")]);
		dec = toDouble(values[columns.get("dec")]);
		mag_w1_vg = toDouble(values[columns.get("mag_w1_vg")]);
		mag_w2_vg = toDouble(values[columns.get("mag_w2_vg")]);
		w1_w2_vg = toDouble(values[columns.get("w1_w2_vg")]);
		qf_w1 = toDouble(values[columns.get("qf_w1")]);
		qf_w2 = toDouble(values[columns.get("qf_w2")]);
		flags_unwise_w1 = toInteger(values[columns.get("flags_unwise_w1")]);
		flags_unwise_w2 = toInteger(values[columns.get("flags_unwise_w2")]);
		flags_info_w1 = toInteger(values[columns.get("flags_info_w1")]);
		flags_info_w2 = toInteger(values[columns.get("flags_info_w2")]);
	}

	@Override
	public CatalogEntry copy() {
		return new UnWiseCatalogEntry(columns, values);
	}

	@Override
	public void loadCatalogElements() {
		catalogElements.add(new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT,
				getDoubleComparator()));
		catalogElements.add(new CatalogElement("source id", unwise_objid, Alignment.LEFT, getStringComparator()));
		catalogElements.add(new CatalogElement("ra", roundTo7DecNZ(ra), Alignment.LEFT, getDoubleComparator()));
		catalogElements.add(new CatalogElement("dec", roundTo7DecNZ(dec), Alignment.LEFT, getDoubleComparator()));
		catalogElements.add(
				new CatalogElement("W1 (mag)", roundTo3DecNZ(mag_w1_vg), Alignment.RIGHT, getDoubleComparator(), true));
		catalogElements.add(
				new CatalogElement("W2 (mag)", roundTo3DecNZ(mag_w2_vg), Alignment.RIGHT, getDoubleComparator(), true));
		catalogElements.add(new CatalogElement("quality factor W1", roundTo3DecNZLZ(qf_w1), Alignment.RIGHT,
				getDoubleComparator()));
		catalogElements.add(new CatalogElement("quality factor W2", roundTo3DecNZLZ(qf_w2), Alignment.RIGHT,
				getDoubleComparator()));
		catalogElements.add(new CatalogElement("coadd flags W1", String.valueOf(flags_unwise_w1), Alignment.RIGHT,
				getDoubleComparator()));
		catalogElements.add(new CatalogElement("coadd flags W2", String.valueOf(flags_unwise_w2), Alignment.RIGHT,
				getDoubleComparator()));
		catalogElements.add(new CatalogElement("info flags W1", String.valueOf(flags_info_w1), Alignment.RIGHT,
				getDoubleComparator()));
		catalogElements.add(new CatalogElement("info flags W2", String.valueOf(flags_info_w2), Alignment.RIGHT,
				getDoubleComparator()));
		catalogElements.add(
				new CatalogElement("W1-W2", roundTo3DecNZ(w1_w2_vg), Alignment.RIGHT, getDoubleComparator(), true));
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 13 * hash + Objects.hashCode(this.unwise_objid);
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
		final UnWiseCatalogEntry other = (UnWiseCatalogEntry) obj;
		return Objects.equals(this.unwise_objid, other.unwise_objid);
	}

	@Override
	public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
		return new UnWiseCatalogEntry(columns, values);
	}

	@Override
	public String getCatalogName() {
		return CATALOG_NAME;
	}

	@Override
	public java.awt.Color getCatalogColor() {
		return JColor.MINT.val;
	}

	@Override
	public String getCatalogQueryUrl() {
		return NOIRLAB_TAP_URL + encodeQuery(createCatalogQuery());
	}

	private String createCatalogQuery() {
		StringBuilder query = new StringBuilder();
		addRow(query, "SELECT unwise_objid,");
		addRow(query, "       ra,");
		addRow(query, "       dec,");
		addRow(query, "       mag_w1_vg,");
		addRow(query, "       mag_w2_vg,");
		addRow(query, "       w1_w2_vg,");
		addRow(query, "       qf_w1,");
		addRow(query, "       qf_w2,");
		addRow(query, "       flags_unwise_w1,");
		addRow(query, "       flags_unwise_w2,");
		addRow(query, "       flags_info_w1,");
		addRow(query, "       flags_info_w2");
		addRow(query, "FROM   unwise_dr1.object");
		addRow(query,
				"WHERE  't'=q3c_radial_query(ra, dec, " + ra + ", " + dec + ", " + searchRadius / DEG_ARCSEC + ")");
		return query.toString();
	}

	@Override
	public String[] getColumnValues() {
		String columnValues = roundTo3DecLZ(getTargetDistance()) + "," + unwise_objid + "," + roundTo7Dec(ra) + ","
				+ roundTo7Dec(dec) + "," + roundTo3Dec(mag_w1_vg) + "," + roundTo3Dec(mag_w2_vg) + ","
				+ roundTo3DecLZ(qf_w1) + "," + roundTo3DecLZ(qf_w2) + "," + flags_unwise_w1 + "," + flags_unwise_w2
				+ "," + flags_info_w1 + "," + flags_info_w2 + "," + roundTo3Dec(w1_w2_vg);
		return columnValues.split(",", -1);
	}

	@Override
	public String[] getColumnTitles() {
		String columnTitles = "dist (arcsec),source id,ra,dec,W1 (mag),W2 (mag),qual. fact. W1,qual. fact. W2,coadd flags W1,coadd flags W2,info flags W1,info flags W2,W1-W2";
		return columnTitles.split(",", -1);
	}

	@Override
	public void applyExtinctionCorrection(Map<String, Double> extinctionsByBand) {
		if (mag_w1_vg != 0) {
			mag_w1_vg = mag_w1_vg - extinctionsByBand.get(WISE_1);
		}
		if (mag_w2_vg != 0) {
			mag_w2_vg = mag_w2_vg - extinctionsByBand.get(WISE_2);
		}
	}

	@Override
	public Map<Band, NumberPair> getBands() {
		Map<Band, NumberPair> bands = new LinkedHashMap<>();
		bands.put(Band.W1, new NumberPair(mag_w1_vg, 0.1));
		bands.put(Band.W2, new NumberPair(mag_w2_vg, 0.1));
		return bands;
	}

	@Override
	public Map<Color, Double> getColors(boolean toVega) {
		Map<Color, Double> colors = new LinkedHashMap<>();
		colors.put(Color.W1_W2, w1_w2_vg);
		return colors;
	}

	@Override
	public String getMagnitudes() {
		StringBuilder mags = new StringBuilder();
		if (mag_w1_vg != 0) {
			mags.append("W1=").append(roundTo3DecNZ(mag_w1_vg)).append(" ");
		}
		if (mag_w2_vg != 0) {
			mags.append("W2=").append(roundTo3DecNZ(mag_w2_vg)).append(" ");
		}
		return mags.toString();
	}

	@Override
	public String getPhotometry() {
		StringBuilder mags = new StringBuilder();
		if (mag_w1_vg != 0) {
			mags.append(roundTo3DecNZ(mag_w1_vg)).append(",");
		} else {
			mags.append(",");
		}
		if (mag_w2_vg != 0) {
			mags.append(roundTo3DecNZ(mag_w2_vg)).append(",");
		} else {
			mags.append(",");
		}
		return mags.toString();
	}

	@Override
	public String getSourceId() {
		return unwise_objid;
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
		return 0;
	}

	@Override
	public double getPmdec() {
		return 0;
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
		return 0;
	}

	public double getW1_w2_vg() {
		return w1_w2_vg;
	}

	public double getW1mag() {
		return mag_w1_vg;
	}

	public double getW2mag() {
		return mag_w2_vg;
	}

}
