package com.voidaspect.jgol.grid;

@FunctionalInterface
public interface CellOperation {

    void apply(int row, int col);

}
