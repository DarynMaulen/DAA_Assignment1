package algorithms.metrics;

/**
 * Immutable snapshot of metrics recorded during a single run.
 * Fields:
 *  - algo: algorithm name
 *  - n: input size
 *  - seed: RNG seed used
 *  - elapsedMs: elapsed milliseconds as measured by Metrics
 *  - comparisons: number of comparisons counted
 *  - swaps: number of swaps counted
 *  - maxDepth: maximum recursion depth observed
 *  - timestamp: wall-clock timestamp when the snapshot was taken
 */
public record MetricsSnapshot(
        String algo,
        int n,
        long seed,
        double elapsedMs,
        long comparisons,
        long swaps,
        long maxDepth,
        long timestamp
) {}
