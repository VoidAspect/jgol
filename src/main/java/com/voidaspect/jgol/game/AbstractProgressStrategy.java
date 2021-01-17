package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.CellListener;

import java.util.concurrent.atomic.AtomicBoolean;

abstract class AbstractProgressStrategy implements ProgressStrategy {

    private final AtomicBoolean frozen = new AtomicBoolean();

    @Override
    public void progress(Grid grid, CellListener listener) {
        if (frozen.get()) return;
        int updates = progressAndCountUpdates(grid, listener);
        frozen.set(updates == 0);
    }

    @Override
    public void freeze() {
        frozen.set(true);
    }

    @Override
    public void unfreeze() {
        frozen.set(false);
    }

    @Override
    public boolean isFrozen() {
        return frozen.get();
    }

    abstract int progressAndCountUpdates(Grid grid, CellListener listener);

}
