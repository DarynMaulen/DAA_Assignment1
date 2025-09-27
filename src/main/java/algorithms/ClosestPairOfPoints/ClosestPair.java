package algorithms.ClosestPairOfPoints;

import algorithms.metrics.Metrics;
import algorithms.utils.SortUtils;

import java.util.Arrays;

/**
 * Closest pair divide-and-conquer implementation (O(n log n)).
 * Approach:
 *  - sort points by X
 *  - recursively compute the closest pair left/right and produce arrays sorted by Y
 *  - merge Y-sorted lists and scan a "strip" near the split line checking up to 7 neighbours
 * The public API returns a ClosestPairResult; optional Metrics may be passed to count comparisons.
 */
public final class ClosestPair {
    public static final int DEFAULT_INSERTION_THRESHOLD = 16;

    private ClosestPair() {}

    /** Public API — default cutoff and no metrics. */
    public static ClosestPairResult findClosest(Point[] points) {
        return findClosest(points, DEFAULT_INSERTION_THRESHOLD, null);
    }

    /** Public API with cutoff and optional metrics instrumentation. */
    public static ClosestPairResult findClosest(Point[] points, int cutoff, Metrics metrics) {
        if (points == null) throw new NullPointerException("points");
        if (points.length < 2) throw new IllegalArgumentException("need at least 2 points");
        cutoff = SortUtils.normalizeCutOff(cutoff, DEFAULT_INSERTION_THRESHOLD);

        if (metrics != null) metrics.start();
        try {
            // sort by X and prepare auxiliary array
            Point[] byX = points.clone();
            Arrays.sort(byX, Point.BY_X);
            Point[] aux = new Point[byX.length];

            ResultWithY result = closestRec(byX, aux, 0, byX.length - 1, cutoff, metrics);
            return result.pair;
        } finally {
            if (metrics != null) metrics.stop();
        }
    }

    /**
     * Internal container: pair result + array of same points sorted by Y.
     * byY is length == (right-left+1) and contains the points from the X-range.
     */
    private static final class ResultWithY {
        final ClosestPairResult pair;
        final Point[] byY;

        ResultWithY(ClosestPairResult pair, Point[] byY) {
            this.pair = pair;
            this.byY = byY;
        }
    }

    /**
     * Recursive D&C: returns pair and Y-sorted points for the subrange byX[left...right].
     */
    private static ResultWithY closestRec(Point[] byX, Point[] aux, int left, int right,
                                          int cutoff, Metrics metrics) {
        int len = right - left + 1;
        if (len <= cutoff) {
            // small problem: sort by Y and solve by brute force
            Point[] small = new Point[len];
            System.arraycopy(byX, left, small, 0, len);
            Arrays.sort(small, Point.BY_Y);
            ClosestPairResult best = NaiveClosest.bruteForce(small, metrics);
            return new ResultWithY(best, small);
        }

        if (metrics != null) metrics.enter();
        try {
            int mid = left + ((right - left) >>> 1);
            double midX = byX[mid].x();

            ResultWithY leftRes = closestRec(byX, aux, left, mid, cutoff, metrics);
            ResultWithY rightRes = closestRec(byX, aux, mid + 1, right, cutoff, metrics);

            // choose best among left and right
            ClosestPairResult bestPair = leftRes.pair.distance2() <= rightRes.pair.distance2() ? leftRes.pair : rightRes.pair;
            double delta2 = bestPair.distance2();

            // merge leftRes.byY and rightRes.byY into mergedByY (sorted by Y)
            Point[] leftY = leftRes.byY;
            Point[] rightY = rightRes.byY;
            Point[] mergedByY = new Point[len];
            int li = 0, ri = 0, mi = 0;
            while (li < leftY.length && ri < rightY.length) {
                if (leftY[li].y() <= rightY[ri].y()) mergedByY[mi++] = leftY[li++];
                else mergedByY[mi++] = rightY[ri++];
            }
            while (li < leftY.length) mergedByY[mi++] = leftY[li++];
            while (ri < rightY.length) mergedByY[mi++] = rightY[ri++];

            // build strip of points within delta of midX (keep order by Y)
            Point[] strip = new Point[len];
            int stripLen = 0;
            double delta = Math.sqrt(delta2);
            for (int i = 0; i < len; i++) {
                if (Math.abs(mergedByY[i].x() - midX) <= delta) {
                    strip[stripLen++] = mergedByY[i];
                }
            }

            // scan strip: for each point compare to next up to 7 points
            for (int i = 0; i < stripLen; i++) {
                Point p = strip[i];
                int upper = Math.min(i + 7, stripLen - 1);
                for (int j = i + 1; j <= upper; j++) {
                    if (metrics != null) metrics.addComparison();
                    double d2 = p.distance2(strip[j]);
                    if (d2 < delta2) {
                        delta2 = d2;
                        bestPair = new ClosestPairResult(p, strip[j], d2);
                        delta = Math.sqrt(delta2);
                    }
                }
            }

            return new ResultWithY(bestPair, mergedByY);
        } finally {
            if (metrics != null) metrics.exit();
        }
    }
}
