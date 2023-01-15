package org.data.grid;

@FunctionalInterface
public interface TypeConverter<From, To> {

    To convert(From toConvert);

}
