package io.quarkiverse.messaginghub.pooled.jms.it.ibmmq;

import jakarta.enterprise.inject.Produces;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;

import com.ibm.mq.jakarta.jms.MQXAConnectionFactory;
import com.ibm.msg.client.jakarta.jms.JmsConstants;
import com.ibm.msg.client.jakarta.wmq.common.CommonConstants;

import io.quarkiverse.messaginghub.pooled.jms.PooledJmsWrapper;
import io.smallrye.common.annotation.Identifier;

public class IbmMqConnectionFactoryProducer {

    public static final String IBMMQ_CF_NAME = "ibmmq";

    @Produces
    @Identifier(IBMMQ_CF_NAME)
    public ConnectionFactory ibmMqConnectionFactory(
            IbmMqConfig config,
            @SuppressWarnings("CdiInjectionPointsInspection") PooledJmsWrapper wrapper) throws JMSException {
        MQXAConnectionFactory xaCf = new MQXAConnectionFactory();
        xaCf.setHostName(config.hostname());
        xaCf.setPort(config.port());
        xaCf.setStringProperty(CommonConstants.WMQ_CHANNEL, config.channel());
        xaCf.setIntProperty(CommonConstants.WMQ_CONNECTION_MODE, CommonConstants.WMQ_CM_CLIENT);
        xaCf.setStringProperty(JmsConstants.USERID, config.user());
        xaCf.setStringProperty(JmsConstants.PASSWORD, config.password());
        xaCf.setStringProperty(CommonConstants.WMQ_QUEUE_MANAGER, config.queueManager());
        xaCf.setBooleanProperty(JmsConstants.USER_AUTHENTICATION_MQCSP, true);
        return wrapper.wrapConnectionFactory(xaCf);
    }
}
