package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.grid.FiniteGridTest;
import com.voidaspect.jgol.grid.GridTest;
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

    static class LifeGridTest extends GridTest {

        @Override
        protected Grid grid(boolean[][] initial) {
            return getGame(initial).grid();
        }

        AbstractLife getGame() {
            return new Life(testedGrid(), mock(ProgressStrategy.class));
        }

        AbstractLife getGame(boolean[][] initial) {
            return new Life(testedGrid(initial), mock(ProgressStrategy.class));
        }
    }
}