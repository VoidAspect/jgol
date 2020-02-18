package com.voidaspect.jgol.grid;

import java.util.Arrays;

public class PaddedInMemoryGrid implements Grid {

    public static final int MIN_SIZE = 1;

    private static final int PADDING = 1;

    private final boolean[][] grid;

    private final int rows;

    private final int columns;

    private final int upperRowBound;

    private final int upperColBound;

    public PaddedInMemoryGrid(int rows, int columns) {
        this(null, rows, columns);
    }

    public PaddedInMemoryGrid(boolean[][] grid, int rows, int columns) {
        this.grid = buildGrid(grid, rows, columns);
        this.rows = rows;
        this.columns = columns;
        this.upperRowBound = rows + 1;
        this.upperColBound = columns + 1;
    }

    @Override
    public boolean get(int row, int col) {
        return grid[row + 1][col + 1];
    }

    @Override
    public void set(int row, int col, boolean state) {
        grid[row + 1][col + 1] = state;
    }

    @Override
    public int neighbors(int row, int col) {
        //@formatter:off
        int up    = row++;
        int down  = row+1;
        int left  = col++;
        int right = col+1;
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
        return (long) columns * rows;
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
            System.arraycopy(row, 0, grid[i + 1], PADDING, columnLength);
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
