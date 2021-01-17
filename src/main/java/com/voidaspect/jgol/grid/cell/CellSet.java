package com.voidaspect.jgol.grid.cell;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

public class CellSet {

    private final LongSet cells;

    public CellSet() {
        cells = new LongOpenHashSet();
    }

    public CellSet(int size) {
        cells = new LongOpenHashSet(size);
    }

    public CellSet(LongSet cells) {
        this.cells = cells;
    }

    public boolean add(int row, int col) {
        return cells.add(Cells.pack(row, col));
    }

    public boolean remove(int row, int col) {
        return cells.remove(Cells.pack(row, col));
    }

    public boolean contains(int row, int col) {
        return cells.contains(Cells.pack(row, col));
    }

    public int size() {
        return cells.size();
    }

    public void forEach(CellOperation operation) {
        cells.forEach((long cell) -> operation.apply(Cells.unpackRow(cell), Cells.unpackCol(cell)));
    }

    public void clear() {
        cells.clear();
    }
}
