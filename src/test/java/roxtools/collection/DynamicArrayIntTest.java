package roxtools.collection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class DynamicArrayIntTest {

    static Stream<Arguments> dynamicArrayIntCreation() {
        return Stream.of(
                arguments(3, 100),
                arguments(3, 1234),
                arguments(111, 1234)
        );
    }

    @MethodSource
    @ParameterizedTest
    void dynamicArrayIntCreation(int blockSize, int totalValues) {
        final var multiplier = 10;

        var dynamicArray = new DynamicArrayInt(blockSize, blockSize);

        for (var i = 0 ; i < totalValues ; i++) {
            dynamicArray.addInt(i * multiplier);
        }

        assertAll(
                () -> assertEquals(totalValues, dynamicArray.size(), "Size doesn't match expected value"),
                () -> assertEquals(blockSize, dynamicArray.getBlockSize(), "Block size doesn't match expected value"),
                () -> assertEquals((totalValues / blockSize) + 1, dynamicArray.getAllocatedBlocks(), "Allocated blocks doesn't match expected value")
        );

        for (var i = 0 ; i < 100 ; i++) {
            var value = dynamicArray.getInt(i);
            assertEquals(i * multiplier, value, "Int at index [" + i + "] doesn't match expected value");
        }

        dynamicArray.resizeBlockSize(blockSize * 2);

        for (var i = 0 ; i < 100 ; i++) {
            var value = dynamicArray.getInt(i);
            assertEquals(i * multiplier, value, "Int at index [" + i + "] doesn't match expected value after resizing");
        }

        dynamicArray.resizeBlockSize(blockSize);

        for (var i = 0 ; i < 100 ; i++) {
            var value = dynamicArray.getInt(i);
            assertEquals(i * multiplier, value, "Int at index [" + i + "] doesn't match expected value after reverting to original block size");
        }

        assertEquals(totalValues, dynamicArray.size(), "Size doesn't match expected value");

        var rmIdx = blockSize / 2;

        var loops = 10;
        for (var i = 0 ; i < loops ; i++) {
            dynamicArray.remove(rmIdx);
            assertEquals(totalValues - (i + 1), dynamicArray.size(), "Size doesn't match expected value after removal loop [" + i + "]");

            var value = dynamicArray.getInt(rmIdx);
            var expected = ((rmIdx + i) + 1) * multiplier;
            assertEquals(expected, value, "Int at index [" + rmIdx + "] doesn't match expected value after removal loop [" + i + "]");
        }

        assertEquals(totalValues - loops, dynamicArray.size(), "Size doesn't match expected value after removing [" + loops + "] values");
    }

}
