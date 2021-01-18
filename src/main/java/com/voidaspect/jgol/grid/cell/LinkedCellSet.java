package com.voidaspect.jgol.grid.cell;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;

public final class LinkedCellSet extends AbstractCellSet<LongLinkedOpenHashSet> {

    public LinkedCellSet() {
        super(new LongLinkedOpenHashSet());
    }

    public LinkedCellSet(int size) {
        super(new LongLinkedOpenHashSet(size));
    }

}
