package roxtools;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WeakMapRecyclableTest {

    @Test
    void testBasic() {
        var keys = new String[] {"a", "b", "c"};

        var map = new WeakMapRecyclable<String, Integer>();

        for (String k : keys) {
            map.put(k, map.size() + 1);
        }

        assertEquals(3, map.size());

        assertTrue(map.contains("a"));
        assertTrue(map.contains("b"));
        assertTrue(map.contains("c"));

        assertFalse(map.contains("A"));

        assertEquals(1, map.get("a").intValue());
        assertEquals(2, map.get("b").intValue());
        assertEquals(3, map.get("c").intValue());

        assertNull(map.get("A"));

        var keys2 = map.getKeysArray(new String[map.size()]);

        Arrays.sort(keys2);

        assertArrayEquals(keys, keys2);

        var entry = map.remove("b");

        assertNotNull(entry);

        assertEquals(2, entry.getValue().intValue());

        assertNull(map.get("b"));
        assertFalse(map.contains("b"));

        assertTrue(map.contains("a"));
        assertTrue(map.contains("c"));

        // to hold keys references:
        assertEquals(3, keys.length);
    }

    @Test
    void testLoseRef() {
        var map = new WeakMapRecyclable<String, Integer>();

        final var recycledValues = new Vector<Integer>();

        map.setRecycleHandler(recycledValues::add);

        map.put(new String("a".getBytes()), 1);
        map.put(new String("b".getBytes()), 2);
        map.put(new String("c".getBytes()), 3);

        assertEquals(3, map.size());

        System.gc();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
        }

        System.gc();

        map.expungeStaleEntries();

        assertTrue(map.isEmpty());

        assertFalse(map.contains("a"));
        assertFalse(map.contains("b"));
        assertFalse(map.contains("c"));

        Collections.sort(recycledValues);

        assertEquals(3, recycledValues.size());

        assertEquals(1, recycledValues.get(0).intValue());
        assertEquals(2, recycledValues.get(1).intValue());
        assertEquals(3, recycledValues.get(2).intValue());
    }

}
