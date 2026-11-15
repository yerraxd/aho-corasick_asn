package tests;

import ahocorasick.AhoCorasick;
import ahocorasick.Match;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TestRunner {

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
        run("classic ushers example", TestRunner::classicExample);
        run("single character patterns", TestRunner::singleCharacters);
        run("overlapping and nested patterns", TestRunner::overlappingAndNested);
        run("no match and empty text", TestRunner::noMatchAndEmptyText);
        run("pattern equals text", TestRunner::patternEqualsText);
        run("duplicate and empty patterns rejected", TestRunner::invalidPatterns);
        run("matches naive search on random data", TestRunner::randomAgainstNaive);
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private interface Check {
        void run();
    }

    private static void run(String name, Check check) {
        try {
            check.run();
            passed++;
            System.out.println("PASS " + name);
        } catch (RuntimeException | AssertionError e) {
            failed++;
            System.out.println("FAIL " + name + ": " + e.getMessage());
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("expected " + expected + " but got " + actual);
        }
    }

    private static Set<Match> asSet(List<Match> matches) {
        return new HashSet<>(matches);
    }

    private static void classicExample() {
        AhoCorasick ac = new AhoCorasick(new String[]{"he", "she", "his", "hers"});
        Set<Match> expected = Set.of(new Match(1, 1), new Match(2, 0), new Match(2, 3));
        assertEquals(expected, asSet(ac.search("ushers")));
    }

    private static void singleCharacters() {
        AhoCorasick ac = new AhoCorasick(new String[]{"a", "b"});
        assertEquals(Set.of(new Match(0, 0), new Match(1, 1), new Match(2, 0)), asSet(ac.search("aba")));
    }

    private static void overlappingAndNested() {
        AhoCorasick ac = new AhoCorasick(new String[]{"a", "aa", "aaa"});
        assertEquals(12L, ac.count("aaaaa"));
    }

    private static void noMatchAndEmptyText() {
        AhoCorasick ac = new AhoCorasick(new String[]{"abc", "xyz"});
        assertEquals(0, ac.search("abxabyxz").size());
        assertEquals(0, ac.search("").size());
    }

    private static void patternEqualsText() {
        AhoCorasick ac = new AhoCorasick(new String[]{"hello"});
        assertEquals(List.of(new Match(0, 0)), ac.search("hello"));
    }

    private static void invalidPatterns() {
        boolean duplicateRejected = false;
        boolean emptyRejected = false;
        try {
            new AhoCorasick(new String[]{"ab", "ab"});
        } catch (IllegalArgumentException e) {
            duplicateRejected = true;
        }
        try {
            new AhoCorasick(new String[]{""});
        } catch (IllegalArgumentException e) {
            emptyRejected = true;
        }
        assertEquals(true, duplicateRejected);
        assertEquals(true, emptyRejected);
    }

    private static Set<Match> naive(String[] patterns, String text) {
        Set<Match> result = new HashSet<>();
        for (int p = 0; p < patterns.length; p++) {
            int from = 0;
            int at;
            while ((at = text.indexOf(patterns[p], from)) != -1) {
                result.add(new Match(at, p));
                from = at + 1;
            }
        }
        return result;
    }

    private static String randomString(Random random, int length, int alphabet) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + random.nextInt(alphabet)));
        }
        return sb.toString();
    }

    private static String[] randomPatterns(Random random, int count) {
        Set<String> unique = new HashSet<>();
        while (unique.size() < count) {
            unique.add(randomString(random, 1 + random.nextInt(5), 3));
        }
        return unique.toArray(new String[0]);
    }

    private static void randomAgainstNaive() {
        Random random = new Random(1);
        for (int round = 0; round < 200; round++) {
            String[] patterns = randomPatterns(random, 1 + random.nextInt(8));
            String text = randomString(random, random.nextInt(60), 3);
            assertEquals(naive(patterns, text), asSet(new AhoCorasick(patterns).search(text)));
        }
    }
}