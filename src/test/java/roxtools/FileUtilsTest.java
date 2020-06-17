package roxtools;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileUtilsTest {

    @Test
    void createTempFile() throws IOException {
        for (var i = 0 ; i < 10 ; i++) {
            var prefix = "pref_" + i + "-";
            var suffix = "-suf_" + i;

            var tempFile = FileUtils.createTempFile(prefix, suffix);
            assertTrue(tempFile.exists(), "Temporary file should've been created");

            var name = tempFile.getName();
            assertAll(
                    () -> assertTrue(name.startsWith(prefix), "Temporary file name should start with prefix: " + prefix),
                    () -> assertTrue(name.endsWith(suffix), "Temporary file name should end with suffix: " + suffix)
            );

            tempFile.delete();
            assertFalse(tempFile.exists(), "Temporary file should've been deleted");
        }

    }

    @Test
    void createTempDirectoryWithMultipleChildFiles() throws IOException {
        var parentDir = FileUtils.createTempDirectory("test-temp-dir", "-junit-test");

        assertAll(
                () -> assertTrue(parentDir.exists(), "Parent directory should've been created"),
                () -> assertTrue(parentDir.isDirectory(), "Parent directory File object should be a directory")
        );

        for (var i = 0 ; i < 10 ; i++) {
            var prefix = "pref_" + i + "-";
            var suffix = "-suf_" + i;

            var childFile = FileUtils.createTempFile(parentDir, prefix, suffix);
            assertTrue(childFile.exists(), "Child file should've been created with index [" + i + "]");

            assertAll(
                    () -> assertTrue(childFile.isFile(), "Child file Object should be a file"),
                    () -> assertTrue(childFile.getName().startsWith(prefix), "Child file name should start with prefix: " + prefix),
                    () -> assertTrue(childFile.getName().endsWith(suffix), "Child file name should end with suffix: " + suffix),
                    () -> assertEquals(childFile.getParentFile(), parentDir, "Child file parent file object should be the temporary directory")
            );

            childFile.delete();
            assertFalse(childFile.exists(), "Child file should've been deleted");
        }

        parentDir.delete();
        assertFalse(parentDir.exists(), "Parent directory should've been deleted");
    }

    @Test
    void createMultipleTempDirectories() throws IOException {
        for (var i = 0 ; i < 10 ; i++) {
            var prefix = "pref_" + i + "-";
            var suffix = "-suf_" + i;

            var tempDirectory = FileUtils.createTempDirectory(prefix, suffix);

            assertAll(
                    () -> assertTrue(tempDirectory.exists(), "Temp directory should've been created"),
                    () -> assertTrue(tempDirectory.isDirectory(), "Temp directory File object should be a directory"),
                    () -> assertTrue(tempDirectory.getName().startsWith(prefix), "Temporary directory name should start with prefix: " + prefix),
                    () -> assertTrue(tempDirectory.getName().endsWith(suffix), "Temporary directory name should end with suffix: " + suffix)
            );

            tempDirectory.delete();
            assertFalse(tempDirectory.exists(), "Temporary directory should've been deleted");
        }
    }

    @Test
    void testTempDirectoryParentDirectory() throws IOException {
        var parentDir = FileUtils.createTempDirectory("test-temp-dir", "-junit-test");

        assertAll(
                () -> assertTrue(parentDir.exists(), "Temp directory should've been created"),
                () -> assertTrue(parentDir.isDirectory(), "Temp directory File object should be a directory")
        );

        for (var i = 0 ; i < 10 ; i++) {
            var prefix = "pref_" + i + "-";
            var suffix = "-suf_" + i;

            var childDirectory = FileUtils.createTempDirectory(parentDir, prefix, suffix);
            assertTrue(childDirectory.exists(), "Child directory should've been created with index [" + i + "]");

            assertAll(
                    () -> assertTrue(childDirectory.isDirectory(), "Child directory Object should be a file"),
                    () -> assertTrue(childDirectory.getName().startsWith(prefix), "Child directory name should start with prefix: " + prefix),
                    () -> assertTrue(childDirectory.getName().endsWith(suffix), "Child directory name should end with suffix: " + suffix),
                    () -> assertEquals(childDirectory.getParentFile(), parentDir, "Child directory parent file object should be the temporary directory")
            );

            childDirectory.delete();
            assertFalse(childDirectory.exists(), "Child directory should've been deleted");
        }

        parentDir.delete();
        assertFalse(parentDir.exists(), "Parent directory should've been deleted");
    }

    @Test
    void testSaveReadFile() throws IOException {
        var file = FileUtils.createTempFile("test-saveread-file", "junit-temp");
        assertTrue(file.exists(), "Temp file should've been created");

        var content = "abc123";
        FileUtils.saveFile(file, content);

        assertAll(
                () -> assertTrue(file.length() >= content.length(), "Temp File's length should be equal to or greater than content length"),
                () -> assertEquals(content, FileUtils.readFileAsString(file), "Temp File's content should match expected value")
        );

        file.delete();
    }

    @Test
    void resolveFilePath() {
        assertAll(
                () -> assertEquals(new File("/foo/bar"), FileUtils.resolveFilePath(new File("/foo/bar"))),
                () -> assertEquals(new File("/foo"), FileUtils.resolveFilePath(new File("/foo/bar/.."))),
                () -> assertEquals(new File("/foo/bar"), FileUtils.resolveFilePath(new File("/foo/bar/xxx/.."))),
                () -> assertEquals(new File("/foo/bar/yyy"), FileUtils.resolveFilePath(new File("/foo/bar/xxx/../yyy"))),
                () -> assertEquals(FileUtils.resolveFilePath(new File("./foo/bar/xxx/..")), FileUtils.resolveFilePath(new File("./foo/bar/yyy/..")))
        );
    }

    @Test
    void isSamePath() {
        assertAll(
                () -> assertTrue(FileUtils.isSamePath(new File("/foo/bar"), new File("/foo/bar")),
                        "/foo/bar and /foo/bar share the same path"),
                () -> assertFalse(FileUtils.isSamePath(new File("/foo/bar"), new File("/foo/bar/aaaa")),
                        "/foo/bar and /foo/bar/aaaa don't share the same path"),
                () -> assertTrue(FileUtils.isSamePath(new File("/foo/bar"), new File("/foo/bar/zzz/../")),
                        "/foo/bar and /foo/bar/zzz/../ share the same path"),
                () -> assertTrue(FileUtils.isSamePath(new File("/foo/bar"), new File("/foo/xxx/zzz/../../bar")),
                        "/foo/bar and /foo/xxx/zzz/../../bar don't share the same path")
        );

    }

    @Test
    void isSubPath() {
        assertTrue(FileUtils.isSubPath(new File("/foo/bar"), new File("/foo/bar/aaaa")), "/foo/bar is the subpath of /foo/bar/aaa");
    }

    @Test
    void checkAuthorityPoint() {
        assertAll(
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> FileUtils.checkAuthorityPoint(new File("/")),
                        "Should throw IllegalArgumentException for root path"),
                () -> assertThrows(
                        IllegalArgumentException.class,
                        () -> FileUtils.checkAuthorityPoint(new File("../../../../../../../../../../../../../../../../../../../../../../../../../../../../../../")),
                        "Should throw IllegalArgumentException for non-absolute path"),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> FileUtils.checkAuthorityPoint(null),
                        "Should throw NullPointerException for null path")
        );
    }

    @Test
    void hasAuthorityOverFile() {
        assertAll(
                () -> assertTrue(FileUtils.hasAuthorityOverFile(new File("/foo"), new File("/foo/bar")),
                        "/foo should've authority over /foo/bar"),
                () -> assertTrue(FileUtils.hasAuthorityOverFile(new File("/foo"), new File("/foo/bar/xxx")),
                        "/foo should've authority over /foo/bar/xxx"),
                () -> assertFalse(FileUtils.hasAuthorityOverFile(new File("/xxx"), new File("/foo/bar")),
                        "/xxx shouldn't have authority over /foo/bar"),
                () -> assertThrows(IllegalArgumentException.class, () -> FileUtils.hasAuthorityOverFile(new File("/foo/.."), new File("/foo/bar/xxx")),
                        "Should've thrown due to non-absolute authority point")
        );
    }

    @Test
    void testDeleteTree() throws IOException {
        // Ensure that these tests are OK for file system security reason:
        {
            resolveFilePath();
            hasAuthorityOverFile();
            isSamePath();
            isSubPath();
        }

        var authorityDir = FileUtils.createTempDirectory("test-fileutils-deletetree", "-junit-temp");

        assertTrue(authorityDir.isDirectory());

        var dir = new File(authorityDir, "thedir");
        dir.mkdirs();
        assertTrue(dir.isDirectory());

        var sub1 = new File(dir, "sub1");
        sub1.mkdirs();
        assertTrue(sub1.isDirectory());

        var sub2 = new File(dir, "sub2");
        sub2.mkdirs();

        ///////////////////////

        FileUtils.saveFile(new File(sub1, "sub1-file1"), "111");
        FileUtils.saveFile(new File(sub1, "sub1-file2"), "222");

        FileUtils.saveFile(new File(sub2, "sub2-file1"), "aaa");
        FileUtils.saveFile(new File(sub2, "sub2-file2"), "bbb");

        FileUtils.saveFile(new File(dir, "file1"), "xxx");
        FileUtils.saveFile(new File(dir, "file2"), "yyy");

        ///////////////////////

        assertTrue(sub2.isDirectory());

        var wrongAuthorityDir = new File(authorityDir, "wrong-authority-dir");

        assertFalse(FileUtils.hasAuthorityOverFile(wrongAuthorityDir, dir));

        Exception errorWrongAuthority = null;
        try {
            assertFalse(FileUtils.deleteTree(wrongAuthorityDir, dir), "Directory shouldn't be deleted due to wrong authority point");
        } catch (Exception e) {
            System.out.println("Expected authority error: " + e.getMessage());
            errorWrongAuthority = e;
        }

        assertNotNull(errorWrongAuthority);
        assertTrue(FileUtils.deleteTree(authorityDir, dir));
        assertFalse(dir.exists());

    }

}
