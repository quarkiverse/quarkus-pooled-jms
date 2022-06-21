package io.quarkiverse.messaginghub.pooled.jms;

import io.quarkus.artemis.jms.runtime.ArtemisJmsWrapper;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class PooledJmsRecorder {
    private PooledJmsRuntimeConfig config;

    public PooledJmsRecorder(PooledJmsRuntimeConfig config) {
        this.config = config;
    }

    public ArtemisJmsWrapper getWrapper(boolean transaction) {
        return new PooledJmsWrapper(transaction, config);
    }
}
