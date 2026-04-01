package io.quarkiverse.messaginghub.pooled.jms.it;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class JmsNamedConfigTestProfile implements QuarkusTestProfile {

    @Override
    public Map<String, String> getConfigOverrides() {
        HashMap<String, String> props = new HashMap<>();

        // Default pool config - without map key (backward-compatible style)
        props.put("quarkus.pooled-jms.max-connections", "5");
        props.put("quarkus.pooled-jms.max-sessions-per-connection", "100");

        // Named "broker1" pool config with different settings
        props.put("quarkus.pooled-jms.\"broker1\".max-connections", "15");
        props.put("quarkus.pooled-jms.\"broker1\".max-sessions-per-connection", "300");
        props.put("quarkus.pooled-jms.\"broker1\".use-anonymous-producers", "false");

        return props;
    }
}
