package io.quarkiverse.messaginghub.pooled.jms.it.ibmmq;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "ibmmq")
public interface IbmMqConfig {
    String hostname();

    int port();

    String channel();

    String queueManager();

    String user();

    String password();
}
