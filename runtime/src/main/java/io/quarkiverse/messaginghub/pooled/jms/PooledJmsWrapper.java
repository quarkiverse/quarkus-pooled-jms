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

    PooledJmsRuntimeConfig getPooledJmsRuntimeConfig() {
        return pooledJmsRuntimeConfig;
    }

    /**
     * Wrap the given connection factory in a {@link DelegatingJmsPoolConnectionFactory}
     * that defers pool creation to the startup phase. The actual pool (with the correct
     * transaction type and named configuration) is created later by
     * {@link PooledJmsConnectionFactoryInitializer}.
     *
     * @param connectionFactory the connection factory to wrap
     * @return the wrapped (delegating) connection factory
     */
    public ConnectionFactory wrapConnectionFactory(ConnectionFactory connectionFactory) {
        DelegatingJmsPoolConnectionFactory delegating = new DelegatingJmsPoolConnectionFactory(connectionFactory);
        poolConnectionFactories.add(delegating);
        return delegating;
    }

    /**
     * Create a new pool for the given connection factory using the named configuration.
     */
    JmsPoolConnectionFactory createPool(String name, ConnectionFactory connectionFactory) {
        PooledJmsPoolConfig config = getConfigForName(name);

        if (transaction && config.transaction().equals(TransactionIntegration.XA)) {
            if (XATransactionSupport.isEnabled()) {
                return XATransactionSupport.getXAConnectionFactory(connectionFactory, config);
            }
            throw new IllegalStateException("XA Transaction support is not available");
        } else if (transaction && config.transaction().equals(TransactionIntegration.ENABLED)) {
            if (LocalTransactionSupport.isEnabled()) {
                return LocalTransactionSupport.getLocalTransactionConnectionFactory(connectionFactory, config);
            }
            throw new IllegalStateException("Local TransactionManager support is not available");
        } else {
            return getConnectionFactory(connectionFactory, config);
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
