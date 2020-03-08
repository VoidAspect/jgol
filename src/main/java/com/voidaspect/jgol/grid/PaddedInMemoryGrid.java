package com.voidaspect.jgol.grid;

import java.util.Arrays;
import java.util.Objects;

public final class PaddedInMemoryGrid extends AbstractGrid {

    private static final int PADDING = 1;

    private final boolean[][] grid;

    private final int upperRowBound;

    private final int upperColBound;

    public PaddedInMemoryGrid(int rows, int columns) {
        super(rows, columns);
        // pad matrix from all sides to avoid range checks on neighbor calculation
        this.grid = new boolean[PADDING + rows + PADDING][PADDING + columns + PADDING];
        this.upperRowBound = rows + PADDING;
        this.upperColBound = columns + PADDING;
    }

    public PaddedInMemoryGrid(boolean[][] grid, int rows, int columns) {
        this(rows, columns);
        fillGrid(grid);
    }

    @Override
    public boolean get(int row, int col) {
        // don't perform range checks, edges are always false
        return grid[PADDING + row][PADDING + col];
    }

    @Override
    public void set(int row, int col, boolean state) {
        Objects.checkIndex(row, rows);
        Objects.checkIndex(col, cols);
        grid[PADDING + row][PADDING + col] = state;
    }

    @Override
    public int neighbors(int row, int col) {
        row += PADDING;
        col += PADDING;
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

    @Override
    protected boolean[][] snapshotWithoutBoundChecking(int fromRow, int fromColumn, int rows, int columns) {
        boolean[][] snapshot = new boolean[rows][];
        fromColumn += PADDING;
        fromRow += PADDING;
        int toColumn = fromColumn + columns;
        for (int i = 0; i < rows; i++) {
            boolean[] row = grid[i + fromRow];
            snapshot[i] = Arrays.copyOfRange(row, fromColumn, toColumn);
        }
        return snapshot;
    }

    @Override
    public void clear() {
        var empty = grid[0];
        for (int i = PADDING; i < upperRowBound; i++) {
            System.arraycopy(empty, PADDING, grid[i], PADDING, upperColBound);
        }
    }

    private int value(int row, int col) {
        return grid[row][col] ? 1 : 0;
    }

    @Override
    protected void fillGrid(boolean[][] initial) {
        if (initial == null) return;
        int rowsLength = Math.min(rows, initial.length);
        for (int i = 0; i < rowsLength; i++) {
            boolean[] row = initial[i];
            if (row == null) continue;
            int columnLength = Math.min(cols, row.length);
            System.arraycopy(row, 0, grid[PADDING + i], PADDING, columnLength);
        }
    }

}
