package org.data.grid;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.math.NumberUtils;

public class DataGrid {

    private static final Logger LOG = Logger.getLogger(DataGrid.class.getName());

    private static final String LINE_SEP = System.lineSeparator();

    private static final String TEMP_FILE_PREFIX = "DataGrid_";

    private final Map<Class, TypeConverter> inputConverters = new HashMap<>();

    private final Map<Class, TypeConverter> outputConverters = new HashMap<>();

    private String inputFile;

    private String outputFile;

    private boolean logDataErrors;

    private boolean insertMissingValues;

    private int version;

    public DataGrid(String fileName) {
        initConverters();
        initDataGrid(new FileLineReader(fileName));
    }

    public DataGrid(LineReader lineReader) {
        initConverters();
        initDataGrid(lineReader);
    }

    public DataGrid(List... columns) {
        initConverters();
        List<List<String>> grid = new ArrayList();
        for (int i = 0; i < columns[0].size(); i++) {
            List<String> row = new ArrayList();
            for (List column : columns) {
                Object value = column.get(i);
                row.add(convert(value, value.getClass()));
            }
            grid.add(row);
        }
        initDataGrid(new ListLineReader(grid));
    }

    private void initDataGrid(LineReader lineReader) {
        deleteTempFiles();
        createTempFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            List<String> labels = lineReader.readLine();
            if (labels == null) {
                throw new GridException("Data grid is empty.");
            }
            writer.write(String.join(",", labels));
            writer.newLine();
            while (true) {
                List<String> values = lineReader.readLine();
                if (values == null) {
                    break;
                }
                writer.write(String.join(",", values));
                writer.newLine();
            }
            inputFile = outputFile;
        } catch (Exception ex) {
            throw new GridException("An incident occurred while creating the data grid.", ex);
        } finally {
            lineReader.close();
        }
    }

    private void initConverters() {
        // Input converters
        inputConverter(String.class, value -> value);
        inputConverter(Boolean.class, value -> Boolean.parseBoolean(value));
        inputConverter(Short.class, value -> Short.parseShort(value));
        inputConverter(Integer.class, value -> Integer.parseInt(value));
        inputConverter(Long.class, value -> Long.parseLong(value));
        inputConverter(Float.class, value -> Float.parseFloat(value));
        inputConverter(Double.class, value -> Double.parseDouble(value));
        inputConverter(LocalDate.class, value -> LocalDate.parse(value));
        inputConverter(LocalTime.class, value -> LocalTime.parse(value));
        inputConverter(LocalDateTime.class, value -> LocalDateTime.parse(value));
        inputConverter(BigInteger.class, value -> new BigInteger(value));
        inputConverter(BigDecimal.class, value -> new BigDecimal(value));
        // Output converters
        outputConverter(String.class, value -> (String) value);
        outputConverter(Boolean.class, value -> value.toString());
        outputConverter(Short.class, value -> value.toString());
        outputConverter(Integer.class, value -> value.toString());
        outputConverter(Long.class, value -> value.toString());
        outputConverter(Float.class, value -> value.toString());
        outputConverter(Double.class, value -> value.toString());
        outputConverter(LocalDate.class, value -> value.toString());
        outputConverter(LocalTime.class, value -> value.toString());
        outputConverter(LocalDateTime.class, value -> value.toString());
        outputConverter(BigInteger.class, value -> value.toString());
        outputConverter(BigDecimal.class, value -> value.toString());
    }

    public DataGrid inputConverter(Class type, TypeConverter<String, ?> inputConverter) {
        inputConverters.put(type, inputConverter);
        return this;
    }

    public DataGrid outputConverter(Class type, TypeConverter<?, String> outputConverter) {
        outputConverters.put(type, outputConverter);
        return this;
    }

    public DataGrid logDataErrors(boolean logDataErrors) {
        this.logDataErrors = logDataErrors;
        return this;
    }

    public DataGrid insertMissingValues(boolean insertMissingValues) {
        this.insertMissingValues = insertMissingValues;
        return this;
    }

    public Stats getStats(String columnName) {
        return getStats(new Column(columnName));
    }

    public Stats getStats(Column column) {
        List<Double> values = extractColumnValues(column);
        return new Stats(column.getColumnName(), values);
    }

    public <T> List<T> extractColumnValues(String columnName) {
        return extractColumnValues(new Column(columnName));
    }

    public <T> List<T> extractColumnValues(String columnName, boolean withHeader) {
        return extractColumnValues(new Column(columnName), withHeader);
    }

    public <T> List<T> extractColumnValues(Column column) {
        return extractColumnValues(column, false);
    }

    public <T> List<T> extractColumnValues(Column column, boolean withHeader) {
        long rowCount = 0;
        List<T> columnValues = new ArrayList();
        try (LineReader reader = new FileLineReader(inputFile)) {
            List<String> labels = reader.readLine();
            Map<String, Integer> columnDict = createColumnDictionary(labels);
            if (withHeader) {
                columnValues.add((T) column.getColumnName());
            }
            while (true) {
                List<String> values = reader.readLine();
                if (values == null) {
                    break;
                }
                rowCount++;
                Data data = new Data(columnDict, values, insertMissingValues, inputConverters);
                columnValues.add(data.get(column));
            }
        } catch (Exception ex) {
            throw new GridException("An incident occurred while extracting columns at line #" + rowCount + " — " + ex.toString(), ex);
        }
        return columnValues;
    }

    public <T> List<T> extractRowValues(long rowNumber) {
        long rowCount = 0;
        List<T> rowValues = new ArrayList();
        try (LineReader reader = new FileLineReader(inputFile)) {
            List<String> labels = reader.readLine();
            Map<String, Integer> columnDict = createColumnDictionary(labels);
            while (true) {
                List<String> values = reader.readLine();
                if (values == null) {
                    break;
                }
                rowCount++;
                if (rowCount == rowNumber) {
                    Data data = new Data(columnDict, values, insertMissingValues, inputConverters);
                    labels.forEach(label -> {
                        rowValues.add(data.get(new Column(label)));
                    });
                    break;
                }
            }
        } catch (Exception ex) {
            throw new GridException("An incident occurred while extracting rows at line #" + rowCount + " — " + ex.toString(), ex);
        }
        return rowValues;
    }

    public DataGrid appendColumns(List... columns) {
        return insertColumns(Action.APPEND, new Column(null), columns);
    }

    public DataGrid insertColumns(Action action, Column referenceColumn, List... columns) {
        long rowCount = 0;
        createTempFile();
        try (
                LineReader reader = new FileLineReader(inputFile);
                LineWriter writer = new FileLineWriter(outputFile);) {
            List<String> labels = reader.readLine();
            Map<String, Integer> columnDict = createColumnDictionary(labels);
            for (List column : columns) {
                Object label = column.get((int) rowCount);
                insertValue(columnDict, labels, convert(label, label.getClass()), action, referenceColumn);
            }
            writer.writeLine(labels);
            while (true) {
                List<String> values = reader.readLine();
                if (values == null) {
                    break;
                }
                rowCount++;
                for (List column : columns) {
                    Object value = column.get((int) rowCount);
                    insertValue(columnDict, values, convert(value, value.getClass()), action, referenceColumn);
                }
                writer.writeLine(values);
            }
            inputFile = outputFile;
        } catch (Exception ex) {
            throw new GridException("An incident occurred while inserting columns at line #" + rowCount + " — " + ex.toString(), ex);
        }
        return this;
    }

    public DataGrid appendRows(List<List> rows) {
        return this;
    }

    public DataGrid insertRows(Action action, long rowNumber, List<List> rows) {
        return this;
    }

    public <T> List<List<T>> getRows() {
        return null;
    }

    public DataGrid sort(String... columnNames) {
        Sorting[] sortings = new Sorting[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            sortings[i] = new Sorting(columnNames[i], true);
        }
        return sort(sortings);
    }

    public DataGrid sort(Sorting... sorting) {
        createTempFile();
        try (LineReader reader = new FileLineReader(inputFile)) {
            DataConverter converter = new DataConverter();

            // Create column dictionary
            List<String> labels = reader.readLine();
            Map<String, Integer> columnDict = createColumnDictionary(labels);

            // Build sort algorithm
            Comparator sortDir = sorting[0].isAscending() ? Comparator.naturalOrder() : Comparator.reverseOrder();
            Comparator<String> comparator = Comparator.comparing(line -> new Data(columnDict, converter.toList((String) line), insertMissingValues, inputConverters)
                    .get(sorting[0].getColumn()), sortDir
            );
            for (int i = 1; i < sorting.length; i++) {
                Column column = sorting[i].getColumn();
                sortDir = sorting[i].isAscending() ? Comparator.naturalOrder() : Comparator.reverseOrder();
                comparator = comparator.thenComparing(line -> new Data(columnDict, converter.toList((String) line), insertMissingValues, inputConverters)
                        .get(column), sortDir
                );
            }

            // Read and sort lines
            String content = Files.lines(Paths.get(inputFile))
                    .skip(1)
                    .sorted(comparator)
                    .collect(Collectors.joining(LINE_SEP));

            // Write lines
            Files.write(Paths.get(outputFile), (converter.toString(labels) + LINE_SEP + content).getBytes());
            inputFile = outputFile;
        } catch (Exception ex) {
            throw new GridException("An incident occurred while sorting the data.", ex);
        }
        return this;
    }

    public DataGrid excludeColumns(String... columnNames) {
        return excludeColumns(createColumnArray(columnNames));
    }

    public DataGrid excludeColumns(Column... columns) {
        return selectColumns(true, columns);
    }

    public DataGrid excludeColumns(String fromColumn, String toColumn) {
        return selectColumns(true, getColumns(fromColumn, toColumn));
    }

    public DataGrid includeColumns(String... columnNames) {
        return includeColumns(createColumnArray(columnNames));
    }

    public DataGrid includeColumns(Column... columns) {
        return selectColumns(false, columns);
    }

    public DataGrid includeColumns(String fromColumn, String toColumn) {
        return selectColumns(false, getColumns(fromColumn, toColumn));
    }

    private Column[] getColumns(String fromColumn, String toColumn) {
        try (LineReader reader = new FileLineReader(inputFile)) {
            List<String> labels = reader.readLine();
            Map<String, Integer> columnDict = createColumnDictionary(labels);
            Integer fromIndex = Data.getColumnIndex(fromColumn, columnDict);
            Integer toIndex = Data.getColumnIndex(toColumn, columnDict);
            Column[] columns = new Column[toIndex - fromIndex + 1];
            int j = 0;
            for (int i = fromIndex; i < toIndex + 1; i++) {
                columns[j++] = new Column(labels.get(i));
            }
            return columns;
        } catch (Exception ex) {
            throw new GridException("An incident occurred while selecting columns at line #1 — " + ex.toString(), ex);
        }
    }

    private DataGrid selectColumns(boolean exclude, Column... columns) {
        for (Column column : columns) {
            column.setColumnType(String.class);
        }
        long rowCount = 0;
        createTempFile();
        try (
                LineReader reader = new FileLineReader(inputFile);
                LineWriter writer = new FileLineWriter(outputFile);) {
            List<String> labels = reader.readLine();
            Map<String, Integer> columnDict = createColumnDictionary(labels);
            if (exclude) {
                List<Column> retainedColumns = new ArrayList();
                for (String label : labels) {
                    boolean omit = false;
                    for (Column c : columns) {
                        if (c.getColumnName().equals(label)) {
                            omit = true;
                            break;
                        }
                    }
                    if (!omit) {
                        retainedColumns.add(new Column(label));
                    }
                }
                columns = retainedColumns.toArray(new Column[0]);
            }
            labels.clear();
            for (Column column : columns) {
                labels.add(column.getColumnName());
            }
            writer.writeLine(labels);
            while (true) {
                List<String> values = reader.readLine();
                if (values == null) {
                    break;
                }
                rowCount++;
                List<String> selectedValues = new ArrayList();
                for (Column column : columns) {
                    Integer index = Data.getColumnIndex(column.getColumnName(), columnDict);
                    selectedValues.add(values.get(index));
                }
                writer.writeLine(selectedValues);
            }
            inputFile = outputFile;
        } catch (Exception ex) {
            throw new GridException("An incident occurred while selecting columns at line #" + rowCount + " — " + ex.toString(), ex);
        }
        return this;
    }

    public DataGrid excludeRows(long from) {
        return selectRows(true, from, 0);
    }

    public DataGrid excludeRows(long from, long to) {
        return selectRows(true, from, to);
    }

    public DataGrid includeRows(long from) {
        return selectRows(false, from, 0);
    }

    public DataGrid includeRows(long from, long to) {
        return selectRows(false, from, to);
    }

    private DataGrid selectRows(boolean exclude, long from, long to) {
        long rowCount = 0;
        createTempFile();
        try (
                LineReader reader = new FileLineReader(inputFile);
                LineWriter writer = new FileLineWriter(outputFile);) {
            writer.writeLine(reader.readLine());
            while (true) {
                List<String> values = reader.readLine();
                if (values == null) {
                    break;
                }
                rowCount++;
                if (exclude) {
                    if (rowCount < from || (to > 0 && rowCount > to)) {
                        writer.writeLine(values);
                    }
                } else {
                    if (to > 0 && rowCount > to) {
                        break;
                    }
                    if (rowCount >= from) {
                        writer.writeLine(values);
                    }
                }
            }
            inputFile = outputFile;
        } catch (Exception ex) {
            throw new GridException("An incident occurred while selecting rows at line #" + rowCount + " — " + ex.toString(), ex);
        }
        return this;
    }

    public DataGrid filter(Filter filter) {
        long rowCount = 0;
        createTempFile();
        try (
                LineReader reader = new FileLineReader(inputFile);
                LineWriter writer = new FileLineWriter(outputFile);) {
            List<String> labels = reader.readLine();
            Map<String, Integer> columnDict = createColumnDictionary(labels);
            writer.writeLine(labels);
            while (true) {
                List<String> values = reader.readLine();
                if (values == null) {
                    break;
                }
                rowCount++;
                Data data = new Data(columnDict, values, insertMissingValues, rowCount, inputConverters);
                try {
                    if (!filter.execute(data)) {
                        continue;
                    }
                } catch (Exception ex) {
                    logDataError(ex, data);
                    continue;
                }
                writer.writeLine(values);
            }
            inputFile = outputFile;
        } catch (Exception ex) {
            throw new GridException("An incident occurred while filtering the data at line #" + rowCount + " — " + ex.toString(), ex);
        }
        return this;
    }

    public DataGrid process(Function function) {
        return process(function, (Column) null, Action.APPEND, null, new Rounding());
    }

    public DataGrid process(Function function, String resultColumn) {
        return process(function, resultColumn, Action.APPEND, null, new Rounding());
    }

    public DataGrid process(Function function, String resultColumn, Rounding rounding) {
        return process(function, resultColumn, Action.APPEND, null, rounding);
    }

    public DataGrid process(Function function, String resultColumn, Action action, String referenceColumn) {
        return process(function, resultColumn, action, referenceColumn, new Rounding());
    }

    public DataGrid process(Function function, String resultColumn, Action action, String referenceColumn, Rounding rounding) {
        return process(function, new Column(resultColumn), action, new Column(referenceColumn), rounding);
    }

    public DataGrid process(Function function, Column resultColumn) {
        return process(function, resultColumn, Action.APPEND, new Column(null), new Rounding());
    }

    public DataGrid process(Function function, Column resultColumn, Rounding rounding) {
        return process(function, resultColumn, Action.APPEND, new Column(null), rounding);
    }

    public DataGrid process(Function function, Column resultColumn, Action action, Column referenceColumn) {
        return process(function, resultColumn, action, referenceColumn, new Rounding());
    }

    public DataGrid process(Function function, Column resultColumn, Action action, Column referenceColumn, Rounding rounding) {
        long rowCount = 0;
        createTempFile();
        try (
                LineReader reader = new FileLineReader(inputFile);
                LineWriter writer = new FileLineWriter(outputFile);) {
            List<String> labels = reader.readLine();
            Map<String, Integer> columnDict = createColumnDictionary(labels);
            if (resultColumn != null) {
                insertValue(columnDict, labels, resultColumn.getColumnName(), action, referenceColumn);
            }
            writer.writeLine(labels);
            while (true) {
                List<String> values = reader.readLine();
                if (values == null) {
                    break;
                }
                rowCount++;
                Data data = new Data(columnDict, values, insertMissingValues, rowCount, inputConverters);
                String value = "";
                Object result;
                try {
                    result = function.execute(data);
                    Class type = result.getClass();
                    if (result == null) {
                        continue;
                    } else if (result instanceof Number) {
                        value = convert(round((Number) result, rounding), type);
                    } else {
                        value = convert(result, type);
                    }
                } catch (ColumnException ex) {
                    throw ex;
                } catch (Exception ex) {
                    logDataError(ex, data);
                }
                if (resultColumn != null) {
                    insertValue(columnDict, values, value, action, referenceColumn);
                }
                writer.writeLine(values);
            }
            inputFile = outputFile;
        } catch (Exception ex) {
            throw new GridException("An incident occurred while processing the data at line #" + rowCount + " — " + ex.toString(), ex);
        }
        return this;
    }

    public DataGrid persist(String fileName) {
        return persist(new FileLineWriter(fileName));
    }

    public DataGrid persist(LineWriter writer) {
        try (LineReader reader = new FileLineReader(inputFile)) {
            while (true) {
                List<String> values = reader.readLine();
                if (values == null) {
                    break;
                }
                writer.writeLine(values);
            }
            inputFile = outputFile;
        } catch (Exception ex) {
            throw new GridException("An incident occurred while persisting the data.", ex);
        } finally {
            writer.close();
        }
        return this;
    }

    public DataGrid printRows() {
        return printRows(0, 0, false);
    }

    public DataGrid printRows(boolean withRowNumbers) {
        return printRows(0, 0, withRowNumbers);
    }

    public DataGrid printRows(int rows) {
        return printRows(1, rows, false);
    }

    public DataGrid printRows(int rows, boolean withRowNumbers) {
        return printRows(1, rows, withRowNumbers);
    }

    public DataGrid printRows(long from, long to) {
        return printRows(from, to, false);
    }

    public DataGrid printRows(long from, long to, boolean withRowNumbers) {
        if (from > 0 || to > 0) {
            includeRows(from, to);
        }
        List<String> cols = Collections.EMPTY_LIST;
        Map<Integer, Integer> maxLengths = new HashMap();
        createTempFile();
        try (
                LineReader reader = new FileLineReader(inputFile);
                LineWriter writer = new FileLineWriter(outputFile);) {
            int rows = 0;
            while (true) {
                List<String> values = reader.readLine();
                if (values == null) {
                    break;
                }
                if (withRowNumbers) {
                    if (rows == 0) {
                        values.add(0, String.valueOf("Row#"));
                    } else {
                        values.add(0, String.valueOf(rows));
                    }
                    rows++;
                }
                cols = values;
                for (int i = 0; i < values.size(); i++) {
                    String value = values.get(i);
                    int length = value.length();
                    Integer maxLength = maxLengths.get(i);
                    if (maxLength == null) {
                        maxLengths.put(i, length);
                    } else {
                        maxLengths.put(i, length > maxLength ? length : maxLength);
                    }
                }
                writer.writeLine(values);
            }
            inputFile = outputFile;
        } catch (Exception ex) {
            throw new GridException("An incident occurred while printing the data.", ex);
        }
        try (LineReader reader = new FileLineReader(inputFile)) {
            List<String> values = reader.readLine();

            // Header
            StringBuilder format = new StringBuilder();
            for (int i = 0; i < cols.size(); i++) {
                Integer maxLength = maxLengths.get(i);
                format.append("%-").append(maxLength).append("s ");
            }
            System.out.println(String.format(format.toString(), values.toArray()));

            // Header underline
            format = new StringBuilder();
            for (int i = 0; i < values.size(); i++) {
                Integer maxLength = maxLengths.get(i);
                for (int j = 0; j < maxLength; j++) {
                    format.append("-");
                }
                format.append(" ");
            }
            System.out.println(String.format(format.toString(), values.toArray()));

            // Rows
            format = new StringBuilder();
            for (int i = 0; i < cols.size(); i++) {
                String value = cols.get(i);
                Integer maxLength = maxLengths.get(i);
                if (NumberUtils.isCreatable(value)) {
                    format.append("%");
                } else {
                    format.append("%-");
                }
                format.append(maxLength).append("s ");
            }
            while (true) {
                values = reader.readLine();
                if (values == null) {
                    break;
                }
                System.out.println(String.format(format.toString(), values.toArray()));
            }
        } catch (Exception ex) {
            throw new GridException("An incident occurred while printing the data.", ex);
        }
        return this;
    }

    public DataGrid printGridStats() {
        long rowCount = 0;
        long fileSize = 0;
        List<String> cols = Collections.EMPTY_LIST;
        try (LineReader reader = new FileLineReader(inputFile)) {
            fileSize = Files.size(Paths.get(inputFile));
            while (true) {
                List<String> values = reader.readLine();
                if (values == null) {
                    break;
                }
                rowCount++;
                cols = values;
            }
        } catch (Exception ex) {
            throw new GridException("An incident occurred while printing stats at line #" + rowCount + " — " + ex.toString(), ex);
        }
        System.out.println("==================================================");
        System.out.println("Data grid statistics");
        System.out.println("--------------------------------------------------");
        System.out.println(String.format("File size ...................... : %,d KB", fileSize / 1000));
        System.out.println(String.format("Number of columns .............. : %,d", cols.size()));
        System.out.println(String.format("Number of rows (header included) : %,d", rowCount));
        System.out.println("==================================================");
        return this;
    }

    public DataGrid joinWith(String fileName, String fileIdColumn, String gridIdColumn) {
        return joinWith(new FileLineReader(fileName), fileIdColumn, gridIdColumn);
    }

    public DataGrid joinWith(LineReader lineReader, String fileIdColumn, String gridIdColumn) {
        Map<String, List<String>> rows = new HashMap();
        List<String> labelsToMerge;
        version++;
        try {
            labelsToMerge = lineReader.readLine();
            for (int i = 0; i < labelsToMerge.size(); i++) {
                String label = labelsToMerge.get(i);
                labelsToMerge.set(i, label + "_" + version);
            }
            fileIdColumn = fileIdColumn + "_" + version;
            Map<String, Integer> columnDict = createColumnDictionary(labelsToMerge);
            while (true) {
                List<String> values = lineReader.readLine();
                if (values == null) {
                    break;
                }
                Data data = new Data(columnDict, values, insertMissingValues, inputConverters);
                rows.put(data.getString(fileIdColumn), values);
            }
        } catch (Exception ex) {
            throw new GridException("An incident occurred while merging a table. — " + ex.toString(), ex);
        } finally {
            lineReader.close();
        }
        createTempFile();
        try (
                LineReader reader = new FileLineReader(inputFile);
                LineWriter writer = new FileLineWriter(outputFile);) {
            List<String> labels = reader.readLine();
            Map<String, Integer> columnDict = createColumnDictionary(labels);
            labels.addAll(labelsToMerge);
            writer.writeLine(labels);
            while (true) {
                List<String> values = reader.readLine();
                if (values == null) {
                    break;
                }
                Data data = new Data(columnDict, values, insertMissingValues, inputConverters);
                List<String> valuesToMerge = rows.get(data.getString(gridIdColumn));
                if (valuesToMerge == null) {
                    valuesToMerge = new ArrayList();
                    int i = 0;
                    while (i < labelsToMerge.size()) {
                        valuesToMerge.add("");
                        i++;
                    }
                } else {
                    values.addAll(valuesToMerge);
                }
                writer.writeLine(values);
            }
            inputFile = outputFile;
        } catch (Exception ex) {
            throw new GridException("An incident occurred while merging a table. — " + ex.toString(), ex);
        }
        return this;
    }

    private void insertValue(Map<String, Integer> columnDict, List<String> values, String value, Action action, Column referenceColumn) {
        String columnName = referenceColumn.getColumnName();
        if (columnName == null) {
            values.add(value);
            return;
        }
        Integer index = columnDict.get(columnName);
        switch (action) {
            case INSERT_BEFORE:
                values.add(index, value);
                break;
            case APPEND:
            case INSERT_AFTER:
                values.add(index + 1, value);
                break;
            case REPLACE:
                values.set(index, value);
                break;
        }
    }

    private void logDataError(Exception ex, Data data) {
        if (logDataErrors) {
            String message = ex.toString() + " — " + data.toString();
            LOG.log(Level.INFO, message);
        }
    }

    private double round(Number value, Rounding rounding) {
        BigDecimal decimal = BigDecimal.valueOf(value.doubleValue());
        decimal = decimal.setScale(rounding.getDecimals(), rounding.getRoundingMode());
        return decimal.doubleValue();
    }

    private Map<String, Integer> createColumnDictionary(List<String> columnNames) {
        Map<String, Integer> columns = new LinkedHashMap();
        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = columnNames.get(i);
            if (columns.containsKey(columnName)) {
                throw new GridException(String.format("Column names must be unique. Column name '%s' exists twice.", columnName));
            }
            columns.put(columnName, i);
        }
        return columns;
    }

    private void createTempFile() {
        try {
            outputFile = File.createTempFile(TEMP_FILE_PREFIX, null).getPath();
        } catch (IOException ex) {
            throw new GridException("Temporary file could not be created.", ex);
        }
    }

    private void deleteTempFiles() {
        String tmpdir = System.getProperty("java.io.tmpdir");
        File[] files = new File(tmpdir).
                listFiles(f -> f.getName().startsWith(TEMP_FILE_PREFIX));
        for (File file : files) {
            file.delete();
        }
    }

    private Column[] createColumnArray(String... columnNames) {
        Column[] columns = new Column[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            columns[i] = new Column(columnNames[i], String.class);
        }
        return columns;
    }

    private String convert(Object toConvert, Class fromClass) {
        TypeConverter<Object, String> converter = outputConverters.get(fromClass);
        if (converter == null) {
            throw new ColumnException(String.format("Column type '%s' is not supported.", fromClass));
        }
        return converter.convert(toConvert);
    }

}
