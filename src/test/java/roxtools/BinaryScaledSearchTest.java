package roxtools;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class BinaryScaledSearchTest {

    public static int[] createSortedSet(int size, int min, int max, long seed) {
        var rand = new Random(seed);
        var scale = max - min;

        var ints = new int[size];
        Arrays.setAll(ints, i -> min + rand.nextInt(scale));

        Arrays.sort(ints);
        return ints;
    }

    static Stream<Arguments> testBinaryScaledSearchAgainstOriginal() {
        var args = new ArrayList<Arguments>(1001);
        args.add(arguments(123, 456));
        var random = new Random(781198943);
        for (var i = 0 ; i < 1000 ; i++) {
            args.add(arguments(random.nextLong(), random.nextLong()));
        }
        return args.stream();
    }

    @MethodSource
    @ParameterizedTest
    void testBinaryScaledSearchAgainstOriginal(long seed1, long seed2) {
        var set = createSortedSet(10000, 0, 100000, seed1);
        var keysToSearch = createSortedSet(10000, 0, 100000, seed2);

        for (var k : keysToSearch) {
            int searchKeyIdx = BinaryScaledSearch.search(set, k);
            int originalSearchKeyIdx = BinaryScaledSearch.binarySearchOriginal(set, k);

            var keyFoundBySearch = searchKeyIdx >= 0;
            var keyFoundByOriginalSearch = originalSearchKeyIdx >= 0;
            assertEquals(keyFoundBySearch, keyFoundByOriginalSearch, "Both search methods should find or not find the key given the same input");

            if (searchKeyIdx < 0) {
                assertEquals(searchKeyIdx, originalSearchKeyIdx, "Since the key wasn't found and the insertion point was returned, both values should match");
            }
            else {
                var searchValue = set[searchKeyIdx];
                var originalSearchValue = set[originalSearchKeyIdx];

                assertEquals(searchValue, originalSearchValue, "Since the key was found, the retrieved values should match");
            }
        }
    }
}
