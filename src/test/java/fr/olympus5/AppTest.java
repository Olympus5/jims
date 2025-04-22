package fr.olympus5;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.UserAuthNoneFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * Unit test for simple App.
 */
public class AppTest {
    private static final int PORT = 2222;

    private SshServer server;
    private FileSystem serverFs;
    private SshClient client;
    private FileSystem clientFs;

    @BeforeEach
    void setUp() throws IOException {
        serverFs = Jimfs.newFileSystem(Configuration.unix());
        server = getSshServer(serverFs, 2222);
        server.start();

        client = SshClient.setUpDefaultClient();
        clientFs = FileSystems.getDefault();
        client.start();
    }

    @AfterEach
    void tearDown() throws IOException {
        client.stop();
        server.stop();
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void putFile() throws IOException, URISyntaxException {
        final ClientSession session = client.connect("testuser", "localhost", 2222).verify().getSession();
        session.auth().verify();
        final SftpClient sftpClient = SftpClientFactory.instance().createSftpClient(session);
        final Path srcFile = clientFs.provider().getPath(this.getClass().getClassLoader().getResource("test.txt").toURI()).toAbsolutePath();

        sftpClient.put(srcFile, "/test.txt");

        assertArrayEquals(Files.readAllBytes(srcFile), Files.readAllBytes(serverFs.getPath("/test.txt")));
    }

    @Test
    void putFileAfterServerRestart() throws IOException, URISyntaxException {
        server.stop(true);
        serverFs = Jimfs.newFileSystem(Configuration.unix());
        server = getSshServer(serverFs, PORT);
        server.start();
        final ClientSession session = client.connect("testuser", "localhost", 2222).verify().getSession();
        session.auth().verify();
        final SftpClient sftpClient = SftpClientFactory.instance().createSftpClient(session);
        final Path srcFile = clientFs.provider().getPath(this.getClass().getClassLoader().getResource("test.txt").toURI()).toAbsolutePath();

        sftpClient.put(srcFile, "/test.txt");

        assertArrayEquals(Files.readAllBytes(srcFile), Files.readAllBytes(serverFs.getPath("/test.txt")));
    }

    private static SshServer getSshServer(final FileSystem serverFs, final int port) {
        final SshServer server = SshServer.setUpDefaultServer();
        server.setPort(port);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        server.setUserAuthFactories(List.of(UserAuthNoneFactory.INSTANCE));
        // TODO: replace vfs with a cleaner solution.
        server.setFileSystemFactory(new VirtualFileSystemFactory(serverFs.getPath("/").toAbsolutePath()));
        server.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory.Builder().build()));
        return server;
    }
}
