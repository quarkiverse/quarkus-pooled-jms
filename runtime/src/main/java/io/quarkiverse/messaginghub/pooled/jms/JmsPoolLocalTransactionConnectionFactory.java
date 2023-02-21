package io.quarkiverse.messaginghub.pooled.jms;

import javax.jms.Connection;
import javax.transaction.TransactionManager;

import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
import org.messaginghub.pooled.jms.pool.PooledConnection;

public class JmsPoolLocalTransactionConnectionFactory extends JmsPoolConnectionFactory {
    private TransactionManager transactionManager;

    public JmsPoolLocalTransactionConnectionFactory() {

    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public PooledConnection createPooledConnection(Connection connection) {
        return new PooledLocalTransactionConnection(connection, transactionManager);
    }
}
