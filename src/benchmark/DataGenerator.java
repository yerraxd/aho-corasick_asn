package benchmark;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public final class DataGenerator {

    private static final int ALPHABET = 4;

    private DataGenerator() {
    }

    public static String text(int length, long seed) {
        Random random = new Random(seed);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + random.nextInt(ALPHABET)));
        }
        return sb.toString();
    }

    public static String[] patterns(int count, long seed) {
        Random random = new Random(seed);
        Set<String> unique = new HashSet<>();
        while (unique.size() < count) {
            int length = 6 + random.nextInt(5);
            StringBuilder sb = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                sb.append((char) ('a' + random.nextInt(ALPHABET)));
            }
            unique.add(sb.toString());
        }
        return unique.toArray(new String[0]);
    }
}