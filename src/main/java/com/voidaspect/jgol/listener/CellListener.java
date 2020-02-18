package com.voidaspect.jgol.listener;

public interface CellListener {

    void onCellSpawned(int row, int col);

    void onCellDied(int row, int col);

}
