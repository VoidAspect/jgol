package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.CellOperation;
import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.CellListener;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class AbstractProgressStrategy implements ProgressStrategy {

    private final AtomicBoolean frozen = new AtomicBoolean();

    private final AtomicBoolean finished = new AtomicBoolean();

    @Override
    public void progress(Grid grid, CellListener listener) {
        if (finished.get() || frozen.get()) return;
        int updates = progressAndCountUpdates(grid, listener);
        frozen.set(updates == 0);
    }

    @Override
    public void freeze() {
        frozen.set(true);
    }

    @Override
    public void unfreeze() {
        frozen.set(false);
    }

    @Override
    public void finish() {
        finished.set(true);
    }

    @Override
    public boolean isFinished() {
        return finished.get();
    }

    @Override
    public boolean isFrozen() {
        return frozen.get();
    }

    abstract int progressAndCountUpdates(Grid grid, CellListener listener);

    static long cell(int row, int col) {
        return (long) row << 32 | col & 0xffffffffL;
    }

    final void nextLiveCell(Grid grid, CellListener listener, NextGen ng, Set<Long> visited, int row, int col) {
        int neighbors = grid.neighbors(row, col);

        if (neighbors < 2 || neighbors > 3) {
            // overcrowding or underpopulation
            ng.willDie(row, col);
            listener.onCellDied(row, col);
        }

        //@formatter:off
        int up    = row - 1;
        int down  = row + 1;
        int left  = col - 1;
        int right = col + 1;
        eval(grid, ng, listener, visited, up,   left); eval(grid, ng, listener, visited, up,   col); eval(grid, ng, listener, visited, up,   right);
        eval(grid, ng, listener, visited, row,  left); /*                this cell                */ eval(grid, ng, listener, visited, row,  right);
        eval(grid, ng, listener, visited, down, left); eval(grid, ng, listener, visited, down, col); eval(grid, ng, listener, visited, down, right);
        //@formatter:on
    }

    private void eval(Grid grid,
                      NextGen ng,
                      CellListener listener,
                      Set<Long> visited,
                      int row,
                      int col) {

        if (!grid.hasCell(row, col) || grid.get(row, col)) return;
        if (!visited.add(cell(row, col))) return;

        int neighbors = grid.neighbors(row, col);

        if (neighbors == 3) {
            // reproduction
            ng.willSpawn(row, col);
            listener.onCellSpawned(row, col);
        }
    }

    static final class NextGen {

        final Grid grid;

        final CellBag spawned;

        final CellBag died;

        NextGen(Grid grid) {
            this.grid = grid;
            this.spawned = new CellBag();
            this.died = new CellBag();
        }

        public void willDie(int row, int col) {
            died.add(row, col);
        }

        public void willSpawn(int row, int col) {
            spawned.add(row, col);
        }

        public int countUpdates() {
            return spawned.size + died.size;
        }

        void updateGrid() {
            spawned.forEach((row, col) -> grid.set(row, col, true));
            died.forEach((row, col) -> grid.set(row, col, false));
        }
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

        private static final int INITIAL_CAPACITY = 64;

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
                // multiply by 1.5
                int needed = capacity + (capacity >> 1);
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
