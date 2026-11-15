package benchmark;

import ahocorasick.AhoCorasick;
import ahocorasick.Kmp;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class Benchmark {

    private static final int[] PATTERN_COUNTS = {10, 100, 1000};
    private static final int[] TEXT_LENGTHS = {1000, 10000, 100000};
    private static final int RUNS = 5;

    public static void main(String[] args) throws IOException {
        warmUp();
        Files.createDirectories(Path.of("results"));
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(Path.of("results/results.csv")))) {
            String header = "patterns,textLength,matches,acBuildMs,acSearchMs,acTotalMs,kmpTotalMs";
            out.println(header);
            System.out.println(header);
            for (int patternCount : PATTERN_COUNTS) {
                for (int textLength : TEXT_LENGTHS) {
                    String line = measure(patternCount, textLength);
                    out.println(line);
                    System.out.println(line);
                }
            }
        }
    }

    private static String measure(int patternCount, int textLength) {
        String[] patterns = DataGenerator.patterns(patternCount, 100 + patternCount);
        String text = DataGenerator.text(textLength, 200 + textLength);

        long buildTotal = 0;
        long searchTotal = 0;
        long kmpTotal = 0;
        long acMatches = 0;
        long kmpMatches = 0;

        for (int run = 0; run < RUNS; run++) {
            long t0 = System.nanoTime();
            AhoCorasick ac = new AhoCorasick(patterns);
            long t1 = System.nanoTime();
            acMatches = ac.count(text);
            long t2 = System.nanoTime();
            kmpMatches = Kmp.countAll(patterns, text);
            long t3 = System.nanoTime();

            buildTotal += t1 - t0;
            searchTotal += t2 - t1;
            kmpTotal += t3 - t2;
        }

        if (acMatches != kmpMatches) {
            throw new IllegalStateException("Mismatch: AC=" + acMatches + " KMP=" + kmpMatches);
        }

        double build = buildTotal / (double) RUNS / 1e6;
        double search = searchTotal / (double) RUNS / 1e6;
        double kmp = kmpTotal / (double) RUNS / 1e6;
        return String.format(Locale.US, "%d,%d,%d,%.4f,%.4f,%.4f,%.4f",
                patternCount, textLength, acMatches, build, search, build + search, kmp);
    }

    private static void warmUp() {
        String[] patterns = DataGenerator.patterns(100, 1);
        String text = DataGenerator.text(10000, 2);
        for (int i = 0; i < 20; i++) {
            new AhoCorasick(patterns).count(text);
            Kmp.countAll(patterns, text);
        }
    }
}