package com.voidaspect.jgol.grid;

class PaddedInMemoryGridTest extends FiniteGridTest {

    @Override
    protected Grid grid(int rows, int cols) {
        return new PaddedInMemoryGrid(rows, cols);
    }

    @Override
    protected Grid grid(boolean[][] initial, int rows, int cols) {
        return new PaddedInMemoryGrid(initial, rows, cols);
    }

}