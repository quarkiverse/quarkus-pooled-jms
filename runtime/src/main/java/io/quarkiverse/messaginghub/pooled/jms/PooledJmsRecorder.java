package io.quarkiverse.messaginghub.pooled.jms;

import java.util.function.Function;

import javax.jms.ConnectionFactory;

import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class PooledJmsRecorder {
    private PooledJmsRuntimeConfig pooledJmsRuntimeConfig;

    public PooledJmsRecorder(PooledJmsRuntimeConfig config) {
        this.pooledJmsRuntimeConfig = config;
    }

    public Function<ConnectionFactory, Object> getWrapper(boolean transaction) {
        return cf -> {
            PooledJmsWrapper wrapper = new PooledJmsWrapper(transaction, pooledJmsRuntimeConfig);
            return wrapper.wrapConnectionFactory(cf);
        };
    }
}
