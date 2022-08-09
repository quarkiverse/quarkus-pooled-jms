package io.quarkiverse.messaginghub.pooled.jms;

import javax.jms.ConnectionFactory;
import javax.transaction.TransactionManager;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.messaginghub.pooled.jms.JmsPoolXAConnectionFactory;

import io.quarkus.arc.Arc;
import io.quarkus.artemis.jms.runtime.ArtemisJmsWrapper;

public class PooledJmsWrapper implements ArtemisJmsWrapper {
    private boolean transaction;
    private PooledJmsRuntimeConfig config;

    public PooledJmsWrapper(boolean transaction, PooledJmsRuntimeConfig config) {
        this.transaction = transaction;
        this.config = config;
    }

    @Override
    public ConnectionFactory wrapConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        if (!config.poolingEnabled) {
            return connectionFactory;
        }

        if (transaction && config.xaEnabled) {
            return getXAConnectionFactory(connectionFactory);
        } else {
            return getConnectionFactory(connectionFactory);
        }
    }

    private ConnectionFactory getXAConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        TransactionManager transactionManager = Arc.container().instance(TransactionManager.class).get();

        JmsPoolXAConnectionFactory xaConnectionFactory = new JmsPoolXAConnectionFactory();
        xaConnectionFactory.setTransactionManager(transactionManager);
        configureConnectionFactory(xaConnectionFactory, connectionFactory);

        return xaConnectionFactory;
    }

    private ConnectionFactory getConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        JmsPoolConnectionFactory poolConnectionFactory = new JmsPoolConnectionFactory();
        configureConnectionFactory(poolConnectionFactory, connectionFactory);

        return poolConnectionFactory;
    }

    private void configureConnectionFactory(JmsPoolConnectionFactory poolConnectionFactory,
            ActiveMQConnectionFactory connectionFactory) {
        poolConnectionFactory.setConnectionFactory(connectionFactory);
        poolConnectionFactory.setMaxConnections(config.maxConnections);
        poolConnectionFactory.setConnectionIdleTimeout(config.connectionIdleTimeout);
        poolConnectionFactory.setConnectionCheckInterval(config.connectionCheckInterval);
        poolConnectionFactory.setUseProviderJMSContext(config.useProviderJMSContext);

        poolConnectionFactory.setMaxSessionsPerConnection(config.maxSessionsPerConnection);
        poolConnectionFactory.setBlockIfSessionPoolIsFull(config.blockIfSessionPoolIsFull);
        poolConnectionFactory.setBlockIfSessionPoolIsFullTimeout(config.blockIfSessionPoolIsFullTimeout);
        poolConnectionFactory.setUseAnonymousProducers(config.useAnonymousProducers);
    }
}
