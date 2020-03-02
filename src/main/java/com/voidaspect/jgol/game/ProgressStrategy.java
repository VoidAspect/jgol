package com.voidaspect.jgol.game;

import com.voidaspect.jgol.Finishable;
import com.voidaspect.jgol.Freezable;
import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.CellListener;

public interface ProgressStrategy extends Finishable, Freezable {

    void progress(Grid grid, CellListener listener);

}
