package io.quarkiverse.messaginghub.pooled.jms;

import io.quarkus.artemis.jms.runtime.ArtemisJmsWrapper;
import io.quarkus.narayana.jta.runtime.TransactionManagerConfiguration;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class PooledJmsRecorder {
    private PooledJmsRuntimeConfig pooledJmsRuntimeConfig;

    private TransactionManagerConfiguration transactionConfig;

    public PooledJmsRecorder(PooledJmsRuntimeConfig config, TransactionManagerConfiguration transactionConfig) {
        this.pooledJmsRuntimeConfig = config;
        this.transactionConfig = transactionConfig;
    }

    public ArtemisJmsWrapper getWrapper(boolean transaction) {
        return new PooledJmsWrapper(transaction, pooledJmsRuntimeConfig, transactionConfig);
    }
}
