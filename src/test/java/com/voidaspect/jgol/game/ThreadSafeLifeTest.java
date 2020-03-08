package com.voidaspect.jgol.game;

import com.voidaspect.jgol.grid.Grid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ThreadSafeLifeTest {

    private static final Logger log = LoggerFactory.getLogger(ThreadSafeLifeTest.class);

    private static final int DEFAULT_GRID_SIDE = 100;

    public static final long DEFAULT_SIZE = (long) DEFAULT_GRID_SIDE * DEFAULT_GRID_SIDE;

    private ThreadSafeLife subject;

    private AbstractLife delegate;

    private Grid grid;

    @BeforeEach
    void setUp() {
        grid = mock(Grid.class);
        when(grid.getColumns()).thenReturn(DEFAULT_GRID_SIDE);
        when(grid.getRows()).thenReturn(DEFAULT_GRID_SIDE);
        when(grid.getSize()).thenReturn(DEFAULT_SIZE);
        delegate = mock(AbstractLife.class);
        when(delegate.grid()).thenReturn(grid);
        subject = ThreadSafeLife.of(delegate);
    }

    @Test
    void shouldFinish() {
        when(delegate.isFinished()).thenReturn(false);

        assertFalse(subject.isFinished());
        assertFalse(subject.isFinished());

        subject.finish();

        when(delegate.isFinished()).thenReturn(true);
        assertTrue(subject.isFinished());
        assertTrue(subject.isFinished());

        subject.finish();
        assertTrue(subject.isFinished());

        verify(delegate, times(1)).finish();
    }

    @Test
    void shouldFreezeAndUnfreeze() {
        when(delegate.isFrozen()).thenReturn(false);

        assertFalse(subject.isFrozen());
        assertFalse(subject.isFrozen());

        subject.freeze();

        when(delegate.isFrozen()).thenReturn(true);
        assertTrue(subject.isFrozen());
        assertTrue(subject.isFrozen());

        subject.freeze();
        assertTrue(subject.isFrozen());

        verify(delegate, times(1)).freeze();

        subject.unfreeze();

        when(delegate.isFrozen()).thenReturn(false);

        assertFalse(subject.isFrozen());
        assertFalse(subject.isFrozen());

        subject.unfreeze();
        assertFalse(subject.isFrozen());

        verify(delegate, times(1)).unfreeze();
    }

    @Test
    void shouldPreventDoubleProxyOfGame() {
        var tSafeLife = ThreadSafeLife.of(delegate);
        assertSame(grid, delegate.grid());
        assertNotSame(delegate.grid(), tSafeLife.grid());

        var doubleProxy = ThreadSafeLife.of(tSafeLife);
        assertSame(tSafeLife, doubleProxy);
    }

    @RepeatedTest(3)
    void shouldAllowParallelReadOfGrid() {
        when(grid.get(0, 0)).thenReturn(true);

        int maxThreads = getMaxThreads();
        var executor = Executors.newFixedThreadPool(maxThreads);

        assertTimeout(Duration.ofMillis(150), () -> {
            var futures = Stream.generate(() -> executor.submit(() -> {
                sleep(100);
                return subject.grid().get(0, 0);
            })).limit(maxThreads).collect(Collectors.toList());
            for (var future : futures) {
                assertDoesNotThrow(() -> assertTrue(future.get()));
            }
        });

        executor.shutdown();
        assertDoesNotThrow(() -> executor.awaitTermination(1, TimeUnit.SECONDS));
    }


    @RepeatedTest(3)
    void shouldHandleWriteContention() {
        when(grid.get(0, 0)).thenReturn(false).thenReturn(true);

        int maxThreads = getMaxThreads();
        var executor = Executors.newFixedThreadPool(maxThreads);

        assertTimeout(Duration.ofMillis(150), () -> {
            var futures = Stream.generate(() -> executor.submit(() -> {
                sleep(100);
                subject.grid().set(0, 0, true);
                return subject.grid().get(0, 0);
            })).limit(maxThreads).collect(Collectors.toList());
            for (var future : futures) {
                assertDoesNotThrow(() -> assertTrue(future.get()));
            }
        });

        executor.shutdown();
        assertDoesNotThrow(() -> executor.awaitTermination(1, TimeUnit.SECONDS));
    }

    @Test
    void shouldWaitForProgressBeforeRead() {
        when(delegate.isFinished()).thenReturn(false);
        when(delegate.isFrozen()).thenReturn(false);
        when(grid.get(0, 0)).thenReturn(true);
        doAnswer(invocation -> {
            sleep(1000);
            when(grid.get(0, 0)).thenReturn(false);
            return null;
        }).when(delegate).nextGen(any());

        int readers = Math.max(getMaxThreads() - 1, 1);
        var executor = Executors.newFixedThreadPool(readers);

        assertTimeout(Duration.ofMillis(1200), () -> {
            var futures = Stream.generate(() -> executor.submit(() -> {
                sleep(100);
                return subject.grid().get(0, 0);
            })).limit(readers).collect(Collectors.toList());
            subject.progress();
            for (var future : futures) {
                assertDoesNotThrow(() -> assertFalse(future.get()));
            }
        });

        executor.shutdown();
        assertDoesNotThrow(() -> executor.awaitTermination(1, TimeUnit.SECONDS));
    }

    private static int getMaxThreads() {
        int maxThreads = Runtime.getRuntime().availableProcessors();
        log.info("Max thread count for synchronization tests {}", maxThreads);
        return maxThreads;
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("sleep interrupted", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}