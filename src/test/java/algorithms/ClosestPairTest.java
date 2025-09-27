package algorithms;

import algorithms.ClosestPairOfPoints.*;
import algorithms.metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ClosestPair divide-and-conquer implementation.
 * - Compares result with brute-force (NaiveClosest) on random inputs.
 * - Tests basic edge cases and metrics integration.
 */
public class ClosestPairTest {
    private static final double EPS = 1e-9;

    @Test
    void testNullAndSmallInputs(){
        assertThrows(NullPointerException.class, () -> {
            ClosestPair.findClosest(null);});

        Point[] one = new Point[]{new Point(0,0)};
        assertThrows(IllegalArgumentException.class, () -> {
            ClosestPair.findClosest(one);});
    }

    @Test
    void testTwoPointsAndDuplicates(){
        Point p1 = new Point(0.0,0.0);
        Point p2 = new Point(3.0,4.0);
        ClosestPairResult result = ClosestPair.findClosest(new Point[]{p1,p2});
        assertEquals(25.0,result.distance2(),EPS);

        Point[] duplicates = new Point[]{new Point(1,1),new Point(1,1),new Point(3,3)};
        ClosestPairResult result1 = ClosestPair.findClosest(duplicates);
        assertEquals(0,result1.distance2(),EPS);
    }

    @Test
    void testCollinearPoints(){
        Point[] points = new Point[]{
                new Point(0.0,0.0),
                new Point(2.0,0.0),
                new Point(5.0,0.0),
                new Point(1.0,0.0),
        };
        ClosestPairResult result = ClosestPair.findClosest(points);
        assertEquals(1.0,result.distance2(),EPS);
    }

    @Test
    void testRandomCompareWithNaive(){
        // many small random instances validated against brute-force O(n^2)
        Random rnd = new Random(12345);
        Point[] points = new Point[50];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point(rnd.nextDouble() *2000 -1000,rnd.nextDouble()*2000 -1000);
        }
        ClosestPairResult fast = ClosestPair.findClosest(points);
        ClosestPairResult naive = NaiveClosest.bruteForce(points);
        assertEquals(naive.distance2(),fast.distance2(),EPS,()-> "Mismatch: fast: " + fast.distance2() + " naive: " + naive.distance2());
    }

    @Test
    void testMetricsIntegration(){
        Random rnd = new Random(42);
        Point[] points = new Point[128];
        for (int i = 0; i < points.length; i++) {
            points[i] = new Point(rnd.nextDouble(),rnd.nextDouble());
        }

        Metrics metrics = new Metrics();
        metrics.reset();
        ClosestPairResult result = ClosestPair.findClosest(points,ClosestPair.DEFAULT_INSERTION_THRESHOLD, metrics);

        // sanity: result non-null and distance >= 0
        assertNotNull(result);
        assertTrue(result.distance2() > 0.0);

        assertTrue(metrics.getCallCount()>=1,"call count should be incremented");
        assertTrue(metrics.getComparisons()>=0,"comparisons should be incremented");
    }
}
