package io.quarkiverse.messaginghub.pooled.jms.it;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.ConnectionFactory;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;

import io.quarkiverse.messaginghub.pooled.jms.DelegatingJmsPoolConnectionFactory;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ClientProxy;
import io.quarkus.arc.InstanceHandle;
import io.smallrye.common.annotation.Identifier;

@Path("/named-pooled-jms")
@ApplicationScoped
public class NamedPooledJmsResource {

    @GET
    @Path("/info/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public String info(@PathParam("name") String name) {
        ConnectionFactory cf;
        if ("default".equals(name)) {
            InstanceHandle<ConnectionFactory> handle = Arc.container().instance(ConnectionFactory.class);
            if (!handle.isAvailable()) {
                return "{\"error\":\"default ConnectionFactory not available\"}";
            }
            cf = handle.get();
        } else {
            InstanceHandle<ConnectionFactory> handle = Arc.container().instance(ConnectionFactory.class,
                    Identifier.Literal.of(name));
            if (!handle.isAvailable()) {
                return "{\"error\":\"" + name + " ConnectionFactory not available\"}";
            }
            cf = handle.get();
        }
        return poolInfo(cf);
    }

    private String poolInfo(ConnectionFactory cf) {
        // Unwrap CDI client proxy if present
        if (cf instanceof ClientProxy) {
            cf = (ConnectionFactory) ClientProxy.unwrap(cf);
        }
        if (cf instanceof DelegatingJmsPoolConnectionFactory) {
            DelegatingJmsPoolConnectionFactory delegating = (DelegatingJmsPoolConnectionFactory) cf;
            if (delegating.isPassthrough()) {
                return "{\"pooled\":false}";
            }
            JmsPoolConnectionFactory pool = delegating.getDelegate();
            return String.format(
                    "{\"maxConnections\":%d,\"maxSessionsPerConnection\":%d,\"useAnonymousProducers\":%b,\"poolType\":\"%s\"}",
                    pool.getMaxConnections(),
                    pool.getMaxSessionsPerConnection(),
                    pool.isUseAnonymousProducers(),
                    pool.getClass().getSimpleName());
        }
        if (cf instanceof JmsPoolConnectionFactory) {
            JmsPoolConnectionFactory pool = (JmsPoolConnectionFactory) cf;
            return String.format(
                    "{\"maxConnections\":%d,\"maxSessionsPerConnection\":%d,\"useAnonymousProducers\":%b,\"poolType\":\"%s\"}",
                    pool.getMaxConnections(),
                    pool.getMaxSessionsPerConnection(),
                    pool.isUseAnonymousProducers(),
                    pool.getClass().getSimpleName());
        }
        return "{\"pooled\":false}";
    }
}
