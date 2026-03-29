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

    public DelegatingJmsPoolConnectionFactory(ConnectionFactory wrappedConnectionFactory) {
        this.wrappedConnectionFactory = wrappedConnectionFactory;
    }

    public void setDelegate(JmsPoolConnectionFactory delegate) {
        this.delegate = delegate;
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

    // --- Connection creation (delegates to the actual pool) ---

    @Override
    public Connection createConnection() throws JMSException {
        return delegate().createConnection();
    }

    @Override
    public Connection createConnection(String userName, String password) throws JMSException {
        return delegate().createConnection(userName, password);
    }

    @Override
    public QueueConnection createQueueConnection() throws JMSException {
        return delegate().createQueueConnection();
    }

    @Override
    public QueueConnection createQueueConnection(String userName, String password) throws JMSException {
        return delegate().createQueueConnection(userName, password);
    }

    @Override
    public TopicConnection createTopicConnection() throws JMSException {
        return delegate().createTopicConnection();
    }

    @Override
    public TopicConnection createTopicConnection(String userName, String password) throws JMSException {
        return delegate().createTopicConnection(userName, password);
    }

    // --- JMSContext creation ---

    @Override
    public JMSContext createContext() {
        return delegate().createContext();
    }

    @Override
    public JMSContext createContext(int sessionMode) {
        return delegate().createContext(sessionMode);
    }

    @Override
    public JMSContext createContext(String userName, String password) {
        return delegate().createContext(userName, password);
    }

    @Override
    public JMSContext createContext(String userName, String password, int sessionMode) {
        return delegate().createContext(userName, password, sessionMode);
    }

    // --- Lifecycle ---

    @Override
    public void start() {
        delegate().start();
    }

    @Override
    public void stop() {
        delegate().stop();
    }

    @Override
    public void clear() {
        delegate().clear();
    }

    @Override
    public void initConnectionsPool() {
        delegate().initConnectionsPool();
    }

    // --- Configuration getters (delegate to the actual pool) ---

    @Override
    public Object getConnectionFactory() {
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
