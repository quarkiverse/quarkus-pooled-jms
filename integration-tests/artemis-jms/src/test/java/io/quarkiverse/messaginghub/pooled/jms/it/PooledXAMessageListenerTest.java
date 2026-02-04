package io.quarkiverse.messaginghub.pooled.jms.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

/**
 * Test demonstrating the issue with MessageListener and XA transactions,
 * and showing the correct approach for consuming messages with XA.
 */
@QuarkusTest
@TestProfile(JmsXATestProfile.class)
public class PooledXAMessageListenerTest {

    @Inject
    ConnectionFactory connectionFactory;

    private static final String TEST_QUEUE = "xa-listener-test-queue";

    /**
     * Test demonstrating the correct way to consume messages with XA transactions:
     * Use synchronous receive() within a @Transactional method.
     * When an exception is thrown, the transaction is rolled back and the message is redelivered.
     */
    @Test
    public void testSynchronousConsumptionWithXA() throws Exception {
        String messageBody = "test-message-" + UUID.randomUUID();

        // Send a message
        sendMessage(messageBody);

        // Try to receive and process with exception - should rollback
        boolean exceptionThrown = false;
        try {
            receiveAndProcessWithException();
        } catch (RuntimeException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown, "Expected exception to be thrown");

        // Message should still be available due to rollback
        String receivedBody = receiveMessage();
        assertEquals(messageBody, receivedBody, "Message should be redelivered after rollback");

        // Successfully process the message
        receiveAndProcess();

        // Message should now be consumed
        String noMessage = receiveMessage();
        assertNull(noMessage, "Message should be consumed after successful processing");
    }

    /**
     * Sends a message to the test queue within a transaction.
     */
    @Transactional
    void sendMessage(String body) {
        try (JMSContext context = connectionFactory.createContext(Session.SESSION_TRANSACTED)) {
            context.createProducer().send(context.createQueue(TEST_QUEUE), body);
        }
    }

    /**
     * Receives and processes a message within a transaction.
     * This is the CORRECT pattern for XA transaction support.
     */
    @Transactional
    void receiveAndProcess() throws JMSException {
        try (JMSContext context = connectionFactory.createContext(Session.SESSION_TRANSACTED);
                JMSConsumer consumer = context.createConsumer(context.createQueue(TEST_QUEUE))) {

            Message message = consumer.receive(5000L);
            if (message != null) {
                String body = message.getBody(String.class);
                // Process message successfully
            }
        }
    }

    /**
     * Receives a message and throws an exception within a transaction.
     * The transaction will be rolled back and the message will be redelivered.
     */
    @Transactional
    void receiveAndProcessWithException() throws JMSException {
        try (JMSContext context = connectionFactory.createContext(Session.SESSION_TRANSACTED);
                JMSConsumer consumer = context.createConsumer(context.createQueue(TEST_QUEUE))) {

            Message message = consumer.receive(5000L);
            if (message != null) {
                String body = message.getBody(String.class);
                throw new RuntimeException("Simulated processing failure");
            }
        }
    }

    /**
     * Receives a message without transaction (for verification).
     */
    String receiveMessage() throws JMSException {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE);
                JMSConsumer consumer = context.createConsumer(context.createQueue(TEST_QUEUE))) {

            Message message = consumer.receive(1000L);
            if (message != null) {
                return message.getBody(String.class);
            }
            return null;
        }
    }
}
