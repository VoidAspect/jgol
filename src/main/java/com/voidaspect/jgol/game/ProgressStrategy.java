package com.voidaspect.jgol.game;

public interface ProgressStrategy {

    void progress();

    void terminate();

    ProgressStrategy NOOP = new ProgressStrategy() {
        @Override
        public void progress() {
        }

        @Override
        public void terminate() {
        }
    };

}
