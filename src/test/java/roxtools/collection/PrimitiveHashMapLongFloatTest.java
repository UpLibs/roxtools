package roxtools.collection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrimitiveHashMapLongFloatTest {

    public static final int KEY_MULTIPLIER = 10;
    public static final int VALUE_MULTIPLIER = 100;

    @ParameterizedTest
    @ValueSource(ints = {10, 100, 1000})
    void primitiveHashMapLongFloatCreation(int totalValues) {
        var map = new PrimitiveHashMapLongFloat();

        for (var i = 0 ; i < totalValues ; i++) {
            var key = calculateKey(i);
            var value = calculateValue(i);

            assertNull(map.put(key, value), "No value should previously exist for key [" + key + "]");
            assertTrue(map.containsKey(key), "Map must contain value for key [" + key + "] after its insertion");
            assertEquals(value, map.get(key), "Retrieved value doesnt match expected value for key [" + key + "]");
        }

        assertEquals(totalValues, map.size(), "Size doesn't match expected value");

        var iterateCount = 0;

        //noinspection KeySetIterationMayUseEntrySet
        for (var key : map.keySet()) {
            var expected = calculateValueFromKey(key);
            var value = map.get(key);

            assertEquals(expected, value, "Retrieved value doesnt match expected value for key [" + key + "]");
            iterateCount++;
        }

        assertEquals(totalValues, iterateCount, "Number of iterations doesn't match the number of total values");

        iterateCount = 0;
        for (var entry : map.entrySet()) {
            var key = entry.getKey();
            var expected = calculateValueFromKey(key);

            assertAll(
                    () -> assertEquals(expected, entry.getValue(), "Entry value doesnt match expected value for key [" + key + "]"),
                    () -> assertEquals(expected, map.get(key), "Retrieved value doesnt match expected value for key [" + key + "]")
            );
            iterateCount++;
        }

        assertEquals(totalValues, iterateCount, "Number of iterations doesn't match the number of total values");

        for (var i = 0 ; i < totalValues / 2 ; i++) {
            var key = calculateKey(i);
            var expected = calculateValue(i);
            var removed = map.remove(key);

            assertAll(
                    () -> assertEquals(expected, removed, "Removed value doesnt match expected value for key [" + key + "]"),
                    () -> assertFalse(map.containsKey(key), "Map shouldn't contain key  [" + key + "] after its removal"),
                    () -> assertNull(map.get(key), "Value of key  [" + key + "] should be null after its removal")
            );
        }

        assertEquals(totalValues / 2, map.size(), "Size doesn't match expected values after removals");

        for (var i = 0 ; i < totalValues / 2 ; i++) {
            var key = calculateKey(i);
            var value = calculateValue(i);

            assertNull(map.put(key, value), "No value should previously exist for key [" + key + "]");
            assertTrue(map.containsKey(key), "Map must contain value for key [" + key + "] after its insertion");
            assertEquals(value, map.get(key), "Retrieved value doesnt match expected value for key [" + key + "]");
        }

        assertEquals(totalValues, map.size(), "Size doesn't match expected values after insertions");

        for (var i = 0 ; i < totalValues ; i++) {
            var key = calculateKey(i);
            var expected = calculateValue(i);
            var removed = map.remove(key);

            assertAll(
                    () -> assertEquals(expected, removed, "Removed value doesnt match expected value for key [" + key + "]"),
                    () -> assertFalse(map.containsKey(key), "Map shouldn't contain key  [" + key + "] after its removal"),
                    () -> assertNull(map.get(key), "Value of key  [" + key + "] should be null after its removal")
            );
        }

        assertTrue(map.isEmpty(), "Map should be completely empty");

    }

    private Long calculateKey(int i) {
        return (Long) (long) (i * KEY_MULTIPLIER);
    }

    private Float calculateValue(int i) {
        return (float) (i * VALUE_MULTIPLIER);
    }

    private Float calculateValueFromKey(Long key) {
        var i = (int) (key / KEY_MULTIPLIER);
        return (Float) (float) (i * VALUE_MULTIPLIER);
    }

}
