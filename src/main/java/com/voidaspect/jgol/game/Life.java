package com.voidaspect.jgol.game;

import com.voidaspect.jgol.GameOfLife;
import com.voidaspect.jgol.grid.Grid;

public final class Life implements GameOfLife {

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
    public void progress() {
        ps.progress();
    }

}
