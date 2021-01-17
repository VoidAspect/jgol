package com.voidaspect.jgol.game;

import com.voidaspect.jgol.GameOfLife;
import com.voidaspect.jgol.grid.Grid;

import java.util.Objects;

public class GameOfLifeBuilder {

    private boolean threadSafe = false;

    private final Grid grid;

    public GameOfLifeBuilder(Grid grid) {
        this.grid = Objects.requireNonNull(grid);
    }

    public GameOfLife build() {
        var ps = chooseProgressStrategy();
        var life = new Life(grid, ps);
        return threadSafe ? ThreadSafeLife.of(life) : life;
    }

    protected ProgressStrategy chooseProgressStrategy() {
        return new AllAtOnceProgressStrategy();
    }

    public boolean isThreadSafe() {
        return threadSafe;
    }

    public GameOfLifeBuilder setThreadSafe(boolean threadSafe) {
        this.threadSafe = threadSafe;
        return this;
    }

    public Grid getGrid() {
        return grid;
    }

}
