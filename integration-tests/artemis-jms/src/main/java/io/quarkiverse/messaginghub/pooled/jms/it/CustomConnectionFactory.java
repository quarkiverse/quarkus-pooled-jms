package io.quarkiverse.messaginghub.pooled.jms.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.jms.ConnectionFactory;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkiverse.messaginghub.pooled.jms.PooledJmsWrapper;
import io.quarkus.arc.properties.UnlessBuildProperty;

public class CustomConnectionFactory {

    @Produces
    @UnlessBuildProperty(name = "quarkus.artemis.enabled", stringValue = "true")
    @ApplicationScoped
    public ConnectionFactory createConnectionFactory(PooledJmsWrapper wrapper) {
        String url = ConfigProvider.getConfig().getValue("artemis.custom.url", String.class);
        return wrapper.wrapConnectionFactory(new ActiveMQConnectionFactory(url));
    }
}
