package algorithms.utils;

import algorithms.metrics.Metrics;

import java.util.concurrent.ThreadLocalRandom;

import static algorithms.utils.ArrayUtils.*;

/**
 * Partition utilities used by QuickSort/Select implementations.
 * Provides:
 *  - Lomuto partition (simple, stable in logic)
 *  - Hoare partition (faster on average, returns boundary index j)
 *  - 3-way Dutch partition (good for many duplicates)
 *  - helper to choose a random pivot index
 * All public methods perform parameter checks via ArrayUtils.
 */
public final class PartitionUtils {

    private PartitionUtils() {}

    /**
     * Lomuto partition scheme.
     * Pivot is assumed to be at arr[right]; returns final pivot index.
     */
    public static int partitionLomuto(int[] arr, int left, int right, Metrics metrics) {
        checkRange(arr, left, right);
        int pivot = arr[right];
        int i = left;
        for (int j = left; j < right; j++) {
            if (metrics != null) metrics.addComparison();
            if (arr[j] <= pivot) {
                swapUnchecked(arr, i, j, metrics); // no inner checks
                i++;
            }
        }
        swapUnchecked(arr, i, right, metrics);
        return i;
    }

    /** Overload without metrics. */
    public static int partitionLomuto(int[] arr, int left, int right) {
        return partitionLomuto(arr, left, right, null);
    }

    /**
     * Hoare partition scheme.
     * Returns an index j such that all elements <= j are <= pivot and > j are >= pivot.
     * Note: caller must use returned j according to Hoare semantics.
     */
    public static int partitionHoare(int[] arr, int left, int right, Metrics metrics) {
        checkRange(arr, left, right);
        int pivot = arr[left + ((right - left) >>> 1)];
        int i = left - 1;
        int j = right + 1;
        while (true) {
            do {
                i++;
                if (metrics != null) metrics.addComparison();
            } while (arr[i] < pivot);
            do {
                j--;
                if (metrics != null) metrics.addComparison();
            } while (arr[j] > pivot);
            if (i >= j) {
                return j;
            }
            swap(arr, i, j, metrics);
        }
    }

    /** Overload without metrics. */
    public static int partitionHoare(int[] arr, int left, int right) {
        return partitionHoare(arr, left, right, null);
    }

    /**
     * 3-way (Dutch National Flag) partition.
     * Returns int[] {ltEnd, gtStart}:
     *  - indices < ltEnd are < pivot
     *  - indices in [ltEnd+1 ... gtStart-1] are == pivot
     *  - indices >= gtStart are > pivot
     * This partition is efficient for arrays with many equal elements.
     */
    public static int[] partition3Way(int[] arr, int left, int right, Metrics metrics) {
        checkRange(arr, left, right);
        int lt = left, i = left, gt = right;
        int pivot = arr[left + ((right - left) >>> 1)];
        while (i <= gt) {
            if (metrics != null) metrics.addComparison(); // counting the comparison for branching
            if (arr[i] < pivot) {
                swap(arr, lt++, i++, metrics);
            } else if (arr[i] > pivot) {
                swap(arr, i, gt--, metrics);
            } else {
                i++;
            }
        }
        // after loop: segment equal to pivot is [lt ... gt]
        return new int[]{lt - 1, gt + 1};
    }

    /** Overload without metrics. */
    public static int[] partition3Way(int[] arr, int left, int right) {
        return partition3Way(arr, left, right, null);
    }

    /** Choose a uniform random pivot index in [left...right]. */
    public static int chooseRandomPivot(int left, int right) {
        return left + ThreadLocalRandom.current().nextInt(right - left + 1);
    }
}
