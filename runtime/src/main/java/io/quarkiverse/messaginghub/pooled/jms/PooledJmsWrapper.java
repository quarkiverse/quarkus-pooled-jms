package io.quarkiverse.messaginghub.pooled.jms;

import java.util.ArrayList;
import java.util.List;

import jakarta.jms.ConnectionFactory;

import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;

import io.quarkiverse.messaginghub.pooled.jms.transaction.LocalTransactionSupport;
import io.quarkiverse.messaginghub.pooled.jms.transaction.XATransactionSupport;

public class PooledJmsWrapper {
    private boolean transaction;
    private PooledJmsRuntimeConfig pooledJmsRuntimeConfig;

    private static List<JmsPoolConnectionFactory> poolConnectionFactories = new ArrayList<>();

    public PooledJmsWrapper(boolean transaction, PooledJmsRuntimeConfig pooledJmsRuntimeConfig) {
        this.transaction = transaction;
        this.pooledJmsRuntimeConfig = pooledJmsRuntimeConfig;
    }

    /**
     * Wrap the given connection factory using the default pool configuration.
     *
     * @param connectionFactory the connection factory to wrap
     * @return the wrapped (pooled) connection factory
     */
    public ConnectionFactory wrapConnectionFactory(ConnectionFactory connectionFactory) {
        return wrapConnectionFactory(PooledJmsRuntimeConfig.DEFAULT_CONNECTION_FACTORY_NAME, connectionFactory);
    }

    /**
     * Wrap the given connection factory using the named pool configuration.
     * If no configuration is found for the given name, the default configuration is used.
     *
     * @param name the configuration name (e.g. the connection factory identifier)
     * @param connectionFactory the connection factory to wrap
     * @return the wrapped (pooled) connection factory
     */
    public ConnectionFactory wrapConnectionFactory(String name, ConnectionFactory connectionFactory) {
        PooledJmsPoolConfig config = getConfigForName(name);

        if (!config.poolingEnabled()) {
            return connectionFactory;
        }

        if (transaction && config.transaction().equals(TransactionIntegration.XA)) {
            if (XATransactionSupport.isEnabled()) {
                JmsPoolConnectionFactory cf = XATransactionSupport.getXAConnectionFactory(connectionFactory, config);
                poolConnectionFactories.add(cf);
                return cf;
            }

            throw new IllegalStateException("XA Transaction support is not available");
        } else if (transaction && config.transaction().equals(TransactionIntegration.ENABLED)) {
            if (LocalTransactionSupport.isEnabled()) {
                JmsPoolConnectionFactory cf = LocalTransactionSupport.getLocalTransactionConnectionFactory(connectionFactory,
                        config);
                poolConnectionFactories.add(cf);
                return cf;
            }

            throw new IllegalStateException("Local TransactionManager support is not available");
        } else {
            JmsPoolConnectionFactory cf = getConnectionFactory(connectionFactory, config);
            poolConnectionFactories.add(cf);
            return cf;
        }
    }

    public void clearAll() {
        for (JmsPoolConnectionFactory cf : poolConnectionFactories) {
            cf.clear();
        }
    }

    private PooledJmsPoolConfig getConfigForName(String name) {
        return pooledJmsRuntimeConfig.connectionFactories().get(name);
    }

    private JmsPoolConnectionFactory getConnectionFactory(ConnectionFactory connectionFactory,
            PooledJmsPoolConfig config) {
        JmsPoolConnectionFactory poolConnectionFactory = new JmsPoolConnectionFactory();
        pooledJmsRuntimeConfigureConnectionFactory(poolConnectionFactory, connectionFactory, config);

        return poolConnectionFactory;
    }

    public static void pooledJmsRuntimeConfigureConnectionFactory(JmsPoolConnectionFactory poolConnectionFactory,
            ConnectionFactory connectionFactory, PooledJmsPoolConfig config) {
        poolConnectionFactory.setConnectionFactory(connectionFactory);
        poolConnectionFactory.setMaxConnections(config.maxConnections());
        poolConnectionFactory.setConnectionIdleTimeout(config.connectionIdleTimeout());
        poolConnectionFactory.setConnectionCheckInterval(config.connectionCheckInterval());
        poolConnectionFactory.setUseProviderJMSContext(config.useProviderJMSContext());

        poolConnectionFactory.setMaxSessionsPerConnection(config.maxSessionsPerConnection());
        poolConnectionFactory.setBlockIfSessionPoolIsFull(config.blockIfSessionPoolIsFull());
        poolConnectionFactory.setBlockIfSessionPoolIsFullTimeout(config.blockIfSessionPoolIsFullTimeout());
        poolConnectionFactory.setUseAnonymousProducers(config.useAnonymousProducers());
    }
}
