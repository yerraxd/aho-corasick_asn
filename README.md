# Aho-Corasick vs KMP

Aho-Corasick for searching many patterns at once, compared with running KMP once per pattern. Plain Java 17, no Maven or Gradle, just `javac` and `java`.

## Project layout

    src/ahocorasick/AhoCorasick.java   trie, failure links, output links, search
    src/ahocorasick/Kmp.java           prefix function and search for one pattern
    src/ahocorasick/Match.java         record with start index and pattern index
    src/benchmark/DataGenerator.java   random patterns and texts with fixed seeds
    src/benchmark/Benchmark.java       timing, writes results/results.csv
    src/tests/TestRunner.java          tests without a test framework
    results/results.csv                raw numbers from the benchmark
    results/plot_patterns.png          plot, time vs number of patterns
    results/plot_text_length.png       plot, time vs text length

## How to build and run

    mkdir -p out
    javac -d out src/ahocorasick/*.java src/benchmark/*.java src/tests/*.java
    java -cp out tests.TestRunner
    java -cp out benchmark.Benchmark

The test runner prints PASS or FAIL for each test and exits with code 1 if anything fails. The benchmark prints a CSV table and saves it to `results/results.csv`.

## Implementation

The automaton is built in three steps.

1. Trie. Every pattern is inserted character by character. Each node has an array of 128 children (ASCII only), and a node where a pattern ends remembers the index of that pattern.
2. Failure links. A BFS from the root gives every node a link to the longest proper suffix of its string that is also a prefix of some pattern. For a child `v` of node `u` reached by character `c`, I start at `fail[u]` and follow failure links until some node has an edge for `c`. The target of that edge becomes `fail[v]`. If I reach the root and it has no such edge, `fail[v]` is the root.
3. Output links. `outputLink[v]` points to the nearest node on the failure chain of `v` that ends a pattern. Without it, a match of "he" inside "she" would be missed when the current node is the one for "she", or the whole failure chain would have to be walked at every position.

Search keeps one current state. For each text character it follows failure links until there is an edge for that character and takes the edge. Then it reports the current node if it ends a pattern, plus every node reachable through output links.

Input rules: patterns must be non-empty, unique and ASCII, otherwise the constructor throws `IllegalArgumentException`. A non-ASCII character in the text resets the state to the root.

`search` returns all matches as `(start, patternIndex)`. `count` only counts them. The benchmark uses `count`, so neither algorithm spends time building a list.

KMP is the standard prefix function version. `Kmp.countAll` runs it for each pattern one after another.

## Tests

There are 8 tests in `TestRunner`:

- the classic example with `he`, `she`, `his`, `hers` on the text `ushers`
- single character patterns
- overlapping and nested patterns (`a`, `aa`, `aaa` on `aaaaa` gives 12 matches)
- no match, and an empty text
- a pattern equal to the whole text
- empty and duplicate patterns are rejected
- 200 random cases compared with a naive `indexOf` search
- 100 random cases compared with KMP

## Experiments

Setup:

- patterns: 10, 100 and 1000, random, length 6 to 10, alphabet `a` to `d`
- text: 1000, 10000 and 100000 characters from the same alphabet
- seeds are fixed, so every run uses the same data
- each case is run 5 times and the average is reported, measured with `System.nanoTime()`
- the benchmark has a warm-up loop for the JIT, and I ran the whole benchmark 3 times, the table is from the last run
- "AC total" is build plus search, "KMP total" includes computing the prefix function for every pattern
- the benchmark checks that both algorithms find the same number of occurrences and throws an exception if they differ

All times are in milliseconds.

| patterns | text length | matches | AC build | AC search | AC total | KMP total | KMP / AC |
|---:|---:|---:|---:|---:|---:|---:|---:|
| 10 | 1000 | 0 | 0.1643 | 0.0457 | 0.2099 | 0.2638 | 1.3x |
| 10 | 10000 | 9 | 0.1620 | 0.4546 | 0.6166 | 2.3979 | 3.9x |
| 10 | 100000 | 71 | 0.1658 | 5.6219 | 5.7877 | 27.1149 | 4.7x |
| 100 | 1000 | 6 | 1.5846 | 0.1042 | 1.6888 | 3.2995 | 2.0x |
| 100 | 10000 | 80 | 1.4645 | 0.6591 | 2.1236 | 32.1126 | 15.1x |
| 100 | 100000 | 729 | 1.9941 | 12.8410 | 14.8351 | 345.5492 | 23.3x |
| 1000 | 1000 | 52 | 23.5990 | 0.2457 | 23.8447 | 36.0968 | 1.5x |
| 1000 | 10000 | 678 | 18.0281 | 1.9744 | 20.0025 | 284.3477 | 14.2x |
| 1000 | 100000 | 6647 | 10.7956 | 18.5348 | 29.3304 | 4415.4481 | 150.5x |


Time against the number of patterns, text length 100000:

(results/plot_patterns.png)

Time against text length, one panel for each number of patterns:

(results/plot_text_length.png)

## Complexity

Notation: `L` is the total length of all patterns, `n` the text length, `k` the number of patterns, `z` the number of matches, `S` the alphabet size.

Aho-Corasick

- Building the trie takes O(L * S). There is one step per pattern character, and every new node gets an array of 128 children filled with -1.
- Failure and output links also take O(L * S) in my code, because the BFS loops over all 128 characters of every node. The number of failure steps alone is O(L). The depth of the failure target grows by at most 1 per character along a pattern, so the steps for one pattern cannot exceed its length.
- Search takes O(n + z). The state depth goes up by at most 1 per text character and every failure step lowers it, so failure steps over the whole text cannot exceed n. Reporting matches through output links costs 1 step per match.
- Space is O(L * S), because every node has an array of S children. With S = 128 that is 512 bytes per node (128 ints), which is the main memory cost of this implementation.

KMP, once per pattern

- Prefix functions for all patterns: O(L) in total.
- Search: O(n) for each pattern, so O(k * n) for all of them, plus O(z) for the matches.
- Space: O(m) for one prefix table, where m is the length of the current pattern. The tables are not needed at the same time.

For a fixed text the difference is O(n + z) against O(k * n + z). The number of patterns multiplies the text scan for KMP and does not appear in the search time of Aho-Corasick.

## Why a trie and failure links

The trie puts all patterns into one structure, so a single state can stand for the longest suffix of the text read so far that is a prefix of some pattern. When the next character does not continue that prefix, the failure link gives the next longest candidate that is still a prefix of some pattern. It is the idea of the prefix function in KMP, applied to a tree of patterns instead of one string. For this reason the text pointer never moves back, and one pass is enough for all patterns.

## Discussion of results

KMP time grows almost linearly with both the text length and the number of patterns. At 100000 characters it takes 27 ms for 10 patterns, 346 ms for 100 and 4415 ms for 1000, so each 10x in patterns costs a bit more than 10x in time. This matches O(k * n). On the first plot the KMP line is much steeper than the Aho-Corasick line.

Aho-Corasick search time depends mainly on the text length. For 1000 patterns it goes 0.25, 1.97, 18.5 ms when the text grows 10x each time. The total for 1000 patterns and 100000 characters is 29.3 ms against 4415 ms for KMP, about 150 times faster.

The search time of Aho-Corasick still grows a little with `k`, although the theory says O(n + z). On 100000 characters it is 5.6 ms for 10 patterns, 12.8 ms for 100 and 18.5 ms for 1000. Possible reasons are that the larger trie fits worse in the cache and that there are more matches to report (71, 729 and 6647), but I did not check which one matters more.

On short texts the advantage is small. With 10 patterns and 1000 characters both algorithms need around 0.2 to 0.3 ms, and most of the Aho-Corasick time (0.16 of 0.21 ms) goes to building the automaton. With 1000 patterns and 1000 characters the build takes 23.6 ms out of 23.8 ms, and Aho-Corasick is only 1.5 times faster. The build cost grows with the number of patterns, from about 0.16 ms for 10 to about 1.5 to 2 ms for 100 and 10 to 24 ms for 1000. If the same patterns are used on many texts, the build is paid once.

The build time for 1000 patterns is different in the three rows (23.6, 18.0 and 10.8 ms) although the patterns are the same. I think this comes from JIT and garbage collection, because each build allocates thousands of 128-element arrays. I would not read much into small differences in the build column.

The matches column has small numbers because the patterns are 6 to 10 characters long over 4 letters. I did not test texts where almost every position is a match. In that case `z` would be large and would take a big part of the search time of both algorithms.

## Advantages and limitations

Aho-Corasick

- Its search time does not multiply with the number of patterns, which is why it wins clearly from about 100 patterns on.
- It finds all patterns in one pass, including the ones that are suffixes of other patterns.
- It uses much more memory (an array of 128 ints for each node), and the automaton has to be built before the first search.
- Adding or removing a pattern means rebuilding the links. My version also rejects duplicate patterns and handles only ASCII.

KMP for each pattern

- It is short, needs very little memory and has no setup beyond one array per pattern. For one pattern, or for 2 or 3 patterns, it is the simpler choice, and the results for 10 patterns on 1000 characters show it is nearly as fast.
- It reads the text once for every pattern, so the cost grows with `k`. At 1000 patterns on 100000 characters that was the difference between 29 ms and 4.4 seconds.
