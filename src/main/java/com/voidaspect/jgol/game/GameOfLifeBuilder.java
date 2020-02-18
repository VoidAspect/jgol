package com.voidaspect.jgol.game;

import com.voidaspect.jgol.GameOfLife;
import com.voidaspect.jgol.grid.Grid;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameOfLifeBuilder {

    public static final int DEFAULT_PARALLEL_PROGRESSION_THRESHOLD = 1_000_000;

    public static final int DEFAULT_CHUNK_SIDE = 1_000;

    private boolean parallel = true;

    private boolean threadSafe = false;

    private ExecutorService progressExecutor;

    private int parallelizationThreshold = DEFAULT_PARALLEL_PROGRESSION_THRESHOLD;

    private int chunkWidth = DEFAULT_CHUNK_SIDE;

    private int chunkHeight = DEFAULT_CHUNK_SIDE;

    private final Grid grid;

    public GameOfLifeBuilder(Grid grid) {
        this.grid = grid;
    }

    public GameOfLife build() {
        long size = grid.getSize();
        final GameOfLife life;
        if (parallel && size > parallelizationThreshold) {
            var progressPool = getProgressExecutor()
                    .orElseGet(() -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
            int chunkSize = chunkWidth * chunkHeight;
            int chunks = (int) (size / chunkSize);
            if (chunks * chunkSize < size) chunks++; // number of chunks should be large enough to cover all cells
            var ps = new ParallelProgressStrategy(grid, progressPool, chunkHeight, chunkWidth, chunks);
            life = new Life(grid, ps, progressPool::shutdown);
        } else {
            var ps = new AllAtOnceProgressStrategy(grid);
            life = new Life(grid, ps);
        }
        return threadSafe ? new ThreadSafeLife(life) : life;
    }

    public Optional<ExecutorService> getProgressExecutor() {
        return Optional.ofNullable(progressExecutor);
    }

    public GameOfLifeBuilder setProgressExecutor(ExecutorService progressExecutor) {
        this.progressExecutor = progressExecutor;
        return this;
    }

    public int getParallelizationThreshold() {
        return parallelizationThreshold;
    }

    public GameOfLifeBuilder setParallelizationThreshold(int parallelizationThreshold) {
        this.parallelizationThreshold = parallelizationThreshold;
        return this;
    }

    public boolean isParallelExecutionSupported() {
        return parallel;
    }

    public GameOfLifeBuilder setParallel(boolean parallel) {
        this.parallel = parallel;
        return this;
    }

    public int getChunkHeight() {
        return chunkHeight;
    }

    public GameOfLifeBuilder setChunkHeight(int chunkHeight) {
        this.chunkHeight = chunkHeight;
        return this;
    }

    public int getChunkWidth() {
        return chunkWidth;
    }

    public GameOfLifeBuilder setChunkWidth(int chunkWidth) {
        this.chunkWidth = chunkWidth;
        return this;
    }

    public boolean isThreadSafe() {
        return threadSafe;
    }

    public GameOfLifeBuilder setThreadSafe(boolean threadSafe) {
        this.threadSafe = threadSafe;
        return this;
    }

    public Grid getGrid() {
        return grid;
    }
}
