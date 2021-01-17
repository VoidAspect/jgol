package com.voidaspect.jgol.grid.cell;

@FunctionalInterface
public interface CellOperation {

    void apply(int row, int col);

}
