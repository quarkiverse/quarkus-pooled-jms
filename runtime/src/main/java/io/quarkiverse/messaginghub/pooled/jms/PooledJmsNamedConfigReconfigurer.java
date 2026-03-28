package io.quarkiverse.messaginghub.pooled.jms;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import jakarta.jms.ConnectionFactory;

import org.jboss.logging.Logger;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ClientProxy;
import io.quarkus.arc.InjectableBean;
import io.quarkus.arc.InstanceHandle;
import io.smallrye.common.annotation.Identifier;

/**
 * Reconfigures pooled JMS ConnectionFactory beans on startup based on their
 * {@link Identifier} qualifier. The SPI wrapper initially wraps all ConnectionFactories
 * with the default pool configuration. This reconfigurer then applies the correct
 * named configuration by inspecting each bean's CDI qualifiers.
 */
public class PooledJmsNamedConfigReconfigurer {
    private static final Logger LOG = Logger.getLogger(PooledJmsNamedConfigReconfigurer.class);

    private final PooledJmsRuntimeConfig pooledJmsRuntimeConfig;

    public PooledJmsNamedConfigReconfigurer(PooledJmsRuntimeConfig pooledJmsRuntimeConfig) {
        this.pooledJmsRuntimeConfig = pooledJmsRuntimeConfig;
    }

    public void reconfigure() {
        Map<String, PooledJmsPoolConfig> configs = pooledJmsRuntimeConfig.connectionFactories();

        // Only reconfigure for names that have explicit pooled-jms configuration.
        // Note: @WithDefaults causes configs.get() to return defaults for any key,
        // so we must check keySet() to find explicitly configured names.
        Set<String> configuredNames = configs.keySet();
        if (configuredNames.size() <= 1) {
            return;
        }

        for (InstanceHandle<ConnectionFactory> handle : Arc.container().listAll(ConnectionFactory.class)) {
            String name = extractIdentifierName(handle.getBean());
            if (name == null || PooledJmsRuntimeConfig.DEFAULT_CONNECTION_FACTORY_NAME.equals(name)) {
                continue;
            }

            if (!configuredNames.contains(name)) {
                LOG.debugf("No pooled-jms config found for '%s', keeping default pool settings", name);
                continue;
            }

            PooledJmsPoolConfig namedConfig = configs.get(name);

            ConnectionFactory cf = handle.get();
            // Unwrap CDI client proxy if present
            if (cf instanceof ClientProxy) {
                cf = (ConnectionFactory) ClientProxy.unwrap(cf);
            }

            if (cf instanceof JmsPoolConnectionFactory) {
                JmsPoolConnectionFactory pool = (JmsPoolConnectionFactory) cf;
                LOG.debugf("Reconfiguring pooled ConnectionFactory '%s' with named config", name);
                applyConfig(pool, namedConfig);
            }
        }
    }

    private static String extractIdentifierName(InjectableBean<?> bean) {
        for (Annotation qualifier : bean.getQualifiers()) {
            if (qualifier instanceof Identifier) {
                return ((Identifier) qualifier).value();
            }
        }
        return null;
    }

    private static void applyConfig(JmsPoolConnectionFactory pool, PooledJmsPoolConfig config) {
        pool.setMaxConnections(config.maxConnections());
        pool.setConnectionIdleTimeout(config.connectionIdleTimeout());
        pool.setConnectionCheckInterval(config.connectionCheckInterval());
        pool.setUseProviderJMSContext(config.useProviderJMSContext());
        pool.setMaxSessionsPerConnection(config.maxSessionsPerConnection());
        pool.setBlockIfSessionPoolIsFull(config.blockIfSessionPoolIsFull());
        pool.setBlockIfSessionPoolIsFullTimeout(config.blockIfSessionPoolIsFullTimeout());
        pool.setUseAnonymousProducers(config.useAnonymousProducers());
    }
}
