package com.voidaspect.jgol.game;

interface ProgressStrategy {

    ProgressStrategy NOOP = () -> {};

    void progress();

}
