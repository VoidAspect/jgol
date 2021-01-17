package com.voidaspect.jgol.grid.cell;

public final class Cells {

    private Cells() {
    }

    public static long pack(int row, int col) {
        return (long) row << 32 | col & 0xffffffffL;
    }

    public static int unpackRow(long cell) {
        return (int) (cell >> 32);
    }

    public static int unpackCol(long cell) {
        return (int) cell;
    }

}
