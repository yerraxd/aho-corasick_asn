package tests;

import ahocorasick.AhoCorasick;
import ahocorasick.Match;

import java.util.HashSet;
import java.util.List;
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
}