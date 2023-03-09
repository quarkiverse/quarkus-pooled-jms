package io.quarkiverse.messaginghub.pooled.jms.it;

import jakarta.enterprise.context.Dependent;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.eclipse.microprofile.config.ConfigProvider;

import io.quarkus.arc.properties.UnlessBuildProperty;

@Dependent
@UnlessBuildProperty(name = "quarkus.artemis.enabled", stringValue = "true")
public class CustomConnectionFactory extends ActiveMQConnectionFactory {
    public CustomConnectionFactory() {
        super(ConfigProvider.getConfig().getValue("artemis.custom.url", String.class));
    }
}
