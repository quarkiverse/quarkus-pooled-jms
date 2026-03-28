package io.quarkiverse.messaginghub.pooled.jms;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import jakarta.jms.ConnectionFactory;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;

import io.quarkiverse.messaginghub.pooled.jms.transaction.LocalTransactionSupport;
import io.quarkiverse.messaginghub.pooled.jms.transaction.XATransactionSupport;

public class PooledJmsWrapper {
    private static final Logger LOG = Logger.getLogger(PooledJmsWrapper.class);

    private boolean transaction;
    private PooledJmsRuntimeConfig pooledJmsRuntimeConfig;

    private static List<JmsPoolConnectionFactory> poolConnectionFactories = new ArrayList<>();

    public PooledJmsWrapper(boolean transaction, PooledJmsRuntimeConfig pooledJmsRuntimeConfig) {
        this.transaction = transaction;
        this.pooledJmsRuntimeConfig = pooledJmsRuntimeConfig;
    }

    /**
     * Wrap the given connection factory, automatically resolving the pool configuration name
     * by matching the connection factory's URL against known artemis configuration URLs.
     * Falls back to the default pool configuration if no match is found.
     *
     * @param connectionFactory the connection factory to wrap
     * @return the wrapped (pooled) connection factory
     */
    public ConnectionFactory wrapConnectionFactory(ConnectionFactory connectionFactory) {
        String name = resolveConnectionFactoryName(connectionFactory);
        return wrapConnectionFactory(name, connectionFactory);
    }

    /**
     * Wrap the given connection factory using the named pool configuration.
     * If no configuration is found for the given name, the default configuration is used.
     *
     * @param name the configuration name (e.g. the connection factory identifier)
     * @param connectionFactory the connection factory to wrap
     * @return the wrapped (pooled) connection factory
     */
    public ConnectionFactory wrapConnectionFactory(String name, ConnectionFactory connectionFactory) {
        PooledJmsPoolConfig config = getConfigForName(name);

        if (!config.poolingEnabled()) {
            return connectionFactory;
        }

        if (transaction && config.transaction().equals(TransactionIntegration.XA)) {
            if (XATransactionSupport.isEnabled()) {
                JmsPoolConnectionFactory cf = XATransactionSupport.getXAConnectionFactory(connectionFactory, config);
                poolConnectionFactories.add(cf);
                return cf;
            }

            throw new IllegalStateException("XA Transaction support is not available");
        } else if (transaction && config.transaction().equals(TransactionIntegration.ENABLED)) {
            if (LocalTransactionSupport.isEnabled()) {
                JmsPoolConnectionFactory cf = LocalTransactionSupport.getLocalTransactionConnectionFactory(connectionFactory,
                        config);
                poolConnectionFactories.add(cf);
                return cf;
            }

            throw new IllegalStateException("Local TransactionManager support is not available");
        } else {
            JmsPoolConnectionFactory cf = getConnectionFactory(connectionFactory, config);
            poolConnectionFactories.add(cf);
            return cf;
        }
    }

    public void clearAll() {
        for (JmsPoolConnectionFactory cf : poolConnectionFactories) {
            cf.clear();
        }
    }

    private PooledJmsPoolConfig getConfigForName(String name) {
        return pooledJmsRuntimeConfig.connectionFactories().get(name);
    }

    private JmsPoolConnectionFactory getConnectionFactory(ConnectionFactory connectionFactory,
            PooledJmsPoolConfig config) {
        JmsPoolConnectionFactory poolConnectionFactory = new JmsPoolConnectionFactory();
        pooledJmsRuntimeConfigureConnectionFactory(poolConnectionFactory, connectionFactory, config);

        return poolConnectionFactory;
    }

    public static void pooledJmsRuntimeConfigureConnectionFactory(JmsPoolConnectionFactory poolConnectionFactory,
            ConnectionFactory connectionFactory, PooledJmsPoolConfig config) {
        poolConnectionFactory.setConnectionFactory(connectionFactory);
        poolConnectionFactory.setMaxConnections(config.maxConnections());
        poolConnectionFactory.setConnectionIdleTimeout(config.connectionIdleTimeout());
        poolConnectionFactory.setConnectionCheckInterval(config.connectionCheckInterval());
        poolConnectionFactory.setUseProviderJMSContext(config.useProviderJMSContext());

        poolConnectionFactory.setMaxSessionsPerConnection(config.maxSessionsPerConnection());
        poolConnectionFactory.setBlockIfSessionPoolIsFull(config.blockIfSessionPoolIsFull());
        poolConnectionFactory.setBlockIfSessionPoolIsFullTimeout(config.blockIfSessionPoolIsFullTimeout());
        poolConnectionFactory.setUseAnonymousProducers(config.useAnonymousProducers());
    }

    /**
     * Resolves the pooled-jms configuration name for a given ConnectionFactory by matching
     * its URL against the artemis configuration URLs.
     * Falls back to the default configuration if no match is found.
     */
    private String resolveConnectionFactoryName(ConnectionFactory cf) {
        Map<String, PooledJmsPoolConfig> configs = pooledJmsRuntimeConfig.connectionFactories();

        // If there are no named configs (only default), skip URL matching
        Set<String> namedKeys = configs.keySet();
        if (namedKeys.size() <= 1) {
            return PooledJmsRuntimeConfig.DEFAULT_CONNECTION_FACTORY_NAME;
        }

        // Try to get the URL from the ConnectionFactory via reflection
        String cfUrl = getConnectionFactoryUrl(cf);
        if (cfUrl == null) {
            return PooledJmsRuntimeConfig.DEFAULT_CONNECTION_FACTORY_NAME;
        }

        // Match against artemis config URLs for each named key
        var mpConfig = ConfigProvider.getConfig();
        for (String name : namedKeys) {
            if (PooledJmsRuntimeConfig.DEFAULT_CONNECTION_FACTORY_NAME.equals(name)) {
                continue;
            }
            try {
                // Try reading the artemis URL for this named config
                String artemisUrl = mpConfig.getValue("quarkus.artemis.\"" + name + "\".url", String.class);
                if (urlsMatch(cfUrl, artemisUrl)) {
                    LOG.debugf("Matched ConnectionFactory URL to pooled-jms config name '%s'", name);
                    return name;
                }
            } catch (NoSuchElementException e) {
                // No artemis URL configured for this name, skip
            }
        }

        return PooledJmsRuntimeConfig.DEFAULT_CONNECTION_FACTORY_NAME;
    }

    /**
     * Extracts the broker URL from a ConnectionFactory using reflection.
     * This avoids a hard compile-time dependency on ActiveMQConnectionFactory.
     */
    private static String getConnectionFactoryUrl(ConnectionFactory cf) {
        try {
            Method toUriMethod = cf.getClass().getMethod("toURI");
            URI uri = (URI) toUriMethod.invoke(cf);
            return uri.toString();
        } catch (Exception e) {
            LOG.debugf("Unable to extract URL from ConnectionFactory: %s", e.getMessage());
            return null;
        }
    }

    /**
     * Checks if a ConnectionFactory URI matches an artemis config URL.
     * The CF URI (from toURI()) may contain additional parameters, so we check
     * if the artemis URL appears within the CF URI.
     */
    private static boolean urlsMatch(String cfUri, String artemisUrl) {
        return cfUri.contains(artemisUrl) || artemisUrl.contains(cfUri);
    }
}
