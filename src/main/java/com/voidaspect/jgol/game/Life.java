package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.CellListener;

final class Life extends AbstractLife {

    private final Grid grid;

    private ProgressStrategy ps;

    Life(Grid grid, ProgressStrategy ps) {
        this.grid = grid;
        this.ps = ps;
    }

    @Override
    public void finish() {
        ps.terminate();
        ps = ProgressStrategy.NOOP;
    }

    @Override
    public Grid grid() {
        return grid;
    }

    @Override
    protected void nextGen(CellListener listener) {
        ps.progress(listener);
    }

}
