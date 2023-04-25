package io.quarkiverse.messaginghub.pooled.jms;

import jakarta.jms.ConnectionFactory;

import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;

import io.quarkiverse.messaginghub.pooled.jms.transaction.LocalTransactionSupport;
import io.quarkiverse.messaginghub.pooled.jms.transaction.XATransactionSupport;

public class PooledJmsWrapper {
    private boolean transaction;
    private PooledJmsRuntimeConfig pooledJmsRuntimeConfig;

    public PooledJmsWrapper(boolean transaction, PooledJmsRuntimeConfig pooledJmsRuntimeConfig) {
        this.transaction = transaction;
        this.pooledJmsRuntimeConfig = pooledJmsRuntimeConfig;
    }

    public ConnectionFactory wrapConnectionFactory(ConnectionFactory connectionFactory) {
        if (!pooledJmsRuntimeConfig.poolingEnabled) {
            return connectionFactory;
        }

        if (transaction && pooledJmsRuntimeConfig.transaction.equals(TransactionIntegration.XA)) {
            if (XATransactionSupport.isEnabled()) {
                return XATransactionSupport.getXAConnectionFactory(connectionFactory, pooledJmsRuntimeConfig);
            }

            throw new IllegalStateException("XA Transaction support is not available");
        } else if (transaction && pooledJmsRuntimeConfig.transaction.equals(TransactionIntegration.ENABLED)) {
            if (LocalTransactionSupport.isEnabled()) {
                return LocalTransactionSupport.getLocalTransactionConnectionFactory(connectionFactory, pooledJmsRuntimeConfig);
            }

            throw new IllegalStateException("Local TransactionManager support is not available");
        } else {
            return getConnectionFactory(connectionFactory);
        }
    }

    private ConnectionFactory getConnectionFactory(ConnectionFactory connectionFactory) {
        JmsPoolConnectionFactory poolConnectionFactory = new JmsPoolConnectionFactory();
        pooledJmsRuntimeConfigureConnectionFactory(poolConnectionFactory, connectionFactory, pooledJmsRuntimeConfig);

        return poolConnectionFactory;
    }

    public static void pooledJmsRuntimeConfigureConnectionFactory(JmsPoolConnectionFactory poolConnectionFactory,
            ConnectionFactory connectionFactory, PooledJmsRuntimeConfig pooledJmsRuntimeConfig) {
        poolConnectionFactory.setConnectionFactory(connectionFactory);
        poolConnectionFactory.setMaxConnections(pooledJmsRuntimeConfig.maxConnections);
        poolConnectionFactory.setConnectionIdleTimeout(pooledJmsRuntimeConfig.connectionIdleTimeout);
        poolConnectionFactory.setConnectionCheckInterval(pooledJmsRuntimeConfig.connectionCheckInterval);
        poolConnectionFactory.setUseProviderJMSContext(pooledJmsRuntimeConfig.useProviderJMSContext);

        poolConnectionFactory.setMaxSessionsPerConnection(pooledJmsRuntimeConfig.maxSessionsPerConnection);
        poolConnectionFactory.setBlockIfSessionPoolIsFull(pooledJmsRuntimeConfig.blockIfSessionPoolIsFull);
        poolConnectionFactory.setBlockIfSessionPoolIsFullTimeout(pooledJmsRuntimeConfig.blockIfSessionPoolIsFullTimeout);
        poolConnectionFactory.setUseAnonymousProducers(pooledJmsRuntimeConfig.useAnonymousProducers);
    }
}
