package com.voidaspect.jgol.game;

import com.voidaspect.jgol.listener.CellListener;

public interface ProgressStrategy {

    void progress(CellListener listener);

    void terminate();

    ProgressStrategy NOOP = new ProgressStrategy() {
        @Override
        public void progress(CellListener listener) {
        }

        @Override
        public void terminate() {
        }
    };

}
