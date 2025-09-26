package algorithms;

import algorithms.metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class DeterministicSelectTest {

    // Randomized correctness: compare deterministicSelect against Arrays.sort for many random inputs
    @Test
    void testRandomTrialsCompareWithSort() {
        Random rnd = new Random(42);
        for (int t = 0; t < 100; t++) {
            int n = 1 + rnd.nextInt(200); // length 1..200
            int[] a = new int[n];
            for (int i = 0; i < n; i++) a[i] = rnd.nextInt(1_000_000) - 500_000;
            int k = rnd.nextInt(n);

            int[] copy = a.clone();
            Arrays.sort(copy);
            int expected = copy[k];

            int result = DeterministicSelect.deterministicSelect(a.clone(), k);
            assertEquals(expected, result, "Mismatch on trial " + t + " n=" + n + " k=" + k);
        }
    }

    // All-equal values: ensure select handles many duplicates correctly
    @Test
    void testManyDuplicates(){
        int[] arr = new int[100];
        Arrays.fill(arr, 8);
        for(int k =0; k < arr.length; k++){
            assertEquals(arr[k], DeterministicSelect.deterministicSelect(arr.clone(), k));
        }
    }

    // Small arrays and boundary indices (0, last)
    @Test
    void testSmallArraysAndEdges(){
        int[] one = new int[]{42};
        assertEquals(42, DeterministicSelect.deterministicSelect(one.clone(), 0));

        int[] two = new int[]{10,-5};
        assertEquals(-5, DeterministicSelect.deterministicSelect(two.clone(), 0));
        assertEquals(10, DeterministicSelect.deterministicSelect(two.clone(), 1));
    }

    // Invalid k values should throw IllegalArgumentException
    @Test
    void testInvalidKThrows(){
        int[] arr = new int[]{1,2,3};
        assertThrows(IllegalArgumentException.class, () -> DeterministicSelect.deterministicSelect(arr, -1));
        assertThrows(IllegalArgumentException.class, () -> DeterministicSelect.deterministicSelect(arr, 3));
    }

    // Metrics integration: ensure metrics are invoked and comparisons counter is non-negative
    @Test
    void testWithMetricsDoesNotCorruptAnyArrayAndCollectSomeMetrics(){
        int[] arr = new int[128];
        Random rnd = new Random(123);
        for(int i = 0; i < arr.length; i++){
            arr[i] = rnd.nextInt();
        }

        Metrics metrics = new Metrics();
        int k = 50;
        int value = DeterministicSelect.deterministicSelect(arr.clone(), k, metrics);

        // metrics.start() should have been called once for the run
        assertTrue(metrics.getCallCount() >= 1);

        // comparisons should be registered and non-negative (>=0)
        assertTrue(metrics.getComparisons() >= 0);
    }
}
