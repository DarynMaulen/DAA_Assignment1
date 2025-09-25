package algorithms;

import algorithms.metrics.Metrics;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MergeSort:
 * - correctness on small, adversarial and random inputs
 * - cutoff behaviour
 * - basic metrics integration checks (depth, comparisons)
 */
public class MergeSortTest {
    @Test
    void testNullAndSmallInputs(){
        MergeSort.mergeSort((int[]) null); // should be a no-op
        int[] empty = new int[0];
        MergeSort.mergeSort(empty);
        assertArrayEquals(new int[0], empty);

        int[] one = new int[]{7};
        MergeSort.mergeSort(one);
        assertArrayEquals(new int[]{7}, one);
    }

    @Test
    void testAdversarialCases(){
        // already sorted
        int[] sorted = new int[100];
        for(int i = 0; i < sorted.length; i++){
            sorted[i] = i;
        }
        int[] copy = sorted.clone();
        Arrays.sort(copy);
        MergeSort.mergeSort(sorted);
        assertArrayEquals(copy, sorted);

        // reversed
        int[] reversed = new int[100];
        for(int i = 0; i < sorted.length; i++){
            reversed[i] = reversed.length - i - 1;
        }
        int[] revCopy = reversed.clone();
        Arrays.sort(revCopy);
        MergeSort.mergeSort(reversed);
        assertArrayEquals(revCopy, reversed);

        // all equal
        int[] equal = new int[100];
        Arrays.fill(equal, 8);
        int[] eqCopy = equal.clone();
        Arrays.sort(eqCopy);
        MergeSort.mergeSort(equal);
        assertArrayEquals(eqCopy, equal);
    }

    @RepeatedTest(20)
    void testRandomCorrectness(){
        Random rand = new Random(42);
        int[] arr = new int[500];
        for(int i = 0; i <arr.length; i++){
            arr[i] = rand.nextInt(1_000_000) - 500_000;
        }
        int[] expected = arr.clone();
        Arrays.sort(expected);
        MergeSort.mergeSort(arr);
        assertArrayEquals(expected, arr);
    }

    @Test
    void testCutOffBehaviourCorrectness(){
        Random rand = new Random(123);
        int[] arr = new int[64];
        for(int i = 0; i <arr.length; i++){
            arr[i] = rand.nextInt(10000) - 500;
        }
        int[] expected = arr.clone();
        Arrays.sort(expected);
        // force insertionSort path by using large cutoff
        MergeSort.mergeSort(arr,1000,null);
        assertArrayEquals(expected,arr);
    }

    @Test
    void testMetricsIntegrationDepthAndComparisons() {
        int n = 1024;
        int[] a = new int[n];
        Random rnd = new Random(7);
        for (int i = 0; i < n; i++){
            a[i] = rnd.nextInt();
        }

        int[] expected = a.clone();
        Arrays.sort(expected);

        Metrics metrics = new Metrics();
        MergeSort.mergeSort(a, MergeSort.DEFAULT_INSERTION_THRESHOLD, metrics);

        assertArrayEquals(expected, a, "mergeSort should sort the array correctly");

        // basic metrics sanity checks
        assertTrue(metrics.getCallCount() >= 1, "metrics callCount should be at least 1");
        assertTrue(metrics.getComparisons() > 0, "should have counted some comparisons");

        long maxDepth = metrics.getMaxDepth();
        double log2 = Math.log(n) / Math.log(2);
        int allowed = 5;
        assertTrue(maxDepth <= Math.ceil(log2) + allowed,
                () -> "maxDepth too large: " + maxDepth + " > " + (Math.ceil(log2) + allowed));
    }
}
