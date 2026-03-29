package io.quarkiverse.messaginghub.pooled.jms;

import java.lang.annotation.Annotation;
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
 * Initializes pooled JMS ConnectionFactory beans on startup. The SPI wrapper defers
 * pool creation by wrapping all ConnectionFactories in a {@link DelegatingJmsPoolConnectionFactory}
 * that stores only the raw ConnectionFactory. This initializer then creates the correct pool
 * (with the proper configuration and transaction type) for each bean based on its
 * {@link Identifier} qualifier and sets it as the delegate.
 */
public class PooledJmsConnectionFactoryInitializer {
    private static final Logger LOG = Logger.getLogger(PooledJmsConnectionFactoryInitializer.class);

    private final PooledJmsWrapper wrapper;

    public PooledJmsConnectionFactoryInitializer(PooledJmsWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public void initialize() {
        Set<String> configuredNames = wrapper.getExplicitlyConfiguredNames();

        for (InstanceHandle<ConnectionFactory> handle : Arc.container().listAll(ConnectionFactory.class)) {
            ConnectionFactory cf = handle.get();
            // Unwrap CDI client proxy if present
            if (cf instanceof ClientProxy) {
                cf = (ConnectionFactory) ClientProxy.unwrap(cf);
            }

            if (cf instanceof DelegatingJmsPoolConnectionFactory) {
                DelegatingJmsPoolConnectionFactory delegating = (DelegatingJmsPoolConnectionFactory) cf;
                String identifierName = extractIdentifierName(handle.getBean());

                // Resolve the config name: use the identifier if it has explicit config,
                // otherwise fall back to the default config.
                String configName;
                if (identifierName == null || !configuredNames.contains(identifierName)) {
                    configName = PooledJmsRuntimeConfig.DEFAULT_CONNECTION_FACTORY_NAME;
                } else {
                    configName = identifierName;
                }

                ConnectionFactory rawCf = delegating.getWrappedConnectionFactory();
                if (!wrapper.isPoolingEnabled(configName)) {
                    LOG.debugf("Pooling disabled for ConnectionFactory '%s', using passthrough",
                            identifierName != null ? identifierName : "default");
                    delegating.setPassthrough();
                    continue;
                }
                JmsPoolConnectionFactory pool = wrapper.createPool(configName, rawCf);
                LOG.debugf("Initializing pooled ConnectionFactory '%s' with config '%s', pool type %s",
                        identifierName != null ? identifierName : "default",
                        configName, pool.getClass().getSimpleName());
                delegating.setDelegate(pool);
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
}
