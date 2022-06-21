/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.quarkiverse.messaginghub.pooled.jms.it;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/pooled-jms")
@ApplicationScoped
public class PooledJmsResource {
    private String queue = "test-jms";

    @Inject
    ConnectionFactory connectionFactory;

    @Inject
    TransactionManager tm;

    @GET
    public String get() {
        return receive();
    }

    @POST
    @Transactional
    public void post(String message) throws Exception {
        send(message);

        if ("fail".equals(message)) {
            tm.setRollbackOnly();
        }
    }

    private void send(String body) {
        try (JMSContext context = connectionFactory.createContext()) {
            JMSProducer producer = context.createProducer();
            producer.send(context.createQueue(queue), body);
        }
    }

    private String receive() {
        try (JMSContext context = connectionFactory.createContext();
                JMSConsumer consumer = context.createConsumer(context.createQueue(queue))) {
            Message message = consumer.receive(1000L);
            if (message != null) {
                return message.getBody(String.class);
            } else {
                return null;
            }
        } catch (JMSException e) {
            throw new RuntimeException("Could not receive message", e);
        }
    }
}
