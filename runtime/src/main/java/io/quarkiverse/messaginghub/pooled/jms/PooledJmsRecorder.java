package io.quarkiverse.messaginghub.pooled.jms;

import java.util.function.Supplier;

import javax.jms.ConnectionFactory;
import javax.transaction.TransactionManager;

import io.quarkus.arc.ArcContainer;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.messaginghub.pooled.jms.JmsPoolXAConnectionFactory;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class PooledJmsRecorder {
    public Supplier<JmsPoolConnectionFactory> createConnectionFactory(boolean xa, BeanContainer beanContainer) {
        return () -> {
            JmsPoolConnectionFactory connectionFactory;

            if (xa) {
                ArcContain
                TransactionManager tm = beanContainer.instance(TransactionManager.class);
                JmsPoolXAConnectionFactory xaConnectionFactory = new JmsPoolXAConnectionFactory();
                xaConnectionFactory.setTransactionManager(tm);
                connectionFactory = xaConnectionFactory;
            } else {
                connectionFactory = new JmsPoolConnectionFactory();
            }

            ConnectionFactory cf;
            cf = beanContainer.instance(ConnectionFactory.class);
            connectionFactory.setConnectionFactory(cf);

            return connectionFactory;
        };
    }
}
