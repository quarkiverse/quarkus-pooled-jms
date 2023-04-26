package io.quarkiverse.messaginghub.pooled.jms.transaction;

import javax.jms.ConnectionFactory;
import javax.transaction.TransactionManager;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.narayana.jta.jms.JmsXAResourceRecoveryHelper;
import org.jboss.tm.XAResourceRecoveryRegistry;
import org.messaginghub.pooled.jms.JmsPoolXAConnectionFactory;

import io.quarkiverse.messaginghub.pooled.jms.PooledJmsRuntimeConfig;
import io.quarkiverse.messaginghub.pooled.jms.PooledJmsWrapper;
import io.quarkus.arc.Arc;

/* Indirects use of classes that may not be present at runtime, allows
 * substitution in native builds to avoid using and inspecting this class
 */
public class XATransactionSupportIndirect {

    public static ConnectionFactory getXAConnectionFactory(ConnectionFactory connectionFactory,
            PooledJmsRuntimeConfig pooledJmsRuntimeConfig) {
        TransactionManager transactionManager = Arc.container().instance(TransactionManager.class).get();

        JmsPoolXAConnectionFactory xaConnectionFactory = new JmsPoolXAConnectionFactory();
        xaConnectionFactory.setTransactionManager(transactionManager);
        PooledJmsWrapper.pooledJmsRuntimeConfigureConnectionFactory(xaConnectionFactory, connectionFactory,
                pooledJmsRuntimeConfig);

        XAResourceRecoveryRegistry xaResourceRecoveryRegistry = Arc.container().instance(XAResourceRecoveryRegistry.class)
                .get();
        boolean recoveryEnable = ConfigProvider.getConfig().getValue("quarkus.transaction-manager.enable-recovery",
                Boolean.class);

        if (xaResourceRecoveryRegistry != null && recoveryEnable) {
            JmsXAResourceRecoveryHelper recoveryHelper = new JmsXAResourceRecoveryHelper(xaConnectionFactory);
            xaResourceRecoveryRegistry.addXAResourceRecovery(() -> recoveryHelper.getXAResources());
        }

        return xaConnectionFactory;
    }
}
