package io.quarkiverse.messaginghub.pooled.jms.it.ibmmq;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class IbmMqTestContainer
        implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    private static final String CONTAINER_NAME = "ibmmq";
    private static final String CHANNEL_NAME = "DEV.ADMIN.SVRCONN";
    private static final String QUEUE_MANAGER_NAME = "LOCALDEVQM";
    private static final int MQ_PORT = 1414;

    private static final DockerImageName CONTAINER_IMAGE = DockerImageName
            .parse("ibm-messaging/mq")
            .withTag("9.4.3.1-r2")
            .withRegistry("icr.io");

    private String containerNetworkId;
    private GenericContainer<?> container;

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        containerNetworkId = context.containerNetworkId().orElse(null);
    }

    @Override
    public Map<String, String> start() {
        container = createAndStartContainer();
        try {
            return createConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("resource")
    private GenericContainer<?> createAndStartContainer() {
        GenericContainer<?> c = new GenericContainer<>(CONTAINER_IMAGE)
                .withEnv(Map.of(
                        "LICENSE", "accept",
                        "MQ_QMGR_NAME", QUEUE_MANAGER_NAME,
                        "MQ_DEV", "true"))
                .withCopyToContainer(
                        MountableFile.forClasspathResource("ibmmq/run/secrets/mqAdminPassword"),
                        "/run/secrets/mqAdminPassword")
                .withCopyToContainer(
                        MountableFile.forClasspathResource("ibmmq/run/secrets/mqAppPassword"),
                        "/run/secrets/mqAppPassword")
                .withCopyToContainer(
                        MountableFile.forClasspathResource("ibmmq/etc/mqm/20-config.mqsc"),
                        "/etc/mqm/20-config.mqsc")
                .withExposedPorts(MQ_PORT, 9443)
                .withCreateContainerCmdModifier(command -> command.withName(CONTAINER_NAME))
                .waitingFor(
                        Wait.forLogMessage(".*The mqweb server is ready to run a smarter planet..*", 1));
        Optional.ofNullable(containerNetworkId).ifPresent(c::withNetworkMode);
        c.start();
        return c;
    }

    private Map<String, String> createConfig() throws IOException {
        int port = containerNetworkId == null
                ? Objects.requireNonNull(container).getMappedPort(MQ_PORT)
                : MQ_PORT;
        String host = containerNetworkId == null ? "localhost" : CONTAINER_NAME;
        String appPassword;
        try (InputStream is = IbmMqTestContainer.class.getClassLoader()
                .getResourceAsStream("ibmmq/run/secrets/mqAppPassword")) {
            appPassword = new String(Objects.requireNonNull(is).readAllBytes(), StandardCharsets.UTF_8);
        }
        return Map.of(
                "ibmmq.hostname", host,
                "ibmmq.port", Integer.toString(port),
                "ibmmq.channel", CHANNEL_NAME,
                "ibmmq.queue-manager", QUEUE_MANAGER_NAME,
                "ibmmq.user", "app",
                "ibmmq.password", appPassword);
    }

    @Override
    public void stop() {
        if (container != null) {
            container.stop();
        }
    }
}
