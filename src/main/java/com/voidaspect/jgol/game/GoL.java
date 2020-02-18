package com.voidaspect.jgol.game;

import com.voidaspect.jgol.GameOfLife;
import com.voidaspect.jgol.grid.Grid;

public final class GoL implements GameOfLife {

    /**
     * Game of life grid - each cell can have one of two states: false = dead, true = alive
     */
    private final Grid grid;

    private final Runnable onFinish;

    private ProgressStrategy ps;

    GoL(Grid grid, ProgressStrategy ps, Runnable onFinish) {
        this.grid = grid;
        this.ps = ps;
        this.onFinish = onFinish;
    }

    @Override
    public void finish() {
        ps = ProgressStrategy.NOOP;
        if (onFinish != null) {
            onFinish.run();
        }
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
