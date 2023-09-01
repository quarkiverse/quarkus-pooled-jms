package io.quarkiverse.messaginghub.pooled.jms;

import java.util.function.Function;

import jakarta.jms.ConnectionFactory;

import io.quarkus.runtime.RuntimeValue;
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

    public RuntimeValue<PooledJmsWrapper> getPooledJmsWrapper(boolean transaction) {
        return new RuntimeValue<>(new PooledJmsWrapper(transaction, pooledJmsRuntimeConfig));
    }
}
