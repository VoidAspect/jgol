package com.voidaspect.jgol.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingProgressListener implements ProgressListener {

    private static final Logger log = LoggerFactory.getLogger(LoggingProgressListener.class);

    @Override
    public void onProgressStart() {
        log.info("started calculating next generation");
    }

    @Override
    public void onProgressFinish() {
        log.info("finished calculating next generation");
    }

    @Override
    public void onCellSpawned(int row, int col) {
        log.info("cell at ({},{}) becomes alive", row, col);
    }

    @Override
    public void onCellDied(int row, int col) {
        log.info("cell at ({},{}) becomes dead", row, col);
    }
}
