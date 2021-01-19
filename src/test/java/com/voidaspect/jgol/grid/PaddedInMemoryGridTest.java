package com.voidaspect.jgol.grid;

class PaddedInMemoryGridTest extends FiniteGridTest {

    @Override
    protected FiniteGrid grid(int rows, int cols) {
        return new PaddedInMemoryGrid(rows, cols);
    }

    @Override
    protected FiniteGrid grid(boolean[][] initial, int rows, int cols) {
        return new PaddedInMemoryGrid(initial, rows, cols);
    }

}