package fr.olympus5;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.UserAuthFactory;
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
    private SshServer server;
    private FileSystem serverFs;
    private SshClient client;
    private FileSystem clientFs;

    @BeforeEach
    void setUp() throws IOException {
        server = SshServer.setUpDefaultServer();
        server.setPort(2222);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        final List<UserAuthFactory> userAuthFactories = List.of(UserAuthNoneFactory.INSTANCE);
        server.setUserAuthFactories(userAuthFactories);
        serverFs = Jimfs.newFileSystem(Configuration.unix());
        // TODO: replace vfs with a cleaner solution.
        server.setFileSystemFactory(new VirtualFileSystemFactory(serverFs.getPath("/work").toAbsolutePath()));
        server.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory.Builder().build()));
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
    public void shouldAnswerWithTrue() throws IOException, URISyntaxException {
        final ClientSession session = client.connect("test", "localhost", 2222).verify().getSession();
        session.auth().verify();
        final SftpClient sftpClient = SftpClientFactory.instance().createSftpClient(session);
        final Path srcFile = clientFs.provider().getPath(this.getClass().getClassLoader().getResource("test.txt").toURI()).toAbsolutePath();

        sftpClient.put(srcFile, "test.txt");

        assertArrayEquals(Files.readAllBytes(srcFile), Files.readAllBytes(serverFs.getPath("/work/test.txt")));
    }
}
