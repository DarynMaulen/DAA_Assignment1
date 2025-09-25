package algorithms;

import algorithms.metrics.Metrics;

/**
 * MergeSort: reusable-buffer merge sort with insertion-sort cutoff and optional Metrics instrumentation.
 */
public final class MergeSort {
    public static final int DEFAULT_INSERTION_THRESHOLD = 16;

    private MergeSort() {}

    /**
     * Public API: sort using default cutoff and no metrics.
     */
    public static void mergeSort(int[] arr) {
        mergeSort(arr, DEFAULT_INSERTION_THRESHOLD, null);
    }

    /**
     * Public API: sort using default cutoff and provided Metrics collector.
     */
    public static void mergeSort(int[] arr, Metrics metrics) {
        mergeSort(arr, DEFAULT_INSERTION_THRESHOLD, metrics);
    }

    /**
     * Core public API:
     * - allocates a single temporary buffer
     * - wraps the run with metrics.start()/stop() when metrics != null
     * @param arr array to sort
     * @param insertionThreshold cutoff for switching to insertion sort
     * @param metrics optional metrics collector
     */
    public static void mergeSort(int[] arr, int insertionThreshold, Metrics metrics) {
        if (arr == null || arr.length < 2) return;
        if (insertionThreshold < 1) insertionThreshold = DEFAULT_INSERTION_THRESHOLD;

        if (metrics != null) metrics.start(); // overall timing start

        try {
            int[] buffer = new int[arr.length]; // single reusable buffer for all merges
            mergeSortRecursive(arr, buffer, 0, arr.length - 1, insertionThreshold, metrics);
        } finally {
            if (metrics != null) metrics.stop(); // overall timing stop
        }
    }

    /**
     * Recursive divide-and-conquer routine.
     * - tracks recursion depth via metrics.enter()/exit()
     * - uses cutoff to switch to insertion sort on small segments
     */
    private static void mergeSortRecursive(int[] a, int[] tmp, int left, int right,
                                           int insertionThreshold, Metrics metrics) {
        if (left >= right) return;

        if (metrics != null) metrics.enter(); // recursion depth ++
        try {
            int len = right - left + 1;
            if (len <= insertionThreshold) {
                insertionSort(a, left, right, metrics); // cutoff to insertion sort
                return;
            }

            int mid = (left + right) >>> 1;
            mergeSortRecursive(a, tmp, left, mid, insertionThreshold, metrics);
            mergeSortRecursive(a, tmp, mid + 1, right, insertionThreshold, metrics);

            if (a[mid] <= a[mid + 1]) return;

            merge(a, tmp, left, mid, right, metrics);
        } finally {
            if (metrics != null) metrics.exit(); // recursion depth --
        }
    }

    /**
     * Linear merge: merge two sorted halves [left...mid] and [mid+1...right]
     * Writes merged output to tmp and then bulk-copies back to array.
     * Counts comparisons via metrics.addComparison().
     */
    private static void merge(int[] arr, int[] tmp, int left, int mid, int right, Metrics metrics) {
        int i = left, j = mid + 1, k = left;
        while (i <= mid && j <= right) {
            if (metrics != null) metrics.addComparison(); // count element compare
            if (arr[i] <= arr[j]) {
                tmp[k++] = arr[i++];
            } else {
                tmp[k++] = arr[j++];
            }
        }
        while (i <= mid) {
            tmp[k++] = arr[i++];
        }
        while (j <= right) {
            tmp[k++] = arr[j++];
        }
        System.arraycopy(tmp, left, arr, left, right - left + 1);
    }

    /**
     * Insertion sort for small sub arrays.
     * Counts comparisons via metrics.addComparison().
     */
    private static void insertionSort(int[] a, int left, int right, Metrics metrics) {
        for (int i = left + 1; i <= right; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= left) {
                if (metrics != null) metrics.addComparison(); // count compare inside insertion loop
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