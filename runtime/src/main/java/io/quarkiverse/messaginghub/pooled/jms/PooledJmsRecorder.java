package io.quarkiverse.messaginghub.pooled.jms;

import io.quarkus.artemis.jms.runtime.ArtemisJmsWrapper;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class PooledJmsRecorder {
    private PooledJmsRuntimeConfig pooledJmsRuntimeConfig;

    public PooledJmsRecorder(PooledJmsRuntimeConfig config) {
        this.pooledJmsRuntimeConfig = config;
    }

    public ArtemisJmsWrapper getWrapper(boolean transaction) {
        return new PooledJmsWrapper(transaction, pooledJmsRuntimeConfig);
    }
}
