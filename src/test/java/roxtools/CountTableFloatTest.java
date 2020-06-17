package roxtools;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CountTableFloatTest {

    @Test
    void multipleOperations() {
        var countTable = new CountTableFloat<String>();

        for (var i = 0 ; i < 10 ; i++) {
            var key = "k:" + i;
            var value = countTable.increment(key);
            assertEquals(1, value, "Value doesn't match single increment");
        }

        for (var i = 0 ; i < 10 ; i += 2) {
            var key = "k:" + i;
            var value = countTable.increment(key);
            assertEquals(2, value, "Value doesn't match double increment");
        }

        for (var i = 0 ; i < 10 ; i++) {
            var key = "k:" + i;
            var expected = i % 2 == 0 ? 2 : 1;
            assertEquals(expected, countTable.get(key), "Value doesn't match increments for key [" + key + "]");
        }

        for (var i = 0 ; i < 10 ; i++) {
            var key = "k:" + i;
            assertTrue(countTable.contains(key), "Table should contain key [" + key + "]");
        }

        for (var i = 10 ; i < 20 ; i++) {
            var key = "k:" + i;
            assertFalse(countTable.contains(key), "Table shouldn't contain key [" + key + "]");
        }

        var tableSize = countTable.size();

        var keysList = countTable.getKeys();
        var keysStringArray = countTable.getKeysArray(new String[tableSize]);
        var keysObjsArray = countTable.getKeysArray();

        assertAll(
                () -> assertEquals(tableSize, keysList.size(), "List of Keys should match the size of the table"),
                () -> assertEquals(tableSize, keysObjsArray.length, "Length of array of keys as Objects should match the size of the table"),
                () -> assertEquals(tableSize, keysStringArray.length, "Length of array of keys as String should match the size of the table")
        );

        for (var i = 0 ; i < keysStringArray.length ; i++) {
            var idx = i;
            var keyFromList = keysList.get(idx);
            var keyFromObjs = (String) keysObjsArray[idx];
            var keyFromStrings = keysStringArray[idx];

            assertAll(
                    () -> assertEquals(keyFromObjs, keyFromList, "Key from Objects array doesn't match key from list at index [" + idx + "]"),
                    () -> assertEquals(keyFromStrings, keyFromList, "Key from String array doesn't match key from list at index [" + idx + "]")
            );
        }

        countTable.clear();

        assertEquals(0, countTable.size(), "Table should've been cleared");

        for (var i = 0 ; i < 20 ; i++) {
            var key = "k:" + i;
            assertFalse(countTable.contains(key), "Table shouldn't contain key [" + key + "]");
        }

    }

    @Test
    void setMinimum() {
        var countTable = new CountTableFloat<String>();

        for (var i = 0 ; i < 10 ; i++) {
            var key = "k:" + i;
            var value = countTable.sum(key, 10);
            assertEquals(10, value, "Value doesn't match sum result");
        }

        for (var i = 0 ; i < 10 ; i += 2) {
            var key = "k:" + i;
            assertTrue(countTable.setMinimum(key, 1), "minimum should've been set for key [" + key + "]");
        }

        for (var i = 1 ; i < 10 ; i += 2) {
            var key = "k:" + i;
            assertFalse(countTable.setMinimum(key, 20), "minimum shouldn't have been set for key [" + key + "]");
        }

        for (var i = 0 ; i < 10 ; i++) {
            var key = "k:" + i;
            var expected = i % 2 == 0 ? 1 : 10;
            assertEquals(expected, countTable.get(key), "Value doesn't match expected minimum for key [" + key + "]");
        }
    }


    @Test
    void setMaximum() {
        var countTable = new CountTableFloat<String>();

        for (var i = 0 ; i < 10 ; i++) {
            var key = "k:" + i;
            var value = countTable.sum(key, 10);
            assertEquals(10, value, "Value doesn't match sum result");
        }

        for (var i = 0 ; i < 10 ; i += 2) {
            var key = "k:" + i;
            assertTrue(countTable.setMaximum(key, 20), "Maximum should've been set for key [" + key + "]");
        }

        for (var i = 1 ; i < 10 ; i += 2) {
            var key = "k:" + i;
            assertFalse(countTable.setMaximum(key, 1), "Maximum shouldn't have been set for key [" + key + "]");
        }

        for (var i = 0 ; i < 10 ; i++) {
            var key = "k:" + i;
            var expected = i % 2 == 0 ? 20 : 10;
            assertEquals(expected, countTable.get(key), "Maximum doesn't match expected value for key [" + key + "]");
        }
    }


    @Test
    void testCountTableFloatAgainstHashMap() {
        var countTable = new CountTableFloat<Integer>();
        var countTable2 = new HashMap<Integer, Float>();

        var random = new Random(123);

        for (var i = 0 ; i < 1000000 ; i++) {
            var key = random.nextInt(10000);
            var amount = random.nextInt(100) + random.nextFloat();

            countTable.sum(key, amount);

            var count = countTable2.get(key);
            count = count != null ? count + amount : amount;
            countTable2.put(key, count);
        }

        assertEquals(countTable.size(), countTable2.size(), "Both tables should've same size");

        var keys = countTable.getKeysArray(new Integer[countTable.size()]);
        var keys2 = ArrayUtils.toArray(countTable2.keySet(), new Integer[countTable2.size()]);

        Arrays.sort(keys);
        Arrays.sort(keys2);

        assertArrayEquals(keys, keys2, "Both arrays of keys should be equal after sorting");

        for (var key : keys) {
            var val = countTable.get(key);
            var val2 = countTable2.get(key);

            assertEquals(val, val2, "Both values should be equal for the key [" + key + "]");
        }
    }

}
