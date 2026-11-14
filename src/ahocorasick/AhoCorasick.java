package ahocorasick;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AhoCorasick {

    private static final int ALPHABET = 128;

    private final String[] patterns;
    private final List<int[]> next = new ArrayList<>();
    private final List<Integer> patternAt = new ArrayList<>();
    private int[] fail;
    private int[] outputLink;

    public AhoCorasick(String[] patterns) {
        this.patterns = patterns.clone();
        addNode();
        buildTrie();
        buildLinks();
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
        if (patternAt.get(state) != -1) {
            throw new IllegalArgumentException("Duplicate pattern: " + pattern);
        }
        patternAt.set(state, index);
    }

    private void buildLinks() {
        int size = next.size();
        fail = new int[size];
        outputLink = new int[size];
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int c = 0; c < ALPHABET; c++) {
            int child = next.get(0)[c];
            if (child != -1) {
                queue.add(child);
            }
        }
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int c = 0; c < ALPHABET; c++) {
                int v = next.get(u)[c];
                if (v == -1) {
                    continue;
                }
                int f = fail[u];
                while (f != 0 && next.get(f)[c] == -1) {
                    f = fail[f];
                }
                int candidate = next.get(f)[c];
                fail[v] = candidate != -1 ? candidate : 0;
                int target = fail[v];
                outputLink[v] = patternAt.get(target) != -1 ? target : outputLink[target];
                queue.add(v);
            }
        }
    }

    private int step(int state, char c) {
        if (c >= ALPHABET) {
            return 0;
        }
        while (state != 0 && next.get(state)[c] == -1) {
            state = fail[state];
        }
        int target = next.get(state)[c];
        return target != -1 ? target : 0;
    }

    public long count(String text) {
        long total = 0;
        int state = 0;
        for (int i = 0; i < text.length(); i++) {
            state = step(state, text.charAt(i));
            int node = patternAt.get(state) != -1 ? state : outputLink[state];
            while (node != 0) {
                total++;
                node = outputLink[node];
            }
        }
        return total;
    }
}