package roxtools;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import roxtools.ipc.JVMRunner;
import roxtools.ipc.ProcessRunner.OutputConsumer;
import roxtools.ipc.ProcessRunner.OutputConsumerListener;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JVMRunnerTest {

    public boolean isOSLinuxCompatible() {
        var os = System.getProperty("os.name");

        if (os == null || os.isBlank()) return false;

        os = os.toLowerCase();

        return os.contains("linux") || (os.contains("mac") && os.contains("os x"));
    }

    @Test
    public void testBasic() throws IOException, InterruptedException {
        if (!isOSLinuxCompatible()) {
            throw new IllegalStateException("Can't test if OS is not Linux compatible!");
        }

        var jvmRunner = new JVMRunner(this.getClass().getName());

        assertTrue(jvmRunner.addVmArgument("-Xmx100m"));
        assertTrue(jvmRunner.containsVmArgument("-Xmx100m"));

        jvmRunner.setVMProperty("test.jvmrunner", "123");
        assertTrue(jvmRunner.containsVMProperty("test.jvmrunner"));

        jvmRunner.execute();
        assertTrue(jvmRunner.isRunning());

        jvmRunner.waitForProcess();
        var output = jvmRunner.getOutputConsumer().waitFinished().getOutputAsString();

        assertEquals("Hello World!\n", output);
    }

    @Test
    public void testOutputListener() throws IOException, InterruptedException {
        if (!isOSLinuxCompatible()) {
            throw new IllegalStateException("Can't test if OS is not Linux compatible!");
        }

        var jvmRunner = new JVMRunner(this.getClass().getName());

        assertTrue(jvmRunner.addVmArgument("-Xmx100m"));
        assertTrue(jvmRunner.containsVmArgument("-Xmx100m"));

        jvmRunner.setVMProperty("test.jvmrunner", "123");
        assertTrue(jvmRunner.containsVMProperty("test.jvmrunner"));

        final var lines = new ArrayList<String>();

        jvmRunner.execute(true, new OutputConsumerListener() {
            @Override
            public void onReadLine(OutputConsumer outputConsumer, String line) {
                lines.add(line);
            }

            @Override
            public void onReadBytes(OutputConsumer outputConsumer, byte[] bytes, int length) {
            }
        });

        assertTrue(jvmRunner.isRunning());

        jvmRunner.waitForProcess();

        var output = jvmRunner.getOutputConsumer().waitFinished().getOutputAsString();
        assertEquals("Hello World!\n", output);
        assertEquals("Hello World!\n", lines.get(0));
        assertEquals(1, lines.size());
    }

    @Test
    public void testClassClasspath() {
        var file = JVMRunner.getClassClasspath(JVMRunner.class);

        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.length() > 1);

        var cp1 = JVMRunner.getClassHierarchyClasspath(false, JVMRunner.class);

        assertNotNull(cp1);
        assertEquals(2, cp1.length);

        var cp2 = JVMRunner.getClassHierarchyClasspath(true, JVMRunner.class);

        assertNotNull(cp2);
        assertEquals(1, cp2.length);

        var cp3 = JVMRunner.getClassesClasspath(true, JVMRunner.class, Test.class, Disabled.class);

        assertNotNull(cp3);
        assertTrue(cp3.length > 1);
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }

}
