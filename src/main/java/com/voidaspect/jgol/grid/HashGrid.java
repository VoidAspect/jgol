package com.voidaspect.jgol.grid;

import com.voidaspect.jgol.grid.cell.CellOperation;
import com.voidaspect.jgol.grid.cell.LinkedCellSet;

public final class HashGrid extends AbstractFiniteGrid {

    private final LinkedCellSet cells;

    public HashGrid(int rows, int cols) {
        super(rows, cols);
        this.cells = new LinkedCellSet();
    }

    public HashGrid(boolean[][] grid, int rows, int columns) {
        this(rows, columns);
        fillGrid(grid);
    }

    @Override
    public boolean get(int row, int col) {
        checkIndex(row, col);
        return cells.contains(row, col);
    }

    @Override
    public void set(int row, int col, boolean state) {
        checkIndex(row, col);
        if (state) {
            cells.add(row, col);
        } else {
            cells.remove(row, col);
        }
    }

    @Override
    public int neighbors(int row, int col) {
        checkIndex(row, col);
        //@formatter:off
        int up    = row - 1;
        int down  = row + 1;
        int left  = col - 1;
        int right = col + 1;
        return value(up,   right) + value(up,   col) + value(up,   left) +
               value(row,  right) + /* this cell */  + value(row,  left) +
               value(down, right) + value(down, col) + value(down, left);
        //@formatter:on
    }

    private int value(int row, int col) {
        return hasCell(row, col) && cells.contains(row, col) ? 1 : 0;
    }

    @Override
    public void clear() {
        cells.clear();
    }

    @Override
    public long liveCells() {
        return cells.size();
    }

    @Override
    public void forEachAlive(CellOperation operation) {
        cells.forEach(operation);
    }

}
