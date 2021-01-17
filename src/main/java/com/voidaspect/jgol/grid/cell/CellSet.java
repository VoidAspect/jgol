package com.voidaspect.jgol.grid.cell;

import java.util.HashSet;
import java.util.Set;

public class CellSet {

    private final Set<Long> cells;

    public CellSet() {
        cells = new HashSet<>();
    }

    public CellSet(int size) {
        cells = new HashSet<>(size);
    }

    protected CellSet(Set<Long> cells) {
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
        for (long cell : cells) {
            operation.apply(Cells.unpackRow(cell), Cells.unpackCol(cell));
        }
    }

    public void clear() {
        cells.clear();
    }
}
