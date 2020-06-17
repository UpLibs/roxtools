package roxtools.benchmarks;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import roxtools.BinaryScaledSearch;
import roxtools.BinaryScaledSearchTest;

public class BinaryScaledSearchBenchmark {

    private static final int BENCHMARK_LOOPS = 999;

    @Test
    @Disabled
    void benchmark() {
        System.out.println("prepare");
        benchmarkImpl(2);

        System.out.println("sleep...");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }

        System.gc();

        System.out.println("sleep...");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }

        System.out.println("benchmark");
        benchmarkImpl(10);
    }

    private void benchmarkImpl(int repetitions) {
        var set = BinaryScaledSearchTest.createSortedSet(100, 0, 100000, 123);
        var keysToSearch = BinaryScaledSearchTest.createSortedSet(10000, 0, 100000, 456);

        var bestTimeBinaryScaledSearch = Long.MAX_VALUE;
        var bestTimeBinarySearchOriginal = Long.MAX_VALUE;

        for (var i = 0 ; i < repetitions ; i++) {
            bestTimeBinaryScaledSearch = Math.min(bestTimeBinaryScaledSearch, benchmarkBinaryScaledSearch(set, keysToSearch));

            bestTimeBinarySearchOriginal = Math.min(bestTimeBinarySearchOriginal, benchmarkBinarySearchOriginal(set, keysToSearch));


            System.out.println("================================================== " + i);
            System.out.println("bestTimeBinaryScaledSearch: " + bestTimeBinaryScaledSearch);
            System.out.println("bestTimeBinarySearchOriginal: " + bestTimeBinarySearchOriginal);
            System.out.println("==================================================");
        }
    }

    private long benchmarkBinaryScaledSearch(int[] set, int[] keysToSearch) {
        var timeBinaryScaledSearch = System.currentTimeMillis();

        for (var loop = BENCHMARK_LOOPS ; loop >= 0 ; loop--) {
            for (var i = keysToSearch.length - 1 ; i >= 0 ; i--) {
                BinaryScaledSearch.search(set, keysToSearch[i]);
                BinaryScaledSearch.search(set, keysToSearch[i]);
                BinaryScaledSearch.search(set, keysToSearch[i]);
                BinaryScaledSearch.search(set, keysToSearch[i]);
                BinaryScaledSearch.search(set, keysToSearch[i]);

                BinaryScaledSearch.search(set, keysToSearch[i]);
                BinaryScaledSearch.search(set, keysToSearch[i]);
                BinaryScaledSearch.search(set, keysToSearch[i]);
                BinaryScaledSearch.search(set, keysToSearch[i]);
                BinaryScaledSearch.search(set, keysToSearch[i]);
            }
        }

        timeBinaryScaledSearch = System.currentTimeMillis() - timeBinaryScaledSearch;

        System.out.println("timeBinaryScaledSearch: " + timeBinaryScaledSearch);

        return timeBinaryScaledSearch;
    }

    private long benchmarkBinarySearchOriginal(int[] set, int[] keysToSearch) {
        var timeBinarySearchOriginal = System.currentTimeMillis();

        for (var loop = BENCHMARK_LOOPS ; loop >= 0 ; loop--) {
            for (var i = keysToSearch.length - 1 ; i >= 0 ; i--) {
                BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]);
                BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]);
                BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]);
                BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]);
                BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]);

                BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]);
                BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]);
                BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]);
                BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]);
                BinaryScaledSearch.binarySearchOriginal(set, keysToSearch[i]);
            }
        }

        timeBinarySearchOriginal = System.currentTimeMillis() - timeBinarySearchOriginal;

        System.out.println("timeBinarySearchOriginal: " + timeBinarySearchOriginal);

        return timeBinarySearchOriginal;
    }

}
