package com.voidaspect.jgol.grid;

public interface FiniteGrid extends Grid {

    boolean[][] snapshot();

    boolean exists(int row, int col);

    int getRows();

    int getColumns();

    long getSize();

}
