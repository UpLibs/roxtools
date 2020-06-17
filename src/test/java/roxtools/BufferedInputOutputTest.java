package roxtools;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roxtools.io.BufferedInputOutput;
import roxtools.io.ByteArrayInputOutput;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class BufferedInputOutputTest {

    static Stream<Arguments> testByteArrayInputOutput() {
        return Stream.of(
                arguments(1, 1000, false), arguments(1, 1000, true),
                arguments(2, 1000, false), arguments(2, 1000, true),
                arguments(3, 1000, false), arguments(3, 1000, true),
                arguments(4, 1000, false), arguments(4, 1000, true),
                arguments(9, 1000, false), arguments(9, 1000, true),
                arguments(10, 1000, false), arguments(10, 1000, true),
                arguments(11, 1000, false), arguments(11, 1000, true)
        );
    }

    @MethodSource
    @ParameterizedTest
    void testByteArrayInputOutput(int blockSize, int writeSize, boolean autoFlush) throws IOException {
        var byteArrayInputOutput = new ByteArrayInputOutput(writeSize);
        var bufferedInputOutput = new BufferedInputOutput(blockSize, byteArrayInputOutput, byteArrayInputOutput);

        assertAll(
                () -> assertEquals(0, bufferedInputOutput.length(), "Length doesn't match expected value"),
                () -> assertEquals(0, bufferedInputOutput.position(), "Position doesn't match expected value")
        );

        final var initialPosition = 0;
        bufferedInputOutput.seek(initialPosition);
        assertEquals(initialPosition, bufferedInputOutput.position(), "Position doesn't match expected value after seeking 0");

        for (var i = 0 ; i < writeSize ; i++) {
            var content = i;
            bufferedInputOutput.write(content);
            assertAll(
                    () -> assertEquals(content + 1, bufferedInputOutput.length(),
                            "Length doesn't match expected value after writing content [" + content + "]"),
                    () -> assertEquals(content + 1, bufferedInputOutput.position(),
                            "Position doesn't match expected value after writing content [" + content + "]")
            );
        }

        if (autoFlush) bufferedInputOutput.flush();

        assertAll(
                () -> assertEquals(writeSize, bufferedInputOutput.length(),
                        "Length doesn't match expected value after writing [" + writeSize + "] entries with autoFlush [" + autoFlush + "]"),
                () -> assertEquals(writeSize, bufferedInputOutput.position(),
                        "Position doesn't match expected value after writing [" + writeSize + "] entries with autoFlush [" + autoFlush + "]")
        );

        bufferedInputOutput.seek(initialPosition);

        if (autoFlush) bufferedInputOutput.flush();

        for (var i = 0 ; i < writeSize ; i++) {
            var idx = i;
            var content = i;
            bufferedInputOutput.write(idx, content);
            assertAll(
                    () -> assertEquals(writeSize, bufferedInputOutput.length(),
                            "Length should've remained the same after rewriting index [" + idx + "] with content [" + content + "]"),
                    () -> assertEquals(initialPosition, bufferedInputOutput.position(),
                            "Should've remained at initial position after rewriting index [" + idx + "] with content [" + content + "]")
            );
        }

        if (autoFlush) bufferedInputOutput.flush();

        assertAll(
                () -> assertEquals(writeSize, bufferedInputOutput.length(), "Length doesn't match expected value after rewriting all entries"),
                () -> assertEquals(initialPosition, bufferedInputOutput.position(), "Should've remained at initial position after rewriting all entries")
        );

        var bytes = bufferedInputOutput.toByteArray();

        assertAll(
                () -> assertEquals(initialPosition, bufferedInputOutput.position(), "Should've remained at initial position after obtaining bytes"),
                () -> assertEquals(writeSize, bytes.length, "Bytes length doesn't match expected value")
        );

        for (var i = 0 ; i < writeSize ; i++) {
            var value = bytes[i];
            var expected = (byte) i;
            assertEquals(expected, value, "Byte doesn't match expected value at index [" + i + "]");
        }

        bufferedInputOutput.seek(writeSize);

        for (int idx = writeSize ; idx < writeSize * 2 ; idx++) {
            var content = idx;
            var expected = idx + 1;
            bufferedInputOutput.write(content);
            assertAll(
                    () -> assertEquals(expected, bufferedInputOutput.length(),
                            "Length doesn't match expected value after writing content [" + content + "]"),
                    () -> assertEquals(expected, bufferedInputOutput.position(),
                            "Position doesn't match expected value after writing content [" + content + "]")
            );
        }

        if (autoFlush) bufferedInputOutput.flush();

        assertAll(
                () -> assertEquals(writeSize * 2, bufferedInputOutput.length(),
                        "Length doesn't match expected value after doubling the number of entries with autoFlush [" + autoFlush + "]"),
                () -> assertEquals(writeSize * 2, bufferedInputOutput.position(),
                        "Position doesn't match expected value after doubling the number of entries with autoFlush [" + autoFlush + "]")
        );

        for (var idx = writeSize * 2 ; idx < writeSize * 3 ; idx++) {
            var content = idx;
            var expected = idx + 1;
            bufferedInputOutput.write(content);
            assertAll(
                    () -> assertEquals(expected, bufferedInputOutput.length(),
                            "Length doesn't match expected value after writing content [" + content + "]"),
                    () -> assertEquals(expected, bufferedInputOutput.position(),
                            "Position doesn't match expected value after writing content [" + content + "]")
            );

        }

        var resize = (writeSize * 2) + (writeSize / 2);

        bufferedInputOutput.setLength(resize);

        if (autoFlush) bufferedInputOutput.flush();

        assertAll(
                () -> assertEquals(resize, bufferedInputOutput.length(), "Length doesn't match expected value after resizing"),
                () -> assertEquals(resize, bufferedInputOutput.position(), "Position doesn't match expected value after resizing")
        );
    }

}
