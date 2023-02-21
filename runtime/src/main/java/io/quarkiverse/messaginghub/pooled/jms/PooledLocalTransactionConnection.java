package io.quarkiverse.messaginghub.pooled.jms;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.messaginghub.pooled.jms.JmsPoolSession;
import org.messaginghub.pooled.jms.pool.PooledConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PooledLocalTransactionConnection extends PooledConnection {
    private static final Logger LOG = LoggerFactory.getLogger(PooledLocalTransactionConnection.class);
    private final TransactionManager transactionManager;

    public PooledLocalTransactionConnection(final Connection connection, final TransactionManager transactionManager) {
        super(connection);
        this.transactionManager = transactionManager;
    }

    @Override
    public Session createSession(boolean transacted, int ackMode) throws JMSException {
        JmsPoolSession session = (JmsPoolSession) super.createSession(transacted, ackMode);

        try {
            if (transactionManager != null && transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION
                    && transacted) {
                session.setIgnoreClose(true);
                transactionManager.getTransaction().registerSynchronization(new Synchronization() {
                    @Override
                    public void beforeCompletion() {
                    }

                    @Override
                    public void afterCompletion(final int status) {
                        boolean isCommitted = (status == Status.STATUS_COMMITTED);

                        try {
                            if (isCommitted) {
                                session.commit();
                            } else {
                                session.rollback();
                            }
                        } catch (JMSException e) {
                            LOG.error("Can not {} JMS Session", isCommitted ? "commit" : "rollback", e);
                        } finally {
                            try {
                                session.setIgnoreClose(false);
                                session.close();
                                decrementReferenceCount();
                            } catch (JMSException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });
                incrementReferenceCount();
            }
            return session;
        } catch (RollbackException e) {
            final JMSException jmsException = new JMSException("Rollback Exception");
            jmsException.initCause(e);
            throw jmsException;
        } catch (SystemException e) {
            final JMSException jmsException = new JMSException("System Exception");
            jmsException.initCause(e);
            throw jmsException;
        }
    }
}
