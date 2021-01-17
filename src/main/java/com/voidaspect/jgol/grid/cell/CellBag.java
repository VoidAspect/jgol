package com.voidaspect.jgol.grid.cell;

import java.util.Arrays;

/**
 * An auxiliary data structure designed to hold a collection of cell indices of a 2-dimensional matrix.
 * <br>Implemented as a Structure of Arrays with on-demand resizing.</br>
 * <br>Arrays <b>r</b> and <b>c</b> hold row and column indices of cells respectively.</br>
 * <p> API of {@code CellBag} allows:</p>
 * <ol>
 *     <li>to add cell indices one by one</li>
 *     <li>to perform {@link CellOperation} on all cells</li>
 * </ol>
 */
public final class CellBag {

    private static final int MAX_ARRAY_LENGTH = Integer.MAX_VALUE - 8;

    private static final int INITIAL_CAPACITY = 64;

    private int[] r;

    private int[] c;

    private int size;

    private int capacity;

    public void forEach(CellOperation action) {
        for (int i = 0; i < size; i++) {
            action.apply(r[i], c[i]);
        }
    }

    public void add(int row, int col) {
        //region ensure capacity
        if (size == 0) { // initial allocation
            capacity = INITIAL_CAPACITY;
            r = new int[INITIAL_CAPACITY];
            c = new int[INITIAL_CAPACITY];
        } else if (size == capacity) { // resize on-demand
            if (size == MAX_ARRAY_LENGTH) {
                throw new IllegalStateException("cell bag too big");
            }
            // multiply by 1.5
            int needed = capacity + (capacity >> 1);
            if (needed < 0 || needed > MAX_ARRAY_LENGTH) { // handle overflow
                needed = MAX_ARRAY_LENGTH;
            }
            capacity = needed;
            r = Arrays.copyOf(r, capacity);
            c = Arrays.copyOf(c, capacity);
        }
        //endregion
        int index = size++;
        r[index] = row;
        c[index] = col;
    }

    public int size() {
        return size;
    }

}
