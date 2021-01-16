package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.AbstractFiniteGrid;
import com.voidaspect.jgol.grid.Grid;
import com.voidaspect.jgol.listener.CellListener;

import java.util.concurrent.locks.StampedLock;

final class ThreadSafeLife extends AbstractLife {

    private final StampedLock gridLock;

    private final AbstractLife life;

    private final ThreadSafeGrid grid;

    private final Grid inner;

    static ThreadSafeLife of(AbstractLife life) {
        return life instanceof ThreadSafeLife
                ? (ThreadSafeLife) life
                : new ThreadSafeLife(life);
    }

    private ThreadSafeLife(AbstractLife life) {
        this.life = life;
        this.gridLock = new StampedLock();
        this.inner = life.grid();
        this.grid = new ThreadSafeGrid();
    }

    @Override
    protected void nextGen(CellListener listener) {
        long stamp = gridLock.writeLock();
        try {
            life.nextGen(listener);
        } finally {
            gridLock.unlockWrite(stamp);
        }
    }

    @Override
    public void finish() {
        long stamp = gridLock.tryOptimisticRead();

        if (stamp != 0) {
            boolean finished = life.isFinished();
            if (gridLock.validate(stamp)) {
                if (finished) return;

                stamp = gridLock.writeLock();
                try {
                    life.finish();
                    return;
                } finally {
                    gridLock.unlockWrite(stamp);
                }
            }
        }

        stamp = gridLock.readLock();

        try {
            if (life.isFinished()) return;

            stamp = upgradeToWrite(stamp);

            life.finish();
        } finally {
            gridLock.unlock(stamp);
        }
    }

    @Override
    public boolean isFinished() {
        long stamp = gridLock.tryOptimisticRead();

        if (stamp != 0) {
            boolean finished = life.isFinished();
            if (gridLock.validate(stamp)) return finished;
        }

        stamp = gridLock.readLock();

        try {
            return life.isFinished();
        } finally {
            gridLock.unlockRead(stamp);
        }
    }

    @Override
    public Grid grid() {
        return grid;
    }

    @Override
    public void freeze() {
        long stamp = gridLock.tryOptimisticRead();

        if (stamp != 0) {
            boolean frozen = life.isFrozen();
            if (gridLock.validate(stamp)) {
                if (frozen) return;

                stamp = gridLock.writeLock();
                try {
                    life.freeze();
                    return;
                } finally {
                    gridLock.unlockWrite(stamp);
                }
            }
        }

        stamp = gridLock.readLock();

        try {
            if (life.isFrozen()) return;

            stamp = upgradeToWrite(stamp);

            life.freeze();
        } finally {
            gridLock.unlock(stamp);
        }
    }

    @Override
    public void unfreeze() {
        long stamp = gridLock.tryOptimisticRead();

        if (stamp != 0) {
            boolean frozen = life.isFrozen();
            if (gridLock.validate(stamp)) {
                if (!frozen) return;

                stamp = gridLock.writeLock();
                try {
                    life.unfreeze();
                    return;
                } finally {
                    gridLock.unlockWrite(stamp);
                }
            }
        }

        stamp = gridLock.readLock();

        try {
            if (!life.isFrozen()) return;

            stamp = upgradeToWrite(stamp);

            life.unfreeze();
        } finally {
            gridLock.unlock(stamp);
        }
    }

    @Override
    public boolean isFrozen() {
        long stamp = gridLock.tryOptimisticRead();

        if (stamp != 0) {
            boolean frozen = life.isFrozen();
            if (gridLock.validate(stamp)) return frozen;
        }

        stamp = gridLock.readLock();

        try {
            return life.isFrozen();
        } finally {
            gridLock.unlockRead(stamp);
        }
    }

    private long upgradeToWrite(long stamp) {
        long ws = gridLock.tryConvertToWriteLock(stamp);
        if (ws != 0) {
            stamp = ws;
        } else {
            gridLock.unlockRead(stamp);
            stamp = gridLock.writeLock();
        }
        return stamp;
    }


    /**
     * Thread-safe view of a {@link Grid} object. Uses read-write locking.
     */
    private final class ThreadSafeGrid extends AbstractFiniteGrid {

        public ThreadSafeGrid() {
            super(inner.getRows(), inner.getColumns());
        }

        @Override
        public boolean get(int row, int col) {
            long stamp = gridLock.tryOptimisticRead();

            if (stamp != 0) {
                boolean alive = inner.get(row, col);
                if (gridLock.validate(stamp)) return alive;
            }

            stamp = gridLock.readLock();

            try {
                return inner.get(row, col);
            } finally {
                gridLock.unlockRead(stamp);
            }
        }

        @Override
        public void set(int row, int col, boolean state) {
            // don't need write lock if no update happens
            long stamp = gridLock.tryOptimisticRead();

            if (stamp != 0) {
                boolean alive = inner.get(row, col);
                if (gridLock.validate(stamp)) {
                    if (alive == state) return;

                    stamp = gridLock.writeLock();
                    try {
                        inner.set(row, col, state);
                        return;
                    } finally {
                        gridLock.unlockWrite(stamp);
                    }
                }
            }

            stamp = gridLock.readLock();

            try {
                if (inner.get(row, col) == state) return;

                stamp = upgradeToWrite(stamp);

                inner.set(row, col, state);
            } finally {
                gridLock.unlock(stamp);
            }
        }

        @Override
        public int neighbors(int row, int col) {
            long stamp = gridLock.tryOptimisticRead();

            if (stamp != 0) {
                int neighbors = inner.neighbors(row, col);
                if (gridLock.validate(stamp)) return neighbors;
            }

            stamp = gridLock.readLock();

            try {
                return inner.neighbors(row, col);
            } finally {
                gridLock.unlockRead(stamp);
            }
        }

        @Override
        protected boolean[][] snapshotWithoutBoundChecking(int fromRow, int fromColumn, int rows, int columns) {
            long stamp = gridLock.readLock();
            try {
                return inner.snapshot(fromRow, fromColumn, rows, columns);
            } finally {
                gridLock.unlockRead(stamp);
            }
        }

        @Override
        public void clear() {
            long stamp = gridLock.writeLock();
            try {
                inner.clear();
            } finally {
                gridLock.unlockWrite(stamp);
            }
        }
    }

}
