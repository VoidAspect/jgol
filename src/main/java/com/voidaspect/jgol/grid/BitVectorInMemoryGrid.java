package com.voidaspect.jgol.grid;

import com.voidaspect.jgol.grid.cell.CellOperation;

import java.util.BitSet;

public final class BitVectorInMemoryGrid extends AbstractFiniteGrid {

    private final BitSet[] grid;

    private final int bits;

    private long liveCells;

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
        checkIndex(row, col);
        return grid[++row] != null && grid[row].get(++col);
    }

    @Override
    public void set(int row, int col, boolean state) {
        checkIndex(row, col);
        BitSet cells;
        if ((cells = grid[++row]) == null) {
            cells = grid[row] = new BitSet(bits);
        }
        if (cells.get(++col) != state) {
            cells.set(col, state);
            liveCells += state ? 1 : -1;
        }
    }

    @Override
    public int neighbors(int row, int col) {
        checkIndex(row, col);
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
        BitSet cells;
        return (cells = grid[row]) != null && cells.get(col) ? 1 : 0;
    }

    @Override
    public void forEachAlive(CellOperation operation) {
        long remaining = liveCells;
        if (remaining == 0) return;
        int fromRow = 1;
        int fromColumn = 1;
        for (int row = fromRow; row <= rows; row++) {
            BitSet cells;
            if ((cells = grid[row]) == null || cells.isEmpty()) continue;
            for (int col = cells.nextSetBit(fromColumn); col <= cols && col > 0; col = cells.nextSetBit(col + 1)) {
                operation.apply(row - 1, col - 1);
                if (--remaining == 0) return;
            }
        }
    }

    @Override
    protected boolean[][] snapshotWithoutBoundChecking(int fromRow, int fromColumn, int rows, int columns) {
        fromRow++;
        fromColumn++;
        int toRow = fromRow + rows;
        int toCol = fromColumn + columns;

        boolean[][] snapshot = new boolean[rows][];

        for (int ri = fromRow; ri < toRow; ri++) {
            boolean[] row = snapshot[ri - fromRow] = new boolean[columns];
            BitSet cells;
            if ((cells = grid[ri]) == null || cells.isEmpty()) continue;
            for (int ci = cells.nextSetBit(fromColumn); ci < toCol && ci > 0; ci = cells.nextSetBit(ci + 1)) {
                row[ci - fromColumn] = true;
            }
        }

        return snapshot;
    }

    @Override
    public void clear() {
        for (int r = 1; r <= rows; r++) {
            BitSet cells;
            if ((cells = grid[r]) != null) {
                cells.clear();
            }
        }
        liveCells = 0;
    }

    @Override
    public long liveCells() {
        return liveCells;
    }
}
