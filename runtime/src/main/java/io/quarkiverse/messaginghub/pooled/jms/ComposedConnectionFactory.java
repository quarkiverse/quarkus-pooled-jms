package io.quarkiverse.messaginghub.pooled.jms;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.XAConnectionFactory;

public interface ComposedConnectionFactory extends ConnectionFactory, XAConnectionFactory {
}
