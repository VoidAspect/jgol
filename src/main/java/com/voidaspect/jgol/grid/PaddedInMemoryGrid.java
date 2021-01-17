package com.voidaspect.jgol.grid;

import java.util.Arrays;

public final class PaddedInMemoryGrid extends AbstractFiniteGrid {

    private static final int PADDING = 1;

    private final boolean[][] grid;

    private final int upperRowBound;

    private final int upperColBound;

    private long liveCells;

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
    protected void fillGrid(boolean[][] initial) {
        if (initial == null) return;
        int rowsLength = Math.min(rows, initial.length);
        for (int i = 0; i < rowsLength; i++) {
            boolean[] row = initial[i];
            if (row == null) continue;
            int columnLength = Math.min(cols, row.length);
            for (int j = 0; j < columnLength; j++) {
                if (row[j]) {
                    liveCells++;
                    grid[i + PADDING][j + PADDING] = true;
                }
            }
        }
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
    public boolean get(int row, int col) {
        checkIndex(row, col);
        return grid[PADDING + row][PADDING + col];
    }

    @Override
    public void set(int row, int col, boolean state) {
        checkIndex(row, col);
        row += PADDING;
        col += PADDING;
        if (grid[row][col] != state) {
            grid[row][col] = state;
            liveCells += state ? 1 : -1;
        }
    }

    @Override
    public int neighbors(int row, int col) {
        checkIndex(row, col);
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
    public void clear() {
        var empty = grid[0];
        for (int i = PADDING; i < upperRowBound; i++) {
            System.arraycopy(empty, PADDING, grid[i], PADDING, upperColBound);
        }
        liveCells = 0;
    }

    @Override
    public long liveCells() {
        return liveCells;
    }

    private static final byte ALIVE = 1;

    private static final byte DEAD = 0;

    private byte value(int row, int col) {
        return grid[row][col] ? ALIVE : DEAD;
    }

}
