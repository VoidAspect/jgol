package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GameOfLifeBuilderTest {

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
        var builder = new GameOfLifeBuilder(grid).setThreadSafe(true);
        assertTrue(builder.isThreadSafe());

        assertEquals(ThreadSafeLife.class, builder.build().getClass());

        builder.setThreadSafe(false);
        assertFalse(builder.isThreadSafe());

        assertEquals(Life.class, builder.build().getClass());
    }


}