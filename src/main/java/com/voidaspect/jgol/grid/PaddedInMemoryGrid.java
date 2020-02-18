package com.voidaspect.jgol.grid;

import java.util.Arrays;
import java.util.Objects;

public final class PaddedInMemoryGrid implements Grid {

    public static final int MIN_SIZE = 1;

    private static final int PADDING = 1;

    private final boolean[][] grid;

    private final int rows;

    private final int columns;

    private final int upperRowBound;

    private final int upperColBound;

    private final long size;

    public PaddedInMemoryGrid(int rows, int columns) {
        this(null, rows, columns);
    }

    public PaddedInMemoryGrid(boolean[][] grid, int rows, int columns) {
        this.grid = buildGrid(grid, rows, columns);
        this.rows = rows;
        this.columns = columns;
        this.size = (long) rows * columns;
        this.upperRowBound = rows + PADDING;
        this.upperColBound = columns + PADDING;
    }

    @Override
    public boolean get(int row, int col) {
        // don't perform range checks, edges are always false
        return grid[row + PADDING][col + PADDING];
    }

    @Override
    public void set(int row, int col, boolean state) {
        Objects.checkIndex(row, rows);
        Objects.checkIndex(col, columns);
        grid[row + PADDING][col + PADDING] = state;
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
    public boolean[][] snapshot() {
        boolean[][] snapshot = new boolean[rows][];
        for (int i = 0; i < rows; i++) {
            boolean[] row = grid[i + PADDING];
            snapshot[i] = Arrays.copyOfRange(row, PADDING, upperColBound);
        }
        return snapshot;
    }

    @Override
    public boolean[][] snapshot(int fromRow, int fromColumn, int rows, int columns) {
        Objects.checkFromIndexSize(fromRow, rows, this.rows);
        Objects.checkFromIndexSize(fromColumn, columns, this.columns);
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

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public int getColumns() {
        return columns;
    }

    @Override
    public long getSize() {
        return size;
    }

    private int value(int row, int col) {
        return grid[row][col] ? 1 : 0;
    }


    private static boolean[][] buildGrid(boolean[][] initial, int rows, int columns) {
        boolean[][] grid = initGrid(rows, columns);
        if (initial == null) return grid;
        int rowsLength = Math.min(rows, initial.length);
        for (int i = 0; i < rowsLength; i++) {
            boolean[] row = initial[i];
            if (row == null) continue;
            int columnLength = Math.min(columns, row.length);
            System.arraycopy(row, 0, grid[i + PADDING], PADDING, columnLength);
        }
        return grid;
    }

    private static boolean[][] initGrid(int rows, int columns) {
        if (rows < MIN_SIZE) {
            throw new IllegalArgumentException("Number of rows expected >= 1, got " + rows);
        }
        if (columns < MIN_SIZE) {
            throw new IllegalArgumentException("Number of columns expected >= 1, got " + rows);
        }
        // pad matrix from all sides to avoid range checks on neighbor calculation
        return new boolean[rows + 2][columns + 2];
    }

}
