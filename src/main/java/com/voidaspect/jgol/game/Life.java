package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.CellListener;

final class Life extends AbstractLife {

    private final MutationAwareGrid grid;

    private final Grid inner;

    private final ProgressStrategy ps;

    Life(Grid grid, ProgressStrategy ps) {
        this.inner = grid;
        this.grid = new MutationAwareGrid();
        this.ps = ps;
    }

    @Override
    public void finish() {
        ps.finish();
    }

    @Override
    public boolean isFinished() {
        return ps.isFinished();
    }

    @Override
    public Grid grid() {
        return grid;
    }

    @Override
    protected void nextGen(CellListener listener) {
        ps.progress(grid, listener);
    }

    @Override
    public void freeze() {
        ps.freeze();
    }

    @Override
    public void unfreeze() {
        ps.unfreeze();
    }

    @Override
    public boolean isFrozen() {
        return ps.isFrozen();
    }

    private final class MutationAwareGrid implements Grid {

        @Override
        public boolean get(int row, int col) {
            return inner.get(row, col);
        }

        @Override
        public void set(int row, int col, boolean state) {
            inner.set(row, col, state);
            unfreeze();
        }

        @Override
        public int neighbors(int row, int col) {
            return inner.neighbors(row, col);
        }

        @Override
        public boolean[][] snapshot() {
            return inner.snapshot();
        }

        @Override
        public boolean[][] snapshot(int fromRow, int fromColumn, int rows, int columns) {
            return inner.snapshot(fromRow, fromColumn, rows, columns);
        }

        @Override
        public void clear() {
            inner.clear();
            freeze();
        }

        @Override
        public int getRows() {
            return inner.getRows();
        }

        @Override
        public int getColumns() {
            return inner.getColumns();
        }

        @Override
        public long getSize() {
            return inner.getSize();
        }
    }

}
