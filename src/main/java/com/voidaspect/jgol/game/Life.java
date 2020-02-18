package com.voidaspect.jgol.game;

import com.voidaspect.jgol.GameOfLife;
import com.voidaspect.jgol.grid.Grid;

public final class Life implements GameOfLife {

    private final Grid grid;

    private final Runnable onFinish;

    private ProgressStrategy ps;

    Life(Grid grid, ProgressStrategy ps) {
        this(grid, ps, null);
    }

    Life(Grid grid, ProgressStrategy ps, Runnable onFinish) {
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
