package io.quarkiverse.messaginghub.pooled.jms.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.eclipse.microprofile.config.ConfigProvider;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;

import io.quarkiverse.messaginghub.pooled.jms.PooledJmsWrapper;
import io.quarkus.arc.ClientProxy;

@Path("/named-pooled-jms")
@ApplicationScoped
public class NamedPooledJmsResource {

    @Inject
    PooledJmsWrapper wrapper;

    @Inject
    ConnectionFactory defaultConnectionFactory;

    @GET
    @Path("/default-info")
    @Produces(MediaType.APPLICATION_JSON)
    public String defaultInfo() {
        return poolInfo(defaultConnectionFactory);
    }

    @GET
    @Path("/named-info")
    @Produces(MediaType.APPLICATION_JSON)
    public String namedInfo() {
        String url = ConfigProvider.getConfig().getValue("quarkus.artemis.url", String.class);
        ConnectionFactory cf = wrapper.wrapConnectionFactory("broker1",
                new ActiveMQConnectionFactory(url));
        return poolInfo(cf);
    }

    private String poolInfo(ConnectionFactory cf) {
        // Unwrap CDI client proxy if present
        if (cf instanceof ClientProxy) {
            cf = (ConnectionFactory) ClientProxy.unwrap(cf);
        }
        if (cf instanceof JmsPoolConnectionFactory) {
            JmsPoolConnectionFactory pool = (JmsPoolConnectionFactory) cf;
            return String.format(
                    "{\"maxConnections\":%d,\"maxSessionsPerConnection\":%d,\"useAnonymousProducers\":%b}",
                    pool.getMaxConnections(),
                    pool.getMaxSessionsPerConnection(),
                    pool.isUseAnonymousProducers());
        }
        return "{\"pooled\":false}";
    }
}
