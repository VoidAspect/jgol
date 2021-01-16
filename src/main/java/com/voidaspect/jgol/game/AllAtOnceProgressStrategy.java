package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.CellListener;

import java.util.HashSet;

final class AllAtOnceProgressStrategy extends AbstractProgressStrategy {

    @Override
    int progressAndCountUpdates(Grid grid, CellListener listener) {
        var ng = new NextGen(grid);
        var visited = new HashSet<Long>();
        grid.forEachAlive((row, col) -> nextLiveCell(grid, listener, ng, visited, row, col));
        ng.updateGrid();
        return ng.countUpdates();
    }

}
