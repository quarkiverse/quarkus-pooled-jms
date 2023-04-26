package io.quarkiverse.messaginghub.pooled.jms.transaction;

import jakarta.jms.ConnectionFactory;

import io.quarkiverse.messaginghub.pooled.jms.PooledJmsRuntimeConfig;

public class LocalTransactionSupport {

    // Classes used by XATransactionSupportIndirect that can be inspected for
    public static final String TRANSACTION_MANAGER_CLASSNAME = "jakarta.transaction.TransactionManager";

    public static boolean isEnabled() {
        // Substitution point to allow disabling, prevent Graal inspecting unavailable classes
        return true;
    }

    public static ConnectionFactory getLocalTransactionConnectionFactory(ConnectionFactory connectionFactory,
            PooledJmsRuntimeConfig pooledJmsRuntimeConfig) {
        return LocalTransactionSupportIndirect.getLocalTransactionConnectionFactory(connectionFactory, pooledJmsRuntimeConfig);
    }

}
