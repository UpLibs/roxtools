package roxtools;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SerializationUtilsTest {

    @Test
    public void testWriteReadInt() throws IOException {
        var totalWrite = 1000;

        var ns0 = IntStream.range(0, totalWrite).toArray();

        var out0 = new ByteArrayOutputStream();
        SerializationUtils.writeInts(ns0, out0);
        var serial0 = out0.toByteArray();

        assertEquals(totalWrite * 4, serial0.length);

        var out = new ByteArrayOutputStream();

        for (var i = 0 ; i < totalWrite ; i++) {
            SerializationUtils.writeInt(i, out);
        }

        var serial = out.toByteArray();

        assertEquals(totalWrite * 4, serial.length);

        assertArrayEquals(serial0, serial);

        var in = new ByteArrayInputStream(serial);

        for (var i = 0 ; i < totalWrite ; i++) {
            var n = SerializationUtils.readInt(in);
            assertEquals(i, n);
        }

        var in2 = new ByteArrayInputStream(serial);

        var ns = SerializationUtils.readInts(totalWrite, in2);

        for (var i = 0 ; i < ns.length ; i++) {
            var n = ns[i];
            assertEquals(i, n);
        }

        var ns2 = SerializationUtils.readInts(serial, 0, serial.length);

        assertArrayEquals(ns, ns2);

        var out2 = new ByteArrayOutputStream();
        SerializationUtils.writeIntsBlock(ns, out2);
        var serial2 = out2.toByteArray();

        assertEquals(4 + ns.length * 4, serial2.length);

        var in3 = new ByteArrayInputStream(serial2);
        var ns3 = SerializationUtils.readIntsBlock(in3);

        assertArrayEquals(ns, ns3);
    }

    @Test
    public void testWriteReadFloat() throws IOException {
        var totalWrite = 1000;

        var ns0 = new float[totalWrite];
        for (var i = 0 ; i < totalWrite ; i++) {
            ns0[i] = i;
        }

        var out0 = new ByteArrayOutputStream();
        SerializationUtils.writeFloats(ns0, out0);
        var serial0 = out0.toByteArray();

        assertEquals(totalWrite * 4, serial0.length);

        var out = new ByteArrayOutputStream();

        for (var i = 0 ; i < totalWrite ; i++) {
            SerializationUtils.writeFloat(i, out);
        }

        var serial = out.toByteArray();

        assertEquals(totalWrite * 4, serial.length);

        assertArrayEquals(serial0, serial);

        var in = new ByteArrayInputStream(serial);

        for (var i = 0 ; i < totalWrite ; i++) {
            var n = (int) SerializationUtils.readFloat(in);
            assertEquals(i, n);
        }

        var in2 = new ByteArrayInputStream(serial);

        var ns = SerializationUtils.readFloats(totalWrite, in2);

        for (var i = 0 ; i < ns.length ; i++) {
            var n = (int) ns[i];
            assertEquals(i, n);
        }

        var ns2 = SerializationUtils.readFloats(serial, 0, serial.length);

        assertArrayEquals(ns, ns2);

        var out2 = new ByteArrayOutputStream();
        SerializationUtils.writeFloatsBlock(ns, out2);
        var seria2 = out2.toByteArray();

        assertEquals(4 + ns.length * 4, seria2.length);

        var in3 = new ByteArrayInputStream(seria2);
        var ns3 = SerializationUtils.readFloatsBlock(in3);

        assertArrayEquals(ns, ns3);

        ////////

        var nns1 = new float[10][totalWrite / 10];

        SerializationUtils.readFloats(serial, 0, serial.length, nns1);

        var nsCursor = 0;

        for (var i = 0 ; i < nns1.length ; i++) {
            var fs = nns1[i];

            for (var j = 0 ; j < fs.length ; j++) {
                var f = fs[j];
                var f0 = ns[nsCursor++];
                assertEquals(f0, f, 0);
            }
        }

        /////////

        var nnns1 = new float[10][10][totalWrite / 100];

        SerializationUtils.readFloats(serial, 0, serial.length, nnns1);

        nsCursor = 0;

        for (var i = 0 ; i < nns1.length ; i++) {
            var ffs = nnns1[i];

            for (var j = 0 ; j < ffs.length ; j++) {
                var fs = ffs[j];

                for (var k = 0 ; k < fs.length ; k++) {
                    var f = fs[k];
                    var f0 = ns[nsCursor++];
                    assertEquals(f0, f, 0);
                }
            }
        }
    }

}
