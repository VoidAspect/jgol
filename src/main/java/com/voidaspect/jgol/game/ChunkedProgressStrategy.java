package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;

abstract class ChunkedProgressStrategy implements ProgressStrategy {

    final Grid grid;

    protected ChunkedProgressStrategy(Grid grid) {
        this.grid = grid;
    }

    NextGen progressChunk(int fromRow, int fromCol, int toRow, int toCol) {
        toRow = Math.min(grid.getColumns(), toRow);
        toCol = Math.min(grid.getRows(), toCol);
        var ng = new NextGen(grid);
        for (int row = fromRow; row < toRow; row++) {
            for (int col = fromCol; col < toCol; col++) {
                int neighbors = grid.neighbors(row, col);
                // depending on whether the cell is alive
                if (grid.get(row, col)) {
                    // overcrowding or underpopulation
                    if (neighbors < 2 || neighbors > 3) ng.willDie(row, col);
                } else {
                    // reproduction
                    if (neighbors == 3) ng.willSpawn(row, col);
                }
            }
        }
        return ng;
    }
}
