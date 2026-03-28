package io.quarkiverse.messaginghub.pooled.jms;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefaults;
import io.smallrye.config.WithParentName;
import io.smallrye.config.WithUnnamedKey;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus.pooled-jms")
public interface PooledJmsRuntimeConfig {

    String DEFAULT_CONNECTION_FACTORY_NAME = "<default>";

    /**
     * Pool configurations per connection factory name.
     * The default (unnamed) configuration applies to the default connection factory
     * and is also used as fallback when a named configuration is not found.
     * Named configurations are used with
     * {@link PooledJmsWrapper#wrapConnectionFactory(String, jakarta.jms.ConnectionFactory)}.
     */
    @WithParentName
    @WithDefaults
    @WithUnnamedKey(DEFAULT_CONNECTION_FACTORY_NAME)
    @ConfigDocMapKey("connection-factory-name")
    Map<String, PooledJmsPoolConfig> connectionFactories();
}
