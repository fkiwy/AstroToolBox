package astro.tool.box.catalog;

import astro.tool.box.container.CatalogElement;
import astro.tool.box.container.NumberPair;
import astro.tool.box.enumeration.Alignment;
import static astro.tool.box.function.AstrometricFunctions.calculateAngularDistance;
import static astro.tool.box.function.AstrometricFunctions.calculatePositionFromProperMotion;
import static astro.tool.box.function.NumericFunctions.roundTo3DecNZLZ;
import static astro.tool.box.util.Comparators.getDoubleComparator;
import static astro.tool.box.util.ConversionFactors.DEG_ARCSEC;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MocaCatalogEntry extends GenericCatalogEntry {

    public static final String CATALOG_NAME = "MOCA DB";

    private String sourceId;

    private double searchRadius;

    public MocaCatalogEntry() {
    }

    public MocaCatalogEntry(String[] titles, String[] values) {
        super(titles, values);
    }

    public List<CatalogEntry> findCatalogEntries() throws IOException {
        String url = "jdbc:mysql://104.248.106.21:3306/mocadb";
        String username = "public";
        String password = "z@nUg_2h7_%?31y88";

        double targetRa = getRa();
        double targetDec = getDec();
        double radius = getSearchRadius() / DEG_ARCSEC;

        NumberPair coords;

        coords = calculatePositionFromProperMotion(new NumberPair(targetRa, targetDec), new NumberPair(-radius, 0));
        double rightBoundary = coords.getX();

        coords = calculatePositionFromProperMotion(new NumberPair(targetRa, targetDec), new NumberPair(radius, 0));
        double leftBoundary = coords.getX();

        double bottomBoundary = targetDec - radius;
        double topBoundary = targetDec + radius;

        String query = String.format("SELECT * FROM summary_all_objects WHERE ra BETWEEN %f AND %f AND `dec` BETWEEN %f AND %f", rightBoundary, leftBoundary, bottomBoundary, topBoundary);

        List<CatalogEntry> catalogEntries = new ArrayList();

        try (Connection connection = DriverManager.getConnection(url, username, password); Statement statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(query);) {
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
                MocaCatalogEntry catalogEntry = new MocaCatalogEntry(columnNames.toArray(new String[0]), columnValues.toArray(new String[0]));
                catalogEntry.setRa(resultSet.getDouble("ra"));
                catalogEntry.setDec(resultSet.getDouble("dec"));
                catalogEntry.setSourceId(resultSet.getString("moca_oid"));
                catalogEntries.add(catalogEntry);
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }

        return catalogEntries;
    }

    @Override
    public void loadCatalogElements() {
        super.loadCatalogElements();
        getCatalogElements().add(0, new CatalogElement("dist (arcsec)", roundTo3DecNZLZ(getTargetDistance()), Alignment.RIGHT, getDoubleComparator()));
    }

    @Override
    public String[] getColumnValues() {
        List<String> columnValues = getCatalogElements().stream().map(CatalogElement::getValue).collect(Collectors.toList());
        return columnValues.toArray(new String[0]);
    }

    @Override
    public String[] getColumnTitles() {
        List<String> columnNames = getCatalogElements().stream().map(CatalogElement::getName).collect(Collectors.toList());
        return columnNames.toArray(new String[0]);
    }

    @Override
    public double getTargetDistance() {
        return calculateAngularDistance(new NumberPair(getTargetRa(), getTargetDec()), new NumberPair(getRa(), getDec()), DEG_ARCSEC);
    }

    @Override
    public String getCatalogName() {
        return CATALOG_NAME;
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
