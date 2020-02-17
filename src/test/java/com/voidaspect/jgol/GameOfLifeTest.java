package com.voidaspect.jgol;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class GameOfLifeTest {

    @Test
    void shouldNotAllowInvalidGrids() {
        assertThrows(IllegalArgumentException.class, () -> new GameOfLife(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new GameOfLife(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new GameOfLife(1, 0));
        assertThrows(IllegalArgumentException.class, () -> new GameOfLife(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> new GameOfLife(0, -1));
        assertThrows(IllegalArgumentException.class, () -> new GameOfLife(-1, -1));
    }

    @Test
    void shouldCreateEmptyGrid() {
        var game = new GameOfLife(1, 1);
        assertGame(new byte[][]{{0}}, game);
    }

    @Test
    void shouldCaptureSnapshots() {
        var game = new GameOfLife(3, 3);
        assertArrayEquals(new boolean[][]{
                {false, false, false},
                {false, false, false},
                {false, false, false},
        }, game.snapshot());
        game.set(1, 1, true);
        assertArrayEquals(new boolean[][]{
                {false, false, false},
                {false, true, false},
                {false, false, false},
        }, game.snapshot());
        game.progress();
        assertArrayEquals(new boolean[][]{
                {false, false, false},
                {false, false, false},
                {false, false, false},
        }, game.snapshot());
    }

    @Test
    void shouldProgressOnLargeEmptyGrid_InLessThan1Second() {
        int side = 20_000;
        var game = new GameOfLife(side, side);
        assertTimeout(Duration.ofSeconds(1), game::progress);
    }

    @Test
    void shouldHandleLargeGrid() {
        int side = 10000;
        int edge = side - 1;
        var game = new GameOfLife(side, side);
        boolean[][] expected = new boolean[side][side];
        assertArrayEquals(expected, game.snapshot());
        // init loads of overcrowded cells - they should die off
        for (int i = 0; i < side; i++) {
            for (int j = 0; j < side; j++) {
                game.set(i, j, true);
            }
        }
        expected[0][0] = true;
        expected[0][edge] = true;
        expected[edge][0] = true;
        expected[edge][edge] = true;
        assertTimeout(Duration.ofSeconds(2), game::progress);
        assertArrayEquals(expected, game.snapshot());
        // lonely cells at the corners die off
        expected[0][0] = false;
        expected[0][edge] = false;
        expected[edge][0] = false;
        expected[edge][edge] = false;
        assertTimeout(Duration.ofSeconds(1), game::progress);
        game.progress();
        assertArrayEquals(expected, game.snapshot());
    }

    @Test
    void shouldClearGrid() {
        var game = game(new byte[][]{
                {1, 1, 1},
                {1, 1, 1},
                {1, 1, 1}
        });
        byte[][] expected = {
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        };
        game.clear();
        assertGame(expected, game);
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
        for (int i = 0; i < expected.length; i++) {
            for (int j = 0; j < expected[i].length; j++) {
                byte actual = game.isAlive(i, j) ? (byte) 1 : (byte) 0;
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
        var game = new GameOfLife(b, rows, columns);
        assertEquals(rows, game.getRows());
        assertEquals(columns, game.getColumns());
        assertEquals(rows * columns, game.getSize());
        assertGame(grid, game);
        return game;
    }

}