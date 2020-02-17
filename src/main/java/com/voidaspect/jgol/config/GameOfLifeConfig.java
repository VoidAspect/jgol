package com.voidaspect.jgol.config;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class GameOfLifeConfig {

    public static final int DEFAULT_PARALLEL_PROGRESSION_THRESHOLD = 1_000_000;

    public static final int DEFAULT_CHUNK_SIDE = 1_000;

    private int rows;

    private int columns;

    private boolean[][] initialState;

    private boolean parallel = true;

    private boolean threadSafe = false;

    private ExecutorService progressExecutor;

    private int parallelizationThreshold = DEFAULT_PARALLEL_PROGRESSION_THRESHOLD;

    private int chunkWidth = DEFAULT_CHUNK_SIDE;

    private int chunkHeight = DEFAULT_CHUNK_SIDE;

    public int getRows() {
        return rows;
    }

    public GameOfLifeConfig setRows(int rows) {
        this.rows = rows;
        return this;
    }

    public int getColumns() {
        return columns;
    }

    public GameOfLifeConfig setColumns(int columns) {
        this.columns = columns;
        return this;
    }

    public Optional<boolean[][]> getInitialState() {
        return Optional.ofNullable(initialState);
    }

    public GameOfLifeConfig setInitialState(boolean[][] initialState) {
        this.initialState = initialState;
        return this;
    }

    public Optional<ExecutorService> getProgressExecutor() {
        return Optional.ofNullable(progressExecutor);
    }

    public GameOfLifeConfig setProgressExecutor(ExecutorService progressExecutor) {
        this.progressExecutor = progressExecutor;
        return this;
    }

    public int getParallelizationThreshold() {
        return parallelizationThreshold;
    }

    public GameOfLifeConfig setParallelizationThreshold(int parallelizationThreshold) {
        this.parallelizationThreshold = parallelizationThreshold;
        return this;
    }

    public boolean isParallelExecutionSupported() {
        return parallel;
    }

    public GameOfLifeConfig setParallel(boolean parallel) {
        this.parallel = parallel;
        return this;
    }

    public int getChunks() {
        long size = getSize();
        int chunkSize = chunkWidth * chunkHeight;
        int chunks = (int) (size / chunkSize);
        if (chunks * chunkSize < size) chunks++;
        return chunks;
    }

    public int getChunkHeight() {
        return chunkHeight;
    }

    public long getSize() {
        return (long) rows * columns;
    }

    public GameOfLifeConfig setChunkHeight(int chunkHeight) {
        this.chunkHeight = chunkHeight;
        return this;
    }

    public int getChunkWidth() {
        return chunkWidth;
    }

    public GameOfLifeConfig setChunkWidth(int chunkWidth) {
        this.chunkWidth = chunkWidth;
        return this;
    }

    public boolean isThreadSafe() {
        return threadSafe;
    }

    public GameOfLifeConfig setThreadSafe(boolean threadSafe) {
        this.threadSafe = threadSafe;
        return this;
    }
}
