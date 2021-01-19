package com.voidaspect.jgol.grid;

class NeighborCountingHashGridTest extends GridTest {

    @Override
    protected Grid grid(boolean[][] initial) {
        return new NeighborCountingHashGrid(initial);
    }

}