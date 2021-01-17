package com.voidaspect.jgol.grid;

import com.voidaspect.jgol.grid.cell.CellOperation;

import java.util.Objects;

public abstract class AbstractFiniteGrid implements Grid {

    public static final int MIN_SIZE = 1;

    protected final int rows;

    protected final int cols;

    protected final long size;

    public AbstractFiniteGrid(int rows, int cols) {
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

    protected final void checkIndex(int row, int col) {
        Objects.checkIndex(row, rows);
        Objects.checkIndex(col, cols);
    }

    protected boolean[][] snapshotWithoutBoundChecking(int fromRow, int fromColumn, int rows, int columns) {
        var snapshot = new boolean[rows][columns];
        for (int row = fromRow; row < rows; row++) {
            boolean[] snapshotRow = snapshot[row - fromRow];
            for (int col = fromColumn; col < columns; col++) {
                snapshotRow[col - fromColumn] = get(row, col);
            }
        }
        return snapshot;
    }

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

    @Override
    public final boolean hasCell(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    @Override
    public void clear() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                set(row, col, false);
            }
        }
    }

    @Override
    public void forEachAlive(CellOperation operation) {
        long remaining = liveCells();
        if (remaining == 0) return;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (get(row, col)) {
                    operation.apply(row, col);
                    if (--remaining == 0) return;
                }
            }
        }
    }

    protected void fillGrid(boolean[][] initial) {
        if (initial == null) return;
        int rowsLength = Math.min(rows, initial.length);
        for (int r = 0; r < rowsLength; r++) {
            boolean[] row = initial[r];
            if (row == null) continue;
            int columnLength = Math.min(cols, row.length);
            for (int c = 0; c < columnLength; c++) {
                if (row[c]) {
                    set(r, c, true);
                }
            }
        }
    }

}
