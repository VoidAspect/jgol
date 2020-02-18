package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.CellListener;

final class AllAtOnceProgressStrategy extends ChunkedProgressStrategy {

    AllAtOnceProgressStrategy(Grid grid) {
        super(grid);
    }

    @Override
    public void progress(CellListener listener) {
        var nextGen = progressChunk(listener, 0, 0, grid.getRows(), grid.getColumns());
        nextGen.updateGrid();
    }

    @Override
    public void terminate() {
    }
}
