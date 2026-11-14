package ahocorasick;

import java.util.ArrayList;
import java.util.List;

public final class Kmp {

    private Kmp() {
    }

    static int[] prefixFunction(String pattern) {
        int[] pi = new int[pattern.length()];
        for (int i = 1; i < pattern.length(); i++) {
            int k = pi[i - 1];
            while (k > 0 && pattern.charAt(i) != pattern.charAt(k)) {
                k = pi[k - 1];
            }
            if (pattern.charAt(i) == pattern.charAt(k)) {
                k++;
            }
            pi[i] = k;
        }
        return pi;
    }

    public static List<Integer> search(String pattern, String text) {
        List<Integer> starts = new ArrayList<>();
        int[] pi = prefixFunction(pattern);
        int k = 0;
        for (int i = 0; i < text.length(); i++) {
            while (k > 0 && text.charAt(i) != pattern.charAt(k)) {
                k = pi[k - 1];
            }
            if (text.charAt(i) == pattern.charAt(k)) {
                k++;
            }
            if (k == pattern.length()) {
                starts.add(i - k + 1);
                k = pi[k - 1];
            }
        }
        return starts;
    }

    public static long count(String pattern, String text) {
        long total = 0;
        int[] pi = prefixFunction(pattern);
        int k = 0;
        for (int i = 0; i < text.length(); i++) {
            while (k > 0 && text.charAt(i) != pattern.charAt(k)) {
                k = pi[k - 1];
            }
            if (text.charAt(i) == pattern.charAt(k)) {
                k++;
            }
            if (k == pattern.length()) {
                total++;
                k = pi[k - 1];
            }
        }
        return total;
    }

    public static long countAll(String[] patterns, String text) {
        long total = 0;
        for (String pattern : patterns) {
            total += count(pattern, text);
        }
        return total;
    }
}