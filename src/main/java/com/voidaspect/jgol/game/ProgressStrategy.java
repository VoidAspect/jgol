package com.voidaspect.jgol.game;

import com.voidaspect.jgol.Freezable;
import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.CellListener;

public interface ProgressStrategy extends Freezable {

    void progress(Grid grid, CellListener listener);

}
