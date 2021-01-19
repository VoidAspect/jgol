package com.voidaspect.jgol.grid;

public abstract class AbstractGrid implements Grid {

    @Override
    public boolean[][] snapshot(int fromRow, int fromColumn, int rows, int columns) {
        boolean[][] snapshot = new boolean[rows][columns];
        long liveCells = liveCells();
        if (liveCells == 0) return snapshot;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                boolean alive = snapshot[row][col] = get(row + fromRow, col + fromColumn);
                if (alive && --liveCells == 0) {
                    return snapshot;
                }
            }
        }
        return snapshot;
    }

    protected void fillGrid(boolean[][] initial) {
        if (initial == null) return;
        int rows = initial.length;
        for (int r = 0; r < rows; r++) {
            boolean[] row = initial[r];
            if (row == null) continue;
            int columns = row.length;
            for (int c = 0; c < columns; c++) {
                if (row[c]) {
                    set(r, c, true);
                }
            }
        }
    }

}
