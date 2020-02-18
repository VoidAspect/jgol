package com.voidaspect.jgol.game;

import com.voidaspect.jgol.GameOfLife;
import com.voidaspect.jgol.grid.Grid;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ThreadSafeLife implements GameOfLife {

    private final ReadWriteLock gridLock;

    private final GameOfLife life;

    private final ThreadSafeGrid grid;

    private final AtomicBoolean finished;

    ThreadSafeLife(GameOfLife life) {
        this.life = life;
        this.gridLock = new ReentrantReadWriteLock();
        this.grid = new ThreadSafeGrid(life.grid());
        this.finished = new AtomicBoolean();
    }

    @Override
    public void progress() {
        if (finished.get()) return;
        var lock = gridLock.writeLock();
        lock.lock();
        try {
            life.progress();
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
                life.finish();
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public Grid grid() {
        return grid;
    }

    /**
     * Thread-safe view of a {@link Grid} object. Uses read-write locking.
     */
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
            if (get(row, col) == state) return; // don't need write lock if no update happens
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
