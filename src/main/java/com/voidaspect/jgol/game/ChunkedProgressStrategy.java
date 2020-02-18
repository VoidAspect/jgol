package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.CellListener;

abstract class ChunkedProgressStrategy implements ProgressStrategy {

    final Grid grid;

    ChunkedProgressStrategy(Grid grid) {
        this.grid = grid;
    }

    NextGen progressChunk(CellListener listener, int fromRow, int fromCol, int toRow, int toCol) {
        toRow = Math.min(grid.getColumns(), toRow);
        toCol = Math.min(grid.getRows(), toCol);
        var ng = new NextGen(grid);
        for (int row = fromRow; row < toRow; row++) {
            for (int col = fromCol; col < toCol; col++) {
                int neighbors = grid.neighbors(row, col);
                // depending on whether the cell is alive
                if (grid.get(row, col)) {
                    if (neighbors < 2 || neighbors > 3) {
                        // overcrowding or underpopulation
                        ng.willDie(row, col);
                        listener.onCellDied(row, col);
                    }
                } else if (neighbors == 3) {
                    // reproduction
                    ng.willSpawn(row, col);
                    listener.onCellSpawned(row, col);
                }
            }
        }
        return ng;
    }
}
