package com.voidaspect.jgol.grid;

class NeighborCountingHashGridTest extends FiniteGridTest {

    @Override
    protected Grid grid(int rows, int cols) {
        return new NeighborCountingHashGrid(rows, cols);
    }

    @Override
    protected Grid grid(boolean[][] initial, int rows, int cols) {
        return new NeighborCountingHashGrid(initial, rows, cols);
    }

}