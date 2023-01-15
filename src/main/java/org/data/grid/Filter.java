package org.data.grid;

@FunctionalInterface
public interface Filter {

    boolean execute(Data data);

}
