package io.quarkiverse.messaginghub.pooled.jms;

import java.lang.annotation.Annotation;

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
        for (InstanceHandle<ConnectionFactory> handle : Arc.container().listAll(ConnectionFactory.class)) {
            ConnectionFactory cf = handle.get();
            // Unwrap CDI client proxy if present
            if (cf instanceof ClientProxy) {
                cf = (ConnectionFactory) ClientProxy.unwrap(cf);
            }

            if (cf instanceof DelegatingJmsPoolConnectionFactory) {
                DelegatingJmsPoolConnectionFactory delegating = (DelegatingJmsPoolConnectionFactory) cf;
                String name = extractIdentifierName(handle.getBean());
                if (name == null) {
                    name = PooledJmsRuntimeConfig.DEFAULT_CONNECTION_FACTORY_NAME;
                }

                ConnectionFactory rawCf = delegating.getWrappedConnectionFactory();
                JmsPoolConnectionFactory pool = wrapper.createPool(name, rawCf);
                LOG.debugf("Initializing pooled ConnectionFactory '%s' with pool type %s", name,
                        pool.getClass().getSimpleName());
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
