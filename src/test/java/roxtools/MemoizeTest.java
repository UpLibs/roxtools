package roxtools;

import org.junit.jupiter.api.Test;
import roxtools.Memoize.MemKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class MemoizeTest {

    @Test
    public void testBasic() {
        var memoize = new Memoize<Long>();
        memoize.put(new MemKey("1"), 123L);

        var val1 = memoize.get(new MemKey("1"));
        assertEquals(123L, val1.longValue());

        var val2 = memoize.get(new MemKey("2"));
        assertNull(val2);
        memoize.put(new MemKey("2"), 456L);
        val2 = memoize.get(new MemKey("2"));
        assertEquals(456L, val2.longValue());

        var val2b = memoize.remove(new MemKey("2"));
        assertSame(val2, val2b);
        assertEquals(456L, val2.longValue());
        assertFalse(memoize.contains(new MemKey("2")));
    }

    @Test
    public void testTimeout() {
        var memoize = new Memoize<Long>();
        memoize.setMemoryTimeout(1000L);
        memoize.put(new MemKey("1"), 123L);
        var val = memoize.get(new MemKey("1"));
        assertEquals(123L, val.longValue());

        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
        }

        var valA = memoize.get(new MemKey("1"));
        assertEquals(123L, valA.longValue());

        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
        }

        var valB = memoize.get(new MemKey("1"));
        assertNull(valB);
    }

}
