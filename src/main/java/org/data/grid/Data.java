package org.data.grid;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.math.NumberUtils;

public class Data {

    private final Map<String, Integer> columnDict;

    private final List<String> values;

    private final boolean insertMissingValues;

    private final long rowIndex;

    private final Map<Class, TypeConverter> typeConverters;

    public Data(Map<String, Integer> columnDict, List<String> values, boolean insertMissingValues, Map<Class, TypeConverter> typeConverters) {
        this(columnDict, values, insertMissingValues, 0, typeConverters);
    }

    public Data(Map<String, Integer> columnDict, List<String> values, boolean insertMissingValues, long rowIndex, Map<Class, TypeConverter> typeConverters) {
        this.columnDict = columnDict;
        this.values = values;
        this.insertMissingValues = insertMissingValues;
        this.rowIndex = rowIndex;
        this.typeConverters = typeConverters;
    }

    @Override
    public String toString() {
        List<String> list = new ArrayList();
        columnDict.entrySet().forEach(entry -> {
            String label = entry.getKey();
            try {
                String value = values.get(entry.getValue());
                list.add(label + "=" + value);
            } catch (IndexOutOfBoundsException ex) {
            }
        });
        return "Data{" + String.join(";", list) + "}";
    }

    public double get(String columnName) {
        return get(new Column(columnName));
    }

    public String getString(String columnName) {
        return get(new Column(columnName, String.class));
    }

    public float getBoolean(String columnName) {
        return get(new Column(columnName, Boolean.class));
    }

    public String getShort(String columnName) {
        return get(new Column(columnName, Short.class));
    }

    public String getInteger(String columnName) {
        return get(new Column(columnName, Integer.class));
    }

    public String getLong(String columnName) {
        return get(new Column(columnName, Long.class));
    }

    public float getFloat(String columnName) {
        return get(new Column(columnName, Float.class));
    }

    public float getDouble(String columnName) {
        return get(new Column(columnName, Double.class));
    }

    public String getDate(String columnName) {
        return get(new Column(columnName, LocalDate.class));
    }

    public String getTime(String columnName) {
        return get(new Column(columnName, LocalTime.class));
    }

    public String getDateTime(String columnName) {
        return get(new Column(columnName, LocalDateTime.class));
    }

    public String getBigInteger(String columnName) {
        return get(new Column(columnName, BigInteger.class));
    }

    public String getBigDecimal(String columnName) {
        return get(new Column(columnName, BigDecimal.class));
    }

    public <T> T get(Column column) {
        Integer index = getColumnIndex(column.getColumnName(), columnDict);
        String value = values.get(index);
        Class columnType = column.getColumnType();
        if (insertMissingValues && value.isEmpty() && Number.class.isAssignableFrom(columnType)) {
            value = "0";
        }
        if (Number.class.isAssignableFrom(columnType) && !NumberUtils.isCreatable(value)) {
            columnType = String.class;
        }
        return (T) convert(value, columnType);
    }

    public long getRowIndex() {
        return rowIndex;
    }

    private Object convert(String toConvert, Class toClass) {
        if (typeConverters == null) {
            return Double.parseDouble(toConvert);
        }
        TypeConverter<String, Object> converter = typeConverters.get(toClass);
        if (converter == null) {
            throw new ColumnException(String.format("Column type '%s' is not supported.", toClass));
        }
        return converter.convert(toConvert);
    }

    public static int getColumnIndex(String columnName, Map<String, Integer> columnDict) {
        Integer index = columnDict.get(columnName);
        if (index == null) {
            throw new ColumnException(String.format("Column with name '%s' does not exist in data grid.", columnName));
        }
        return index;
    }

}
