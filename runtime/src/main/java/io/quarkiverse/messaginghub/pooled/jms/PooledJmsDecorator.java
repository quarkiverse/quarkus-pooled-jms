package io.quarkiverse.messaginghub.pooled.jms;

import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.XAConnection;
import jakarta.jms.XAConnectionFactory;
import jakarta.jms.XAJMSContext;

import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.messaginghub.pooled.jms.JmsPoolXAConnectionFactory;

@Priority(0)
@Decorator
public class PooledJmsDecorator implements ComposedConnectionFactory {
    @Inject
    @Any
    @Delegate
    ComposedConnectionFactory delegate;

    PooledJmsWrapper wrapper;
    ConnectionFactory factory;

    public PooledJmsDecorator(PooledJmsRuntimeConfig config) {
        wrapper = new PooledJmsWrapper(true, config);
    }

    private ConnectionFactory getConnectionFactory() {
        if (factory == null) {
            factory = wrapper.wrapConnectionFactory(delegate);
        }
        return factory;
    }

    @Override
    public Connection createConnection() throws JMSException {
        return isInJmsPoolConnectionFactory() ? delegate.createConnection() : getConnectionFactory().createConnection();
    }

    @Override
    public Connection createConnection(final String userName, final String password) throws JMSException {
        return isInJmsPoolConnectionFactory() ? delegate.createConnection(userName, password)
                : getConnectionFactory().createConnection(userName, password);
    }

    @Override
    public JMSContext createContext() {
        return getConnectionFactory().createContext();
    }

    @Override
    public JMSContext createContext(final String userName, final String password) {
        return getConnectionFactory().createContext(userName, password);
    }

    @Override
    public JMSContext createContext(final String userName, final String password, final int sessionMode) {
        return getConnectionFactory().createContext(userName, password, sessionMode);
    }

    @Override
    public JMSContext createContext(final int sessionMode) {
        return getConnectionFactory().createContext(sessionMode);
    }

    @Override
    public XAConnection createXAConnection() throws JMSException {
        return isInJmsPoolConnectionFactory() ? delegate.createXAConnection()
                : ((XAConnectionFactory) getConnectionFactory()).createXAConnection();
    }

    @Override
    public XAConnection createXAConnection(final String userName, final String password) throws JMSException {
        return isInJmsPoolConnectionFactory() ? delegate.createXAConnection(userName, password)
                : ((XAConnectionFactory) getConnectionFactory()).createXAConnection(userName, password);
    }

    @Override
    public XAJMSContext createXAContext() {
        return ((XAConnectionFactory) factory).createXAContext();
    }

    @Override
    public XAJMSContext createXAContext(final String userName, final String password) {
        return ((XAConnectionFactory) factory).createXAContext(userName, password);
    }

    private boolean isInJmsPoolConnectionFactory() {
        for (StackTraceElement stack : Thread.currentThread().getStackTrace()) {
            String className = stack.getClassName();
            String methodName = stack.getMethodName();
            if ((className.equals(JmsPoolConnectionFactory.class.getName())
                    || className.equals(JmsPoolXAConnectionFactory.class.getName()))
                    && methodName.equals("createProviderConnection")) {
                return true;
            }
        }
        return false;
    }
}
