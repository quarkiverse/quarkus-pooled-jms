package io.quarkiverse.messaginghub.pooled.jms;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

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

        try {
            boolean isTransacted = transactionManager != null && transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION;

            if (isTransacted) {
                transacted = true;
                ackMode = Session.SESSION_TRANSACTED;
            }
            JmsPoolSession session = (JmsPoolSession) super.createSession(transacted, ackMode);
            if (isTransacted) {
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
                            } catch (JMSException e) {
                                throw new RuntimeException(e);
                            } finally {
                                decrementReferenceCount();
                            }
                        }
                    }
                });
                incrementReferenceCount();
            } else {
                session.setIgnoreClose(false);
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
