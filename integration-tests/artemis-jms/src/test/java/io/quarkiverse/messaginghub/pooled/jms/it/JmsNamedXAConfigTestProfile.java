package io.quarkiverse.messaginghub.pooled.jms.it;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class JmsNamedXAConfigTestProfile implements QuarkusTestProfile {

    @Override
    public String getConfigProfile() {
        return "named-xa-config";
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        HashMap<String, String> props = new HashMap<>();

        // Default pool config
        props.put("quarkus.pooled-jms.max-connections", "5");
        props.put("quarkus.pooled-jms.max-sessions-per-connection", "100");

        // Named "broker1" pool config with XA transaction
        props.put("quarkus.pooled-jms.\"broker1\".max-connections", "10");
        props.put("quarkus.pooled-jms.\"broker1\".max-sessions-per-connection", "200");
        props.put("quarkus.pooled-jms.\"broker1\".transaction", "xa");

        // Enable XA recovery
        props.put("quarkus.transaction-manager.enable-recovery", "true");

        return props;
    }
}
