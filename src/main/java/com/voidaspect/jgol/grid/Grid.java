package com.voidaspect.jgol.grid;

import com.voidaspect.jgol.grid.cell.CellOperation;

public interface Grid {

    boolean get(int row, int col);

    void set(int row, int col, boolean state);

    int neighbors(int row, int col);

    boolean[][] snapshot(int fromRow, int fromColumn, int rows, int columns);

    void clear();

    long liveCells();

    void forEachAlive(CellOperation operation);

}
