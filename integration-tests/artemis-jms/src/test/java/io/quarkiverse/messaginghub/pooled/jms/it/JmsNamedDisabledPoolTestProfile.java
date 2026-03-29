package io.quarkiverse.messaginghub.pooled.jms.it;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class JmsNamedDisabledPoolTestProfile implements QuarkusTestProfile {

    @Override
    public String getConfigProfile() {
        return "named-config";
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        HashMap<String, String> props = new HashMap<>();

        // Default pool config
        props.put("quarkus.pooled-jms.max-connections", "5");
        props.put("quarkus.pooled-jms.max-sessions-per-connection", "100");

        // Named "broker1" with pooling disabled
        props.put("quarkus.pooled-jms.\"broker1\".pooling.enabled", "false");

        return props;
    }
}
