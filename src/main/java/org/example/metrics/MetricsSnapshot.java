package org.example.metrics;

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