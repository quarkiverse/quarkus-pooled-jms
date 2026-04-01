package io.quarkiverse.messaginghub.pooled.jms.it.ibmmq;

import java.time.Duration;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import io.smallrye.common.annotation.Identifier;

@Path("/ibmmq")
@ApplicationScoped
public class IbmMqResource {

    private static final String ARTEMIS_TOPIC = "artemis-to-ibmmq";
    private static final String IBMMQ_QUEUE_ATOI = "ARTEMIS.TO.IBMMQ";
    private static final String IBMMQ_QUEUE_ITOA = "IBMMQ.TO.ARTEMIS";

    private final ConnectionFactory artemisCf;
    private final ConnectionFactory ibmmqCf;

    public IbmMqResource(
            @Identifier("artemis") @SuppressWarnings("CdiInjectionPointsInspection") ConnectionFactory artemisCf,
            @Identifier(IbmMqConnectionFactoryProducer.IBMMQ_CF_NAME) ConnectionFactory ibmmqCf) {
        this.artemisCf = artemisCf;
        this.ibmmqCf = ibmmqCf;
    }

    @POST
    @Path("/artemis-send")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String artemisSend(String body) {
        sendToArtemis(body);
        forwardArtemisToIbmmq();
        return receiveFromIbmmq(IBMMQ_QUEUE_ATOI);
    }

    @POST
    @Path("/ibmmq-send")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String ibmmqSend(String body) {
        sendToIbmmq(body);
        forwardIbmmqToArtemis();
        return receiveFromArtemis();
    }

    @GET
    @Path("/pool-info")
    @Produces(MediaType.APPLICATION_JSON)
    public String poolInfo() {
        return String.format("{\"artemis\":\"%s\",\"ibmmq\":\"%s\"}",
                artemisCf.getClass().getSimpleName(),
                ibmmqCf.getClass().getSimpleName());
    }

    @Transactional
    void sendToArtemis(String body) {
        try (JMSContext ctx = artemisCf.createContext(Session.SESSION_TRANSACTED)) {
            ctx.createProducer().send(ctx.createQueue(ARTEMIS_TOPIC), body);
        }
    }

    @Transactional
    void sendToIbmmq(String body) {
        try (JMSContext ctx = ibmmqCf.createContext(Session.SESSION_TRANSACTED)) {
            ctx.createProducer().send(ctx.createQueue(IBMMQ_QUEUE_ITOA), body);
        }
    }

    @Transactional
    void forwardArtemisToIbmmq() {
        try (JMSContext artemisCtx = artemisCf.createContext(Session.SESSION_TRANSACTED);
                JMSConsumer consumer = artemisCtx.createConsumer(artemisCtx.createQueue(ARTEMIS_TOPIC));
                JMSContext ibmmqCtx = ibmmqCf.createContext(Session.SESSION_TRANSACTED)) {
            var msg = consumer.receive(Duration.ofSeconds(5).toMillis());
            if (msg != null) {
                ibmmqCtx.createProducer().send(ibmmqCtx.createQueue(IBMMQ_QUEUE_ATOI), msg);
            }
        }
    }

    @Transactional
    void forwardIbmmqToArtemis() {
        try (JMSContext ibmmqCtx = ibmmqCf.createContext(Session.SESSION_TRANSACTED);
                JMSConsumer consumer = ibmmqCtx.createConsumer(ibmmqCtx.createQueue(IBMMQ_QUEUE_ITOA));
                JMSContext artemisCtx = artemisCf.createContext(Session.SESSION_TRANSACTED)) {
            var msg = consumer.receive(Duration.ofSeconds(5).toMillis());
            if (msg != null) {
                artemisCtx.createProducer().send(artemisCtx.createQueue(ARTEMIS_TOPIC), msg);
            }
        }
    }

    @Transactional
    String receiveFromIbmmq(String queue) {
        try (JMSContext ctx = ibmmqCf.createContext(Session.SESSION_TRANSACTED);
                JMSConsumer consumer = ctx.createConsumer(ctx.createQueue(queue))) {
            return Optional.ofNullable(consumer.receive(Duration.ofSeconds(5).toMillis()))
                    .map(message -> {
                        try {
                            return message.getBody(String.class);
                        } catch (JMSException e) {
                            return null;
                        }
                    })
                    .orElse(null);
        }
    }

    @Transactional
    String receiveFromArtemis() {
        try (JMSContext ctx = artemisCf.createContext(Session.SESSION_TRANSACTED);
                JMSConsumer consumer = ctx.createConsumer(ctx.createQueue(ARTEMIS_TOPIC))) {
            return Optional.ofNullable(consumer.receive(Duration.ofSeconds(5).toMillis()))
                    .map(message -> {
                        try {
                            return message.getBody(String.class);
                        } catch (JMSException e) {
                            return null;
                        }
                    })
                    .orElse(null);
        }
    }
}
