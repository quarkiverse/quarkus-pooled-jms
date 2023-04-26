package io.quarkiverse.messaginghub.pooled.jms.transaction;

import javax.jms.ConnectionFactory;

import io.quarkiverse.messaginghub.pooled.jms.PooledJmsRuntimeConfig;

public class XATransactionSupport {

    // Classes used by PooledJmsProcessor and/or XATransactionSupportIndirect that can be inspected for
    public static final String XA_RECOVERY_REGISTRY_CLASSNAME = "org.jboss.tm.XAResourceRecoveryRegistry";
    public static final String JMS_XA_RESOURCE_HELPER_CLASSNAME = "org.jboss.narayana.jta.jms.JmsXAResourceRecoveryHelper";

    public static boolean isEnabled() {
        // Substitution point to allow disabling, prevent Graal inspecting unavailable classes
        return true;
    }

    public static ConnectionFactory getXAConnectionFactory(ConnectionFactory connectionFactory,
            PooledJmsRuntimeConfig pooledJmsRuntimeConfig) {
        return XATransactionSupportIndirect.getXAConnectionFactory(connectionFactory, pooledJmsRuntimeConfig);
    }
}
