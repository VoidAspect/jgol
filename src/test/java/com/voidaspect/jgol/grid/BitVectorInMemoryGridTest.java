package com.voidaspect.jgol.grid;

class BitVectorInMemoryGridTest extends FiniteGridTest {

    @Override
    protected FiniteGrid grid(int rows, int cols) {
        return new BitVectorInMemoryGrid(rows, cols);
    }

    @Override
    protected FiniteGrid grid(boolean[][] initial, int rows, int cols) {
        return new BitVectorInMemoryGrid(initial, rows, cols);
    }

}