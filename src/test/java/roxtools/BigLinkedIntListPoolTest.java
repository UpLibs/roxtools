package roxtools;

import org.junit.jupiter.api.Test;
import roxtools.BigLinkedIntListPool.BigLinkedIntList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BigLinkedIntListPoolTest {

    static public final boolean ALLOW_BIG_TESTS;

    static {

        var runtime = Runtime.getRuntime();

        var maxMemory = runtime.maxMemory();

        final var bigMemory = 1024L * 1024 * 1024 * 2;

        boolean allow = maxMemory >= bigMemory;

        allow = false;

        ALLOW_BIG_TESTS = allow;

        System.out.println("** ALLOW_BIG_TESTS: " + ALLOW_BIG_TESTS + " > " + (maxMemory / 1024) + "KB / " + (bigMemory / 1024) + "KB");

    }

    static private boolean checkAllowBigTests() {
        if (!ALLOW_BIG_TESTS) {
            Throwable throwable = new Throwable();

            StackTraceElement[] stackTrace = throwable.getStackTrace();
            String methodName = stackTrace[1].getMethodName();

            System.out.println("** Skipping[" + methodName + "]> ALLOW_BIG_TESTS: " + ALLOW_BIG_TESTS);
        }
        return ALLOW_BIG_TESTS;
    }

    @Test
    public void testBasic1() {
        testBasicImplem(100, 1000);
    }

    @Test
    public void testBasic2() {
        if (checkAllowBigTests()) {
            testBasicImplem(100, 10000);
        }
    }

    @Test
    public void testBasic3() {
        if (checkAllowBigTests()) {
            testBasicImplem(1000, 100);
        }
    }

    @Test
    public void testBasic4() {
        testBasicImplem(10000, 100);
    }

    private void testBasicImplem(int totalLists, int totalValues) {
        BigLinkedIntListPool pool = new BigLinkedIntListPool();

        assertTrue(pool.size() == 0);

        BigLinkedIntList[] linkedLists = new BigLinkedIntList[totalLists];

        int valIdRange = totalValues * 10;

        for (var i = 0 ; i < linkedLists.length ; i++) {
            BigLinkedIntList bigLinkedList = linkedLists[i] = pool.createLinkedList();

            for (int j = 0 ; j < totalValues ; j++) {
                Integer val = (i * valIdRange) + j;
                bigLinkedList.add(val);
            }

            assertTrue(bigLinkedList.size() == totalValues);
        }

        for (var i = 0 ; i < linkedLists.length ; i++) {
            BigLinkedIntList bigLinkedList = linkedLists[i];

            for (int j = 0 ; j < totalValues ; j++) {
                Integer val = (i * valIdRange) + j;
                Integer val2 = bigLinkedList.get(j);

                assertTrue(val.equals(val2));
            }

            int j = 0;
            for (Integer val : bigLinkedList) {
                Integer v = (i * valIdRange) + j;

                assertTrue(val.equals(v));

                j++;
            }

            assertTrue(bigLinkedList.size() == totalValues);
        }

        for (var i = 0 ; i < linkedLists.length ; i++) {
            BigLinkedIntList bigLinkedList = linkedLists[i];
            bigLinkedList.removeLast();

            bigLinkedList.add(-1);
        }

        for (var i = 0 ; i < linkedLists.length ; i++) {
            BigLinkedIntList bigLinkedList = linkedLists[i];

            assertTrue(bigLinkedList.size() == totalValues);

            Integer valLast = bigLinkedList.get(bigLinkedList.size() - 1);
            Integer valLast2 = bigLinkedList.getLast();
            assertTrue(valLast == -1);
            assertTrue(valLast.equals(valLast2));
        }

        System.out.println(pool);

    }

    @Test
    public void testRemove() {
        BigLinkedIntListPool pool = new BigLinkedIntListPool();

        ArrayList<BigLinkedIntList> lists = new ArrayList<BigLinkedIntList>();

        int totalSize = 0;

        for (var i = 0 ; i < 100 ; i++) {

            BigLinkedIntList list = pool.createLinkedList();

            lists.add(list);

            for (int j = 0 ; j < 1000 ; j++) {
                list.add(j);
            }

            totalSize += list.size();
        }

        assertTrue(pool.size() == totalSize);

        int totalSize2 = 0;

        for (BigLinkedIntList list : lists) {

            Integer[] array = list.toArray();
            ArrayList<Integer> checkList = new ArrayList<Integer>(array.length);
            Collections.addAll(checkList, array);

            assertTrue(list.size() == checkList.size());

            int preRemoveSize = list.size();

            int removeCount = 0;
            for (int j = 0 ; j < 1000 ; j += 2) {
                int rmIdx = j - removeCount;
                Integer val = list.remove(rmIdx);
                Integer val2 = checkList.remove(rmIdx);

                assertNotNull(val);
                assertNotNull(val2);

                assertTrue(val.equals(val2));

                assertTrue(val.equals(j));

                removeCount++;
            }

            assertTrue(list.size() == checkList.size());

            int sz = list.size();

            assertTrue(sz == preRemoveSize / 2);


            for (var i = 0 ; i < sz ; i++) {
                Integer v = list.get(i);
                Integer v2 = checkList.get(i);

                assertNotNull(v);
                assertNotNull(v2);

                assertTrue(v.equals(v2));
            }

            totalSize2 += list.size();

        }

        assertTrue(pool.size() == totalSize2);

        for (BigLinkedIntList list : lists) {

            Integer[] array = list.toArray();

            assertTrue(array.length == list.size());

            Integer prevV = null;
            for (Integer v : list) {

                if (prevV != null) {
                    assertTrue(v == prevV + 2);
                }

                prevV = v;
            }

        }

    }


    @Test
    public void testRandomOperations() {
        testRandomOperationsImplem(100, 1000, 10);
    }

    private void testRandomOperationsImplem(int totalLists, int operationsPerList, int repetitions) {

        Random rand = new Random(1232746512);

        BigLinkedIntListPool pool = new BigLinkedIntListPool();

        ArrayList<BigLinkedIntList> lists = new ArrayList<BigLinkedIntList>();

        for (var i = 0 ; i < totalLists ; i++) {
            lists.add(pool.createLinkedList());
        }

        for (var i = 0 ; i < repetitions ; i++) {

            for (BigLinkedIntList list : lists) {
                for (int j = 0 ; j < operationsPerList ; j++) {
                    int ordinal = rand.nextInt(ListOP.values().length);
                    ListOP op = ListOP.values()[ordinal];

                    doOperation(rand, list, op);
                }
            }

        }

    }

    static public enum ListOP {
        ADD,
        ADD_MANY,
        REMOVE,
        REMOVE_MANY,
        CLEAR,
        GET,
        GET_MANY,
        SET,
        SET_MANY,
        SET_ALL,
        CHECK_ARRAY
    }

    private boolean doOperation(Random rand, BigLinkedIntList list, ListOP op) {

        switch (op) {
            case ADD:
                return op_add(rand, list);
            case ADD_MANY:
                return op_add_many(rand, list);
            case REMOVE:
                return op_remove(rand, list);
            case REMOVE_MANY:
                return op_remove_many(rand, list);
            case CLEAR:
                return op_clear(rand, list);
            case GET:
                return op_get(rand, list);
            case GET_MANY:
                return op_get_many(rand, list);
            case SET:
                return op_set(rand, list);
            case SET_MANY:
                return op_set_many(rand, list);
            case SET_ALL:
                return op_set_all(rand, list);
            case CHECK_ARRAY:
                return op_check_array(rand, list);
            default:
                throw new IllegalStateException("Can't handle op: " + op);
        }

    }

    private boolean op_set_all(Random rand, BigLinkedIntList list) {

        int size = rand.nextInt(100);

        Integer[] set = new Integer[size];

        for (var i = 0 ; i < set.length ; i++) {
            set[i] = rand.nextInt(10000);
        }

        list.setAll(set);

        assertTrue(list.size() == set.length);

        Integer[] check = list.toArray();

        for (var i = 0 ; i < check.length ; i++) {
            Integer val = check[i];
            Integer val2 = set[i];
            assertTrue(val.equals(val2));
        }

        return true;
    }

    private boolean op_check_array(Random rand, BigLinkedIntList list) {
        if (list.isEmpty()) return false;

        Integer[] array = list.toArray();

        for (var i = 0 ; i < array.length ; i++) {
            Integer v = array[i];
            Integer v2 = list.get(i);

            assertTrue(v.equals(v2));
        }

        return true;
    }

    private boolean op_set_many(Random rand, BigLinkedIntList list) {
        if (list.isEmpty()) return false;

        int total = rand.nextInt(100);

        for (var i = 0 ; i < total ; i++) {
            boolean ok = op_set(rand, list);
            if (!ok) break;
        }

        return true;
    }

    private boolean op_set(Random rand, BigLinkedIntList list) {
        if (list.isEmpty()) return false;

        int idx = rand.nextInt(list.size());
        int val = rand.nextInt(10000);

        Integer prev = list.set(idx, val);

        assertTrue(prev != null);

        assertTrue(list.get(idx) == val);

        return true;
    }

    private boolean op_get_many(Random rand, BigLinkedIntList list) {
        if (list.isEmpty()) return false;

        int total = rand.nextInt(100);

        for (var i = 0 ; i < total ; i++) {
            int idx = rand.nextInt(list.size());
            Integer v = list.get(idx);
            assertTrue(v != null);
        }

        return true;
    }

    private boolean op_get(Random rand, BigLinkedIntList list) {
        if (list.isEmpty()) return false;

        int idx = rand.nextInt(list.size());
        Integer v = list.get(idx);
        assertTrue(v != null);

        return true;
    }

    private boolean op_clear(Random rand, BigLinkedIntList list) {
        list.clear();

        assertTrue(list.isEmpty());

        return true;
    }

    private boolean op_remove_many(Random rand, BigLinkedIntList list) {
        if (list.isEmpty()) return false;

        int total = rand.nextInt(100);

        for (var i = 0 ; i < total ; i++) {
            boolean ok = op_remove(rand, list);
            if (!ok) break;
        }

        return true;

    }

    private boolean op_remove(Random rand, BigLinkedIntList list) {

        if (list.isEmpty()) return false;

        if (rand.nextBoolean()) {
            int idx = rand.nextInt(list.size());
            Integer prev = list.remove(idx);
            assertTrue(prev != null);
        }
        else {
            Integer prev;
            if (rand.nextBoolean()) {
                prev = list.removeFirst();
            }
            else {
                prev = list.removeLast();
            }

            assertTrue(prev != null);
        }

        return true;
    }

    private boolean op_add_many(Random rand, BigLinkedIntList list) {

        int total = rand.nextInt(100);

        int size = list.size();

        for (var i = 0 ; i < total ; i++) {
            list.add(rand.nextInt(10000));
        }

        assertTrue(list.size() == size + total);

        return true;
    }

    private boolean op_add(Random rand, BigLinkedIntList list) {
        int size = list.size();
        list.add(rand.nextInt(10000));
        assertTrue(list.size() == size + 1);

        return true;
    }

    ///////////////////////////////////

    @Test
    public void testClearReferencedList() {

        BigLinkedIntListPool pool = new BigLinkedIntListPool();

        assertTrue(pool.size() == 0);

        int valuesPerList = 100;

        BigLinkedIntList[] linkedLists = new BigLinkedIntList[100];

        int addedValues = 0;

        for (var i = 0 ; i < linkedLists.length ; i++) {
            BigLinkedIntList bigLinkedList = linkedLists[i] = pool.createLinkedList();

            for (int j = 0 ; j < valuesPerList ; j++) {
                Integer val = i * j;
                bigLinkedList.add(val);
                addedValues++;
            }

            assertTrue(bigLinkedList.size() == valuesPerList);
        }

        assertTrue(pool.getReferencedListsSize() == linkedLists.length);

        assertTrue(pool.size() == addedValues);

        int unreferencedListsCount = 0;

        for (var i = 0 ; i < linkedLists.length ; i += 2) {
            linkedLists[i] = null;
            unreferencedListsCount++;
        }

        assertTrue(unreferencedListsCount > 0);

        int clearCount = 0;

        for (var i = 0 ; i < 5 ; i++) {

            System.gc();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.gc();

            clearCount += pool.clearUnreferencedLists();
        }

        assertTrue(clearCount == unreferencedListsCount);

        assertTrue(pool.getReferencedListsSize() == linkedLists.length - unreferencedListsCount);

        assertTrue(pool.size() == (addedValues - (unreferencedListsCount * valuesPerList)));

    }


    @Test
    public void testClearUnreferencedList() {

        BigLinkedIntListPool pool = new BigLinkedIntListPool();

        assertTrue(pool.size() == 0);

        int valuesPerList = 100;

        BigLinkedIntList[] linkedLists = new BigLinkedIntList[100];

        int addedValues = 0;

        for (var i = 0 ; i < linkedLists.length ; i++) {
            BigLinkedIntList bigLinkedList = linkedLists[i] = pool.createLinkedListUnreferenced();

            for (int j = 0 ; j < valuesPerList ; j++) {
                Integer val = i * j;
                bigLinkedList.add(val);
                addedValues++;
            }

            assertTrue(bigLinkedList.size() == valuesPerList);
        }

        assertEquals(pool.getReferencedListsSize(), 0);

        assertTrue(pool.size() == addedValues);

        int unreferencedListsCount = 0;

        for (var i = 0 ; i < linkedLists.length ; i += 2) {
            linkedLists[i] = null;
            unreferencedListsCount++;
        }

        assertTrue(unreferencedListsCount > 0);

        int clearCount = 0;

        for (var i = 0 ; i < 5 ; i++) {

            System.gc();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.gc();

            clearCount += pool.clearUnreferencedLists();
        }

        assertTrue(clearCount == 0);

        assertTrue(pool.getReferencedListsSize() == 0);

        assertTrue(pool.size() == addedValues);

    }


}
