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
}