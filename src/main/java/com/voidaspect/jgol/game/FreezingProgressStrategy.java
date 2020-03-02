package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.CellListener;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class FreezingProgressStrategy implements ProgressStrategy {

    private final AtomicBoolean frozen = new AtomicBoolean();

    private final AtomicBoolean finished = new AtomicBoolean();

    @Override
    public final void progress(Grid grid, CellListener listener) {
        if (finished.get() || frozen.get()) return;
        int updates = progressAndCountUpdates(grid, listener);
        frozen.set(updates == 0);
    }

    @Override
    public final void freeze() {
        frozen.set(true);
    }

    @Override
    public final void unfreeze() {
        frozen.set(false);
    }

    @Override
    public void finish() {
        finished.set(true);
    }

    @Override
    public boolean isFinished() {
        return finished.get();
    }

    @Override
    public boolean isFrozen() {
        return frozen.get();
    }

    protected abstract int progressAndCountUpdates(Grid grid, CellListener listener);

}
