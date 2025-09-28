package bench;

import algorithms.DeterministicSelect;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * JMH benchmark: compare DeterministicSelect vs Arrays.sort for selecting k-th element.
 * - clones input array for each invocation to avoid interference
 * - parametrized by n, distribution, and k-position
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 6, time = 200, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 1)
@State(Scope.Thread)
public class SelectVsSortBenchmark {

    @Param({"1000","10000"})
    public int n;

    // distribution: "uniform", "duplicates", "sorted", "reverse"
    @Param({"uniform","duplicates","sorted","reverse"})
    public String distribution;

    // k position: "first", "middle", "last", "random"
    @Param({"first","middle","last","random"})
    public String kPos;

    @Param({"42"}) // seed for reproducibility; can pass via -p
    public long seed;

    private int[] base; // original input for trial
    private int k;

    @Setup(Level.Trial)
    public void setupTrial() {
        Random rnd = new Random(seed);
        base = new int[n];
        switch (distribution) {
            case "uniform":
                for (int i = 0; i < n; i++) base[i] = rnd.nextInt();
                break;
            case "duplicates":
                for (int i = 0; i < n; i++) base[i] = rnd.nextInt(10); // many duplicates
                break;
            case "sorted":
                for (int i = 0; i < n; i++) base[i] = i;
                break;
            case "reverse":
                for (int i = 0; i < n; i++) base[i] = n - i;
                break;
            default:
                throw new IllegalStateException("unknown distribution");
        }

        switch (kPos) {
            case "first": k = 0; break;
            case "last": k = n - 1; break;
            case "random": k = Math.abs(new Random(seed + 12345).nextInt(n)); break;
            case "middle":
            default: k = n/2;
        }
    }
    @Benchmark
    public void deterministicSelect(Blackhole bh) {
        int[] a = base.clone(); // select works in-place
        int v = DeterministicSelect.deterministicSelect(a, k); // returns k-th smallest
        bh.consume(v);
    }

    @Benchmark
    public void sortThenPick(Blackhole bh) {
        int[] a = base.clone();
        Arrays.sort(a);
        bh.consume(a[k]);
    }
}