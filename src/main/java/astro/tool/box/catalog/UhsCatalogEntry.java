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
import static astro.tool.box.main.ToolboxHelper.showWarnDialog;
import static astro.tool.box.main.ToolboxHelper.writeErrorLog;
import static astro.tool.box.util.Comparators.getDoubleComparator;
import static astro.tool.box.util.Comparators.getLongComparator;
import static astro.tool.box.util.Comparators.getStringComparator;
import static astro.tool.box.util.ConversionFactors.ARCMIN_ARCSEC;
import static astro.tool.box.util.ConversionFactors.DEG_ARCSEC;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.Band;
import astro.tool.box.enumeration.Color;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.util.ServiceHelper;

public class UhsCatalogEntry implements CatalogEntry, ProperMotionQuery, ProperMotionCatalog {

	public static final String CATALOG_NAME = "UHS DR3";

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

	private static final Map<Integer, String> TYPE_TABLE = new HashMap<>();

	static {
		TYPE_TABLE.put(1, "Galaxy");
		TYPE_TABLE.put(0, "Noise");
		TYPE_TABLE.put(-1, "Star");
		TYPE_TABLE.put(-2, "Probable star");
		TYPE_TABLE.put(-3, "Probable galaxy");
		TYPE_TABLE.put(-9, "Saturated");
	}

	public UhsCatalogEntry() {
	}

	public UhsCatalogEntry(Map<String, Integer> columns, String[] values) {
		this.columns = columns;
		this.values = values;
		sourceId = toLong(values[columns.get("sourceID")]);
		ra = toDouble(values[columns.get("ra")]);
		dec = toDouble(values[columns.get("dec")]);
		ra_err = fixValue(toDouble(values[columns.get("sigRa")]));
		dec_err = fixValue(toDouble(values[columns.get("sigDec")]));
		pmra = fixValue(toDouble(values[columns.get("muRa")]));
		pmra_err = fixValue(toDouble(values[columns.get("sigMuRa")]));
		pmdec = fixValue(toDouble(values[columns.get("muDec")]));
		pmdec_err = fixValue(toDouble(values[columns.get("sigMuDec")]));
		epoch = toDouble(values[columns.get("epoch")]);
		objectType = toInteger(values[columns.get("mergedClass")]);
		j_ap3 = fixValue(toDouble(values[columns.get("jAperMag3")]));
		j_ap3_err = fixValue(toDouble(values[columns.get("jAperMag3Err")]));
		h_ap3 = fixValue(toDouble(values[columns.get("hAperMag3")]));
		h_ap3_err = fixValue(toDouble(values[columns.get("hAperMag3Err")]));
		ks_ap3 = fixValue(toDouble(values[columns.get("kAperMag3")]));
		ks_ap3_err = fixValue(toDouble(values[columns.get("kAperMag3Err")]));
		j_h_pnt = fixValue(toDouble(values[columns.get("jmhPnt")]));
		h_ks_pnt = fixValue(toDouble(values[columns.get("hmkPnt")]));
	}

	private double fixValue(double value) {
		return value < -999999 ? 0 : value;
	}

	@Override
	public CatalogEntry copy() {
		return new UhsCatalogEntry(columns, values);
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
		catalogElements.add(new CatalogElement("object type", TYPE_TABLE.get(objectType), Alignment.LEFT,
				getStringComparator(), true));
		catalogElements.add(new CatalogElement("epoch", roundTo3DecNZ(epoch), Alignment.LEFT, getStringComparator()));
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

	public List<CatalogEntry> findCatalogEntries() {
		List<CatalogEntry> catalogEntries = new ArrayList();

		try {
			double radius = getSearchRadius() / ARCMIN_ARCSEC;
			String url = "http://wsa.roe.ac.uk:8080/wsa/WSASQL?database=UHSDR3&programmeID=107&from=source&formaction=region&ra=%f&dec=%f&sys=J&radius=%f&xSize=&ySize=&format=CSV&compress=NONE&select=default";
			String paramUrl = url.formatted(getRa(), getDec(), radius);
			String htmlContent = downloadHtmlFromUrl(paramUrl);

			Document doc = Jsoup.parse(htmlContent);
			Elements links = doc.select("a[href]");
			for (Element link : links) {
				String href = link.attr("href");
				if (href.endsWith(".csv")) {
					String result = downloadHtmlFromUrl(href);
					List<String[]> csvRows = parseCsvData(result);
					if (!csvRows.isEmpty()) {
						String[] headers = csvRows.get(0);
						Map<String, Integer> headerRow = new HashMap<>();
						for (int i = 0; i < headers.length; i++) {
							headerRow.put(headers[i], i);
						}
						for (int i = 1; i < csvRows.size(); i++) {
							catalogEntries.add(new UhsCatalogEntry(headerRow, csvRows.get(i)));
						}
						break;
					}
				}
			}
		} catch (IOException e) {
			writeErrorLog(e);
			showWarnDialog(null, ServiceHelper.SERVICE_NOT_AVAILABLE.formatted(CATALOG_NAME));
		}

		return catalogEntries;
	}

	public List<CatalogEntry> filterCatalogEntries() {
		return findCatalogEntries().stream().filter(e -> e.getTotalProperMotion() >= tpm).toList();
	}

	@Override
	public String getMotionQueryUrl() {
		return null;
	}

	@Override
	public void setTpm(double tpm) {
		this.tpm = tpm;
	}

	private static String downloadHtmlFromUrl(String url) throws IOException {
		StringBuilder content = new StringBuilder();
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				content.append(line).append("\n");
			}
		} finally {
			connection.disconnect();
		}
		return content.toString();
	}

	public static List<String[]> parseCsvData(String csvData) {
		List<String[]> rows = new ArrayList<>();
		String[] lines = csvData.split("\n");
		boolean isHeader = true;
		for (String line : lines) {
			if (line.trim().isEmpty() || (!isHeader && line.trim().startsWith("#"))) {
				continue;
			}
			if (isHeader) {
				line = line.replaceFirst("#", "");
				isHeader = false;
			}
			String[] rowValues = line.split(",");
			for (int i = 0; i < rowValues.length; i++) {
				rowValues[i] = rowValues[i].trim();
			}
			rows.add(rowValues);
		}
		return rows;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + (int) (this.sourceId ^ (this.sourceId >>> 32));
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
		final UhsCatalogEntry other = (UhsCatalogEntry) obj;
		return this.sourceId == other.sourceId;
	}

	@Override
	public CatalogEntry getInstance(Map<String, Integer> columns, String[] values) {
		return new UhsCatalogEntry(columns, values);
	}

	@Override
	public String getCatalogName() {
		return CATALOG_NAME;
	}

	@Override
	public java.awt.Color getCatalogColor() {
		return JColor.DARK_YELLOW.val;
	}

	@Override
	public String getCatalogQueryUrl() {
		return null;
	}

	@Override
	public String[] getColumnValues() {
		String columnValues = roundTo3DecLZ(getTargetDistance()) + "," + sourceId + "," + roundTo7Dec(ra) + ","
				+ roundTo7Dec(ra_err) + "," + roundTo7Dec(dec) + "," + roundTo7Dec(dec_err) + "," + roundTo3Dec(pmra)
				+ "," + roundTo3Dec(pmra_err) + "," + roundTo3Dec(pmdec) + "," + roundTo3Dec(pmdec_err) + ","
				+ TYPE_TABLE.get(objectType) + "," + roundTo3Dec(epoch) + "," + roundTo3Dec(j_ap3) + ","
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
				object type,\
				epoch,\
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
