package io.quarkiverse.messaginghub.pooled.jms;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "pooled-jms", phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED)
public class PooledJmsRuntimeConfig {

    /**
     * Whether to enable {@link javax.jms.XAConnection} support and integrate with {@link javax.transaction.TransactionManager}
     * If you enable it, you need to include `io.quarkus:quarkus-narayana-jta` extension.
     */
    @ConfigItem(name = "xa.enabled", defaultValue = "false")
    public boolean xaEnabled;

    /**
     * Determines the maximum number of Connections the pool maintains in a single Connection pool (defaults to one).
     */
    @ConfigItem(defaultValue = "1")
    public int maxConnections;

    /**
     * The idle timeout (default 30 seconds) controls how long a Connection that hasn't been or currently isn't loaned
     * out to any client will remain idle in the Connection pool before it is eligible to be closed and discarded.
     * To disable idle timeouts the value should be set to 0 or a negative number.
     */
    @ConfigItem(defaultValue = "30")
    public int connectionIdleTimeout;

    /**
     * used to establish a periodic check for expired Connections which will close all Connection that have exceeded
     * the set expiration value. This value is set to 0ms by default and only activates if set to a positive non-zero value.
     */
    @ConfigItem(defaultValue = "0")
    public long connectionCheckInterval;

    /**
     * by default the JMS pool will use it's own generic JMSContext classes to wrap a Connection borrowed from the pool
     * instead of using the JMSContext functionality of the JMS ConnectionFactory that was configured. This generic
     * JMSContext implementation may be limited compared to the Provider version and if that functionality is critical
     * to the application this option can be enabled to force the pool to use the Provider JMSContext implementation.
     * When enabled the JMSContext API is then not part of the Connections that are pooled by this JMS Connection pooling
     * library.
     */
    @ConfigItem(defaultValue = "false")
    public boolean useProviderJMSContext;

    /**
     * For each Connection in the pool there can be a configured maximum number of Sessions that the pooled Connection
     * will loan out before either blocking or throwing an error (based on configuration). By default this value is 500
     * meaning that each provider Connection is limited to 500 sessions, this limit can be disabled by setting the value
     * to a negative number.
     */
    @ConfigItem(defaultValue = "500")
    public int maxSessionsPerConnection;

    /**
     * When true (default) a call to createSession on a Connection from the pool will block until another previously
     * created and loaned out session is closed an thereby becomes available. When false a call to createSession when
     * no Session is available will throw an IllegalStateException to indicate that the Connection is not able to provide
     * a new Session at that time.
     */
    @ConfigItem(defaultValue = "true")
    public boolean blockIfSessionPoolIsFull;

    /**
     * When the blockIfSessionPoolIsFull option is enabled and this value is set then a call to createSession that has
     * blocked awaiting a Session will wait for the specified number of milliseconds before throwing an IllegalStateException.
     * By default this value is set to -1 indicating that the createSession call should block forever if configured to wait.
     */
    @ConfigItem(defaultValue = "-1")
    public int blockIfSessionPoolIsFullTimeout;

    /**
     * By default a Session that has been loaned out on a call to createSession will use a single anonymous JMS MessageProducer
     * as the underlying producer for all calls to createProducer. In some rare cases this is not desirable and this
     * feature can be disabled using this option, when disabled every call to createProducer will result in a new
     * MessageProducer
     * instance being created.
     */
    @ConfigItem(defaultValue = "true")
    public boolean useAnonymousProducers;
}
