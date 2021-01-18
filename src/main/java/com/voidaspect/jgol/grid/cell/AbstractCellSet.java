package com.voidaspect.jgol.grid.cell;

import it.unimi.dsi.fastutil.longs.LongSet;

class AbstractCellSet<T extends LongSet> {

    final T cells;

    AbstractCellSet(T cells) {
        this.cells = cells;
    }

    public final boolean add(int row, int col) {
        return cells.add(Cells.pack(row, col));
    }

    public final boolean remove(int row, int col) {
        return cells.remove(Cells.pack(row, col));
    }

    public final boolean contains(int row, int col) {
        return cells.contains(Cells.pack(row, col));
    }

    public final int size() {
        return cells.size();
    }

    public final void forEach(CellOperation operation) {
        cells.forEach((long cell) -> apply(operation, cell));
    }

    public final void clear() {
        cells.clear();
    }

    final void apply(CellOperation operation, long cell) {
        operation.apply(Cells.unpackRow(cell), Cells.unpackCol(cell));
    }

}
