package algorithms.utils;

import algorithms.metrics.Metrics;
import java.util.Objects;

/**
 * Small, safe array helpers.
 *
 * - swap: safe index checks and optional metrics increment
 * - requireNonNull / checkIndex / checkRange: simple guards used by algorithms
 *
 * These methods are intentionally lightweight and thread-agnostic.
 */
public final class ArrayUtils {

    /**
     * Swap elements at indices i and j and record a swap in metrics.
     * Performs null and bounds checks.
     * @param arr     array to operate on (must not be null)
     * @param i       first index
     * @param j       second index
     * @param metrics optional metrics collector (maybe null)
     * @throws NullPointerException      when arr is null
     * @throws IndexOutOfBoundsException when i or j are out of bounds
     */
    public static void swap(int[] arr, int i, int j, Metrics metrics) {
        requireNonNull(arr, "arr");
        checkIndex(arr, i);
        checkIndex(arr, j);
        if (i == j) return;
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
        if (metrics != null) metrics.addSwap();
    }

    /**
     * Swap overload without metrics.
     */
    public static void swap(int[] arr, int i, int j) {
        swap(arr, i, j, null);
    }

    public static void swapUnchecked(int[] arr, int i, int j, Metrics metrics) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
        if (metrics != null) metrics.addSwap();
    }

    /**
     * Null-check helper.
     */
    public static void requireNonNull(Object o, String name) {
        if (Objects.isNull(o)) throw new NullPointerException(name + " must not be null");
    }

    /**
     * Check that an index belongs to array bounds.
     *
     * @throws NullPointerException      if arr is null
     * @throws IndexOutOfBoundsException if index is outside [0, arr.length-1]
     */
    public static void checkIndex(int[] arr, int index) {
        if (arr == null) throw new NullPointerException("arr is null");
        if (index < 0 || index >= arr.length)
            throw new IndexOutOfBoundsException(index + " is out of range [0," + (arr.length - 1) + "]");
    }

    /**
     * Validate that left...right is a valid inclusive range inside the array.
     *
     * @throws IllegalArgumentException if the range is invalid
     */
    public static void checkRange(int[] arr, int left, int right) {
        requireNonNull(arr, "arr");
        if (left < 0 || left > right || right >= arr.length) {
            throw new IllegalArgumentException("Invalid range: [" + left + "," + right + "] for length " + arr.length);
        }
    }
}
