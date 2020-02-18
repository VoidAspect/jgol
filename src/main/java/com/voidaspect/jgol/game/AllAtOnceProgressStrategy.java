package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;

final class AllAtOnceProgressStrategy extends ChunkedProgressStrategy {


    AllAtOnceProgressStrategy(Grid grid) {
        super(grid);
    }

    @Override
    public void progress() {
        var nextGen = progressChunk(0, 0, grid.getRows(), grid.getColumns());
        nextGen.updateGrid();
    }
}
