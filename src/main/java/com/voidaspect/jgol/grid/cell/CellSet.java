package com.voidaspect.jgol.grid.cell;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public final class CellSet extends AbstractCellSet<LongOpenHashSet> {

    public CellSet() {
        super(new LongOpenHashSet());
    }

    public CellSet(int size) {
        super(new LongOpenHashSet(size));
    }

}
