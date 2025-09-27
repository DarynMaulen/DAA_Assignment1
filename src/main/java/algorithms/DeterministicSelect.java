package algorithms;

import algorithms.metrics.Metrics;
import algorithms.utils.ArrayUtils;
import algorithms.utils.PartitionUtils;

/**
 * Deterministic selection (median-of-medians, group size = 5).
 * Returns k-th the smallest element (0-based).
 * Guarantees O(n) worst-case time by selecting a "median of medians" pivot.
 */
public final class DeterministicSelect {

    private DeterministicSelect() {}

    /** Public API: k is 0-based (0 <= k < arr.length). */
    public static int deterministicSelect(int[] arr, int k) {
        return deterministicSelect(arr, k, null);
    }

    /** Public API with optional metrics instrumentation. */
    public static int deterministicSelect(int[] arr, int k, Metrics metrics) {
        ArrayUtils.requireNonNull(arr, "arr");
        if (arr.length == 0) throw new IllegalArgumentException("array must not be empty");
        if (k < 0 || k >= arr.length) throw new IllegalArgumentException("k out of range: " + k);

        if (metrics != null) metrics.start();
        try {
            int index = selectIndex(arr, 0, arr.length - 1, k, metrics);
            return arr[index];
        } finally {
            if (metrics != null) metrics.stop();
        }
    }

    /**
     * Returns the array index of the k-th smallest element within arr[left...right].
     * 'k' is a global index (0-based relative to whole array), not an offset.
     */
    private static int selectIndex(int[] arr, int left, int right, int k, Metrics metrics) {
        // defensive checks
        ArrayUtils.checkRange(arr, left, right);
        if (k < left || k > right) {
            throw new IllegalArgumentException("k (" + k + ") not in current range [" + left + "," + right + "]");
        }

        if (left == right) return left;

        if (metrics != null) metrics.enter();
        try {
            // compute medians of groups of up to 5 and move medians to front
            int medianPos = left;
            for (int i = left; i <= right; i += 5) {
                int subRight = Math.min(i + 4, right);
                insertionSort(arr, i, subRight, metrics);               // sort small group
                int medianIndex = i + ((subRight - i) >>> 1);         // group median
                ArrayUtils.swap(arr, medianPos, medianIndex, metrics); // move group median to position
                medianPos++;
            }

            int numMedians = medianPos - left;

            // find pivot = median of medians
            int pivotIndex;
            if (numMedians == 1) {
                pivotIndex = left;
            } else {
                int target = left + numMedians / 2; // median position among medians
                pivotIndex = selectIndex(arr, left, left + numMedians - 1, target, metrics);
            }

            // partition around pivot and recurse only to the side containing k
            ArrayUtils.swap(arr, pivotIndex, right, metrics); // move pivot to end
            int p = PartitionUtils.partitionLomuto(arr, left, right, metrics); // pivot final index

            if (k == p) {
                return p;
            } else if (k < p) {
                return selectIndex(arr, left, p - 1, k, metrics);
            } else {
                return selectIndex(arr, p + 1, right, k, metrics);
            }
        } finally {
            if (metrics != null) metrics.exit();
        }
    }

    /** Private insertion sort used only for tiny ranges (groups of <=5). */
    private static void insertionSort(int[] a, int left, int right, Metrics metrics) {
        for (int i = left + 1; i <= right; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= left) {
                if (metrics != null) metrics.addComparison();
                if (a[j] > key) {
                    a[j + 1] = a[j];
                    j--;
                } else {
                    break;
                }
            }
            a[j + 1] = key;
        }
    }
}
