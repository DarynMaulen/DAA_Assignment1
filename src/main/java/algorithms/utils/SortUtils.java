package algorithms.utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Miscellaneous sorting helpers:
 *  - in-place Fisher–Yates shuffle
 *  - normalizeCutOff: ensure reasonable cutoff value
 * Shuffle is helpful to protect QuickSort/Select from adversarial inputs.
 */
public final class SortUtils {

    private SortUtils() {}

    /**
     * Fisher–Yates in-place shuffle.
     * @param arr array to shuffle (must not be null)
     */
    public static void shuffle(int[] arr) {
        ArrayUtils.requireNonNull(arr, "arr");
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            ArrayUtils.swap(arr, i, j);
        }
    }

    /**
     * Normalize cutoff value: if requested < 1, return defaultVal.
     */
    public static int normalizeCutOff(int requested, int defaultVal) {
        return requested < 1 ? defaultVal : requested;
    }
}
