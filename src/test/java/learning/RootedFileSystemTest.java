package learning;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.apache.sshd.common.file.root.RootedFileSystemProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RootedFileSystemTest {

    private FileSystem fs;

    @BeforeEach
    void setUp() {
        fs = Jimfs.newFileSystem(Configuration.unix());
    }

    @Test
    void rootedOnRoot_createFile() throws IOException {
        final FileSystem rootedFs = new RootedFileSystemProvider().newFileSystem(fs.getPath("/"), Collections.emptyMap());

        final Path result = Files.createFile(rootedFs.getPath("/test"));

        assertTrue(Files.exists(result));
    }
}
