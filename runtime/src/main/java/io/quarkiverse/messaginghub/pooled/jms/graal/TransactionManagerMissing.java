package io.quarkiverse.messaginghub.pooled.jms.graal;

import java.util.function.BooleanSupplier;

import io.quarkiverse.messaginghub.pooled.jms.transaction.LocalTransactionSupport;

public final class TransactionManagerMissing implements BooleanSupplier {

    @Override
    public boolean getAsBoolean() {
        try {
            Class.forName(LocalTransactionSupport.TRANSACTION_MANAGER_CLASSNAME);
            return false;
        } catch (ClassNotFoundException e) {
            return true;
        }
    }
}