package roxtools.benchmarks;

import roxtools.collection.SimpleHashMap;

import java.util.HashMap;
import java.util.Map;

public class SimpleHashMapBenchmark {

    public void showBench(Map<Integer, Integer> map, int totalValues) {
        System.out.println("---------------------------------------------------");
        System.out.println("Bench... " + map.getClass() + " ; " + totalValues);
        long time = bench(map, totalValues);
        System.out.println("Map: " + map.getClass());
        System.out.println("Time: " + time);
        System.out.println("---------------------------------------------------");
    }

    public long bench(Map<Integer, Integer> map, int totalValues) {
        long time = System.currentTimeMillis();

        for (var i = totalValues - 1 ; i >= 0 ; i--) {
            Integer k = i * 10;
            Integer v = i * 100;
            map.put(k, v);
            map.get(k);
        }

        time = System.currentTimeMillis() - time;

        return time;
    }

    public void bench(int totalValues) {
        showBench(new SimpleHashMap<>(), totalValues);
        showBench(new HashMap<>(), totalValues);
    }

}
