package algorithms.ClosestPairOfPoints;

import algorithms.metrics.Metrics;

/**
 * Simple O(n^2) brute-force the closest pair.
 * Useful for correctness checks and small inputs.
 */
public class NaiveClosest {
    /**
     * Returns the closest pair among points (assumes length >= 2).
     * Uses Metrics to count comparisons.
     */
    public static ClosestPairResult bruteForce(Point[] pts, Metrics metrics) {
        if (pts == null) throw new NullPointerException("pts");
        int n = pts.length;
        if (n < 2) throw new IllegalArgumentException("need at least 2 points");

        double best = Double.POSITIVE_INFINITY;
        Point p1 = null, p2 = null;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (metrics != null) metrics.addComparison();
                double d2 = pts[i].distance2(pts[j]);
                if (d2 < best) {
                    best = d2;
                    p1 = pts[i];
                    p2 = pts[j];
                }
            }
        }
        return new ClosestPairResult(p1, p2, best);
    }

    public static ClosestPairResult bruteForce(Point[] pts) {
        return bruteForce(pts, null);
    }
}
