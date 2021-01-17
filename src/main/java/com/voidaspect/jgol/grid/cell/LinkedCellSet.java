package com.voidaspect.jgol.grid.cell;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;

public class LinkedCellSet extends CellSet {

    public LinkedCellSet() {
        super(new LongLinkedOpenHashSet());
    }

    public LinkedCellSet(int size) {
        super(new LongLinkedOpenHashSet(size));
    }

}
