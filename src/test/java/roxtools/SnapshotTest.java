package roxtools;

import org.junit.jupiter.api.Test;
import roxtools.snapshot.SnapshotCapturer;
import roxtools.snapshot.directory.DirectorySnapshotCapturer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SnapshotTest {

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void testBasic() throws IOException {
        var directoryRoot = FileUtils.createTempDirectory("directory-snapshot-", "-root");
        var directory = new File(directoryRoot, "test-basic");

        directory.mkdirs();

        try {
            SnapshotCapturer snapshotCapturer = new DirectorySnapshotCapturer("test", directoryRoot, directory);

            var ver = 1.0d;

            for (var i = 0 ; i <= 9 ; i++) {
                var file = new File(directory, "t" + i + ".txt");
                FileUtils.saveFile(file, "test" + i + "--" + ver);
            }

            for (var i = 0 ; i <= 9 ; i++) {
                var file = new File(directory, "t" + i + ".txt");
                var content = FileUtils.readFileAsString(file);
                assertEquals("test" + i + "--" + ver, content);
            }

            var snapshot1 = snapshotCapturer.takeSnapshot();
            var snapshotID = snapshot1.getSnapshotID();

            assertEquals("test", snapshotID.getGruopId());

            ver = 2.0;

            for (var i = 0 ; i <= 9 ; i++) {
                var file = new File(directory, "t" + i + ".txt");
                FileUtils.saveFile(file, "test" + i + "--" + ver);
            }

            for (var i = 0 ; i <= 9 ; i++) {
                var file = new File(directory, "t" + i + ".txt");
                var content = FileUtils.readFileAsString(file);
                assertEquals("test" + i + "--" + ver, content);
            }

            snapshotCapturer.restoreSnapshot(snapshot1);

            ver = 1.0;

            for (var i = 0 ; i <= 9 ; i++) {
                var file = new File(directory, "t" + i + ".txt");
                var content = FileUtils.readFileAsString(file);
                assertEquals("test" + i + "--" + ver, content);
            }

            var files = directory.listFiles();

            Arrays.sort(files);

            assertEquals(10, files.length);

            for (var i = 0 ; i <= 9 ; i++) {
                var file = files[i];
                var file2 = new File(directory, "t" + i + ".txt");
                assertEquals(file2, file);
            }
        } finally {
            FileUtils.deleteTree(directoryRoot, directory);
        }
    }

}
