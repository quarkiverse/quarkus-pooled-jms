package io.quarkiverse.messaginghub.pooled.jms;

import java.util.function.Function;

import jakarta.annotation.Priority;
import jakarta.decorator.Decorator;
import jakarta.decorator.Delegate;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;

@Priority(0)
@Decorator
public class PooledJmsDecorator implements ConnectionFactory {
    @Inject
    @Any
    @Delegate
    ConnectionFactory delegate;

    PooledJmsWrapper wrapper;
    ConnectionFactory factory;
    boolean isContext = false;

    public PooledJmsDecorator(PooledJmsRuntimeConfig config) {
        wrapper = new PooledJmsWrapper(true, config);
    }

    private ConnectionFactory getConnectionFactory() {
        if (factory == null) {
            factory = wrapper.wrapConnectionFactory(delegate);
        }
        return isContext ? delegate : factory;
    }

    @Override
    public Connection createConnection() throws JMSException {
        return getConnectionFactory().createConnection();
    }

    @Override
    public Connection createConnection(final String userName, final String password) throws JMSException {
        return getConnectionFactory().createConnection(userName, password);
    }

    @Override
    public JMSContext createContext() {
        return runInContext(ConnectionFactory::createContext);
    }

    @Override
    public JMSContext createContext(final String userName, final String password) {
        return runInContext(cf -> cf.createContext(userName, password));
    }

    @Override
    public JMSContext createContext(final String userName, final String password, final int sessionMode) {
        return runInContext(cf -> cf.createContext(userName, password, sessionMode));
    }

    @Override
    public JMSContext createContext(final int sessionMode) {
        return runInContext(cf -> cf.createContext(sessionMode));
    }

    private JMSContext runInContext(Function<ConnectionFactory, JMSContext> process) {
        ConnectionFactory cf = getConnectionFactory();
        isContext = true;
        try {
            return process.apply(cf);
        } finally {
            isContext = false;
        }
    }
}
