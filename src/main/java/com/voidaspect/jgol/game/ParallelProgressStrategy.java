package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

final class ParallelProgressStrategy extends ChunkedProgressStrategy {

    private final ExecutorService progressPool;

    private final int chunkHeight;

    private final int chunkWidth;

    private final int chunks;

    public ParallelProgressStrategy(
            Grid grid,
            ExecutorService progressPool,
            int chunkHeight,
            int chunkWidth,
            int chunks
    ) {
        super(grid);
        this.progressPool = progressPool;
        this.chunkHeight = chunkHeight;
        this.chunkWidth = chunkWidth;
        this.chunks = chunks;
    }

    @Override
    public void progress() {
        int rows = grid.getRows();
        int columns = grid.getColumns();
        var progressTasks = new ArrayList<Callable<NextGen>>(chunks);
        for (int row = 0; row < rows; row += chunkHeight) {
            for (int col = 0; col < columns; col += chunkWidth) {
                int fromRow = row;
                int fromCol = col;
                int toRow = fromRow + chunkHeight;
                int toCol = fromCol + chunkWidth;
                progressTasks.add(() -> progressChunk(fromRow, fromCol, toRow, toCol));
            }
        }
        try {
            var gridUpdates = new ArrayList<Callable<Void>>(chunks);
            for (var chunk : progressPool.invokeAll(progressTasks)) {
                gridUpdates.add(gridUpdate(chunk.get()));
            }
            for (var update : progressPool.invokeAll(gridUpdates)) {
                update.get();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
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
