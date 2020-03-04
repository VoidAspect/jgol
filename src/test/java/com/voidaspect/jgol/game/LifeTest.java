package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LifeTest {

    private Grid grid;

    private ProgressStrategy strategy;

    @BeforeEach
    void setUp() {
        grid = mock(Grid.class);
        strategy = mock(ProgressStrategy.class);
    }

    @Test
    void shouldPreventDoubleProxyOfGrid() {
        var life = new Life(grid, strategy);
        var proxy = life.grid();

        assertNotSame(grid, proxy);

        life = new Life(proxy, strategy);

        assertSame(proxy, life.grid());

        assertNotSame(grid, proxy);
        assertNotSame(grid, life.grid());
    }
}