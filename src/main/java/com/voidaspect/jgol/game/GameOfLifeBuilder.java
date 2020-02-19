package com.voidaspect.jgol.game;

import com.voidaspect.jgol.GameOfLife;
import com.voidaspect.jgol.grid.Grid;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameOfLifeBuilder {

    public static final int DEFAULT_PARALLEL_PROGRESSION_THRESHOLD = 1_000_000;

    public static final int DEFAULT_CHUNK_SIDE = 1_000;

    private static final int MIN_CHUNK_SIZE = 1;

    private boolean parallel = true;

    private boolean threadSafe = false;

    private boolean keepPoolAlive = false;

    private ExecutorService progressExecutor;

    private int parallelizationThreshold = DEFAULT_PARALLEL_PROGRESSION_THRESHOLD;

    private int chunkWidth = DEFAULT_CHUNK_SIDE;

    private int chunkHeight = DEFAULT_CHUNK_SIDE;

    private final Grid grid;

    public GameOfLifeBuilder(Grid grid) {
        this.grid = Objects.requireNonNull(grid);
    }

    public GameOfLife build() {
        var ps = chooseProgressStrategy();
        var life = new Life(grid, ps);
        return threadSafe ? new ThreadSafeLife(life) : life;
    }

    protected ProgressStrategy chooseProgressStrategy() {
        final ProgressStrategy ps;
        if (isParallel()) {
            int chunks = getChunks();
            boolean keepPoolAlive = shouldKeepPoolAlive();
            var progressPool = getProgressExecutor().orElseGet(() -> Executors
                    // by default, progress strategy will maintain a fixed thread pool.
                    // Threads are released upon termination.
                    .newFixedThreadPool(Math.min(chunks, Runtime.getRuntime().availableProcessors())));
            ps = new ParallelProgressStrategy(grid, progressPool, keepPoolAlive, chunkHeight, chunkWidth, chunks);
        } else {
            ps = new AllAtOnceProgressStrategy(grid);
        }
        return ps;
    }

    public int getChunks() {
        long size = grid.getSize();
        int chunkSize = chunkWidth * chunkHeight;
        int chunks = (int) (size / chunkSize);
        // number of chunks should be large enough to cover all cells
        return chunks * chunkSize < size ? chunks + 1 : chunks;
    }

    public int getChunkHeight() {
        return chunkHeight;
    }

    public GameOfLifeBuilder setChunkHeight(int chunkHeight) {
        if (chunkHeight < MIN_CHUNK_SIZE || chunkHeight > grid.getRows()) {
            throw new IllegalArgumentException("Chunk height should be between 1 and "
                    + grid.getRows() + ", got " + chunkHeight);
        }
        this.chunkHeight = chunkHeight;
        return this;
    }

    public int getChunkWidth() {
        return chunkWidth;
    }

    public GameOfLifeBuilder setChunkWidth(int chunkWidth) {
        if (chunkWidth < MIN_CHUNK_SIZE || chunkWidth > grid.getColumns()) {
            throw new IllegalArgumentException("Chunk width should be between 1 and "
                    + grid.getColumns() + ", got " + chunkWidth);
        }
        this.chunkWidth = chunkWidth;
        return this;
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

    public boolean isParallel() {
        return parallel && grid.getSize() > parallelizationThreshold;
    }

    public GameOfLifeBuilder setParallel(boolean parallel) {
        this.parallel = parallel;
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

    public boolean shouldKeepPoolAlive() {
        // if default pool is used, always shut it down
        return progressExecutor != null && keepPoolAlive;
    }

    public GameOfLifeBuilder setKeepPoolAlive(boolean keepPoolAlive) {
        this.keepPoolAlive = keepPoolAlive;
        return this;
    }
}
