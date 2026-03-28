package io.quarkiverse.messaginghub.pooled.jms.transaction;

import jakarta.jms.ConnectionFactory;

import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;

import io.quarkiverse.messaginghub.pooled.jms.PooledJmsPoolConfig;

public class LocalTransactionSupport {

    // Classes used by XATransactionSupportIndirect that can be inspected for
    public static final String TRANSACTION_MANAGER_CLASSNAME = "jakarta.transaction.TransactionManager";

    public static boolean isEnabled() {
        // Substitution point to allow disabling, prevent Graal inspecting unavailable classes
        return true;
    }

    public static JmsPoolConnectionFactory getLocalTransactionConnectionFactory(ConnectionFactory connectionFactory,
            PooledJmsPoolConfig config) {
        return LocalTransactionSupportIndirect.getLocalTransactionConnectionFactory(connectionFactory, config);
    }

}
