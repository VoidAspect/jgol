package com.voidaspect.jgol.grid;

public interface Grid {

    boolean get(int row, int col);

    void set(int row, int col, boolean state);

    int neighbors(int row, int col);

    boolean[][] snapshot();

    void clear();

    int getRows();

    int getColumns();

    long getSize();
}
