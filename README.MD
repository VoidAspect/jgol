# jgol - Conway's Game of Life in Java

This library implements a cellular automaton called
[Game of Life, or, simply, Life](https://www.conwaylife.com/wiki/Conway%27s_Game_of_Life)
as defined by J. Conway.

## API
Interface `com.voidaspect.jgol.GameOfLife` is the main entry point.
It provides access to the game's grid and methods for game's progression.

### Progression Configuration
Actual implementation is flexibly configured via `com.voidaspect.jgol.game.GameOfLifeBuilder`.
It supports optional thread safety

Each chunk, by default, is a **square 1000x1000** region of the grid.

### Grid configuration
Game's state is an object that implements `com.voidaspect.jgol.grid.Grid` interface.
It represents the notion of a two-dimensional grid with two states for each cell.

Implementation can be flexible: grid can be strictly finite or circular, in-memory or persistent etc.

It supports read and write access and allows to count live neighbors of a cell.

Cell on a grid is defined by two indices: `row` and `col`.

For example, `com.voidaspect.jgol.grid.PaddedInMemoryGrid` uses a two-dimensional array of type
`boolean[][]` to store cell values. 
This implementation is optimized for large dense grids.

For sparse grids, `com.voidaspect.jgol.grid.HashGrid` is preferable.

### Progress Listener
`com.voidaspect.jgol.listener.ProgressListener` interface allows client to process events when game progresses.

It supports 4 events:
* progress started (game started to calculate new generation)
* cell spawned (dead cell had exactly 3 live neighbors)
* cell died (live cell had less than 2 or more than 3 live neighbors)
* progress finished (new generation created)

Thread safety is desirable for `ProgressListener` implementations.
