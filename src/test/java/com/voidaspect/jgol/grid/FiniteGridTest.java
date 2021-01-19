package com.voidaspect.jgol.grid;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public abstract class FiniteGridTest extends GridTest {

    @Test
    void shouldBeFinite() {
        var grid = grid(3, 3);

        assertTrue(grid.exists(0, 0));
        assertTrue(grid.exists(0, 1));
        assertTrue(grid.exists(0, 2));
        assertTrue(grid.exists(1, 0));
        assertTrue(grid.exists(1, 1));
        assertTrue(grid.exists(1, 2));
        assertTrue(grid.exists(2, 0));
        assertTrue(grid.exists(2, 1));
        assertTrue(grid.exists(2, 2));

        assertFalse(grid.exists(-1, 0));
        assertFalse(grid.exists(0, -1));
        assertFalse(grid.exists(3, 1));
        assertFalse(grid.exists(1, 3));
    }

    @Test
    void shouldNotAllowInvalidGrids() {
        assertThrows(IllegalArgumentException.class, () -> grid(0, 0));
        assertThrows(IllegalArgumentException.class, () -> grid(0, 1));
        assertThrows(IllegalArgumentException.class, () -> grid(1, 0));
        assertThrows(IllegalArgumentException.class, () -> grid(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> grid(0, -1));
        assertThrows(IllegalArgumentException.class, () -> grid(-1, -1));
    }

    @Test
    void gridSizeShouldBeEqualToRowsMultiplyByCols() {
        var grid = grid(2, 3);
        assertEquals(2, grid.getRows());
        assertEquals(3, grid.getColumns());
        assertEquals(6, grid.getSize());
        grid = grid(4, 4);
        assertEquals(4, grid.getRows());
        assertEquals(4, grid.getColumns());
        assertEquals(16, grid.getSize());
    }

    @Test
    void shouldCreate1x1Grid() {
        var grid = grid(1, 1);
        assertArrayEquals(new boolean[][]{{false}}, grid.snapshot());
        assertEquals(1, grid.getRows());
        assertEquals(1, grid.getColumns());
        assertEquals(1, grid.getSize());
    }

    @Test
    void shouldCaptureSnapshots() {
        var grid = grid(3, 3);
        assertArrayEquals(new boolean[][]{
                {false, false, false},
                {false, false, false},
                {false, false, false},
        }, grid.snapshot());
        grid.set(1, 1, true);
        assertArrayEquals(new boolean[][]{
                {false, false, false},
                {false, true, false},
                {false, false, false},
        }, grid.snapshot());
    }

    @Override
    protected FiniteGrid grid(boolean[][] initial) {
        return grid(initial, initial.length, initial[0].length);
    }

    protected abstract FiniteGrid grid(int rows, int cols);

    protected abstract FiniteGrid grid(boolean[][] initial, int rows, int cols);

    protected FiniteGrid testedGrid(int rows, int cols) {
        return new NeighborCountingGrid(rows, cols);
    }

    protected FiniteGrid testedGrid(boolean[][] initial, int rows, int cols) {
        return new PaddedInMemoryGrid(initial, rows, cols);
    }
}
