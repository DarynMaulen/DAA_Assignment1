package algorithms.metrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Lightweight, thread-friendly metrics collector for instrumentation of algorithms.
 * Records:
 *  - elapsed time (ns -> exposed as ms)
 *  - number of comparisons and swaps (AtomicLong for concurrency)
 *  - callCount (how many times start() was invoked)
 *  - recursion depth tracking: per-thread currentDepth (ThreadLocal) and global maxDepth (AtomicLong)
 * Usage:
 *  Metrics m = new Metrics();
 *  m.start();
 *  // instrument algorithm:
 *  m.enter(); // when entering a recursive call
 *  m.addComparison(); // when comparing two elements
 *  m.exit(); // when leaving a recursive call
 *  m.stop();
 *  MetricsSnapshot snap = m.snapshot("mergesort", n, seed);
 */
public class Metrics {
    // time tracking
    private long startNs = 0L;
    private long endNs = 0L;
    private volatile boolean running = false;

    // counters (AtomicLong to be safe in multi-thread context)
    private final AtomicLong comparisons = new AtomicLong(0);
    private final AtomicLong swaps = new AtomicLong(0);
    private final AtomicLong callCount = new AtomicLong(0);

    // recursion depth tracking: thread-local current depth and global maximum
    private final ThreadLocal<Integer> currentDepth = ThreadLocal.withInitial(() -> 0);
    private final AtomicLong maxDepth = new AtomicLong(0);

    /**
     * Start overall timing and increment call count.
     * Safe to call multiple times — subsequent start() while already running is ignored.
     */
    public void start() {
        if (running) return;
        startNs = System.nanoTime();
        running = true;
        callCount.incrementAndGet();
    }

    /**
     * Stop overall timing.
     */
    public void stop() {
        if (!running) return;
        endNs = System.nanoTime();
        running = false;
    }

    /**
     * Get elapsed time in milliseconds.
     * @return elapsed time in milliseconds.
     */
    public double getElapsedMs() {
        long elapsedNs = running ? (System.nanoTime() - startNs) : (endNs - startNs);
        return elapsedNs / 1_000_000.0;
    }

    /**
     * Increase recursion depth.
     * Updates per-thread currentDepth and global maxDepth if needed.
     */
    public void enter() {
        int d = currentDepth.get() + 1;
        currentDepth.set(d);
        maxDepth.updateAndGet(old -> Math.max(old, d));
    }

    /**
     * Decrease recursion depth.
     * Ensures depth never becomes negative.
     */
    public void exit() {
        int d = currentDepth.get() - 1;
        if (d < 0) d = 0;
        currentDepth.set(d);
    }

    // Accessors for depths
    public int getCurrentDepth() {
        return currentDepth.get();
    }

    public long getMaxDepth() {
        return maxDepth.get();
    }

    // Counters incrementer
    public void addComparison() { comparisons.incrementAndGet(); }
    public void addSwap() { swaps.incrementAndGet(); }

    // Counters accessors
    public long getComparisons() { return comparisons.get(); }
    public long getSwaps() { return swaps.get(); }
    public long getCallCount() { return callCount.get(); }

    /**
     * Reset all counters and timing state to initial values.
     * Useful between separate benchmark runs.
     */
    public void reset() {
        startNs = 0L;
        endNs = 0L;
        running = false;
        comparisons.set(0);
        swaps.set(0);
        maxDepth.set(0);
        currentDepth.set(0);
    }

    /**
     * Create an immutable snapshot of current metrics for logging/export.
     * @param algo algorithm name
     * @param n input size
     * @param seed optional seed used for randomisation
     * @return MetricsSnapshot with current values and timestamp
     */
    public MetricsSnapshot snapshot(String algo, int n, Long seed) {
        return new MetricsSnapshot(
                algo,
                n,
                seed == null ? -1L : seed,
                getElapsedMs(),
                getComparisons(),
                getSwaps(),
                getMaxDepth(),
                System.currentTimeMillis()
        );
    }
}
