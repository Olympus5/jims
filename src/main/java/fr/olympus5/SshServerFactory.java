package fr.olympus5;

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.UserAuthNoneFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;

import java.nio.file.FileSystem;
import java.util.Collections;
import java.util.List;

public class SshServerFactory {
    public static SshServer getSshServer(final FileSystem serverFs, final int port) {
        final SshServer server = SshServer.setUpDefaultServer();
        server.setPort(port);
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        server.setUserAuthFactories(List.of(UserAuthNoneFactory.INSTANCE));
        server.setFileSystemFactory(new VirtualFileSystemFactory(serverFs.getPath("/").toAbsolutePath()));
        server.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory.Builder().build()));
        return server;
    }
}
