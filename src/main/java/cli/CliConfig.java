package cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * CLI configuration holder and parser for the benchmarking tool.
 * Responsibilities:
 *  - parse command-line arguments
 *  - provide a validated immutable configuration object
 * Usage: CliConfig.parseArgs(args) -> CliConfig instance
 */
public final class CliConfig {
    public enum Algorithm { MERGESORT, QUICKSORT, SELECT, CLOSEST, ALL }

    public final List<Algorithm> algorithms;
    public final int minN;
    public final int maxN;
    public final int stepN;
    public final int repetitions;
    public final Long seed;
    public final int insertionThreshold;
    public final Path csvOut;
    public final boolean csvAppend;

    public CliConfig(List<Algorithm> algorithms, int minN, int maxN,
                     int stepN, int repetitions, Long seed,
                     int insertionThreshold, Path csvOut, boolean csvAppend) {
        this.algorithms = List.copyOf(algorithms);
        this.minN = minN;
        this.maxN = maxN;
        this.stepN = stepN;
        this.repetitions = repetitions;
        this.seed = seed;
        this.insertionThreshold = insertionThreshold;
        this.csvOut = csvOut;
        this.csvAppend = csvAppend;
    }

    /**
     * Basic validation of numeric ranges and selections.
     * Throws IllegalArgumentException on invalid config.
     */
    public void validate() {
        if (algorithms.isEmpty()) {
            throw new IllegalArgumentException("no algorithms selected");
        }
        if (minN <= 0) {
            throw new IllegalArgumentException("minN must be greater than 0");
        }
        if (maxN <= minN) {
            throw new IllegalArgumentException("maxN must be greater than minN");
        }
        if (stepN <= 0) {
            throw new IllegalArgumentException("stepN must be greater than 0");
        }
        if (repetitions <= 0) {
            throw new IllegalArgumentException("repetitions must be greater than 0");
        }
    }

    /**
     * Parse command-line args into a CliConfig.
     * Supported options:
     *  --algorithms <comma-separated>
     *  --n <min:max[:step]>
     *  --repetitions <r>
     *  --seed <long>
     *  --cutoff <k>
     *  --csv <path>
     *  --append / --no-append
     * Throws IllegalArgumentException on parse errors or unknown options.
     */
    public static CliConfig parseArgs(String[] args) {
        List<Algorithm> algorithms = new ArrayList<>();
        int minN = 1000, maxN = 1000, stepN = 1;
        int repetitions = 3;
        Long seed = null;
        int insertionThreshold = 16;
        Path csvOut = Paths.get("metrics.csv"); // default output path
        boolean csvAppend = true;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--help":
                    throw new IllegalArgumentException(usage());

                case "--algorithms":
                    String valAlg = requireNext(args, i, "--algorithms"); i++;
                    String[] split = valAlg.split(",");
                    for (String s : split) {
                        switch (s.trim().toLowerCase()) {
                            case "mergesort": algorithms.add(Algorithm.MERGESORT); break;
                            case "quicksort": algorithms.add(Algorithm.QUICKSORT); break;
                            case "select": algorithms.add(Algorithm.SELECT); break;
                            case "closest": algorithms.add(Algorithm.CLOSEST); break;
                            case "all": algorithms.add(Algorithm.ALL); break;
                            default: throw new IllegalArgumentException("unknown algorithm: " + s);
                        }
                    }
                    break;

                case "--n":
                    String v = requireNext(args, i, "--n"); i++;
                    try {
                        if (v.contains(":")) {
                            String[] p = v.split(":");
                            if (p.length < 2) throw new IllegalArgumentException("invalid --n: " + v);
                            minN = Integer.parseInt(p[0]);
                            maxN = Integer.parseInt(p[1]);
                            stepN = (p.length > 2) ? Integer.parseInt(p[2]) : 1;
                        } else {
                            minN = maxN = Integer.parseInt(v);
                            stepN = 1;
                        }
                    } catch (NumberFormatException ex) {
                        throw new IllegalArgumentException("invalid number in --n: " + v, ex);
                    }
                    break;

                case "--repetitions":
                    String r = requireNext(args, i, "--repetitions"); i++;
                    try { repetitions = Integer.parseInt(r); }
                    catch (NumberFormatException ex) { throw new IllegalArgumentException("invalid --repetitions: " + r, ex); }
                    break;

                case "--seed":
                    String sVal = requireNext(args, i, "--seed"); i++;
                    try { seed = Long.parseLong(sVal); }
                    catch (NumberFormatException ex) { throw new IllegalArgumentException("invalid --seed: " + sVal, ex); }
                    break;

                case "--cutoff":
                    String cVal = requireNext(args, i, "--cutoff"); i++;
                    try { insertionThreshold = Integer.parseInt(cVal); }
                    catch (NumberFormatException ex) { throw new IllegalArgumentException("invalid --cutoff: " + cVal, ex); }
                    break;

                case "--csv":
                    String path = requireNext(args, i, "--csv"); i++;
                    csvOut = Paths.get(path);
                    break;

                case "--append":
                    csvAppend = true;
                    break;

                case "--no-append":
                    csvAppend = false;
                    break;

                default:
                    throw new IllegalArgumentException("unknown option: " + arg + "\n" + usage());
            }
        }

        if (algorithms.isEmpty()) algorithms.add(Algorithm.ALL);

        if (algorithms.contains(Algorithm.ALL)) {
            algorithms.clear();
            algorithms.add(Algorithm.MERGESORT);
            algorithms.add(Algorithm.QUICKSORT);
            algorithms.add(Algorithm.SELECT);
            algorithms.add(Algorithm.CLOSEST);
        }

        CliConfig cfg = new CliConfig(algorithms, minN, maxN, stepN, repetitions,
                seed, insertionThreshold, csvOut, csvAppend);
        cfg.validate();
        return cfg;
    }

    /** Help text. */
    public static String usage() {
        return """
                Usage:
                  --algorithms <merge,quick,select,closest,all>    algorithms to run (comma-separated)
                  --n <N|min:max[:step]>                    problem size or range
                  --repetitions <r>                                repetitions per size (default 3)
                  --seed <long>                             random seed (optional)
                  --cutoff <k>                              insertion-sort cutoff (default 16)
                  --csv <path>                              CSV output file (semicolon separated)
                  --append / --no-append                    append or overwrite CSV (default append)
                  --help                                show this message
                """;
    }

    /** Helper: get the next argument value or throw if missing. */
    private static String requireNext(String[] args, int i, String opt){
        if (i + 1 >= args.length) throw new IllegalArgumentException("missing value for " + opt);
        return args[i+1];
    }
}
