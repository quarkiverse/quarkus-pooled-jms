package io.quarkiverse.messaginghub.pooled.jms.transaction;

import javax.jms.ConnectionFactory;
import javax.transaction.TransactionManager;

import io.quarkiverse.messaginghub.pooled.jms.JmsPoolLocalTransactionConnectionFactory;
import io.quarkiverse.messaginghub.pooled.jms.PooledJmsRuntimeConfig;
import io.quarkiverse.messaginghub.pooled.jms.PooledJmsWrapper;
import io.quarkus.arc.Arc;

/* Indirects use of classes that may not be present at runtime, allows
 * substitution in native builds to avoid using and inspecting this class
 */
public class LocalTransactionSupportIndirect {

    public static ConnectionFactory getLocalTransactionConnectionFactory(ConnectionFactory connectionFactory,
            PooledJmsRuntimeConfig pooledJmsRuntimeConfig) {
        TransactionManager transactionManager = Arc.container().instance(TransactionManager.class).get();

        JmsPoolLocalTransactionConnectionFactory poolLocalTransactionConnectionFactory = new JmsPoolLocalTransactionConnectionFactory();
        poolLocalTransactionConnectionFactory.setTransactionManager(transactionManager);
        PooledJmsWrapper.pooledJmsRuntimeConfigureConnectionFactory(poolLocalTransactionConnectionFactory, connectionFactory,
                pooledJmsRuntimeConfig);

        return poolLocalTransactionConnectionFactory;
    }

}
