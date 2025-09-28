package bench;

/**
 * Thin wrapper that delegates to JMH's CLI entry point.
 */
public final class BenchMain {
    private BenchMain() {}

    public static void main(String[] args) throws Exception {
        // Force ignoring JMH lock (use with care)
        System.setProperty("jmh.ignoreLock", "true");
        org.openjdk.jmh.Main.main(args);
    }
}
