package algorithms.metrics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Simple CSV appender for MetricsSnapshot.
 * Important:
 *  - Uses semicolon (;) as the field separator to be Excel-friendly in many locales.
 *  - Writes header atomically (CREATE_NEW) the first time the file is created.
 *  - Escapes fields that contain the separator or quotes by wrapping them in double-quotes
 *    and doubling internal quotes (standard CSV escaping).
 * Thread-safety: append(...) is synchronized to support concurrent callers.
 */
public class MetricsCsvWriter {
    private final Path file;
    private static final char SEP = ';';
    // header fields in the chosen order (semicolon-separated)
    private static final String HEADER = "algo;n;seed;elapsedMs;comparisons;swaps;maxDepth;timestamp";

    public MetricsCsvWriter(Path file) {
        this.file = file;
    }

    public MetricsCsvWriter(String pathStr) {
        this(Paths.get(pathStr));
    }

    /**
     * Append a snapshot as a single line in the CSV file.
     * Creates parent directories if needed and ensures header is present.
     * @param s non-null snapshot
     */
    public synchronized void append(MetricsSnapshot s) throws IOException {
        Objects.requireNonNull(s, "snapshot is null");
        createParentIfNeeded();
        ensureHeaderAtomically();

        try (BufferedWriter out = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            out.write(toCsvLine(s));
            out.newLine();
            out.flush();
        }
    }

    // Ensure parent directories exist
    private void createParentIfNeeded() throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }

    // Try to create the file with header once
    private void ensureHeaderAtomically() throws IOException {
        try {
            Files.writeString(file, HEADER + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (FileAlreadyExistsException ignored) {
        }
    }

    // Convert snapshot to one CSV line
    private String toCsvLine(MetricsSnapshot s) {
        StringBuilder sb = new StringBuilder();
        sb.append(escape(s.algo())).append(SEP);
        sb.append(s.n()).append(SEP);
        sb.append(Long.toUnsignedString(s.seed())).append(SEP);
        sb.append(String.format(Locale.ROOT, "%.3f", s.elapsedMs())).append(SEP);
        sb.append(s.comparisons()).append(SEP);
        sb.append(s.swaps()).append(SEP);
        sb.append(s.maxDepth()).append(SEP);
        sb.append(escape(Instant.ofEpochMilli(s.timestamp()).toString()));
        return sb.toString();
    }

    /**
     * Escape a field value if it contains the separator, quotes, or newlines.
     * Uses double quotes (") as wrapper and duplicates internal quotes per CSV rules.
     */
    private String escape(String value) {
        if (value == null || value.isEmpty()) return "";
        boolean needQuotes = value.indexOf(SEP) >= 0 || value.contains("\"") || value.contains("\n") || value.contains("\r");
        if (!needQuotes) return value;
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
