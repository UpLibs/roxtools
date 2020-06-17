package roxtools;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BigHashMapTest {

    @ParameterizedTest
    @ValueSource(ints = {100000, 2000000})
    void bigHashMapIntegerIntegerCreation(int totalInserts) {
        var bigHashMap = new BigHashMap<Integer, Integer>();

        for (var i = 0 ; i < totalInserts ; i++) {
            bigHashMap.put(i, i * 10);
        }

        assertEquals(totalInserts, bigHashMap.size(), "Size doesn't match expected value");

        var entrySetIterations = 0;
        for (var entry : bigHashMap.entrySet()) {

            assertEquals(entrySetIterations, (int) entry.getKey(), "Entry Key doesn't match expected value");
            assertEquals(entrySetIterations * 10, (int) entry.getValue(), "Entry Value doesn't match expected value");

            entrySetIterations++;
        }

        assertEquals(totalInserts, entrySetIterations, "Number of iterations from EntrySet doesn't match the number of total values");

        for (int key = 0 ; key < totalInserts ; key++) {
            assertTrue(bigHashMap.containsKey(key), "Map should contain key [" + key + "]");
        }

        for (int key = totalInserts ; key < totalInserts + 1000 ; key++) {
            assertFalse(bigHashMap.containsKey(key), "Map shouldn't contain key [" + key + "]");

        }

        for (int key = 0 ; key < totalInserts / 2 ; key++) {
            var finalKey = key;
            var removed = bigHashMap.remove(finalKey);
            assertAll(
                    () -> assertNotNull(removed, "Removed value of key [" + finalKey + "] shouldn't be null"),
                    () -> assertEquals(finalKey * 10, removed, "Removed value of key [" + finalKey + "] doesn't match expected value")
            );
        }

        assertEquals(totalInserts - (totalInserts / 2), bigHashMap.size(), "Size doesn't match expected values after removals");

        for (int key = 0 ; key < totalInserts ; key++) {
            var containsKey = bigHashMap.containsKey(key);

            if (key < totalInserts / 2) {
                assertFalse(containsKey, "Map shouldn't contain key [" + key + "]");
            }
            else {
                assertTrue(containsKey, "Map should contain key [" + key + "]");
            }
        }

    }

}
