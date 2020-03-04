package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.CellListener;

import java.util.ArrayList;
import java.util.concurrent.*;

final class ParallelProgressStrategy extends ChunkedProgressStrategy {

    private static final int TERMINATION_TIMEOUT_SECONDS = 10;

    private final ExecutorService progressPool;

    private final boolean keepPoolAlive;

    private final int chunkHeight;

    private final int chunkWidth;

    private final int chunks;

    ParallelProgressStrategy(
            ExecutorService progressPool,
            boolean keepPoolAlive,
            int chunkHeight,
            int chunkWidth,
            int chunks
    ) {
        this.progressPool = progressPool;
        this.keepPoolAlive = keepPoolAlive;
        this.chunkHeight = chunkHeight;
        this.chunkWidth = chunkWidth;
        this.chunks = chunks;
    }

    @Override
    int progressAndCountUpdates(Grid grid, CellListener listener) {
        int rows = grid.getRows();
        int columns = grid.getColumns();
        var progressTasks = new ArrayList<Callable<NextGen>>(chunks);
        for (int row = 0; row < rows; row += chunkHeight) {
            for (int col = 0; col < columns; col += chunkWidth) {
                int fromRow = row;
                int fromCol = col;
                int toRow = fromRow + chunkHeight;
                int toCol = fromCol + chunkWidth;
                progressTasks.add(() -> progressChunk(grid, listener, fromRow, fromCol, toRow, toCol));
            }
        }
        int updates = 0;
        try {
            var gridUpdates = new ArrayList<Callable<Void>>(chunks);
            for (var chunk : progressPool.invokeAll(progressTasks)) {
                var nextGen = chunk.get();
                updates += nextGen.countUpdates();
                gridUpdates.add(gridUpdate(nextGen));
            }
            for (var update : progressPool.invokeAll(gridUpdates)) {
                update.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return updates;
    }

    @Override
    public void finish() {
        super.finish();

        if (keepPoolAlive) return;

        progressPool.shutdown();
        try {
            if (progressPool.awaitTermination(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) return;
            throw new RuntimeException(new TimeoutException("Could not shutdown executor after 10 seconds"));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private Callable<Void> gridUpdate(NextGen nextGen) {
        return () -> {
            nextGen.updateGrid();
            return null;
        };
    }
}
