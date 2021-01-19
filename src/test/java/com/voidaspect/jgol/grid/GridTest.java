package com.voidaspect.jgol.grid;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class GridTest {

    @Test
    void shouldAccessGridByIndex() {
        var grid = grid(new boolean[][]{
                {false, false, true},
                {false, false}
        });

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

        assertFalse(grid.get(-1, 0));
        assertFalse(grid.get(0, -1));
        assertFalse(grid.get(3, 1));
        assertFalse(grid.get(1, 3));

        assertEquals(0, grid.neighbors(-1, 0));
        assertEquals(0, grid.neighbors(0, -1));
        assertEquals(0, grid.neighbors(3, 1));
        assertEquals(0, grid.neighbors(1, 3));
    }

    @Test
    void shouldSnapshotRegion() {
        boolean[][] initial = {
                {false, false, false},
                {false, true, false},
                {false, false, false},
        };

        var grid = grid(initial);

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
    }

    @Test
    void shouldClearGrid() {
        var grid = grid(new boolean[][]{
                {true, true},
                {true, true},
        });
        boolean[][] expected = {
                {false, false},
                {false, false},
        };
        grid.clear();
        assertArrayEquals(expected, grid.snapshot(0, 0, 2, 2));
    }

    @Test
    void shouldIterateOverLiveCells() {
        var grid = grid(new boolean[][] {
                {true, false, true},
                {false, false, false},
                {true, true, true}
        });

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

    @Test
    void shouldReturnLiveCellAmount() {
        var grid = grid(new boolean[][] {
                {true, false, true},
                {false, false, false},
                {true, true, true}
        });

        assertEquals(5, grid.liveCells());

        grid.set(0, 0, false);
        assertEquals(4, grid.liveCells());

        grid.set(0, 0, false);
        assertEquals(4, grid.liveCells());

        grid.set(0, 1, true);
        assertEquals(5, grid.liveCells());

        grid.set(0, 1, true);
        assertEquals(5, grid.liveCells());

        grid.clear();
        assertEquals(0, grid.liveCells());
    }

    protected abstract Grid grid(boolean[][] initial);

    protected Grid testedGrid() {
        return new NeighborCountingHashGrid();
    }

    protected Grid testedGrid(boolean[][] initial) {
        return new HashGrid(initial);
    }
}
