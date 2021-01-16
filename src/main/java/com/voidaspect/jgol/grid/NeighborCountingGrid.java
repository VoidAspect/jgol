package com.voidaspect.jgol.grid;

import java.util.Arrays;

public class NeighborCountingGrid extends AbstractFiniteGrid {

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
    public boolean get(int row, int col) {
        return (grid[row][col] & ALIVE_MASK) != 0;
    }

    @Override
    public void set(int row, int col, boolean state) {
        var thisRow = grid[row];

        boolean alive = (thisRow[col] & ALIVE_MASK) != 0;

        if (alive == state) return;

        thisRow[col] ^= ALIVE_MASK;

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

        int update = state ? ALIVE_NEIGHBOR : -ALIVE_NEIGHBOR;

        // up row
        if (notTopEdge) {
            var upRow = grid[up];

            if (notLeftEdge) upRow[left] += update;
            upRow[col] += update;
            if (notRightEdge) upRow[right] += update;
        }
        // middle row
        if (notLeftEdge) thisRow[left] += update;
        if (notRightEdge) thisRow[right] += update;
        // down row
        if (notDownEdge) {
            var downRow = grid[down];

            if (notLeftEdge) downRow[left] += update;
            downRow[col] += update;
            if (notRightEdge) downRow[right] += update;
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

}
