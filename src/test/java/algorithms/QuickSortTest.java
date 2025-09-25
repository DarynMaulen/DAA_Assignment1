package algorithms;

import algorithms.metrics.Metrics;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for QuickSort implementation.
 * Covers:
 *  - edge cases (null, empty, single element)
 *  - adversarial inputs (sorted, reversed, many duplicates)
 *  - random correctness (multiple trials)
 *  - cutoff behaviour (force insertion sort)
 *  - basic metrics integration (comparisons, recursion depth)
 */
public class QuickSortTest {

    @Test
    void testNullAndSmallInputs() {
        // null and trivial arrays should be no-ops and remain unchanged
        QuickSort.quickSort((int[]) null);
        int[] empty = new int[0];
        QuickSort.quickSort(empty);
        assertArrayEquals(new int[0], empty);

        int[] one = new int[]{7};
        QuickSort.quickSort(one);
        assertArrayEquals(new int[]{7}, one);
    }

    @Test
    void testAdversarial() {
        // already sorted: ensure algorithm leaves sorted array sorted
        int[] sorted = new int[100];
        for (int i = 0; i < sorted.length; i++) sorted[i] = i;
        int[] copy = sorted.clone();
        QuickSort.quickSort(sorted);
        Arrays.sort(copy);
        assertArrayEquals(copy, sorted);

        // reversed: ensure correctness on descending input
        int[] reversed = new int[100];
        for (int i = 0; i < reversed.length; i++) reversed[i] = reversed.length - i - 1;
        int[] revCopy = reversed.clone();
        QuickSort.quickSort(reversed);
        Arrays.sort(revCopy);
        assertArrayEquals(revCopy, reversed);

        // many duplicates: stress duplicate handling
        int[] dup = new int[200];
        Random rnd = new Random(1);
        for (int i = 0; i < dup.length; i++) dup[i] = rnd.nextInt(5);
        int[] dupCopy = dup.clone();
        QuickSort.quickSort(dup);
        Arrays.sort(dupCopy);
        assertArrayEquals(dupCopy, dup);
    }

    @RepeatedTest(20)
    void testRandomCorrectness() {
        // multiple random trials to increase confidence
        Random rnd = new Random(42);
        int[] a = new int[500];
        for (int i = 0; i < a.length; i++) a[i] = rnd.nextInt();
        int[] expected = a.clone();
        Arrays.sort(expected);
        QuickSort.quickSort(a);
        assertArrayEquals(expected, a);
    }

    @Test
    void testCutOffBehaviourCorrectness(){
        // force insertion-sort path by setting cutoff >= array length
        Random rand = new Random(123);
        int[] arr = new int[64];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = rand.nextInt(1000);
        }
        int[] expected = arr.clone();
        Arrays.sort(expected);
        QuickSort.quickSort(arr, 65, null); // insertion path for whole array
        assertArrayEquals(expected, arr);
    }

    @Test
    void testMetricsDepthAndComparisons() {
        // ensure metrics are collected and recursion depth stays bounded
        int n = 1024;
        int[] a = new int[n];
        Random rnd = new Random(7);
        for (int i = 0; i < n; i++) a[i] = rnd.nextInt();

        int[] expected = a.clone();
        Arrays.sort(expected);

        Metrics metrics = new Metrics();
        QuickSort.quickSort(a, QuickSort.DEFAULT_INSERTION_THRESHOLD, metrics);

        assertArrayEquals(expected, a, "quickSort should sort the array correctly");

        // basic metrics sanity checks
        assertTrue(metrics.getCallCount() >= 1, "metrics callCount should be at least 1");
        assertTrue(metrics.getComparisons() > 0, "should have counted some comparisons");

        long maxDepth = metrics.getMaxDepth();
        double log2 = Math.log(n) / Math.log(2);
        int allowed = 10;
        assertTrue(maxDepth <= 2 * Math.ceil(log2) + allowed,
                () -> "maxDepth too large: " + maxDepth);
    }
}
