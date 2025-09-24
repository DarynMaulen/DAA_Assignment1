package org.example.metrics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

public class MetricsCsvWriter {
    private final Path file;

    public MetricsCsvWriter(Path file) {
        this.file = file;
    }

    public MetricsCsvWriter(String pathStr) {
        this(Paths.get(pathStr));
    }

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

    private void createParentIfNeeded() throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }

    private void ensureHeaderAtomically() throws IOException {
        try {
            String header = "algo,n,seed,elapsedMs,comparisons,swaps,maxDepth,timestamp";
            Files.writeString(file, header + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (FileAlreadyExistsException ignored) {
        }
    }

    private String toCsvLine(MetricsSnapshot s) {
        StringBuilder sb = new StringBuilder();
        sb.append(escape(s.algo())).append(',');
        sb.append(s.n()).append(',');
        sb.append(s.seed()).append(',');
        sb.append(String.format(Locale.ROOT, "%.3f", s.elapsedMs())).append(',');
        sb.append(s.comparisons()).append(',');
        sb.append(s.swaps()).append(',');
        sb.append(s.maxDepth()).append(',');
        sb.append(Instant.ofEpochMilli(s.timestamp()).toString());
        return sb.toString();
    }

    private String escape(String value) {
        if (value == null) return "";
        boolean needQuotes = value.contains(",") || value.contains("\"") ||  value.contains("\n") || value.contains("\r");
        if (!needQuotes) return value;
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}