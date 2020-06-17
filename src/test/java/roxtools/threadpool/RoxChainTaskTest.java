package roxtools.threadpool;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class RoxChainTaskTest {

    static Stream<Arguments> testRoxChainTaskPool() {
        return Stream.of(
                arguments(List.of(1, 2, 3, 4, 5), List.of(1005.0001d, 2005.0001, 3005.0001, 4005.0001, 5005.0001)),
                arguments(List.of(6, 7, 8, 9, 10), List.of(6005.0001d, 7005.0001, 8005.0001, 9005.0001, 10005.0001))
        );
    }

    @MethodSource
    @ParameterizedTest
    void testRoxChainTaskPool(List<Integer> inputs, List<Double> outputs) {
        var chainTaskPool = getPoolWithChainedTasks();

        chainTaskPool.addInitialInputs(inputs);
        chainTaskPool.start();

        assertTrue(chainTaskPool.isStarted(), "ChainTaskPool should have been started");

        var finalOutput = chainTaskPool.getChainFinalOutput();

        assertAll(
                () -> assertTrue(chainTaskPool.isChainFinished(), "Chain should have finished"),
                () -> assertNotNull(finalOutput, "Output of chained tasks shouldn't be null"),
                () -> assertEquals(5, finalOutput.size(), "Number of outputs doesn't match expected value"),
                () -> assertEquals(outputs, finalOutput, "Outputs don't match expected values for inputs")
        );
    }

    private RoxChainTaskPool<Integer, Double> getPoolWithChainedTasks() {
        var chainTaskPool = new RoxChainTaskPool<Integer, Double>();

        chainTaskPool.add(new RoxChainTask<Integer, Integer>(10) {
            @Override
            public Integer task(Integer input) {
                return input * 10;
            }
        });

        chainTaskPool.add(new RoxChainTask<Integer, Float>(10) {
            @Override
            public Float task(Integer input) {
                int res = input * 10;
                return res + 0.5f;
            }
        });

        chainTaskPool.add(new RoxChainTask<Float, Double>(10) {
            @Override
            public Double task(Float input) {
                float res = input * 10;
                return res + 0.0001d;
            }
        });
        return chainTaskPool;
    }


}
