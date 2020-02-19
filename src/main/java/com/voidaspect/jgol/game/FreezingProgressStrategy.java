package com.voidaspect.jgol.game;

import com.voidaspect.jgol.listener.CellListener;

import java.util.concurrent.atomic.AtomicBoolean;

abstract class FreezingProgressStrategy implements ProgressStrategy {

    private final AtomicBoolean frozen = new AtomicBoolean();

    @Override
    public final void progress(CellListener listener) {
        if (frozen.get()) return;
        int updates = progressAndCountUpdates(listener);
        if (updates == 0) {
            frozen.set(true);
            terminate();
        }
    }

    abstract int progressAndCountUpdates(CellListener listener);
}
