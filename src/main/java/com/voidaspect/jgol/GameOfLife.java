package com.voidaspect.jgol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.*;

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
     * If we have more than 1 million cells - progress calculation will be executed in parallel
     */
    private static final int PARALLEL_PROGRESSION_THRESHOLD = 1_000_000;

    private static final int CHUNK_SIDE_SIZE = 1000;

    private static final int CHUNK_SIZE = CHUNK_SIDE_SIZE * CHUNK_SIDE_SIZE;

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

    /**
     * Final index for iterating over rows, exclusive
     */
    private final int upperRowBound;

    /**
     * Final index for iterating over columns, exclusive
     */
    private final int upperColBound;

    private final boolean parallel;

    private final int rowChunkSize;

    private final int colChunkSize;

    private final int chunkCount;

    private final long size;

    private final ExecutorService progressPool;

    public GameOfLife(int rows, int columns) {
        this(null, rows, columns);
    }

    public GameOfLife(boolean[][] state, int rows, int columns) {
        this.grid = buildGrid(state, rows, columns);
        this.rows = rows;
        this.columns = columns;
        this.upperRowBound = rows + 1;
        this.upperColBound = columns + 1;
        this.size = (long) rows * columns;
        this.parallel = size > PARALLEL_PROGRESSION_THRESHOLD;
        if (parallel) {
            int chunkCount = (int) (size / CHUNK_SIZE);
            if (chunkCount * PARALLEL_PROGRESSION_THRESHOLD < size) {
                chunkCount++;
            }
            this.chunkCount = chunkCount;
            rowChunkSize = CHUNK_SIDE_SIZE;
            colChunkSize = CHUNK_SIDE_SIZE;
            progressPool = Executors.newWorkStealingPool(chunkCount);
        } else {
            rowChunkSize = rows;
            colChunkSize = columns;
            progressPool = null;
            chunkCount = 1;
        }
    }

    public void set(int row, int col, boolean state) {
        Objects.checkIndex(row, rows);
        Objects.checkIndex(col, columns);
        grid[row + 1][col + 1] = state;
    }

    public boolean isAlive(int row, int col) {
        Objects.checkIndex(row, rows);
        Objects.checkIndex(col, columns);
        return grid[row + 1][col + 1];
    }

    public boolean[][] snapshot() {
        boolean[][] snapshot = new boolean[rows][];
        for (int i = 0, row = LOWER_BOUND; row < upperRowBound; row++) {
            snapshot[i++] = Arrays.copyOfRange(grid[row], LOWER_BOUND, upperColBound);
        }
        return snapshot;
    }

    public void clear() {
        // this row is used for padding, its values are always false
        boolean[] empty = grid[0];
        for (int i = LOWER_BOUND; i < upperRowBound; i++) {
            // rewrite this row with false
            System.arraycopy(empty, LOWER_BOUND, grid[i], LOWER_BOUND, columns);
        }
    }

    public void progress() {
        if (!parallel) {
            var nextGen = progressChunk(LOWER_BOUND, LOWER_BOUND, upperRowBound, upperColBound);
            nextGen.updateGrid();
            return;
        }
        var progressTasks = new ArrayList<Callable<NextGen>>(chunkCount);
        for (int row = LOWER_BOUND; row < upperRowBound; row += rowChunkSize) {
            for (int col = LOWER_BOUND; col < upperColBound; col += colChunkSize) {
                int fromRow = row, fromCol = col, toRow = fromRow + rowChunkSize, toCol = fromCol + colChunkSize;
                progressTasks.add(() -> progressChunk(fromRow, fromCol, toRow, toCol));
            }
        }
        try {
            var gridUpdates = new ArrayList<Callable<Object>>(chunkCount);
            for (var chunk : progressPool.invokeAll(progressTasks)) {
                var chunkNextGen = chunk.get();
                gridUpdates.add(Executors.callable(chunkNextGen::updateGrid));
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

    private NextGen progressChunk(int fromRow, int fromCol, int toRow, int toCol) {
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

    private static boolean[][] buildGrid(boolean[][] initial, int rows, int columns) {
        if (rows < MIN_DIMENSION_SIZE) {
            throw new IllegalArgumentException("Number of rows expected >= 1, got " + rows);
        }
        if (columns < MIN_DIMENSION_SIZE) {
            throw new IllegalArgumentException("Number of columns expected >= 1, got " + rows);
        }
        // pad matrix from all sides to avoid range checks on neighbor calculation
        boolean[][] grid = new boolean[rows + 2][columns + 2];
        if (initial == null) {
            return grid;
        }
        int rowsLength = Math.min(rows, initial.length);
        for (int i = 0; i < rowsLength; i++) {
            boolean[] row = initial[i];
            if (row == null) continue;
            int columnLength = Math.min(columns, row.length);
            System.arraycopy(row, 0, grid[i + 1], LOWER_BOUND, columnLength);
        }
        return grid;
    }

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
}
