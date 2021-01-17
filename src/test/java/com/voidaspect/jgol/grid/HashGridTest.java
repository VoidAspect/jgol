package com.voidaspect.jgol.grid;

class HashGridTest extends FiniteGridTest {

    @Override
    protected Grid grid(int rows, int cols) {
        return new HashGrid(rows, cols);
    }

    @Override
    protected Grid grid(boolean[][] initial, int rows, int cols) {
        return new HashGrid(initial, rows, cols);
    }

}