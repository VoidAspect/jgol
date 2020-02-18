package com.voidaspect.jgol.game;

import com.voidaspect.jgol.GameOfLife;
import com.voidaspect.jgol.grid.Grid;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ThreadSafeGoL implements GameOfLife {

    private final ReadWriteLock gridLock;

    private final GoL game;

    private final ThreadSafeGrid grid;

    private final AtomicBoolean finished;

    ThreadSafeGoL(Grid grid, ProgressStrategy ps, Runnable onFinish) {
        this.game = new GoL(grid, ps, onFinish);
        this.gridLock = new ReentrantReadWriteLock();
        this.grid = new ThreadSafeGrid(game.grid());
        this.finished = new AtomicBoolean();
    }

    @Override
    public void progress() {
        if (finished.get()) return;
        var lock = gridLock.writeLock();
        lock.lock();
        try {
            game.progress();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void finish() {
        if (finished.compareAndSet(false, true)) {
            var lock = gridLock.writeLock();
            lock.lock();
            try {
                game.finish();
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public Grid grid() {
        return grid;
    }

    private final class ThreadSafeGrid implements Grid {

        private final Grid inner;

        private ThreadSafeGrid(Grid inner) {
            this.inner = inner;
        }

        @Override
        public boolean get(int row, int col) {
            var lock = gridLock.readLock();
            lock.lock();
            try {
                return inner.get(row, col);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void set(int row, int col, boolean state) {
            var lock = gridLock.writeLock();
            lock.lock();
            try {
                inner.set(row, col, state);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public int neighbors(int row, int col) {
            var lock = gridLock.readLock();
            lock.lock();
            try {
                return inner.neighbors(row, col);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean[][] snapshot() {
            var lock = gridLock.readLock();
            try {
                return inner.snapshot();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void clear() {
            var lock = gridLock.writeLock();
            try {
                inner.clear();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public int getRows() {
            var lock = gridLock.readLock();
            try {
                return inner.getRows();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public int getColumns() {
            var lock = gridLock.readLock();
            try {
                return inner.getColumns();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public long getSize() {
            var lock = gridLock.readLock();
            try {
                return inner.getSize();
            } finally {
                lock.unlock();
            }
        }
    }

}
