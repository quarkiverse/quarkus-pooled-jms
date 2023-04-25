package io.quarkiverse.messaginghub.pooled.jms.graal;

import java.util.function.BooleanSupplier;

import io.quarkiverse.messaginghub.pooled.jms.transaction.LocalTransactionSupport;
import io.quarkiverse.messaginghub.pooled.jms.transaction.XATransactionSupport;

public final class TransactionManagerOrRecoveryRegistryMissing implements BooleanSupplier {

    @Override
    public boolean getAsBoolean() {
        try {
            Class.forName(LocalTransactionSupport.TRANSACTION_MANAGER_CLASSNAME);
            Class.forName(XATransactionSupport.XA_RECOVERY_REGISTRY_CLASSNAME);
            Class.forName(XATransactionSupport.JMS_XA_RESOURCE_HELPER_CLASSNAME);

            return false;
        } catch (ClassNotFoundException e) {
            return true;
        }
    }
}