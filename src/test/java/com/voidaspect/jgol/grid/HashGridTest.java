package com.voidaspect.jgol.grid;

class HashGridTest extends GridTest {

    @Override
    protected Grid grid(boolean[][] initial) {
        return new HashGrid(initial);
    }

}