package io.quarkiverse.messaginghub.pooled.jms;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.QueueConnection;
import jakarta.jms.TopicConnection;

import org.jboss.logging.Logger;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;

/**
 * A delegating wrapper around {@link JmsPoolConnectionFactory} that defers pool creation.
 * The SPI wrapper creates instances of this class storing only the raw {@link ConnectionFactory},
 * without creating any pool. At startup, the correct pool (including the proper transaction type)
 * is created and set as the delegate.
 * <p>
 * If the delegate has not been set by the initializer (e.g. for {@code @Dependent}-scoped
 * producer beans), lazy initialization is performed on first use using the default pool config.
 * <p>
 * Extends {@link JmsPoolConnectionFactory} so that {@code instanceof} checks still work.
 */
public class DelegatingJmsPoolConnectionFactory extends JmsPoolConnectionFactory {

    private static final Logger LOG = Logger.getLogger(DelegatingJmsPoolConnectionFactory.class);

    private volatile JmsPoolConnectionFactory delegate;
    private final ConnectionFactory wrappedConnectionFactory;
    private final PooledJmsWrapper wrapper;
    private volatile boolean passthrough;

    public DelegatingJmsPoolConnectionFactory(ConnectionFactory wrappedConnectionFactory) {
        this(wrappedConnectionFactory, null);
    }

    DelegatingJmsPoolConnectionFactory(ConnectionFactory wrappedConnectionFactory, PooledJmsWrapper wrapper) {
        this.wrappedConnectionFactory = wrappedConnectionFactory;
        this.wrapper = wrapper;
    }

    public void setDelegate(JmsPoolConnectionFactory delegate) {
        this.delegate = delegate;
    }

    /**
     * Enable passthrough mode. In this mode, connection and context creation
     * methods delegate directly to the raw {@link ConnectionFactory} without pooling.
     */
    public void setPassthrough() {
        this.passthrough = true;
    }

    public boolean isPassthrough() {
        return passthrough;
    }

    public JmsPoolConnectionFactory getDelegate() {
        return delegate;
    }

    public ConnectionFactory getWrappedConnectionFactory() {
        return wrappedConnectionFactory;
    }

    /**
     * Ensures this factory is initialized, either via the startup initializer or lazily.
     * After this call, either {@code delegate} is set or {@code passthrough} is true.
     */
    private void ensureInitialized() {
        if (delegate != null || passthrough) {
            return;
        }
        if (wrapper != null) {
            synchronized (this) {
                if (delegate != null || passthrough) {
                    return;
                }
                String configName = PooledJmsRuntimeConfig.DEFAULT_CONNECTION_FACTORY_NAME;
                if (!wrapper.isPoolingEnabled(configName)) {
                    LOG.debugf("Lazy init: pooling disabled for %s, using passthrough",
                            wrappedConnectionFactory.getClass().getSimpleName());
                    passthrough = true;
                } else {
                    JmsPoolConnectionFactory pool = wrapper.createPool(configName, wrappedConnectionFactory);
                    LOG.debugf("Lazy init: created pool for %s, pool type %s",
                            wrappedConnectionFactory.getClass().getSimpleName(),
                            pool.getClass().getSimpleName());
                    delegate = pool;
                }
            }
            return;
        }
        throw new IllegalStateException("Pooled JMS ConnectionFactory has not been initialized yet");
    }

    private JmsPoolConnectionFactory delegate() {
        JmsPoolConnectionFactory d = delegate;
        if (d == null) {
            throw new IllegalStateException("Pooled JMS ConnectionFactory has not been initialized yet");
        }
        return d;
    }

    // --- Connection creation ---

    @Override
    public Connection createConnection() throws JMSException {
        ensureInitialized();
        if (passthrough) {
            return wrappedConnectionFactory.createConnection();
        }
        return delegate().createConnection();
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        ensureInitialized();
        if (passthrough) {
            return wrappedConnectionFactory.createConnection(userName, password);
        }
        return delegate().createConnection(userName, password);
    }

    @Override
    public QueueConnection createQueueConnection() throws JMSException {
        ensureInitialized();
        if (passthrough) {
            return ((jakarta.jms.QueueConnectionFactory) wrappedConnectionFactory).createQueueConnection();
        }
        return delegate().createQueueConnection();
    }

    @Override
    public QueueConnection createQueueConnection(String userName, String password) throws JMSException {
        ensureInitialized();
        if (passthrough) {
            return ((jakarta.jms.QueueConnectionFactory) wrappedConnectionFactory).createQueueConnection(userName,
                    password);
        }
        return delegate().createQueueConnection(userName, password);
    }

    @Override
    public TopicConnection createTopicConnection() throws JMSException {
        ensureInitialized();
        if (passthrough) {
            return ((jakarta.jms.TopicConnectionFactory) wrappedConnectionFactory).createTopicConnection();
        }
        return delegate().createTopicConnection();
    }

    @Override
    public TopicConnection createTopicConnection(String userName, String password) throws JMSException {
        ensureInitialized();
        if (passthrough) {
            return ((jakarta.jms.TopicConnectionFactory) wrappedConnectionFactory).createTopicConnection(userName,
                    password);
        }
        return delegate().createTopicConnection(userName, password);
    }

    // --- JMSContext creation ---

    @Override
    public JMSContext createContext() {
        ensureInitialized();
        if (passthrough) {
            return wrappedConnectionFactory.createContext();
        }
        return delegate().createContext();
    }

    @Override
    public JMSContext createContext(int sessionMode) {
        ensureInitialized();
        if (passthrough) {
            return wrappedConnectionFactory.createContext(sessionMode);
        }
        return delegate().createContext(sessionMode);
    }

    @Override
    public JMSContext createContext(String userName, String password) {
        ensureInitialized();
        if (passthrough) {
            return wrappedConnectionFactory.createContext(userName, password);
        }
        return delegate().createContext(userName, password);
    }

    @Override
    public JMSContext createContext(String userName, String password, int sessionMode) {
        ensureInitialized();
        if (passthrough) {
            return wrappedConnectionFactory.createContext(userName, password, sessionMode);
        }
        return delegate().createContext(userName, password, sessionMode);
    }

    // --- Lifecycle ---

    @Override
    public void start() {
        if (!passthrough) {
            delegate().start();
        }
    }

    @Override
    public void stop() {
        if (!passthrough) {
            delegate().stop();
        }
    }

    @Override
    public void clear() {
        if (!passthrough) {
            delegate().clear();
        }
    }

    @Override
    public void initConnectionsPool() {
        if (!passthrough) {
            delegate().initConnectionsPool();
        }
    }

    // --- Configuration getters (delegate to the actual pool) ---

    @Override
    public Object getConnectionFactory() {
        if (passthrough) {
            return wrappedConnectionFactory;
        }
        return delegate().getConnectionFactory();
    }

    @Override
    public int getMaxConnections() {
        return delegate().getMaxConnections();
    }

    @Override
    public int getMaxSessionsPerConnection() {
        return delegate().getMaxSessionsPerConnection();
    }

    @Override
    public int getMaxIdleSessionsPerConnection() {
        return delegate().getMaxIdleSessionsPerConnection();
    }

    @Override
    public int getConnectionIdleTimeout() {
        return delegate().getConnectionIdleTimeout();
    }

    @Override
    public long getConnectionCheckInterval() {
        return delegate().getConnectionCheckInterval();
    }

    @Override
    public boolean isBlockIfSessionPoolIsFull() {
        return delegate().isBlockIfSessionPoolIsFull();
    }

    @Override
    public long getBlockIfSessionPoolIsFullTimeout() {
        return delegate().getBlockIfSessionPoolIsFullTimeout();
    }

    @Override
    public boolean isUseAnonymousProducers() {
        return delegate().isUseAnonymousProducers();
    }

    @Override
    public int getExplicitProducerCacheSize() {
        return delegate().getExplicitProducerCacheSize();
    }

    @Override
    public boolean isUseProviderJMSContext() {
        return delegate().isUseProviderJMSContext();
    }

    @Override
    public boolean isFaultTolerantConnections() {
        return delegate().isFaultTolerantConnections();
    }

    @Override
    public int getNumConnections() {
        return delegate().getNumConnections();
    }

    // --- Configuration setters (delegate to the actual pool) ---

    @Override
    public void setConnectionFactory(Object connectionFactory) {
        delegate().setConnectionFactory(connectionFactory);
    }

    @Override
    public void setMaxConnections(int maxConnections) {
        delegate().setMaxConnections(maxConnections);
    }

    @Override
    public void setMaxSessionsPerConnection(int maxSessionsPerConnection) {
        delegate().setMaxSessionsPerConnection(maxSessionsPerConnection);
    }

    @Override
    public void setMaxIdleSessionsPerConnection(int maxIdleSessionsPerConnection) {
        delegate().setMaxIdleSessionsPerConnection(maxIdleSessionsPerConnection);
    }

    @Override
    public void setConnectionIdleTimeout(int connectionIdleTimeout) {
        delegate().setConnectionIdleTimeout(connectionIdleTimeout);
    }

    @Override
    public void setConnectionCheckInterval(long connectionCheckInterval) {
        delegate().setConnectionCheckInterval(connectionCheckInterval);
    }

    @Override
    public void setBlockIfSessionPoolIsFull(boolean block) {
        delegate().setBlockIfSessionPoolIsFull(block);
    }

    @Override
    public void setBlockIfSessionPoolIsFullTimeout(long blockIfSessionPoolIsFullTimeout) {
        delegate().setBlockIfSessionPoolIsFullTimeout(blockIfSessionPoolIsFullTimeout);
    }

    @Override
    public void setUseAnonymousProducers(boolean useAnonymousProducers) {
        delegate().setUseAnonymousProducers(useAnonymousProducers);
    }

    @Override
    public void setExplicitProducerCacheSize(int explicitProducerCacheSize) {
        delegate().setExplicitProducerCacheSize(explicitProducerCacheSize);
    }

    @Override
    public void setUseProviderJMSContext(boolean useProviderJMSContext) {
        delegate().setUseProviderJMSContext(useProviderJMSContext);
    }

    @Override
    public void setFaultTolerantConnections(boolean faultTolerantConnections) {
        delegate().setFaultTolerantConnections(faultTolerantConnections);
    }
}
