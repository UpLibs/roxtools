package roxtools;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roxtools.io.ByteArrayInputOutput;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ByteArrayInputOutputTest {

    static Stream<Arguments> testBasic() {
        return Stream.of(
                arguments(10, 100),
                arguments(100, 1000),
                arguments(1, 100),
                arguments(1, 1000),
                arguments(1024, 1024 * 1024),
                arguments(1024, 1024 * 1024 * 3)
        );
    }

    @MethodSource
    @ParameterizedTest
    void testBasic(int initialCapacity, int writeSize) throws IOException {
        var byteArrayInputOutput = new ByteArrayInputOutput(initialCapacity);

        assertAll(
                () -> assertEquals(0, byteArrayInputOutput.length(), "Length doesn't match expected value"),
                () -> assertEquals(0, byteArrayInputOutput.position(), "Position doesn't match expected value")
        );

        for (var i = 0 ; i < writeSize ; i++) {
            var content = i;
            byteArrayInputOutput.write(content);
            assertAll(
                    () -> assertEquals(content + 1, byteArrayInputOutput.length(),
                            "Length doesn't match expected value after writing byte [" + content + "]"),
                    () -> assertEquals(content + 1, byteArrayInputOutput.position(),
                            "Position doesn't match expected value after writing byte [" + content + "]")
            );
        }

        assertAll(
                () -> assertEquals(writeSize, byteArrayInputOutput.length(),
                        "Length doesn't match expected value after writing [" + writeSize + "] bytes"),
                () -> assertTrue(byteArrayInputOutput.capacity() >= writeSize,
                        "Capacity should be equal or greater than length after writing [" + writeSize + "] bytes")
        );

        byteArrayInputOutput.seek(0);
        assertEquals(0, byteArrayInputOutput.position(), "Position doesn't match expected value after seeking 0");

        var bytes = byteArrayInputOutput.toByteArray();
        assertEquals(writeSize, bytes.length, "Bytes length doesn't match expected value");

        for (var i = 0 ; i < writeSize ; i++) {
            var value = bytes[i];
            var expected = (byte) i;
            assertEquals(expected, value, "Byte doesn't match expected value at index [" + i + "]");
        }

        var halvedWriteSize = writeSize / 2;
        byteArrayInputOutput.setLength(halvedWriteSize);

        assertAll(
                () -> assertEquals(halvedWriteSize, byteArrayInputOutput.length(), "Length should've been halved"),
                () -> assertTrue(byteArrayInputOutput.capacity() >= halvedWriteSize,
                        "Capacity should be equal or greater than length after writing [" + writeSize + "] bytes")
        );

        var halvedBytes = byteArrayInputOutput.toByteArray();
        assertEquals(halvedWriteSize, halvedBytes.length, "Bytes length should be halved");

        for (var i = 0 ; i < halvedWriteSize ; i++) {
            var value = bytes[i];
            var expected = (byte) i;
            assertEquals(expected, value, "Byte doesn't match expected value at index [" + i + "]");
        }

        byteArrayInputOutput.reset();

        assertAll(
                () -> assertEquals(0, byteArrayInputOutput.length(), "Length should've been reset"),
                () -> assertEquals(0, byteArrayInputOutput.position(), "Position should've been reset"),
                () -> assertTrue(byteArrayInputOutput.capacity() >= halvedWriteSize,
                        "Capacity should've remained greater than or equal to [" + halvedWriteSize + "]")
        );
    }

}
