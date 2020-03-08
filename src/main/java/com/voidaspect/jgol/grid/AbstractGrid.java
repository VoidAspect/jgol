package com.voidaspect.jgol.grid;

import java.util.Objects;

public abstract class AbstractGrid implements Grid {

    public static final int MIN_SIZE = 1;

    protected final int rows;

    protected final int cols;

    protected final long size;

    public AbstractGrid(int rows, int cols) {
        if (rows < MIN_SIZE) {
            throw new IllegalArgumentException("Number of rows expected >= 1, got " + rows);
        }
        if (cols < MIN_SIZE) {
            throw new IllegalArgumentException("Number of columns expected >= 1, got " + rows);
        }
        this.rows = rows;
        this.cols = cols;
        this.size = (long) rows * cols;
    }

    protected abstract boolean[][] snapshotWithoutBoundChecking(int fromRow, int fromColumn, int rows, int columns);

    @Override
    public final boolean[][] snapshot() {
        return snapshotWithoutBoundChecking(0, 0, rows, cols);
    }

    @Override
    public final boolean[][] snapshot(int fromRow, int fromColumn, int rows, int columns) {
        Objects.checkFromIndexSize(fromRow, rows, this.rows);
        Objects.checkFromIndexSize(fromColumn, columns, this.cols);
        return snapshotWithoutBoundChecking(fromRow, fromColumn, rows, columns);
    }

    @Override
    public final int getRows() {
        return rows;
    }

    @Override
    public final int getColumns() {
        return cols;
    }

    @Override
    public final long getSize() {
        return size;
    }
}
