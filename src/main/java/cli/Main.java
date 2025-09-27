package cli;

import algorithms.MergeSort;
import algorithms.QuickSort;
import algorithms.DeterministicSelect;
import algorithms.ClosestPairOfPoints.ClosestPair;
import algorithms.ClosestPairOfPoints.Point;
import algorithms.ClosestPairOfPoints.ClosestPairResult;

import algorithms.metrics.Metrics;
import algorithms.metrics.MetricsCsvWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * CLI entrypoint that runs selected algorithms over ranges of n,
 * collects metrics and optionally appends them into a CSV file.
 * Typical flow:
 *  - parse args -> CliConfig
 *  - prepare CSV (delete if --no-append)
 *  - for each n and repetition:
 *      - generate input (array or points) using seedUsed
 *      - run algorithm with fresh Metrics
 *      - append snapshot to CSV
 */
public class Main {
    public static void main(String[] args) throws IOException {
        CliConfig cfg;
        try {
            cfg = CliConfig.parseArgs(args);
        } catch (IllegalArgumentException ex) {
            String msg = ex.getMessage();
            // if message looks like usage, treat as normal help
            if (msg != null && msg.startsWith("Usage:")) {
                System.out.println(msg);
            } else {
                System.err.println(msg);
            }
            return;
        }

            System.out.println("CLI config: " + cfg);
        Path csvPath = cfg.csvOut;
        MetricsCsvWriter writer = null;
        if (csvPath != null) {
            try {
                if (!cfg.csvAppend) {
                    Files.deleteIfExists(csvPath);
                }
                writer = new MetricsCsvWriter(csvPath);
            } catch (IOException ex) {
                System.err.println("Failed to prepare CSV file : " + ex.getMessage());
                return;
            }
        }

        Random globalRand = new Random();

        for (int n = cfg.minN; n <= cfg.maxN; n += cfg.stepN) {
            for (int rep = 0; rep < cfg.repetitions; rep++) {
                Long userSeed = cfg.seed;
                long seedUsed = (userSeed == null) ? globalRand.nextLong() : (userSeed + rep);

                int[] data = generateRandomArray(n, seedUsed);

                // MERGESORT
                if (cfg.algorithms.contains(CliConfig.Algorithm.ALL) || cfg.algorithms.contains(CliConfig.Algorithm.MERGESORT)) {
                    Metrics metrics = new Metrics();
                    int[] arr = data.clone();
                    MergeSort.mergeSort(arr, cfg.insertionThreshold, metrics);
                    if (writer != null) {
                        try {
                            writer.append(metrics.snapshot("mergesort", n, seedUsed));
                        } catch (IOException e) {
                            System.err.println("Failed to write CSV (mergesort): " + e.getMessage());
                        }
                    }
                }

                // QUICKSORT
                if (cfg.algorithms.contains(CliConfig.Algorithm.ALL) || cfg.algorithms.contains(CliConfig.Algorithm.QUICKSORT)) {
                    Metrics metrics = new Metrics();
                    int[] arr = data.clone();
                    QuickSort.quickSort(arr, cfg.insertionThreshold, metrics);
                    if (writer != null) {
                        try {
                            writer.append(metrics.snapshot("quicksort", n, seedUsed));
                        } catch (IOException e) {
                            System.err.println("Failed to write CSV (quicksort): " + e.getMessage());
                        }
                    }
                }

                // SELECT
                if (cfg.algorithms.contains(CliConfig.Algorithm.ALL) || cfg.algorithms.contains(CliConfig.Algorithm.SELECT)) {
                    Metrics metrics = new Metrics();
                    int[] arr = data.clone();
                    int k = n / 2;
                    int value = DeterministicSelect.deterministicSelect(arr, k, metrics);
                    if (writer != null) {
                        try {
                            writer.append(metrics.snapshot("select", n, seedUsed));
                        } catch (IOException e) {
                            System.err.println("Failed to write CSV (select): " + e.getMessage());
                        }
                    }
                }

                // CLOSEST PAIR
                if (cfg.algorithms.contains(CliConfig.Algorithm.ALL) || cfg.algorithms.contains(CliConfig.Algorithm.CLOSEST)) {
                    Metrics metrics = new Metrics();
                    Point[] pts = generateRandomPoints(n, seedUsed);
                    ClosestPairResult res = ClosestPair.findClosest(pts, cfg.insertionThreshold, metrics);
                    if (writer != null) {
                        try {
                            writer.append(metrics.snapshot("closest", n, seedUsed));
                        } catch (IOException e) {
                            System.err.println("Failed to write CSV (closest): " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /** Generate random int array from given seed. */
    private static int[] generateRandomArray(int n, long seed) {
        Random rnd = new Random(seed);
        int[] data = new int[n];
        for (int i = 0; i < n; i++) {
            data[i] = rnd.nextInt();
        }
        return data;
    }

    /** Generate random points in [0,1)^2 from given seed. */
    private static Point[] generateRandomPoints(int n, long seed) {
        Random rnd = new Random(seed);
        Point[] pts = new Point[n];
        for (int i = 0; i < n; i++) {
            pts[i] = new Point(rnd.nextDouble(), rnd.nextDouble());
        }
        return pts;
    }
}
