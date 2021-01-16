package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.CellListener;

final class AllAtOnceProgressStrategy extends AbstractProgressStrategy {

    @Override
    int progressAndCountUpdates(Grid grid, CellListener listener) {
        var ng = new NextGen(grid, listener);
        grid.forEachAlive(ng::nextLiveCell);
        ng.updateGrid();
        return ng.countUpdates();
    }

}
