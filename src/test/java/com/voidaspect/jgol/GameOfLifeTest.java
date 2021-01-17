package com.voidaspect.jgol;

import com.voidaspect.jgol.grid.BitVectorInMemoryGrid;
import com.voidaspect.jgol.grid.HashGrid;
import com.voidaspect.jgol.listener.LoggingProgressListener;
import com.voidaspect.jgol.listener.ProgressListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameOfLifeTest {

    private final LoggingProgressListener lpl = new LoggingProgressListener();

    @Test
    void shouldSendProgressEvents() {
        var progressListener = mock(ProgressListener.class);
        var game = game(new byte[][]{
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 1, 1, 1, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0}
        });
        game.progress(progressListener);

        verify(progressListener).onProgressStart();
        verify(progressListener).onProgressFinish();
        verify(progressListener).onCellSpawned(1, 2);
        verify(progressListener).onCellSpawned(3, 2);
        verify(progressListener).onCellDied(2, 1);
        verify(progressListener).onCellDied(2, 3);
        verifyNoMoreInteractions(progressListener);
    }

    @Test
    void shouldProgressOverManyGenerations() {
        boolean[][] initial = {
                {true, true},
                {true, true}
        };
        int generations = Integer.MAX_VALUE;
        var game = GameOfLife.builder(new HashGrid(initial, 20000, 20000)).build();
        assertFalse(game.isFrozen());
        while (generations-- > 0) {
            game.progress();
        }
        assertTrue(game.isFrozen());
        assertArrayEquals(initial, game.grid().snapshot(0, 0, 2, 2));

        game.grid().clear();

        game.grid().set(0, 0, true);
        game.grid().set(0, 1, true);
        assertFalse(game.isFrozen());

        boolean[][] expected = {{false, false}};
        game.progress(lpl);
        assertFalse(game.isFrozen());
        assertArrayEquals(expected, game.grid().snapshot(0, 0, 1, 2));

        game.progress(lpl);
        assertTrue(game.isFrozen());
        assertArrayEquals(expected, game.grid().snapshot(0, 0, 1, 2));
    }

    @Test
    void shouldProgressOnLargeEmptyGrid() {
        int side = 20_000;
        var grid = new HashGrid(side, side);
        var game = GameOfLife.builder(grid).build();
        game.progress();
    }

    @Test
    void shouldHandleLargeGrid() {
        int side = 10000;
        int edge = side - 1;
        var grid = new BitVectorInMemoryGrid(side, side);
        var game = GameOfLife.builder(grid).build();
        boolean[][] expected = new boolean[side][side];
        assertArrayEquals(expected, grid.snapshot());
        // init loads of overcrowded cells - they should die off
        for (int i = 0; i < side; i++) {
            for (int j = 0; j < side; j++) {
                grid.set(i, j, true);
            }
        }
        expected[0][0] = true;
        expected[0][edge] = true;
        expected[edge][0] = true;
        expected[edge][edge] = true;
        game.progress();
        assertArrayEquals(expected, grid.snapshot());
        // lonely cells at the corners die off
        expected[0][0] = false;
        expected[0][edge] = false;
        expected[edge][0] = false;
        expected[edge][edge] = false;
        game.progress();
        assertArrayEquals(expected, grid.snapshot());
        assertTimeout(Duration.ofMillis(5), (Executable) game::progress);
        assertArrayEquals(expected, grid.snapshot());
    }

    @Test
    void aloneShouldDieOff() {
        var game = game(new byte[][]{
                {0, 0, 0},
                {0, 1, 0},
                {0, 0, 0}
        });
        byte[][] expected = {
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        };
        game.progress(lpl);
        assertGame(expected, game);
        game.progress(lpl);
        assertGame(expected, game);
    }

    @Test
    void twoTogetherShouldDieOff() {
        byte[][] initial = {
                {0, 0, 0},
                {0, 1, 1},
                {0, 0, 0}
        };
        byte[][] expected = {
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        };
        var game = game(initial);
        game.progress(lpl);
        assertGame(expected, game);
        game.progress(lpl);
        assertGame(expected, game);
    }

    @Test
    void cellWithThreeNeighborsShouldSpawn() {
        byte[][] initial = {
                {1, 1, 0},
                {1, 0, 0},
                {0, 0, 0}
        };
        var game = game(initial);
        byte[][] expected = {
                {1, 1, 0},
                {1, 1, 0},
                {0, 0, 0}
        };
        game.progress(lpl);
        assertGame(expected, game);
        game.progress(lpl);
        assertGame(expected, game);
    }

    @Test
    void cellWithFourOrMoreNeighborsShouldDieOff() {
        byte[][] initial = {
                {1, 1, 0},
                {1, 1, 0},
                {0, 0, 1}
        };
        var game = game(initial);
        game.progress();
        byte[][] expected = {
                {1, 1, 0},
                {1, 0, 1},
                {0, 1, 0}
        };
        assertGame(expected, game);
        game.progress(lpl);
        assertGame(expected, game);
    }

    @Test
    void tubShouldSurvive_Still() {
        byte[][] initial = {
                {0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 1, 0, 1, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0}
        };
        var game = game(initial);
        game.progress(lpl);
        assertGame(initial, game);
        game.progress(lpl);
        assertGame(initial, game);
    }

    @Test
    void blockShouldSurvive_Still() {
        byte[][] initial = {
                {0, 0, 0, 0},
                {0, 1, 1, 0},
                {0, 1, 1, 0},
                {0, 0, 0, 0}
        };
        var game = game(initial);
        game.progress(lpl);
        assertGame(initial, game);
        game.progress(lpl);
        assertGame(initial, game);
    }

    @Test
    void boatShouldSurvive_Still() {
        byte[][] initial = {
                {0, 0, 0, 0, 0},
                {0, 1, 1, 0, 0},
                {0, 1, 0, 1, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0}
        };
        var game = game(initial);
        game.progress(lpl);
        assertGame(initial, game);
        game.progress(lpl);
        assertGame(initial, game);
    }

    @Test
    void beeHiveShouldSurvive_Still() {
        byte[][] initial = {
                {0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 0, 0},
                {0, 1, 0, 0, 1, 0},
                {0, 0, 1, 1, 0, 0},
                {0, 0, 0, 0, 0, 0}
        };
        var game = game(initial);
        game.progress(lpl);
        assertGame(initial, game);
        game.progress(lpl);
        assertGame(initial, game);
    }

    @Test
    void loafShouldSurvive_Still() {
        byte[][] initial = {
                {0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 0, 0},
                {0, 1, 0, 0, 1, 0},
                {0, 0, 1, 0, 1, 0},
                {0, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0, 0}
        };
        var game = game(initial);
        game.progress(lpl);
        assertGame(initial, game);
        game.progress(lpl);
        assertGame(initial, game);
    }

    @Test
    void blinkerShouldOscillate_Period_2() {
        byte[][] initial = {
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 1, 1, 1, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0}
        };
        byte[][] next = {
                {0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 1, 0, 0},
                {0, 0, 0, 0, 0}
        };
        var game = game(initial);
        game.progress(lpl);
        assertGame(next, game);
        game.progress(lpl);
        assertGame(initial, game);
        game.progress(lpl);
        assertGame(next, game);
    }

    @Test
    void beaconShouldOscillate_Period_2() {
        byte[][] initial = {
                {0, 0, 0, 0, 0, 0},
                {0, 1, 1, 0, 0, 0},
                {0, 1, 0, 0, 0, 0},
                {0, 0, 0, 0, 1, 0},
                {0, 0, 0, 1, 1, 0},
                {0, 0, 0, 0, 0, 0}
        };
        byte[][] next = {
                {0, 0, 0, 0, 0, 0},
                {0, 1, 1, 0, 0, 0},
                {0, 1, 1, 0, 0, 0},
                {0, 0, 0, 1, 1, 0},
                {0, 0, 0, 1, 1, 0},
                {0, 0, 0, 0, 0, 0}
        };
        var game = game(initial);
        game.progress(lpl);
        assertGame(next, game);
        game.progress(lpl);
        assertGame(initial, game);
        game.progress(lpl);
        assertGame(next, game);
    }

    @Test
    void toadShouldOscillate_Period_2() {
        byte[][] initial = {
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 1, 0},
                {0, 1, 1, 1, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0}
        };
        byte[][] next = {
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, 0, 0},
                {0, 1, 0, 0, 1, 0},
                {0, 1, 0, 0, 1, 0},
                {0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0}
        };
        var game = game(initial);
        game.progress(lpl);
        assertGame(next, game);
        game.progress(lpl);
        assertGame(initial, game);
        game.progress(lpl);
        assertGame(next, game);
    }

    @Test
    void gliderShouldMove_reform_Period_4() {
        byte[][] initial = {
                {0, 0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0},
                {0, 0, 0, 1, 0, 0},
                {0, 1, 1, 1, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0}
        };
        byte[][] step1 = {
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 1, 0, 1, 0, 0},
                {0, 0, 1, 1, 0, 0},
                {0, 0, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0}
        };
        byte[][] step2 = {
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, 0, 0},
                {0, 1, 0, 1, 0, 0},
                {0, 0, 1, 1, 0, 0},
                {0, 0, 0, 0, 0, 0}
        };
        byte[][] step3 = {
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0},
                {0, 0, 0, 1, 1, 0},
                {0, 0, 1, 1, 0, 0},
                {0, 0, 0, 0, 0, 0}
        };
        byte[][] step4 = {
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, 0, 0},
                {0, 0, 0, 0, 1, 0},
                {0, 0, 1, 1, 1, 0},
                {0, 0, 0, 0, 0, 0}
        };
        int side = 100_000;
        var game = game(initial, side, side);
        game.progress(lpl);
        assertGame(step1, game);
        game.progress(lpl);
        assertGame(step2, game);
        game.progress(lpl);
        assertGame(step3, game);
        game.progress(lpl);
        assertGame(step4, game);
    }

    private static void assertGame(byte[][] expected, GameOfLife game) {
        var grid = game.grid();
        for (int i = 0; i < expected.length; i++) {
            for (int j = 0; j < expected[i].length; j++) {
                byte actual = grid.get(i, j) ? (byte) 1 : (byte) 0;
                assertEquals(expected[i][j], actual, "mismatch at row=" + i + " column=" + j);
            }
        }
    }

    private static GameOfLife game(byte[][] grid) {
        int rows = grid.length;
        int columns = grid[0].length;
        return game(grid, rows, columns);
    }

    private static GameOfLife game(byte[][] grid, int rows, int columns) {
        boolean[][] b = new boolean[grid.length][];
        for (int i = 0; i < grid.length; i++) {
            boolean[] r = b[i] = new boolean[grid[i].length];
            for (int j = 0; j < grid[i].length; j++) {
                r[j] = grid[i][j] != 0;
            }
        }
        var inMemoryGrid = new HashGrid(b, rows, columns);
        assertEquals(rows, inMemoryGrid.getRows());
        assertEquals(columns, inMemoryGrid.getColumns());
        var game = GameOfLife.builder(inMemoryGrid).build();
        assertGame(grid, game);
        return game;
    }
}