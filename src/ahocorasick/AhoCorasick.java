package ahocorasick;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AhoCorasick {

    private static final int ALPHABET = 128;

    private final String[] patterns;
    private final List<int[]> next = new ArrayList<>();
    private final List<Integer> patternAt = new ArrayList<>();

    public AhoCorasick(String[] patterns) {
        this.patterns = patterns.clone();
        addNode();
    }

    private int addNode() {
        int[] row = new int[ALPHABET];
        Arrays.fill(row, -1);
        next.add(row);
        patternAt.add(-1);
        return next.size() - 1;
    }
    private void buildTrie() {
        for (int index = 0; index < patterns.length; index++) {
            insert(patterns[index], index);
        }
    }

    private void insert(String pattern, int index) {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("Empty pattern at index " + index);
        }
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) >= ALPHABET) {
                throw new IllegalArgumentException("Non-ASCII character in pattern " + index);
            }
        }
        int state = 0;
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (next.get(state)[c] == -1) {
                int created = addNode();
                next.get(state)[c] = created;
            }
            state = next.get(state)[c];
        }
        patternAt.set(state, index);
    }
}