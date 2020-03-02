package com.voidaspect.jgol.game;

import com.voidaspect.jgol.GameOfLife;
import com.voidaspect.jgol.listener.CellListener;
import com.voidaspect.jgol.listener.ProgressListener;

public abstract class AbstractLife implements GameOfLife {

    @Override
    public void progress() {
        progressAndListen(ProgressListener.NOOP);
    }

    @Override
    public void progress(ProgressListener listener) {
        progressAndListen(listener != null ? listener : ProgressListener.NOOP);
    }

    private void progressAndListen(ProgressListener listener) {
        if (isFinished()) return;
        listener.onProgressStart();
        if (!isFrozen()) {
            nextGen(listener);
        }
        listener.onProgressFinish();
    }

    protected abstract void nextGen(CellListener listener);

}
