package io.quarkiverse.messaginghub.pooled.jms;

public enum TransactionIntegration {
    /**
     * Integrate JMS Session with Quarkus TransactionManger and force transacted=true
     */
    ENABLED,

    /**
     * Integrate JMS Session with Quarkus TransactionManager and enable XA support and forcetransacted=false
     */
    XA,

    /**
     * Disable integrate with Quarkus TransactionManager
     */
    DISABLED
}
