package com.voidaspect.jgol.grid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaddedInMemoryGridTest {

    @Test
    void shouldNotAllowInvalidGrids() {
        assertThrows(IllegalArgumentException.class, () -> new PaddedInMemoryGrid(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new PaddedInMemoryGrid(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new PaddedInMemoryGrid(1, 0));
        assertThrows(IllegalArgumentException.class, () -> new PaddedInMemoryGrid(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new PaddedInMemoryGrid(0, -1));
        assertThrows(IllegalArgumentException.class, () -> new PaddedInMemoryGrid(-1, -1));
    }

    @Test
    void shouldAccessGridByIndex() {
        var grid = new PaddedInMemoryGrid(new boolean[][]{
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
        var grid = new PaddedInMemoryGrid(1, 1);
        assertArrayEquals(new boolean[][]{{false}}, grid.snapshot());
        assertEquals(1, grid.getRows());
        assertEquals(1, grid.getColumns());
        assertEquals(1, grid.getSize());
    }

    @Test
    void shouldCaptureSnapshots() {
        var grid = new PaddedInMemoryGrid(3, 3);
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

        var grid = new PaddedInMemoryGrid(initial, 3, 3);

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
        var grid = new PaddedInMemoryGrid(new boolean[][]{
                {true, true, true},
                {true, true, true},
                {true, true, true}
        }, 3, 3);
        boolean[][] expected = {
                {false, false, false},
                {false, false, false},
                {false, false, false}
        };
        grid.clear();
        assertArrayEquals(expected, grid.snapshot());
    }
}