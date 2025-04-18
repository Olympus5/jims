package fr.olympus5;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.UserAuthFactory;
import org.apache.sshd.server.auth.UserAuthNoneFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import org.apache.sshd.sftp.client.impl.DefaultSftpClientFactory;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Unit test for simple App.
 */
public class AppTest {
    private SshServer server;
    private SshClient client;
    private SftpClientFactory sftpClientFactory;

    @BeforeEach
    void setUp() throws IOException {
        server = SshServer.setUpDefaultServer();
        server.setPort(2222);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        final List<UserAuthFactory> userAuthFactories = List.of(UserAuthNoneFactory.INSTANCE);
        server.setUserAuthFactories(userAuthFactories);
        // TODO: initialize with jimfs
        server.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory.Builder().build()));
        server.start();

        client = SshClient.setUpDefaultClient();
        client.start();

        sftpClientFactory = DefaultSftpClientFactory.INSTANCE;
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
    public void shouldAnswerWithTrue() throws IOException {
        final ClientSession session = client.connect("test", "localhost", 2222).verify(1, TimeUnit.SECONDS).getSession();
        session.auth().verify(1, TimeUnit.SECONDS);
        final SftpClient sftpClient = sftpClientFactory.createSftpClient(session);
    }
}
