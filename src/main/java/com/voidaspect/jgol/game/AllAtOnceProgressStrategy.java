package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.grid.cell.CellBag;
import com.voidaspect.jgol.grid.cell.CellSet;
import com.voidaspect.jgol.listener.CellListener;

final class AllAtOnceProgressStrategy extends AbstractProgressStrategy {

    @Override
    int progressAndCountUpdates(Grid grid, CellListener listener) {
        var ng = new NextGen(grid, listener);
        grid.forEachAlive(ng::evaluate);
        ng.updateGrid();
        return ng.countUpdates();
    }

    private static final class NextGen {

        final Grid grid;

        final CellListener listener;

        final CellSet visited;

        final CellBag spawned;

        final CellBag died;

        NextGen(Grid grid, CellListener listener) {
            this.grid = grid;
            this.listener = listener;
            long estimatedDeadNeighbors = Math.min(grid.liveCells() * 8, grid.getSize() - grid.liveCells());
            this.visited = new CellSet((int) estimatedDeadNeighbors);
            this.spawned = new CellBag();
            this.died = new CellBag();
        }

        void willDie(int row, int col) {
            died.add(row, col);
            listener.onCellDied(row, col);
        }

        void willSpawn(int row, int col) {
            spawned.add(row, col);
            listener.onCellSpawned(row, col);
        }

        int countUpdates() {
            return spawned.size() + died.size();
        }

        void evaluate(int row, int col) {
            int neighbors = grid.neighbors(row, col);

            if (neighbors < 2 || neighbors > 3) {
                // overcrowding or underpopulation
                willDie(row, col);
            }

            //@formatter:off
            int up    = row - 1;
            int down  = row + 1;
            int left  = col - 1;
            int right = col + 1;
            visit(up,   left); visit(up,   col); visit(up,   right);
            visit(row,  left); /*current cell*/  visit(row,  right);
            visit(down, left); visit(down, col); visit(down, right);
            //@formatter:on
        }

        private void visit(int row, int col) {
            // only evaluate existing dead cells that were not yet visited
            if (!grid.hasCell(row, col) || grid.get(row, col)) return;
            if (!visited.add(row, col)) return;

            int neighbors = grid.neighbors(row, col);

            if (neighbors == 3) {
                // reproduction
                willSpawn(row, col);
            }
        }

        void updateGrid() {
            spawned.forEach((row, col) -> grid.set(row, col, true));
            died.forEach((row, col) -> grid.set(row, col, false));
        }
    }

}
