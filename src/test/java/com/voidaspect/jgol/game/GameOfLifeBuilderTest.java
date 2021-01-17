package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameOfLifeBuilderTest {

    private static final int LARGE_GRID_ROWS = 100_000;

    private static final int LARGE_GRID_COLUMNS = 200_000;

    private Grid grid;

    @BeforeEach
    void setUp() {
        grid = mock(Grid.class);
    }

    @Test
    void shouldConfigureDefaults() {
        var builder = new GameOfLifeBuilder(grid);
        assertFalse(builder.isThreadSafe());
        assertSame(grid, builder.getGrid());
    }

    @Test
    void gridShouldNotBeNull() {
        assertThrows(NullPointerException.class, () -> new GameOfLifeBuilder(null));
    }

    @Test
    void whenThreadSafeTrue_ShouldCreateThreadSafeLife() {
        setupLargeGrid();

        var builder = new GameOfLifeBuilder(grid).setThreadSafe(true);
        assertTrue(builder.isThreadSafe());

        assertEquals(ThreadSafeLife.class, builder.build().getClass());

        builder.setThreadSafe(false);
        assertFalse(builder.isThreadSafe());

        assertEquals(Life.class, builder.build().getClass());
    }

    private void setupLargeGrid() {
        when(grid.getRows()).thenReturn(LARGE_GRID_ROWS);
        when(grid.getColumns()).thenReturn(LARGE_GRID_COLUMNS);
        when(grid.getSize()).thenReturn((long) LARGE_GRID_ROWS * LARGE_GRID_COLUMNS);
    }

}