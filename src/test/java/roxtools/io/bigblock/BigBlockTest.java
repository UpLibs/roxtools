package roxtools.io.bigblock;

import org.junit.jupiter.api.Test;
import roxtools.FileUtils;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BigBlockTest {

    @Test
    void testBigBlockStorageDirectory() throws IOException {
        var sourceDir = FileUtils.createTempDirectory("test-bigblock-source");
        var storageDir = FileUtils.createTempDirectory("test-bigblock-");

        var storage = new BigBlockStorageDirectory(storageDir);

        try {
            for (var i = 0 ; i < 100 ; i++) {
                var name = "test" + i + ".txt";
                var content = "content" + i;
                FileUtils.saveFile(new File(sourceDir, name), content);
            }

            var bigBlock = new BigBlock("test1", storage);

            var blockIndex = bigBlock.buildFromDirectory(sourceDir);

            testBigBlockEntries(bigBlock, blockIndex);

            var storage2 = new BigBlockStorageDirectory(storageDir);

            var bigBlock2 = new BigBlock("test1", storage2);
            var blockIndex2 = bigBlock2.load();

            testBigBlockEntries(bigBlock2, blockIndex2);
        } finally {
            FileUtils.deleteTree(sourceDir.getParentFile(), sourceDir);
            sourceDir.delete();

            cleanStorageDirectory(storage);
            storageDir.delete();
        }
    }

    private void testBigBlockEntries(BigBlock bigBlock, BigBlockIndex blockIndex) {
        assertEquals(100, blockIndex.getTotalEntries(), "Total entries doesn't match expected value");

        for (var i = 0 ; i < 100 ; i++) {
            var name = "test" + i + ".txt";
            var content = "content" + i;

            assertTrue(blockIndex.containsEntry(name), "Block must contain entry with name: [" + name + "]");
            var entry = blockIndex.getEntry(name);

            assertAll(
                    () -> assertNotNull(entry, "Entry [" + name + "]: value shouldn't be null"),
                    () -> assertEquals(name, entry.getName(), "Entry [" + name + "]: name doesn't match expected value"),
                    () -> assertEquals(content.getBytes().length, entry.getLength(), "Entry [" + name + "]: length doesn't match expected value"),
                    () -> assertArrayEquals(content.getBytes(), bigBlock.getFileData(name), "Entry [" + name + "]: File data doesn't match expected value")
            );
        }

        for (var i = 0 ; i < 100 ; i++) {
            var name = "testNull" + i + ".txt";

            assertAll(
                    () -> assertFalse(blockIndex.containsEntry(name), "Index should not contain entry with name [" + name + "]"),
                    () -> assertNull(blockIndex.getEntry(name), "Entry [" + name + "]: value should be null")
            );
        }
    }

    private void cleanStorageDirectory(BigBlockStorageDirectory storageDirectory) {
        deleteFiles(storageDirectory.listBlockIndexFiles());
        deleteFiles(storageDirectory.listBlockFiles());
    }

    private void deleteFiles(File[] files) {
        if (files == null) return;
        for (var file : files) {
            file.delete();
        }
    }

}
