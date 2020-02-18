package com.voidaspect.jgol.game;

import com.voidaspect.jgol.GameOfLife;
import com.voidaspect.jgol.listener.CellListener;
import com.voidaspect.jgol.listener.ProgressListener;

public abstract class AbstractLife implements GameOfLife {

    @Override
    public void progress() {
        nextGen(ProgressListener.NOOP);
    }

    @Override
    public void progress(ProgressListener listener) {
        if (listener == null) {
            listener = ProgressListener.NOOP;
        }
        listener.onProgressStart();
        nextGen(listener);
        listener.onProgressFinish();
    }

    protected abstract void nextGen(CellListener listener);

}
