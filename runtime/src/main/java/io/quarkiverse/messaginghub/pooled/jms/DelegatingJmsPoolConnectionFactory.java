package io.quarkiverse.messaginghub.pooled.jms;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.QueueConnection;
import jakarta.jms.TopicConnection;

import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;

/**
 * A delegating wrapper around {@link JmsPoolConnectionFactory} that defers pool creation.
 * The SPI wrapper creates instances of this class storing only the raw {@link ConnectionFactory},
 * without creating any pool. At startup, the correct pool (including the proper transaction type)
 * is created and set as the delegate.
 * <p>
 * Extends {@link JmsPoolConnectionFactory} so that {@code instanceof} checks still work.
 */
public class DelegatingJmsPoolConnectionFactory extends JmsPoolConnectionFactory {

    private volatile JmsPoolConnectionFactory delegate;
    private final ConnectionFactory wrappedConnectionFactory;
    private volatile boolean passthrough;

    public DelegatingJmsPoolConnectionFactory(ConnectionFactory wrappedConnectionFactory) {
        this.wrappedConnectionFactory = wrappedConnectionFactory;
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
        if (passthrough) {
            return wrappedConnectionFactory.createConnection();
        }
        return delegate().createConnection();
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        if (passthrough) {
            return wrappedConnectionFactory.createConnection(userName, password);
        }
        return delegate().createConnection(userName, password);
    }

    @Override
    public QueueConnection createQueueConnection() throws JMSException {
        if (passthrough) {
            return ((jakarta.jms.QueueConnectionFactory) wrappedConnectionFactory).createQueueConnection();
        }
        return delegate().createQueueConnection();
    }

    @Override
    public QueueConnection createQueueConnection(String userName, String password) throws JMSException {
        if (passthrough) {
            return ((jakarta.jms.QueueConnectionFactory) wrappedConnectionFactory).createQueueConnection(userName,
                    password);
        }
        return delegate().createQueueConnection(userName, password);
    }

    @Override
    public TopicConnection createTopicConnection() throws JMSException {
        if (passthrough) {
            return ((jakarta.jms.TopicConnectionFactory) wrappedConnectionFactory).createTopicConnection();
        }
        return delegate().createTopicConnection();
    }

    @Override
    public TopicConnection createTopicConnection(String userName, String password) throws JMSException {
        if (passthrough) {
            return ((jakarta.jms.TopicConnectionFactory) wrappedConnectionFactory).createTopicConnection(userName,
                    password);
        }
        return delegate().createTopicConnection(userName, password);
    }

    // --- JMSContext creation ---

    @Override
    public JMSContext createContext() {
        if (passthrough) {
            return wrappedConnectionFactory.createContext();
        }
        return delegate().createContext();
    }

    @Override
    public JMSContext createContext(int sessionMode) {
        if (passthrough) {
            return wrappedConnectionFactory.createContext(sessionMode);
        }
        return delegate().createContext(sessionMode);
    }

    @Override
    public JMSContext createContext(String userName, String password) {
        if (passthrough) {
            return wrappedConnectionFactory.createContext(userName, password);
        }
        return delegate().createContext(userName, password);
    }

    @Override
    public JMSContext createContext(String userName, String password, int sessionMode) {
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
