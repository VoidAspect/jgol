package com.voidaspect.jgol.listener;

public interface ProgressListener extends CellListener {

    void onProgressStart();

    void onProgressFinish();

    ProgressListener NOOP = new ProgressListener() {
        @Override
        public void onProgressStart() {
        }

        @Override
        public void onCellSpawned(int row, int col) {
        }

        @Override
        public void onCellDied(int row, int col) {
        }

        @Override
        public void onProgressFinish() {
        }
    };
}
