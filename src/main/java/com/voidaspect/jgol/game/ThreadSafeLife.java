package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.CellListener;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

final class ThreadSafeLife extends AbstractLife {

    private final ReadWriteLock gridLock;

    private final AbstractLife life;

    private final ThreadSafeGrid grid;

    private final Grid inner;

    private final AtomicBoolean finished;

    ThreadSafeLife(AbstractLife life) {
        this.life = life;
        this.gridLock = new ReentrantReadWriteLock();
        this.inner = life.grid();
        this.grid = new ThreadSafeGrid();
        this.finished = new AtomicBoolean();
    }

    @Override
    protected void nextGen(CellListener listener) {
        var lock = gridLock.writeLock();
        lock.lock();
        try {
            life.nextGen(listener);
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
    public boolean isFinished() {
        return finished.get();
    }

    @Override
    public Grid grid() {
        return grid;
    }

    @Override
    public void freeze() {
        var lock = gridLock.writeLock();
        lock.lock();
        try {
            life.freeze();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void unfreeze() {
        var lock = gridLock.writeLock();
        lock.lock();
        try {
            life.unfreeze();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isFrozen() {
        var lock = gridLock.readLock();
        lock.lock();
        try {
            return life.isFrozen();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Thread-safe view of a {@link Grid} object. Uses read-write locking.
     */
    private final class ThreadSafeGrid implements Grid {

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
        public boolean[][] snapshot(int fromRow, int fromColumn, int rows, int columns) {
            var lock = gridLock.readLock();
            try {
                return inner.snapshot(fromRow, fromColumn, rows, columns);
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
