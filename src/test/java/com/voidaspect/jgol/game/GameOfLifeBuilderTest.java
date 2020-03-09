package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.ProgressListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameOfLifeBuilderTest {

    private static final int LARGE_GRID_ROWS = 10000;

    private static final int LARGE_GRID_COLUMNS = 20000;

    private Grid grid;

    @BeforeEach
    void setUp() {
        grid = mock(Grid.class);
    }

    @Test
    void shouldConfigureDefaults() {
        var builder = new GameOfLifeBuilder(grid);
        assertEquals(GameOfLifeBuilder.DEFAULT_CHUNK_SIDE, builder.getChunkHeight());
        assertEquals(GameOfLifeBuilder.DEFAULT_CHUNK_SIDE, builder.getChunkWidth());
        assertEquals(GameOfLifeBuilder.DEFAULT_PARALLEL_PROGRESSION_THRESHOLD, builder.getParallelizationThreshold());
        assertTrue(builder.isParallelExecutionSupported());
        assertFalse(builder.isThreadSafe());
        assertTrue(builder.getProgressExecutor().isEmpty());
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

    @Test
    void whenParallelFalse_ShouldNotUseParallelStrategy() {
        setupLargeGrid();

        var builder = new GameOfLifeBuilder(grid).setParallel(false);
        assertFalse(builder.isParallelExecutionSupported());
        assertFalse(builder.isParallel());

        assertEquals(AllAtOnceProgressStrategy.class, builder.chooseProgressStrategy().getClass());
    }

    @Test
    void whenParallelThresholdNotReached_ShouldNotUseParallelStrategy() {
        setupGrid(500, 500);

        var builder = new GameOfLifeBuilder(grid).setParallelizationThreshold(250_001);
        assertTrue(builder.isParallelExecutionSupported());
        assertFalse(builder.isParallel());

        assertEquals(AllAtOnceProgressStrategy.class, builder.chooseProgressStrategy().getClass());
    }

    @Test
    void shouldCleanupExecutorOnParallelStrategy() throws Exception {
        setupLargeGrid();

        var executor = mock(ExecutorService.class);
        when(executor.awaitTermination(anyLong(), any())).thenReturn(true);

        var builder = new GameOfLifeBuilder(grid).setProgressExecutor(executor);

        assertFalse(builder.shouldKeepPoolAlive());
        assertTrue(builder.getProgressExecutor().isPresent());
        assertSame(executor, builder.getProgressExecutor().get());
        assertTrue(builder.isParallel());

        assertEquals(ParallelProgressStrategy.class, builder.chooseProgressStrategy().getClass());

        var life = builder.build();
        life.finish();
        verify(executor, times(1)).shutdown();
        verify(executor, times(1)).awaitTermination(anyLong(), any());
        verifyNoMoreInteractions(executor);
    }

    @Test
    void whenExecutorShouldShutDownFalse_ShouldNotCallShutdown() {
        setupLargeGrid();

        var executor = mock(ExecutorService.class);

        var builder = new GameOfLifeBuilder(grid)
                .setProgressExecutor(executor)
                .setKeepPoolAlive(true);

        assertTrue(builder.shouldKeepPoolAlive());
        assertTrue(builder.getProgressExecutor().isPresent());
        assertSame(executor, builder.getProgressExecutor().get());
        assertTrue(builder.isParallel());

        assertEquals(ParallelProgressStrategy.class, builder.chooseProgressStrategy().getClass());

        var life = builder.build();
        life.finish();
        verifyNoInteractions(executor);
    }

    @Test
    void shouldAlways_ShutdownDefaultExecutor() {
        setupLargeGrid();

        var builder = new GameOfLifeBuilder(grid).setKeepPoolAlive(false);

        assertFalse(builder.shouldKeepPoolAlive());
        assertTrue(builder.getProgressExecutor().isEmpty());
        assertTrue(builder.isParallel());

        var ps = builder.chooseProgressStrategy();
        assertEquals(ParallelProgressStrategy.class, ps.getClass());
        ps.finish();
        assertTrue(ps.isFinished());
        assertThrows(RejectedExecutionException.class, () -> ((ParallelProgressStrategy) ps)
                .progressAndCountUpdates(grid, ProgressListener.NOOP));
    }

    @Test
    void chunkCountShouldBeEnoughToCoverAllCells() {
        setupGrid(10_00, 10_01);

        var builder = new GameOfLifeBuilder(grid);
        assertEquals(1_000_000, builder.getChunkWidth() * builder.getChunkHeight());
    }

    @Test
    void shouldNotAllowIllegalChunkWidthOrHeight() {
        setupGrid(20, 10);
        var builder = new GameOfLifeBuilder(grid);

        assertThrows(IllegalArgumentException.class, () -> builder.setChunkHeight(0));
        assertThrows(IllegalArgumentException.class, () -> builder.setChunkHeight(-1));
        assertThrows(IllegalArgumentException.class, () -> builder.setChunkHeight(-2));
        assertThrows(IllegalArgumentException.class, () -> builder.setChunkHeight(21));
        assertThrows(IllegalArgumentException.class, () -> builder.setChunkHeight(22));
        assertDoesNotThrow(() -> builder.setChunkHeight(1));
        assertDoesNotThrow(() -> builder.setChunkHeight(20));

        assertThrows(IllegalArgumentException.class, () -> builder.setChunkWidth(0));
        assertThrows(IllegalArgumentException.class, () -> builder.setChunkWidth(-1));
        assertThrows(IllegalArgumentException.class, () -> builder.setChunkWidth(-2));
        assertThrows(IllegalArgumentException.class, () -> builder.setChunkWidth(11));
        assertThrows(IllegalArgumentException.class, () -> builder.setChunkWidth(12));
        assertDoesNotThrow(() -> builder.setChunkWidth(1));
        assertDoesNotThrow(() -> builder.setChunkWidth(10));

        verify(grid, atLeastOnce()).getRows();
        verify(grid, atLeastOnce()).getColumns();
    }

    private void setupLargeGrid() {
        setupGrid(LARGE_GRID_ROWS, LARGE_GRID_COLUMNS);
    }

    private void setupGrid(int rows, int columns) {
        when(grid.getRows()).thenReturn(rows);
        when(grid.getColumns()).thenReturn(columns);
        when(grid.getSize()).thenReturn((long) rows * columns);
    }
}