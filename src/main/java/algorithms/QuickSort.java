package algorithms;

import algorithms.metrics.Metrics;

import java.util.concurrent.ThreadLocalRandom;

/**
 * QuickSort with:
 *  - randomized pivot
 *  - recurse on the smaller partition, iterate on the larger one
 *  - insertion-sort cutoff for small partitions
 *  - optional Metrics instrumentation (start/stop, enter/exit, comparisons, swaps)
 */
public final class QuickSort {
    public static final int DEFAULT_INSERTION_THRESHOLD = 16;

    private QuickSort() {}

    public static void quickSort(int[] arr) {
        quickSort(arr, DEFAULT_INSERTION_THRESHOLD, null);
    }

    public static void quickSort(int[] arr, Metrics metrics) {
        quickSort(arr, DEFAULT_INSERTION_THRESHOLD, metrics);
    }

    public static void quickSort(int[] arr, int insertionThreshold, Metrics metrics) {
        if (arr == null || arr.length < 2) return;
        if (insertionThreshold < 1) insertionThreshold = DEFAULT_INSERTION_THRESHOLD;

        if (metrics != null) metrics.start();
        try {
            quickSortRecursive(arr, 0, arr.length - 1, insertionThreshold, metrics);
        } finally {
            if (metrics != null) metrics.stop();
        }
    }

    /**
     * Recursively sort using "recurse on smaller, iterate on larger" pattern.
     * This keeps expected stack depth to O(log n).
     */
    private static void quickSortRecursive(int[] arr, int left, int right, int insertionThreshold, Metrics metrics) {
        while (left < right) {
            int len = right - left + 1;
            if (len <= insertionThreshold) {
                insertionSort(arr, left, right, metrics);
                return;
            }

            if (metrics != null) metrics.enter(); // track recursion depth
            try {
                // randomized pivot
                int pivotIndex = left + ThreadLocalRandom.current().nextInt(len);
                swap(arr, pivotIndex, right, metrics); // move pivot to end

                int p = partitionLomuto(arr, left, right, metrics);

                int leftSize = p - left;
                int rightSize = right - p;

                // recurse on smaller partition, iterate on larger partition
                if (leftSize < rightSize) {
                    quickSortRecursive(arr, left, p - 1, insertionThreshold, metrics);
                    left = p + 1; // tail-iterate on right side
                } else {
                    quickSortRecursive(arr, p + 1, right, insertionThreshold, metrics);
                    right = p - 1; // tail-iterate on left side
                }
            } finally {
                if (metrics != null) metrics.exit();
            }
        }
    }

    /** Lomuto partition implementation. Pivot is at arr[right]. Returns final pivot index. */
    private static int partitionLomuto(int[] arr, int left, int right, Metrics metrics) {
        int pivot = arr[right];
        int i = left;
        for (int j = left; j < right; j++) {
            if (metrics != null) metrics.addComparison();
            if (arr[j] <= pivot) {
                swap(arr, i, j, metrics);
                i++;
            }
        }
        swap(arr, i, right, metrics);
        return i;
    }

    private static void insertionSort(int[] arr, int left, int right, Metrics metrics) {
        for (int i = left + 1; i <= right; i++) {
            int key = arr[i];
            int j = i - 1;
            while (j >= left) {
                if (metrics != null) metrics.addComparison();
                if (arr[j] > key) {
                    arr[j + 1] = arr[j];
                    if (metrics != null) metrics.addSwap();
                    j--;
                } else {
                    break;
                }
            }
            arr[j + 1] = key;
        }
    }

    private static void swap(int[] arr, int i, int j, Metrics metrics) {
        if (i == j) return;
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
        if (metrics != null) metrics.addSwap();
    }
}
