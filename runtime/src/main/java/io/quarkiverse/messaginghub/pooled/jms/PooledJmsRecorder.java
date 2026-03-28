package io.quarkiverse.messaginghub.pooled.jms;

import java.util.function.Function;

import jakarta.jms.ConnectionFactory;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class PooledJmsRecorder {
    private final RuntimeValue<PooledJmsRuntimeConfig> pooledJmsRuntimeConfig;

    private volatile PooledJmsWrapper spiWrapper;

    public PooledJmsRecorder(RuntimeValue<PooledJmsRuntimeConfig> config) {
        this.pooledJmsRuntimeConfig = config;
    }

    public Function<ConnectionFactory, Object> getWrapper(boolean transaction) {
        return cf -> {
            if (spiWrapper == null) {
                spiWrapper = new PooledJmsWrapper(transaction, pooledJmsRuntimeConfig.getValue());
            }
            return spiWrapper.wrapConnectionFactory(cf);
        };
    }

    public RuntimeValue<PooledJmsWrapper> getPooledJmsWrapper(boolean transaction) {
        return new RuntimeValue<>(new PooledJmsWrapper(transaction, pooledJmsRuntimeConfig.getValue()));
    }

    public void reconfigureNamedConnectionFactories(boolean transaction) {
        PooledJmsWrapper wrapper = spiWrapper;
        if (wrapper == null) {
            wrapper = new PooledJmsWrapper(transaction, pooledJmsRuntimeConfig.getValue());
        }
        new PooledJmsNamedConfigReconfigurer(wrapper).reconfigure();
    }
}
