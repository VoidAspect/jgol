package com.voidaspect.jgol;

import com.voidaspect.jgol.grid.PaddedInMemoryGrid;
import com.voidaspect.jgol.listener.ProgressListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class GameOfLifeTest {

    @Test
    void shouldSendProgressEvents() {
        var recordedDeath = new ArrayList<Cell>();
        var recordedSpawn = new ArrayList<Cell>();
        var progressStarted = new AtomicInteger();
        var progressFinished = new AtomicInteger();

        var progressListener = new ProgressListener() {
            @Override
            public void onCellSpawned(int row, int col) {
                recordedSpawn.add(new Cell(row, col));
            }

            @Override
            public void onCellDied(int row, int col) {
                recordedDeath.add(new Cell(row, col));
            }

            @Override
            public void onProgressStart() {
                progressStarted.incrementAndGet();
            }

            @Override
            public void onProgressFinish() {
                progressFinished.incrementAndGet();
            }
        };
        var game = game(new byte[][]{
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0},
                {0, 1, 1, 1, 0},
                {0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0}
        });
        game.progress(progressListener);
        assertEquals(1, progressStarted.get());
        assertEquals(1, progressFinished.get());
        assertEquals(2, recordedDeath.size());
        assertEquals(2, recordedSpawn.size());
        assertAll(
                recordedDeath.get(0).test(2, 1),
                recordedDeath.get(1).test(2, 3)
        );
        assertAll(
                recordedSpawn.get(0).test(1, 2),
                recordedSpawn.get(1).test(3, 2)
        );
    }

    @Test
    void shouldNotChangeAfterFinish() {
        byte[][] expected = {{1}};
        var game = game(expected);
        game.finish();
        game.progress();
        assertGame(expected, game);
    }

    @Test
    void shouldProgressOnLargeEmptyGrid() {
        int side = 20_000;
        var grid = new PaddedInMemoryGrid(side, side);
        var game = GameOfLife.builder(grid).build();
        game.progress();
    }

    @Test
    void shouldHandleLargeGrid() {
        int side = 10000;
        int edge = side - 1;
        var grid = new PaddedInMemoryGrid(side, side);
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
        game.finish();
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
        game.progress();
        assertGame(expected, game);
        game.progress();
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
        game.progress();
        assertGame(expected, game);
        game.progress();
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
        game.progress();
        assertGame(expected, game);
        game.progress();
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
        game.progress();
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
        game.progress();
        assertGame(initial, game);
        game.progress();
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
        game.progress();
        assertGame(initial, game);
        game.progress();
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
        game.progress();
        assertGame(initial, game);
        game.progress();
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
        game.progress();
        assertGame(initial, game);
        game.progress();
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
        game.progress();
        assertGame(initial, game);
        game.progress();
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
        game.progress();
        assertGame(next, game);
        game.progress();
        assertGame(initial, game);
        game.progress();
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
        game.progress();
        assertGame(next, game);
        game.progress();
        assertGame(initial, game);
        game.progress();
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
        game.progress();
        assertGame(next, game);
        game.progress();
        assertGame(initial, game);
        game.progress();
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
        var game = game(initial);
        game.progress();
        assertGame(step1, game);
        game.progress();
        assertGame(step2, game);
        game.progress();
        assertGame(step3, game);
        game.progress();
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
        boolean[][] b = new boolean[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                b[i][j] = grid[i][j] != 0;
            }
        }
        var inMemoryGrid = new PaddedInMemoryGrid(b, rows, columns);
        assertEquals(rows, inMemoryGrid.getRows());
        assertEquals(columns, inMemoryGrid.getColumns());
        var game = GameOfLife.builder(inMemoryGrid).build();
        assertGame(grid, game);
        return game;
    }

    private static final class Cell {
        final int row;
        final int col;

        private Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }

        Executable test(int row, int col) {
            return () -> {
                assertEquals(row, this.row);
                assertEquals(col, this.col);
            };
        }
    }

}