package io.quarkiverse.messaginghub.pooled.jms;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus.pooled-jms")
public interface PooledJmsRuntimeConfig {

    /**
     * Whether to enable pooling capabilities for JMS connections.
     */
    @WithDefault("true")
    @WithName("pooling.enabled")
    boolean poolingEnabled();

    /**
     * Whether to enable {@link jakarta.jms.XAConnection} support and integrate with
     * {@link jakarta.transaction.TransactionManager}
     * If you enable it, you need to include `io.quarkus:quarkus-narayana-jta` extension.
     */
    @WithDefault("disabled")
    TransactionIntegration transaction();

    /**
     * Determines the maximum number of Connections the pool maintains in a single Connection pool (defaults to one).
     */
    @WithDefault("1")
    int maxConnections();

    /**
     * The idle timeout (default 30 seconds) controls how long a Connection that hasn't been or currently isn't loaned
     * out to any client will remain idle in the Connection pool before it is eligible to be closed and discarded.
     * To disable idle timeouts the value should be set to 0 or a negative number.
     */
    @WithDefault("30")
    int connectionIdleTimeout();

    /**
     * used to establish a periodic check for expired Connections which will close all Connection that have exceeded
     * the set expiration value. This value is set to 0ms by default and only activates if set to a positive non-zero value.
     */
    @WithDefault("0")
    long connectionCheckInterval();

    /**
     * by default the JMS pool will use it's own generic JMSContext classes to wrap a Connection borrowed from the pool
     * instead of using the JMSContext functionality of the JMS ConnectionFactory that was configured. This generic
     * JMSContext implementation may be limited compared to the Provider version and if that functionality is critical
     * to the application this option can be enabled to force the pool to use the Provider JMSContext implementation.
     * When enabled the JMSContext API is then not part of the Connections that are pooled by this JMS Connection pooling
     * library.
     */
    @WithDefault("false")
    boolean useProviderJMSContext();

    /**
     * For each Connection in the pool there can be a configured maximum number of Sessions that the pooled Connection
     * will loan out before either blocking or throwing an error (based on configuration). By default this value is 500
     * meaning that each provider Connection is limited to 500 sessions, this limit can be disabled by setting the value
     * to a negative number.
     */
    @WithDefault("500")
    int maxSessionsPerConnection();

    /**
     * When true (default) a call to createSession on a Connection from the pool will block until another previously
     * created and loaned out session is closed an thereby becomes available. When false a call to createSession when
     * no Session is available will throw an IllegalStateException to indicate that the Connection is not able to provide
     * a new Session at that time.
     */
    @WithDefault("true")
    boolean blockIfSessionPoolIsFull();

    /**
     * When the blockIfSessionPoolIsFull option is enabled and this value is set then a call to createSession that has
     * blocked awaiting a Session will wait for the specified number of milliseconds before throwing an IllegalStateException.
     * By default this value is set to -1 indicating that the createSession call should block forever if configured to wait.
     */
    @WithDefault("-1")
    int blockIfSessionPoolIsFullTimeout();

    /**
     * By default a Session that has been loaned out on a call to createSession will use a single anonymous JMS MessageProducer
     * as the underlying producer for all calls to createProducer. In some rare cases this is not desirable and this
     * feature can be disabled using this option, when disabled every call to createProducer will result in a new
     * MessageProducer
     * instance being created.
     */
    @WithDefault("true")
    boolean useAnonymousProducers();
}
