package algorithms.ClosestPairOfPoints;

/**
 * Result container for closest-pair queries.
 * Holds the two points and squared distance between them.
 */
public record ClosestPairResult(Point a, Point b, double distance2) {
    /** Actual Euclidean distance. */
    public double distance() {
        return Math.sqrt(distance2);
    }
}
