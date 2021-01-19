package com.voidaspect.jgol.grid;

class NeighborCountingGridTest extends FiniteGridTest {

    @Override
    protected FiniteGrid grid(int rows, int cols) {
        return new NeighborCountingGrid(rows, cols);
    }

    @Override
    protected FiniteGrid grid(boolean[][] initial, int rows, int cols) {
        return new NeighborCountingGrid(initial, rows, cols);
    }

}