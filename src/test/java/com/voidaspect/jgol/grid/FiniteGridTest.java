package com.voidaspect.jgol.grid;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public abstract class FiniteGridTest {

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
    void shouldAccessGridByIndex() {
        var grid = grid(new boolean[][]{
                {false, false, true},
                {false, false}
        }, 3, 3);

        assertFalse(grid.get(0, 0));
        assertFalse(grid.get(0, 1));

        assertTrue(grid.get(0, 2));

        grid.set(0, 2, false);
        assertFalse(grid.get(0, 2));

        assertFalse(grid.get(1, 0));
        assertFalse(grid.get(1, 1));
        assertFalse(grid.get(1, 2));
        assertFalse(grid.get(2, 0));
        assertFalse(grid.get(2, 1));
        assertFalse(grid.get(2, 2));

        assertThrows(IndexOutOfBoundsException.class, () -> grid.set(-1, 0, true));
        assertThrows(IndexOutOfBoundsException.class, () -> grid.set(0, -1, true));
        assertThrows(IndexOutOfBoundsException.class, () -> grid.set(3, 1, true));
        assertThrows(IndexOutOfBoundsException.class, () -> grid.set(1, 3, true));
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

    @Test
    void shouldSnapshotRegion() {
        boolean[][] initial = {
                {false, false, false},
                {false, true, false},
                {false, false, false},
        };

        var grid = grid(initial, 3, 3);

        assertArrayEquals(initial, grid.snapshot(0, 0, 3, 3));
        assertArrayEquals(new boolean[][]{
                {false, false, false},
                {false, true, false},
        }, grid.snapshot(0, 0, 2, 3));
        assertArrayEquals(new boolean[][]{
                {false, false},
                {false, true},
        }, grid.snapshot(0, 0, 2, 2));
        assertArrayEquals(new boolean[][]{
                {false, false},
                {false, true},
                {false, false}
        }, grid.snapshot(0, 0, 3, 2));
        assertArrayEquals(new boolean[][]{{false}}, grid.snapshot(2, 2, 1, 1));
        assertArrayEquals(new boolean[0][], grid.snapshot(2, 2, 0, 0));

        assertThrows(IndexOutOfBoundsException.class, () -> grid
                .snapshot(-1, -1, 1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> grid
                .snapshot(0, 0, 4, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> grid
                .snapshot(0, 0, 1, 4));
        assertThrows(IndexOutOfBoundsException.class, () -> grid
                .snapshot(0, 0, -1, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> grid
                .snapshot(0, 0, 3, -1));
    }

    @Test
    void shouldClearGrid() {
        var grid = grid(new boolean[][]{
                {true, true},
                {true, true},
        }, 2, 2);
        boolean[][] expected = {
                {false, false},
                {false, false},
        };
        grid.clear();
        assertArrayEquals(expected, grid.snapshot());
    }

    @Test
    void shouldIterateOverLiveCells() {
        var grid = grid(new boolean[][] {
                {true, false, true},
                {false, false, false},
                {true, true, true}
        }, 3, 3);

        Map<Integer, Set<Integer>> alive = new HashMap<>();

        grid.forEachAlive(((row, col) -> alive.compute(row, (key, value) -> {
            if (value == null) value = new HashSet<>();
            value.add(col);
            return value;
        })));

        assertEquals(Map.of(
                0, Set.of(0, 2),
                2, Set.of(0, 1, 2)
        ), alive);
    }

    protected abstract Grid grid(int rows, int cols);

    protected abstract Grid grid(boolean[][] initial, int rows, int cols);

    protected Grid testedGrid(int rows, int cols) {
        return new NeighborCountingGrid(rows, cols);
    }

    protected Grid testedGrid(boolean[][] initial, int rows, int cols) {
        return new PaddedInMemoryGrid(initial, rows, cols);
    }
}
