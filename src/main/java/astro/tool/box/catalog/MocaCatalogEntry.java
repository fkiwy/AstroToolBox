package astro.tool.box.catalog;

import static astro.tool.box.function.AstrometricFunctions.calculateAngularDistance;
import static astro.tool.box.function.NumericFunctions.roundTo3DecNZLZ;
import static astro.tool.box.main.ToolboxHelper.showWarnDialog;
import static astro.tool.box.main.ToolboxHelper.writeErrorLog;
import static astro.tool.box.util.Comparators.getDoubleComparator;
import static astro.tool.box.util.ConversionFactors.DEG_ARCSEC;

import java.awt.Color;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Alignment;
import astro.tool.box.enumeration.JColor;
import astro.tool.box.util.ServiceHelper;

public class MocaCatalogEntry extends GenericCatalogEntry {

	public static final String CATALOG_NAME = "MOCA DB";

	private String sourceId;

	private double searchRadius;

	public MocaCatalogEntry() {
	}

	public MocaCatalogEntry(String[] titles, String[] values) {
		super(titles, values);
	}

	public List<CatalogEntry> findCatalogEntries() {
		String url = "jdbc:mysql://104.248.106.21:3306/mocadb";
		String username = "public";
		String password = "z@nUg_2h7_%?31y88";

		double ra = getRa();
		double dec = getDec();
		double radius = getSearchRadius() / DEG_ARCSEC;

		String query = "SELECT * FROM summary_all_objects o LEFT JOIN moca_associations a ON o.moca_aid = a.moca_aid LEFT JOIN moca_membership_types m ON o.moca_mtid = m.moca_mtid WHERE ACOS(SIN(RADIANS(`dec`)) * SIN(RADIANS(%f)) + COS(RADIANS(`dec`)) * COS(RADIANS(%f)) * COS(RADIANS(ra - %f))) * 180 / PI() <= %f"
				.formatted(dec, dec, ra, radius);

		List<CatalogEntry> catalogEntries = new ArrayList();

		try (Connection connection = DriverManager.getConnection(url, username, password);
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(query);) {
			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();
			List<String> columnNames = new ArrayList();
			for (int i = 1; i <= columnCount; i++) {
				columnNames.add(metaData.getColumnName(i));
			}
			while (resultSet.next()) {
				List<String> columnValues = new ArrayList();
				for (String columnName : columnNames) {
					columnValues.add(resultSet.getString(columnName));
				}
				MocaCatalogEntry catalogEntry = new MocaCatalogEntry(columnNames.toArray(String[]::new),
						columnValues.toArray(String[]::new));
				catalogEntry.setRa(resultSet.getDouble("ra"));
				catalogEntry.setDec(resultSet.getDouble("dec"));
				catalogEntry.setSourceId(resultSet.getString("moca_oid"));
				catalogEntries.add(catalogEntry);
			}
		} catch (SQLException e) {
			writeErrorLog(e);
			showWarnDialog(null, ServiceHelper.SERVICE_NOT_AVAILABLE.formatted("MOCA database"));
		}

		return catalogEntries;
	}

	@Override
	public void loadCatalogElements() {
		super.loadCatalogElements();
		getCatalogElements().add(0, new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()),
				Alignment.RIGHT, getDoubleComparator()));
	}

	@Override
	public String[] getColumnValues() {
		List<String> columnValues = getCatalogElements().stream().map(CatalogElement::getValue)
				.collect(Collectors.toList());
		return columnValues.toArray(String[]::new);
	}

	@Override
	public String[] getColumnTitles() {
		List<String> columnNames = getCatalogElements().stream().map(CatalogElement::getName)
				.collect(Collectors.toList());
		return columnNames.toArray(String[]::new);
	}

	@Override
	public double getTargetDistance() {
		return calculateAngularDistance(new NumberPair(getTargetRa(), getTargetDec()),
				new NumberPair(getRa(), getDec()), DEG_ARCSEC);
	}

	@Override
	public String getCatalogName() {
		return CATALOG_NAME;
	}

	@Override
	public Color getCatalogColor() {
		return JColor.DARK_ORANGE.val;
	}

	@Override
	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	@Override
	public double getSearchRadius() {
		return searchRadius;
	}

	@Override
	public void setSearchRadius(double searchRadius) {
		this.searchRadius = searchRadius;
	}

}
