package com.voidaspect.jgol.grid;

import com.voidaspect.jgol.grid.cell.CellOperation;
import com.voidaspect.jgol.grid.cell.Cells;
import com.voidaspect.jgol.grid.cell.LinkedCellSet;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;

public final class NeighborCountingHashGrid extends AbstractGrid {

    private final LinkedCellSet cells;

    private final NeighborMap neighbors;

    public NeighborCountingHashGrid() {
        this.cells = new LinkedCellSet();
        this.neighbors = new NeighborMap();
    }

    public NeighborCountingHashGrid(boolean[][] initial) {
        this();
        fillGrid(initial);
    }

    @Override
    public boolean get(int row, int col) {
        return cells.contains(row, col);
    }

    @Override
    public void set(int row, int col, boolean state) {
        boolean modified;
        byte neighbor;
        if (state) {
            modified = cells.add(row, col);
            neighbor = 1;
        } else {
            modified = cells.remove(row, col);
            neighbor = -1;
        }

        if (!modified) return;

        //@formatter:off
        int up    = row - 1;
        int down  = row + 1;
        int left  = col - 1;
        int right = col + 1;
        neighbors.add(up,   right, neighbor); neighbors.add(up,   col, neighbor); neighbors.add(up,   left, neighbor);
        neighbors.add(row,  right, neighbor);           /* this cell */           neighbors.add(row,  left, neighbor);
        neighbors.add(down, right, neighbor); neighbors.add(down, col, neighbor); neighbors.add(down, left, neighbor);
        //@formatter:on
        
    }

    @Override
    public int neighbors(int row, int col) {
        return neighbors.get(row, col);
    }

    @Override
    public long liveCells() {
        return cells.size();
    }

    @Override
    public void clear() {
        cells.clear();
        neighbors.clear();
    }

    @Override
    public void forEachAlive(CellOperation operation) {
        cells.forEach(operation);
    }

    private static final class NeighborMap {

        final Long2ByteOpenHashMap mapping;

        NeighborMap() {
            this.mapping = new Long2ByteOpenHashMap();
        }

        void add(int row, int col, byte neighbors) {
            mapping.addTo(Cells.pack(row, col), neighbors);
        }

        byte get(int row, int col) {
            return mapping.get(Cells.pack(row, col));
        }

        void clear() {
            mapping.clear();
        }
    }
}
