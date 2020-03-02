package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.CellListener;

final class AllAtOnceProgressStrategy extends ChunkedProgressStrategy {

    @Override
    protected int progressAndCountUpdates(Grid grid, CellListener listener) {
        var nextGen = progressChunk(grid, listener, 0, 0, grid.getRows(), grid.getColumns());
        nextGen.updateGrid();
        return nextGen.countUpdates();
    }

}
