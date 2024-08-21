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

    public ConnectionFactory wrapConnectionFactory(ConnectionFactory connectionFactory) {
        if (!pooledJmsRuntimeConfig.poolingEnabled()) {
            return connectionFactory;
        }

        if (transaction && pooledJmsRuntimeConfig.transaction().equals(TransactionIntegration.XA)) {
            if (XATransactionSupport.isEnabled()) {
                JmsPoolConnectionFactory cf = XATransactionSupport.getXAConnectionFactory(connectionFactory,
                        pooledJmsRuntimeConfig);
                poolConnectionFactories.add(cf);
                return cf;
            }

            throw new IllegalStateException("XA Transaction support is not available");
        } else if (transaction && pooledJmsRuntimeConfig.transaction().equals(TransactionIntegration.ENABLED)) {
            if (LocalTransactionSupport.isEnabled()) {
                JmsPoolConnectionFactory cf = LocalTransactionSupport.getLocalTransactionConnectionFactory(connectionFactory,
                        pooledJmsRuntimeConfig);
                poolConnectionFactories.add(cf);
                return cf;
            }

            throw new IllegalStateException("Local TransactionManager support is not available");
        } else {
            JmsPoolConnectionFactory cf = getConnectionFactory(connectionFactory);
            poolConnectionFactories.add(cf);
            return cf;
        }
    }

    public void clearAll() {
        for (JmsPoolConnectionFactory cf : poolConnectionFactories) {
            cf.clear();
        }
    }

    private JmsPoolConnectionFactory getConnectionFactory(ConnectionFactory connectionFactory) {
        JmsPoolConnectionFactory poolConnectionFactory = new JmsPoolConnectionFactory();
        pooledJmsRuntimeConfigureConnectionFactory(poolConnectionFactory, connectionFactory, pooledJmsRuntimeConfig);

        return poolConnectionFactory;
    }

    public static void pooledJmsRuntimeConfigureConnectionFactory(JmsPoolConnectionFactory poolConnectionFactory,
            ConnectionFactory connectionFactory, PooledJmsRuntimeConfig pooledJmsRuntimeConfig) {
        poolConnectionFactory.setConnectionFactory(connectionFactory);
        poolConnectionFactory.setMaxConnections(pooledJmsRuntimeConfig.maxConnections());
        poolConnectionFactory.setConnectionIdleTimeout(pooledJmsRuntimeConfig.connectionIdleTimeout());
        poolConnectionFactory.setConnectionCheckInterval(pooledJmsRuntimeConfig.connectionCheckInterval());
        poolConnectionFactory.setUseProviderJMSContext(pooledJmsRuntimeConfig.useProviderJMSContext());

        poolConnectionFactory.setMaxSessionsPerConnection(pooledJmsRuntimeConfig.maxSessionsPerConnection());
        poolConnectionFactory.setBlockIfSessionPoolIsFull(pooledJmsRuntimeConfig.blockIfSessionPoolIsFull());
        poolConnectionFactory.setBlockIfSessionPoolIsFullTimeout(pooledJmsRuntimeConfig.blockIfSessionPoolIsFullTimeout());
        poolConnectionFactory.setUseAnonymousProducers(pooledJmsRuntimeConfig.useAnonymousProducers());
    }
}
