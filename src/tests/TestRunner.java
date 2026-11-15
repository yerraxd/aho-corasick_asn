package tests;

import ahocorasick.Match;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestRunner {

    private static int passed;
    private static int failed;

    public static void main(String[] args) {
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
}