package com.voidaspect.jgol.grid;

import java.util.BitSet;
import java.util.Objects;

public class BitVectorInMemoryGrid extends AbstractFiniteGrid {

    private final BitSet[] grid;

    private final int bits;

    public BitVectorInMemoryGrid(int rows, int cols) {
        super(rows, cols);
        this.grid = new BitSet[rows + 2];
        bits = cols + 2;
    }

    public BitVectorInMemoryGrid(boolean[][] grid, int rows, int columns) {
        this(rows, columns);
        fillGrid(grid);
    }

    @Override
    public boolean get(int row, int col) {
        return grid[++row] != null && grid[row].get(++col);
    }

    @Override
    public void set(int row, int col, boolean state) {
        Objects.checkIndex(row, rows);
        Objects.checkIndex(col, cols);
        BitSet cells;
        if ((cells = grid[++row]) == null) {
            cells = grid[row] = new BitSet(bits);
        }
        cells.set(++col, state);
    }

    @Override
    public int neighbors(int row, int col) {
        row++;
        col++;
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
        return grid[row] != null && grid[row].get(col) ? 1 : 0;
    }

    @Override
    public void forEachAlive(CellOperation operation) {
        for (int row = 1; row <= cols; row++) {
            BitSet cells;
            if ((cells = grid[row]) == null) continue;
            for (int col = cells.nextSetBit(1); col > 0; col = cells.nextSetBit(col + 1)) {
                operation.apply(row - 1, col - 1);
            }
        }
    }

    @Override
    public void forEachAlive(int fromRow, int fromColumn, int toRow, int toCol, CellOperation operation) {
        Objects.checkFromToIndex(fromRow, toRow, rows);
        Objects.checkFromToIndex(fromColumn, toCol, cols);
        fromRow++;
        fromColumn++;
        toRow++;
        toCol++;
        for (int row = fromRow; row < toRow; row++) {
            BitSet cells;
            if ((cells = grid[row]) == null) continue;
            for (int col = cells.nextSetBit(fromColumn); col < toCol && col > 0; col = cells.nextSetBit(col + 1)) {
                operation.apply(row - 1, col - 1);
            }
        }
    }

    @Override
    public void clear() {
        for (int r = 1; r <= rows; r++) {
            BitSet cells;
            if ((cells = grid[r]) != null) {
                cells.clear();
            }
        }
    }
}
