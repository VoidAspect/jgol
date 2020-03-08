package com.voidaspect.jgol.grid;

import java.util.Arrays;

public class NeighborCountingGrid extends AbstractGrid {

    private static final byte ALIVE_MASK = 1;

    private static final byte ALIVE_NEIGHBOR = 2;

    private final byte[][] grid;

    public NeighborCountingGrid(int rows, int cols) {
        super(rows, cols);
        grid = new byte[rows][cols];
    }

    public NeighborCountingGrid(boolean[][] initial, int rows, int cols) {
        this(rows, cols);
        fillGrid(initial);

    }

    @Override
    protected boolean[][] snapshotWithoutBoundChecking(int fromRow, int fromColumn, int rows, int columns) {
        var snapshot = new boolean[rows][columns];
        for (int row = fromRow; row < rows; row++) {
            boolean[] snapshotRow = snapshot[row - fromRow];
            for (int col = fromColumn; col < columns; col++) {
                snapshotRow[col - fromColumn] = get(row, col);
            }
        }
        return snapshot;
    }

    @Override
    public boolean get(int row, int col) {
        return (grid[row][col] & ALIVE_MASK) != 0;
    }

    @Override
    public void set(int row, int col, boolean state) {
        if (get(row, col) == state) return;

        if (state) {
            grid[row][col] |= ALIVE_MASK;
        } else {
            grid[row][col] ^= ALIVE_MASK;
        }

        CellOperation operation = state
                ? (r, c) -> grid[r][c] += ALIVE_NEIGHBOR
                : (r, c) -> grid[r][c] -= ALIVE_NEIGHBOR;

        //@formatter:off
        int up    = row - 1;
        int down  = row + 1;
        int left  = col - 1;
        int right = col + 1;

        boolean notLeftEdge  = left >= 0;
        boolean notRightEdge = right < cols;
        boolean notTopEdge   = up >= 0;
        boolean notDownEdge  = down < rows;
        //@formatter:on

        // up row
        if (notTopEdge) {
            if (notLeftEdge) operation.apply(up, left);
            operation.apply(up, col);
            if (notRightEdge) operation.apply(up, right);
        }
        // middle row
        if (notLeftEdge) operation.apply(row, left);
        if (notRightEdge) operation.apply(row, right);
        // down row
        if (notDownEdge) {
            if (notLeftEdge) operation.apply(down, left);
            operation.apply(down, col);
            if (notRightEdge) operation.apply(down, right);
        }
    }

    @Override
    public int neighbors(int row, int col) {
        return grid[row][col] >>> 1;
    }

    @Override
    public void clear() {
        byte[] first = grid[0];
        Arrays.fill(first, (byte) 0);
        for (int row = 1; row < rows; row++) {
            System.arraycopy(first, 0, grid[row], 0, cols);
        }
    }

    private void fillGrid(boolean[][] initial) {
        if (initial == null) return;
        int rowsLength = Math.min(rows, initial.length);
        for (int r = 0; r < rowsLength; r++) {
            boolean[] row = initial[r];
            if (row == null) continue;
            int columnLength = Math.min(cols, row.length);
            for (int c = 0; c < columnLength; c++) {
                set(r, c, initial[r][c]);
            }
        }
    }
}
