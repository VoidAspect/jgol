package com.voidaspect.jgol;

import com.voidaspect.jgol.grid.Grid;

/**
 * Conway's Game of Life - stateful cellular automaton governed by following rules:
 * <ol>
 *     <li>each cell can be either dead or alive</li>
 *     <li>if cell is alive and it has less than 2 alive neighbors - it becomes dead</li>
 *     <li>if cell is alive and it has more than 3 alive neighbors - it becomes dead</li>
 *     <li>if cell is dead and it has exactly 3 alive neighbors - it becomes alive</li>
 * </ol>
 * State is updated via {@link GameOfLife#progress()} method.
 */
public interface GameOfLife {

    void progress();

    void finish();

    Grid grid();

}
