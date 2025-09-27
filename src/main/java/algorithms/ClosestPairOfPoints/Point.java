package algorithms.ClosestPairOfPoints;

import java.util.Comparator;

/**
 * Immutable 2D point record used by closest-pair algorithm.
 * Provides squared-distance helper and ready-to-use comparators.
 */
public record Point(double x, double y) {

    /** Squared Euclidean distance.*/
    public double distance2(Point other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return dx * dx + dy * dy;
    }

    /** Euclidean distance (sqrt). */
    public double distance(Point other) {
        return Math.sqrt(distance2(other));
    }

    /** Comparator by X coordinate. */
    public static final Comparator<Point> BY_X = Comparator.comparingDouble(p -> p.x);

    /** Comparator by Y coordinate. */
    public static final Comparator<Point> BY_Y = Comparator.comparingDouble(p -> p.y);
}