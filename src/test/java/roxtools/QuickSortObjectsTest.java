package roxtools;

import org.junit.jupiter.api.Test;
import roxtools.QuickSortObjects.ObjectCompareValueInt;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QuickSortObjectsTest {

    static private Object[] createIntArray(int size, long seed) {
        var random = new Random(seed);

        size += (random.nextInt(size / 10));

        var array = new int[size];
        var objs = new Object[size];

        for (var i = 0 ; i < size ; i++) {
            var value = random.nextInt(size * 10);
            array[i] = value;
            objs[i] = "s:" + value;
        }

        return new Object[] {array, objs};
    }

    static private Object[] createFloatArray(int size, long seed) {
        var random = new Random(seed);

        size += (random.nextInt(size / 10));

        var array = new float[size];
        var objs = new Object[size];

        for (var i = 0 ; i < size ; i++) {
            var value = random.nextInt(size * 10) / 2f;
            array[i] = value;
            objs[i] = "s:" + value;
        }

        return new Object[] {array, objs};
    }

    static private Object[] createFloatIntArray(int size, long seed) {
        var random = new Random(seed);

        size += (random.nextInt(size / 10));

        var array = new float[size];
        var objs = new int[size];

        for (var i = 0 ; i < size ; i++) {
            var value = random.nextInt(size * 10) / 2f;
            array[i] = value;
            objs[i] = (int) value;
        }

        return new Object[] {array, objs};
    }

    static private Object[] createFloatFloatArray(int size, long seed) {
        var random = new Random(seed);

        size += (random.nextInt(size / 10));

        var array = new float[size];
        var objs = new float[size];

        for (var i = 0 ; i < size ; i++) {
            var value = random.nextInt(size * 10) / 2f;
            array[i] = value;
            objs[i] = value * 2;
        }

        return new Object[] {array, objs};
    }

    static private Object[] createFloatIntFloat2DArray(int size, long seed) {
        var random = new Random(seed);

        size += (random.nextInt(size / 10));

        var array = new float[size];
        var objs = new int[size];
        var objs2 = new float[size][];

        for (var i = 0 ; i < size ; i++) {
            var value = random.nextInt(size * 10) / 2f;
            array[i] = value;
            objs[i] = (int) value;
            objs2[i] = new float[] {value, value * 2};
        }

        return new Object[] {array, objs, objs2};
    }

    static private Object[] createDoubleArray(int size, long seed) {
        var random = new Random(seed);

        size += (random.nextInt(size / 10));

        var array = new double[size];
        var objs = new Object[size];

        for (var i = 0 ; i < size ; i++) {
            var value = random.nextInt(size * 10) / 2d;
            array[i] = value;
            objs[i] = "s:" + value;
        }

        return new Object[] {array, objs};
    }

    static private Number[] createNumberArray(int size, long seed) {
        var random = new Random(seed);

        size += (random.nextInt(size / 10));

        var array = new Number[size];

        for (var i = 0 ; i < size ; i++) {
            var value = random.nextInt(size * 10) / 2d;

            var type = random.nextInt(3);

            if (type == 0) {
                array[i] = (int) value;
            }
            else if (type == 1) {
                array[i] = (float) value;
            }
            else if (type == 2) {
                array[i] = value;
            }

        }

        return array;
    }

    static private String[] createStringArray(int size, long seed) {
        var random = new Random(seed);

        var objs = new String[size];

        for (var i = 0 ; i < size ; i++) {
            var value = random.nextInt(size * 10);
            objs[i] = String.valueOf(value);
        }

        return objs;
    }

    static private void checkSortInt(int[] a, Object[] o) {
        for (var i = 1 ; i < a.length ; i++) {
            var value = a[i];
            int previous = a[i - 1];

            if (previous > value) throw new IllegalStateException("Array not sorted");

            var vO = o[i];

            if (!("s:" + value).equals(vO)) throw new IllegalStateException("Object not matching index");
        }
    }

    static private void checkSortFloat(float[] a, Object[] o) {
        for (var i = 1 ; i < a.length ; i++) {
            var value = a[i];
            var previous = a[i - 1];

            if (previous > value) throw new IllegalStateException("Array not sorted");

            var vO = o[i];

            if (!("s:" + value).equals(vO)) throw new IllegalStateException("Object not matching index");
        }
    }

    static private void checkSortNumberAsInteger(Number[] a) {
        for (var i = 1 ; i < a.length ; i++) {
            var value = a[i];
            var previous = a[i - 1];

            if (previous.intValue() > value.intValue()) throw new IllegalStateException("Array not sorted");
        }
    }

    static private void checkSortNumberAsFloat(Number[] a) {
        for (var i = 1 ; i < a.length ; i++) {
            var value = a[i];
            var previous = a[i - 1];

            if (previous.floatValue() > value.floatValue()) throw new IllegalStateException("Array not sorted");
        }
    }

    static private void checkSortNumberAsDouble(Number[] a) {
        for (var i = 1 ; i < a.length ; i++) {
            var value = a[i];
            var previous = a[i - 1];

            if (previous.doubleValue() > value.doubleValue()) throw new IllegalStateException("Array not sorted");
        }
    }

    static private void checkSortFloatInt(float[] a, int[] o) {
        for (var i = 1 ; i < a.length ; i++) {
            var value = a[i];
            var previous = a[i - 1];

            if (previous > value) throw new IllegalStateException("Array not sorted");

            var vO = o[i];

            if (((int) value) != vO) throw new IllegalStateException("Value not matching index");
        }
    }

    static private void checkSortFloatFloat(float[] a, float[] o) {
        for (var i = 1 ; i < a.length ; i++) {
            var value = a[i];
            var previous = a[i - 1];

            if (previous > value) throw new IllegalStateException("Array not sorted");

            var vO = o[i];

            if ((value * 2) != vO) throw new IllegalStateException("Value not matching index");
        }
    }

    static private void checkSortFloatIntFloat2D(float[] a, int[] o, float[][] o2) {
        for (var i = 1 ; i < a.length ; i++) {
            var value = a[i];
            var previous = a[i - 1];

            if (previous > value) throw new IllegalStateException("Array not sorted");

            var vO = o[i];

            if (((int) value) != vO) throw new IllegalStateException("Value not matching index");

            var vO2 = o2[i];

            if (value != vO2[0]) throw new IllegalStateException("Value not matching index");
            if (value * 2 != vO2[1]) throw new IllegalStateException("Value not matching index");
        }
    }

    static private void checkSortDouble(double[] a, Object[] o) {
        for (var i = 1 ; i < a.length ; i++) {
            var value = a[i];
            var previous = a[i - 1];

            if (previous > value) throw new IllegalStateException("Array not sorted");

            var vO = o[i];

            if (!("s:" + value).equals(vO)) throw new IllegalStateException("Object not matching index");
        }

    }

    static private void checkSortString(String[] o) {
        for (var i = 1 ; i < o.length ; i++) {
            var value = Integer.parseInt(o[i]);
            var previous = Integer.parseInt(o[i - 1]);

            if (previous > value) throw new IllegalStateException("Array not sorted");
        }
    }

    /////////////////////////////////////////////////////////////////////

    @Test
    public void testSortInt() {
        var seed = 1859301237985L ^ -458203475L ^ 45631581085001L;

        for (var loop = 0 ; loop < 10 ; loop++) {
            var ret = createIntArray(10000, seed);

            var a = (int[]) ret[0];
            var o = (Object[]) ret[1];

            QuickSortObjects.sort(a, o);

            checkSortInt(a, o);

            seed = seed ^ (seed * 31 + Arrays.hashCode(a));
        }
    }

    @Test
    public void testSortFloat() {
        var seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 2;

        for (var loop = 0 ; loop < 10 ; loop++) {
            var ret = createFloatArray(10000, seed);

            var a = (float[]) ret[0];
            var o = (Object[]) ret[1];

            QuickSortObjects.sort(a, o);

            checkSortFloat(a, o);

            seed = seed ^ (seed * 31 + Arrays.hashCode(a));
        }
    }

    @Test
    public void testSortFloatAndInt() {
        var seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 3;

        for (var loop = 0 ; loop < 10 ; loop++) {
            var ret = createFloatIntArray(10000, seed);

            var a = (float[]) ret[0];
            var o = (int[]) ret[1];

            QuickSortObjects.sort(a, o);

            checkSortFloatInt(a, o);

            seed = seed ^ (seed * 31 + Arrays.hashCode(a));
        }
    }


    @Test
    public void testSortFloatAndFloat() {
        var seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 5;

        for (var loop = 0 ; loop < 10 ; loop++) {
            var ret = createFloatFloatArray(10000, seed);

            var a = (float[]) ret[0];
            var o = (float[]) ret[1];

            QuickSortObjects.sort(a, o);

            checkSortFloatFloat(a, o);

            seed = seed ^ (seed * 31 + Arrays.hashCode(a));
        }
    }

    @Test
    public void testSortFloatAndIntAndFloat2D() {
        var seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 7;

        for (var loop = 0 ; loop < 10 ; loop++) {
            var ret = createFloatIntFloat2DArray(10000, seed);

            var a = (float[]) ret[0];
            var o = (int[]) ret[1];
            var o2 = (float[][]) ret[2];

            QuickSortObjects.sort(a, o, o2);

            checkSortFloatIntFloat2D(a, o, o2);

            seed = seed ^ (seed * 31 + Arrays.hashCode(a));
        }
    }


    @Test
    public void testSortDouble() {
        var seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 11;

        for (var loop = 0 ; loop < 10 ; loop++) {
            var ret = createDoubleArray(10000, seed);

            var a = (double[]) ret[0];
            var o = (Object[]) ret[1];

            QuickSortObjects.sort(a, o);

            checkSortDouble(a, o);

            seed = seed ^ (seed * 31 + Arrays.hashCode(a));
        }
    }


    @Test
    public void testSortNumberAsInteger() {
        var seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 2557;

        for (var loop = 0 ; loop < 10 ; loop++) {
            var a = createNumberArray(10000, seed);

            QuickSortObjects.sortNumbersAsInt(a);

            checkSortNumberAsInteger(a);

            seed = seed ^ (seed * 31 + Arrays.hashCode(a));
        }
    }

    @Test
    public void testSortNumberAsFloat() {
        var seed = 1859301237985L ^ -458203475L ^ 45631581085001L * (2557 + 1);

        for (var loop = 0 ; loop < 10 ; loop++) {
            var a = createNumberArray(10000, seed);

            QuickSortObjects.sortNumbersAsFloat(a);

            checkSortNumberAsFloat(a);

            seed = seed ^ (seed * 31 + Arrays.hashCode(a));
        }
    }


    @Test
    public void testSortNumberAsDouble() {
        var seed = 1859301237985L ^ -458203475L ^ 45631581085001L * (2557 + 3);

        for (var loop = 0 ; loop < 10 ; loop++) {
            var a = createNumberArray(10000, seed);

            QuickSortObjects.sortNumbersAsDouble(a);

            checkSortNumberAsDouble(a);

            seed = seed ^ (seed * 31 + Arrays.hashCode(a));
        }
    }

    @Test
    public void testSortOffset() {
        var offset = 1000;

        var seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 13;

        for (var loop = 0 ; loop < 10 ; loop++) {
            var ret = createIntArray(10000, seed);

            var a = (int[]) ret[0];
            var o = (Object[]) ret[1];

            for (var i = 0 ; i < offset ; i++) {
                var v = (-offset) + i;
                a[i] = v;
                o[i] = "s:" + v;
            }

            QuickSortObjects.sort(a, o, offset, a.length - 1);

            checkSortInt(a, o);

            seed = seed ^ (seed * 31 + Arrays.hashCode(a));
        }

    }

    @Test
    public void testSortObjects() {
        var seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 17;

        for (var loop = 0 ; loop < 10 ; loop++) {
            var o = createStringArray(10000, seed);

            QuickSortObjects.sort(o, (ObjectCompareValueInt<String>) Integer::parseInt);

            checkSortString(o);

            seed = seed ^ (seed * 31 + Arrays.hashCode(o));
        }
    }

    @Test
    public void testSortObjectsOffset() {
        var offset = 1000;

        var seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 19;

        for (var loop = 0 ; loop < 10 ; loop++) {
            var o = createStringArray(10000, seed);

            for (var i = 0 ; i < offset ; i++) {
                var value = (-offset) + i;
                o[i] = String.valueOf(value);
            }

            QuickSortObjects.sort(o, (ObjectCompareValueInt<String>) Integer::parseInt, offset, o.length - 1);

            checkSortString(o);

            seed = seed ^ (seed * 31 + Arrays.hashCode(o));
        }
    }

    @Test
    public void testBigSort() {
        var seed = 1859301237985L ^ -458203475L ^ 45631581085001L * 1759;

        var runtime = Runtime.getRuntime();
        var availableMemory = runtime.maxMemory() - (runtime.totalMemory() - runtime.freeMemory());

        var availableArraySize = (int) ((availableMemory / (4 + 8)) * 0.90);

        if (availableArraySize < 10000) availableArraySize = 10000;
        else if (availableArraySize > 10000000) availableArraySize = 10000000;

        var a = new int[availableArraySize];

        var random = new Random(seed);

        Arrays.setAll(a, i -> random.nextInt(a.length));

        var objsStrs = new String[1000];

        Arrays.setAll(objsStrs, String::valueOf);

        var o = new Object[a.length];

        Arrays.setAll(o, i -> objsStrs[i % objsStrs.length]);

        assertFalse(ArrayUtils.isSorted(a));
        QuickSortObjects.sort(a, o);
        assertTrue(ArrayUtils.isSorted(a));
    }

}
