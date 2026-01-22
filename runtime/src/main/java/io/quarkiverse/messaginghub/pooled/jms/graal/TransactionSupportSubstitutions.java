package io.quarkiverse.messaginghub.pooled.jms.graal;

import jakarta.jms.ConnectionFactory;

import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

import io.quarkiverse.messaginghub.pooled.jms.PooledJmsRuntimeConfig;

final class TransactionSupportSubstitutions {
}

/**
 * Substitutions to disable requirement for TransactionManager classes when not needed/present.
 */
@TargetClass(className = "io.quarkiverse.messaginghub.pooled.jms.transaction.LocalTransactionSupport", onlyWith = TransactionManagerMissing.class)
final class Target_io_quarkiverse_messaginghub_pooled_jms_transaction_LocalTransactionSupport {
    @Substitute
    public static boolean isEnabled() {
        // Disable so Graal doesnt need to inspect the original class or need its imports/classes
        return false;
    }

    @Substitute
    public static JmsPoolConnectionFactory getLocalTransactionConnectionFactory(ConnectionFactory connectionFactory,
            PooledJmsRuntimeConfig pooledJmsRuntimeConfig) {
        throw new IllegalStateException("TransactionManager not present");
    }
}

/**
 * Substitutions to disable requirement for TransactionManager and XA Recovery related classes when not present/needed.
 */
@TargetClass(className = "io.quarkiverse.messaginghub.pooled.jms.transaction.XATransactionSupport", onlyWith = TransactionManagerOrRecoveryRegistryMissing.class)
final class Target_io_quarkiverse_messaginghub_pooled_jms_transaction_XATransactionSupport {
    @Substitute
    public static boolean isEnabled() {
        // Disable so Graal doesnt need to inspect the original class or need its imports/classes
        return false;
    }

    @Substitute
    public static JmsPoolConnectionFactory getXAConnectionFactory(ConnectionFactory connectionFactory,
            PooledJmsRuntimeConfig pooledJmsRuntimeConfig) {
        throw new IllegalStateException("XAResourceRecoveryRegistry not present");
    }
}
