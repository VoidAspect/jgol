package com.voidaspect.jgol.grid;

class NeighborCountingGridTest extends GridTest {

    @Override
    protected Grid grid(int rows, int cols) {
        return new NeighborCountingGrid(rows, cols);
    }

    @Override
    protected Grid grid(boolean[][] initial, int rows, int cols) {
        return new NeighborCountingGrid(initial, rows, cols);
    }

}