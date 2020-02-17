package com.voidaspect.jgol;

import com.voidaspect.jgol.config.GameOfLifeConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Conway's Game of Life - stateful cellular automaton governed by following rules:
 * <ol>
 *     <li>each cell can be either dead or alive</li>
 *     <li>if cell is alive and it has less than 2 alive neighbors - it becomes dead</li>
 *     <li>if cell is alive and it has more than 3 alive neighbors - it becomes dead</li>
 *     <li>if cell is dead and it has exactly 3 alive neighbors - it becomes alive</li>
 * </ol>
 * State is updated via {@link GameOfLife#progress()} method.
 * <br>Initial state can be configured via constructor or via {@link GameOfLife#set(int, int, boolean)} method.
 * <br>Current state of each individual cell can be accessed via {@link GameOfLife#isAlive(int, int)} method.
 * <br>Snapshot of the game's overall state can be created via {@link GameOfLife#snapshot()} method.
 * <br>State can be cleared (all cells become dead) via {@link GameOfLife#clear()} method.
 */
public final class GameOfLife {

    /**
     * Minimal number of columns and rows for a valid GoL grid
     */
    private static final int MIN_DIMENSION_SIZE = 1;

    /**
     * Initial index for iterating over columns and rows, inclusive
     */
    private static final int LOWER_BOUND = 1;

    /**
     * Game of life grid - each cell can have one of two states: false = dead, true = alive
     */
    private final boolean[][] grid;

    /**
     * Number of rows on the grid
     */
    private final int rows;

    /**
     * Number of columns on the grid
     */
    private final int columns;

    private final long size;

    /**
     * Final index for iterating over rows, exclusive
     */
    private final int upperRowBound;

    /**
     * Final index for iterating over columns, exclusive
     */
    private final int upperColBound;

    private final int chunkWidth;

    private final int chunkHeight;

    private final int chunks;

    private final ExecutorService progressPool;

    private final boolean threadSafe;

    private final boolean parallel;

    private final ReadWriteLock gridLock;

    private final ProgressStrategy ps;

    public GameOfLife(int rows, int columns) {
        this(new GameOfLifeConfig().setRows(rows).setColumns(columns));
    }

    public GameOfLife(boolean[][] initialState, int rows, int columns) {
        this(new GameOfLifeConfig().setRows(rows).setColumns(columns).setInitialState(initialState));
    }

    public GameOfLife(GameOfLifeConfig config) {
        int rows = config.getRows();
        int columns = config.getColumns();
        grid = config.getInitialState()
                .map(initial -> buildGrid(initial, rows, columns))
                .orElseGet(() -> initGrid(rows, columns));
        this.rows = rows;
        this.columns = columns;
        upperRowBound = rows + 1;
        upperColBound = columns + 1;
        size = config.getSize();
        ProgressStrategy ps;
        parallel = config.isParallelExecutionSupported() && size > config.getParallelizationThreshold();
        if (parallel) {
            chunkWidth = config.getChunkWidth();
            chunkHeight = config.getChunkHeight();
            this.chunks = config.getChunks();
            progressPool = config.getProgressExecutor()
                    .orElseGet(() -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
            ps = new ParallelProgressStrategy();
        } else {
            chunkWidth = rows;
            chunkHeight = columns;
            chunks = 1;
            progressPool = null;
            ps = new AllAtOnceProgressStrategy();
        }
        threadSafe = config.isThreadSafe();
        if (threadSafe) {
            gridLock = new ReentrantReadWriteLock();
            ps = new ThreadSafeProgressStrategy(ps);
        } else {
            gridLock = null;
        }
        this.ps = ps;
    }

    public void set(int row, int col, boolean state) {
        Objects.checkIndex(row, rows);
        Objects.checkIndex(col, columns);
        if (threadSafe) {
            lockAndSet(row, col, state);
        } else {
            grid[row + 1][col + 1] = state;
        }
    }

    private void lockAndSet(int row, int col, boolean state) {
        var lock = gridLock.writeLock();
        lock.lock();
        try {
            grid[row + 1][col + 1] = state;
        } finally {
            lock.unlock();
        }
    }

    public boolean isAlive(int row, int col) {
        Objects.checkIndex(row, rows);
        Objects.checkIndex(col, columns);
        return threadSafe ? lockAndGet(row, col) : grid[row + 1][col + 1];
    }

    private boolean lockAndGet(int row, int col) {
        var lock = gridLock.readLock();
        lock.lock();
        try {
            return grid[row + 1][col + 1];
        } finally {
            lock.unlock();
        }
    }

    public boolean[][] snapshot() {
        boolean[][] snapshot = new boolean[rows][];
        return threadSafe ? lockAndCopyTo(snapshot) : copyTo(snapshot);
    }

    private boolean[][] lockAndCopyTo(boolean[][] snapshot) {
        var lock = gridLock.readLock();
        lock.lock();
        try {
            return copyTo(snapshot);
        } finally {
            lock.unlock();
        }
    }

    private boolean[][] copyTo(boolean[][] snapshot) {
        for (int i = 0, row = LOWER_BOUND; row < upperRowBound; row++) {
            snapshot[i++] = Arrays.copyOfRange(grid[row], LOWER_BOUND, upperColBound);
        }
        return snapshot;
    }

    public void clear() {
        if (threadSafe) {
            lockAndClean();
        } else {
            cleanInner();
        }
    }

    private void lockAndClean() {
        var lock = gridLock.writeLock();
        lock.lock();
        try {
            cleanInner();
        } finally {
            lock.unlock();
        }
    }

    private void cleanInner() {
        for (int i = LOWER_BOUND; i < upperRowBound; i++) {
            // rewrite this row with false
            System.arraycopy(grid[0], LOWER_BOUND, grid[i], LOWER_BOUND, columns);
        }
    }

    public void progress() {
        ps.progress();
    }

    private void spawn(int row, int col) {
        grid[row][col] = true;
    }

    private void kill(int row, int col) {
        grid[row][col] = false;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public long getSize() {
        return size;
    }

    public int getChunks() {
        return chunks;
    }

    public boolean isParallel() {
        return parallel;
    }

    private static boolean[][] buildGrid(boolean[][] initial, int rows, int columns) {
        boolean[][] grid = initGrid(rows, columns);
        int rowsLength = Math.min(rows, initial.length);
        for (int i = 0; i < rowsLength; i++) {
            boolean[] row = initial[i];
            if (row == null) continue;
            int columnLength = Math.min(columns, row.length);
            System.arraycopy(row, 0, grid[i + 1], LOWER_BOUND, columnLength);
        }
        return grid;
    }

    private static boolean[][] initGrid(int rows, int columns) {
        if (rows < MIN_DIMENSION_SIZE) {
            throw new IllegalArgumentException("Number of rows expected >= 1, got " + rows);
        }
        if (columns < MIN_DIMENSION_SIZE) {
            throw new IllegalArgumentException("Number of columns expected >= 1, got " + rows);
        }
        // pad matrix from all sides to avoid range checks on neighbor calculation
        return new boolean[rows + 2][columns + 2];
    }

    //region progress strategy
    private interface ProgressStrategy {
        void progress();
    }

    private abstract class ChunkedProgressStrategy implements ProgressStrategy {

        NextGen progressChunk(int fromRow, int fromCol, int toRow, int toCol) {
            toRow = Math.min(upperRowBound, toRow);
            toCol = Math.min(upperColBound, toCol);
            var died = new CellBag();
            var spawned = new CellBag();
            for (int row = fromRow; row < toRow; row++) {
                for (int col = fromCol; col < toCol; col++) {
                    int neighbors = neighbors(row, col);
                    // depending on whether the cell is alive
                    if (grid[row][col]) {
                        // overcrowding or underpopulation
                        if (neighbors < 2 || neighbors > 3) died.add(row, col);
                    } else {
                        // reproduction
                        if (neighbors == 3) spawned.add(row, col);
                    }
                }
            }
            return new NextGen(spawned, died);
        }

        private int neighbors(int row, int col) {
            //@formatter:off
            int up    = row - 1;
            int down  = row + 1;
            int left  = col - 1;
            int right = col + 1;
            return value(up,   right) + value(up,   col) + value(up,   left) +
                   value(row,  right) + /* this cell */  + value(row,  left) +
                   value(down, right) + value(down, col) + value(down, left);
            //@formatter:on
        }

        private int value(int row, int col) {
            return grid[row][col] ? 1 : 0;
        }

    }

    private final class AllAtOnceProgressStrategy extends ChunkedProgressStrategy {
        @Override
        public void progress() {
            var nextGen = progressChunk(LOWER_BOUND, LOWER_BOUND, upperRowBound, upperColBound);
            nextGen.updateGrid();
        }
    }

    private final class ParallelProgressStrategy extends ChunkedProgressStrategy {

        private final ExecutorService progressPool;

        public ParallelProgressStrategy() {
            progressPool = GameOfLife.this.progressPool;
        }

        @Override
        public void progress() {
            var progressTasks = new ArrayList<Callable<NextGen>>(chunks);
            for (int row = LOWER_BOUND; row < upperRowBound; row += chunkWidth) {
                for (int col = LOWER_BOUND; col < upperColBound; col += chunkHeight) {
                    int fromRow = row;
                    int fromCol = col;
                    int toRow = fromRow + chunkWidth;
                    int toCol = fromCol + chunkHeight;
                    progressTasks.add(() -> progressChunk(fromRow, fromCol, toRow, toCol));
                }
            }
            try {
                var gridUpdates = new ArrayList<Callable<Void>>(chunks);
                for (var chunk : progressPool.invokeAll(progressTasks)) {
                    gridUpdates.add(gridUpdate(chunk.get()));
                }
                for (var update : progressPool.invokeAll(gridUpdates)) {
                    update.get();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        private Callable<Void> gridUpdate(NextGen nextGen) {
            return () -> {
                nextGen.updateGrid();
                return null;
            };
        }
    }

    private final class ThreadSafeProgressStrategy implements ProgressStrategy {

        private final ProgressStrategy inner;

        private final Lock mutex;

        private ThreadSafeProgressStrategy(ProgressStrategy inner) {
            this.inner = inner;
            this.mutex = gridLock.writeLock();
        }

        @Override
        public void progress() {
            mutex.lock();
            try {
                inner.progress();
            } finally {
                mutex.unlock();
            }
        }
    }
    //endregion

    //region bulk cell ops
    private final class NextGen {

        final CellBag spawned;

        final CellBag died;

        private NextGen(CellBag spawned, CellBag died) {
            this.spawned = spawned;
            this.died = died;
        }

        void updateGrid() {
            spawned.forEach(GameOfLife.this::spawn);
            died.forEach(GameOfLife.this::kill);
        }
    }

    @FunctionalInterface
    private interface CellOperation {
        void apply(int row, int col);
    }

    /**
     * An auxiliary data structure designed to hold a collection of cell indices of a 2-dimensional matrix.
     * <br>Implemented as a Structure of Arrays with on-demand resizing.</br>
     * <br>Arrays <b>r</b> and <b>c</b> hold row and column indices of cells respectively.</br>
     * <p> API of {@code CellBag} allows:</p>
     * <ol>
     *     <li>to add cell indices one by one</li>
     *     <li>to perform {@link CellOperation} on all cells</li>
     * </ol>
     */
    private static final class CellBag {

        private static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

        private static final int INITIAL_CAPACITY = 16;

        private int[] r;

        private int[] c;

        private int size;

        private int capacity;

        public void forEach(CellOperation action) {
            for (int i = 0; i < size; i++) {
                action.apply(r[i], c[i]);
            }
        }

        public void add(int row, int col) {
            //region ensure capacity
            if (size == 0) { // initial allocation
                capacity = INITIAL_CAPACITY;
                r = new int[INITIAL_CAPACITY];
                c = new int[INITIAL_CAPACITY];
            } else if (size == capacity) { // resize on-demand
                if (size == MAX_ARRAY_LENGTH) {
                    throw new IllegalStateException("cell bag too big");
                }
                // if capacity is small (less than 64) - multiply 2, else multiply by 1.5
                int needed = capacity > 64
                        ? capacity + (capacity >> 1)
                        : capacity << 1;
                if (needed < 0 || needed > MAX_ARRAY_LENGTH) { // handle overflow
                    needed = MAX_ARRAY_LENGTH;
                }
                capacity = needed;
                r = Arrays.copyOf(r, capacity);
                c = Arrays.copyOf(c, capacity);
            }
            //endregion
            int index = size++;
            r[index] = row;
            c[index] = col;
        }

    }
    //endregion
}
