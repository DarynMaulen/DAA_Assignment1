package org.example.metrics;

import java.util.concurrent.atomic.AtomicLong;

public class Metrics {
    private long startNs = 0L;
    private long endNs = 0L;
    private volatile boolean running = false;

    private final AtomicLong comparisons = new AtomicLong(0);
    private final AtomicLong swaps = new AtomicLong(0);
    private final AtomicLong callCount = new AtomicLong(0);

    private final ThreadLocal<Integer> currentDepth = ThreadLocal.withInitial(() -> 0);
    private final AtomicLong maxDepth = new AtomicLong(0);

    public void start() {
        if (running) return;
        startNs = System.nanoTime();
        running = true;
        callCount.incrementAndGet();
    }

    public void stop() {
        if (!running) return;
        endNs = System.nanoTime();
        running = false;
    }

    public double getElapsedMs() {
        long elapsedNs = running ? (System.nanoTime() - startNs) : (endNs - startNs);
        return elapsedNs / 1_000_000.0;
    }

    public void enter() {
        int d = currentDepth.get() + 1;
        currentDepth.set(d);
        maxDepth.updateAndGet(old -> Math.max(old, d));
    }

    public void exit() {
        int d = currentDepth.get() - 1;
        if (d < 0) d = 0;
        currentDepth.set(d);
    }

    public int getCurrentDepth() {
        return currentDepth.get();
    }

    public long getMaxDepth() {
        return maxDepth.get();
    }

    public void addComparison() { comparisons.incrementAndGet(); }
    public void addSwap() { swaps.incrementAndGet(); }

    public long getComparisons() { return comparisons.get(); }
    public long getSwaps() { return swaps.get(); }
    public long getCallCount() { return callCount.get(); }

    public void reset() {
        startNs = 0L;
        endNs = 0L;
        running = false;
        comparisons.set(0);
        swaps.set(0);
        maxDepth.set(0);
        currentDepth.set(0);
    }

    public MetricsSnapshot snapshot(String algo, int n, Long seed) {
        return new MetricsSnapshot(
                algo,
                n,
                seed,
                getElapsedMs(),
                getComparisons(),
                getSwaps(),
                getMaxDepth(),
                System.currentTimeMillis()
        );
    }
}