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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.Message;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

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
