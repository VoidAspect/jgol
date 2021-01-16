package com.voidaspect.jgol.grid;

class BitVectorInMemoryGridTest extends FiniteGridTest {

    @Override
    protected Grid grid(int rows, int cols) {
        return new BitVectorInMemoryGrid(rows, cols);
    }

    @Override
    protected Grid grid(boolean[][] initial, int rows, int cols) {
        return new BitVectorInMemoryGrid(initial, rows, cols);
    }

}