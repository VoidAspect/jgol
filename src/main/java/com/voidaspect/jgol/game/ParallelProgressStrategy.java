package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.CellListener;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.*;

final class ParallelProgressStrategy extends AbstractProgressStrategy {

    private static final int TERMINATION_TIMEOUT_SECONDS = 10;

    private final ExecutorService progressPool;

    private final boolean keepPoolAlive;

    private final int chunkHeight;

    private final int chunkWidth;

    ParallelProgressStrategy(
            ExecutorService progressPool,
            boolean keepPoolAlive,
            int chunkHeight,
            int chunkWidth
    ) {
        this.progressPool = progressPool;
        this.keepPoolAlive = keepPoolAlive;
        this.chunkHeight = chunkHeight;
        this.chunkWidth = chunkWidth;
    }

    @Override
    int progressAndCountUpdates(Grid grid, CellListener listener) {
        int rows = grid.getRows();
        int cols = grid.getColumns();
        var progressTasks = new ArrayList<Callable<NextGen>>();
        Set<Long> visited = ConcurrentHashMap.newKeySet();
        for (int row = 0; row < rows; row += chunkHeight) {
            for (int col = 0; col < cols; col += chunkWidth) {
                int fromRow = row;
                int fromCol = col;
                int toRow = Math.min(rows, fromRow + chunkHeight);
                int toCol = Math.min(cols, fromCol + chunkWidth);
                progressTasks.add(() -> progressChunk(grid, listener, visited, fromRow, fromCol, toRow, toCol));
            }
        }
        int updates = 0;
        try {
            for (var chunk : progressPool.invokeAll(progressTasks)) {
                var nextGen = chunk.get();
                updates += nextGen.countUpdates();
                nextGen.updateGrid();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return updates;
    }


    private NextGen progressChunk(Grid grid, CellListener listener, Set<Long> visited, int fromRow, int fromCol, int toRow, int toCol) {
        var ng = new NextGen(grid, listener, visited);
        grid.forEachAlive(fromRow, fromCol, toRow, toCol, ng::evaluate);
        return ng;
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
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

}
