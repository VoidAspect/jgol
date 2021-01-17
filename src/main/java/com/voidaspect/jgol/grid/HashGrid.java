package com.voidaspect.jgol.grid;

import java.util.LinkedHashSet;
import java.util.Objects;

import static com.voidaspect.jgol.grid.Cells.*;

public final class HashGrid extends AbstractFiniteGrid {

    private final LinkedHashSet<Long> cells;

    public HashGrid(int rows, int cols) {
        super(rows, cols);
        this.cells = new LinkedHashSet<>();
    }

    public HashGrid(boolean[][] grid, int rows, int columns) {
        this(rows, columns);
        fillGrid(grid);
    }

    @Override
    public boolean get(int row, int col) {
        Objects.checkIndex(row, rows);
        Objects.checkIndex(col, cols);
        return cells.contains(pack(row, col));
    }

    @Override
    public void set(int row, int col, boolean state) {
        Objects.checkIndex(row, rows);
        Objects.checkIndex(col, cols);

        long cell = pack(row, col);

        if (state) {
            cells.add(cell);
        } else {
            cells.remove(cell);
        }
    }

    @Override
    public int neighbors(int row, int col) {
        Objects.checkIndex(row, rows);
        Objects.checkIndex(col, cols);
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
        return hasCell(row, col) && cells.contains(pack(row, col)) ? 1 : 0;
    }

    @Override
    public void clear() {
        cells.clear();
    }

    @Override
    public void forEachAlive(CellOperation operation) {
        cells.forEach(cell -> operation.apply(unpackRow(cell), unpackCol(cell)));
    }

}
