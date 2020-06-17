package roxtools;

import org.junit.jupiter.api.Test;
import roxtools.ipc.ProcessRunner;
import roxtools.ipc.ProcessRunner.OutputConsumer;
import roxtools.ipc.ProcessRunner.OutputConsumerListener;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProcessRunnerTest {

    public boolean isOSLinuxCompatible() {
        String os = System.getProperty("os.name");

        if (os == null || os.trim().isEmpty()) return false;

        os = os.toLowerCase();

        return os.contains("linux") || (os.contains("mac") && os.contains("os x"));
    }

    @Test
    public void testBasic() throws IOException, InterruptedException {
        if (!isOSLinuxCompatible()) {
            throw new IllegalStateException("Can't test if OS is not Linux compatible!");
        }

        var processRunner = new ProcessRunner("/bin/ls", "/");
        processRunner.execute(true);

        var exitCode = processRunner.waitForProcess(true);
        assertEquals(0, exitCode);

        var output = processRunner.getOutputConsumer().getOutputAsString();
        assertTrue(output.trim().length() > 1);
        assertTrue(output.trim().split("\\r?\\n").length >= 3);
    }

    @Test
    public void testOutputConsumerListener() throws IOException, InterruptedException {
        if (!isOSLinuxCompatible()) {
            throw new IllegalStateException("Can't test if OS is not Linux compatible!");
        }

        var processRunner = new ProcessRunner("/bin/ls", "/");

        final var outputBuffer = new StringBuilder();
        final var outputLinesBuffer = new StringBuilder();

        processRunner.setOutputConsumerListener(new OutputConsumerListener() {
            @Override
            public void onReadBytes(OutputConsumer outputConsumer, byte[] bytes, int length) {
                outputBuffer.append(new String(bytes, 0, length));
            }

            @Override
            public void onReadLine(OutputConsumer outputConsumer, String line) {
                outputLinesBuffer.append(line);
            }
        });

        processRunner.execute(true);

        var exitCode = processRunner.waitForProcess(true);
        assertEquals(0, exitCode);

        var output = outputBuffer.toString();
        assertTrue(output.trim().length() > 1);
        assertTrue(output.trim().split("\\r?\\n").length >= 3);

        var outputLines = outputLinesBuffer.toString();
        assertEquals(output, outputLines);
    }

}
