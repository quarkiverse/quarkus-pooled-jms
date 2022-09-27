package io.quarkiverse.messaginghub.pooled.jms;

import javax.jms.ConnectionFactory;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.jboss.narayana.jta.jms.JmsXAResourceRecoveryHelper;
import org.jboss.tm.XAResourceRecovery;
import org.jboss.tm.XAResourceRecoveryRegistry;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.messaginghub.pooled.jms.JmsPoolXAConnectionFactory;

import io.quarkus.arc.Arc;
import io.quarkus.artemis.jms.runtime.ArtemisJmsWrapper;
import io.quarkus.narayana.jta.runtime.TransactionManagerConfiguration;

public class PooledJmsWrapper implements ArtemisJmsWrapper {
    private boolean transaction;
    private PooledJmsRuntimeConfig pooledJmsRuntimeConfig;

    private TransactionManagerConfiguration transactionConfig;

    public PooledJmsWrapper(boolean transaction, PooledJmsRuntimeConfig pooledJmsRuntimeConfig,
            TransactionManagerConfiguration transactionConfig) {
        this.transaction = transaction;
        this.pooledJmsRuntimeConfig = pooledJmsRuntimeConfig;
        this.transactionConfig = transactionConfig;
    }

    @Override
    public ConnectionFactory wrapConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        if (!pooledJmsRuntimeConfig.poolingEnabled) {
            return connectionFactory;
        }

        if (transaction && pooledJmsRuntimeConfig.xaEnabled) {
            return getXAConnectionFactory(connectionFactory);
        } else {
            return getConnectionFactory(connectionFactory);
        }
    }

    private ConnectionFactory getXAConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        TransactionManager transactionManager = Arc.container().instance(TransactionManager.class).get();

        JmsPoolXAConnectionFactory xaConnectionFactory = new JmsPoolXAConnectionFactory();
        xaConnectionFactory.setTransactionManager(transactionManager);
        pooledJmsRuntimeConfigureConnectionFactory(xaConnectionFactory, connectionFactory);

        XAResourceRecoveryRegistry xaResourceRecoveryRegistry = Arc.container().instance(XAResourceRecoveryRegistry.class)
                .get();
        if (xaResourceRecoveryRegistry != null && transactionConfig.enableRecovery) {
            JmsXAResourceRecoveryHelper recoveryHelper = new JmsXAResourceRecoveryHelper(xaConnectionFactory);
            xaResourceRecoveryRegistry.addXAResourceRecovery(new XAResourceRecovery() {
                @Override
                public XAResource[] getXAResources() {
                    return recoveryHelper.getXAResources();
                }
            });
        }

        return xaConnectionFactory;
    }

    private ConnectionFactory getConnectionFactory(ActiveMQConnectionFactory connectionFactory) {
        JmsPoolConnectionFactory poolConnectionFactory = new JmsPoolConnectionFactory();
        pooledJmsRuntimeConfigureConnectionFactory(poolConnectionFactory, connectionFactory);

        return poolConnectionFactory;
    }

    private void pooledJmsRuntimeConfigureConnectionFactory(JmsPoolConnectionFactory poolConnectionFactory,
            ActiveMQConnectionFactory connectionFactory) {
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
