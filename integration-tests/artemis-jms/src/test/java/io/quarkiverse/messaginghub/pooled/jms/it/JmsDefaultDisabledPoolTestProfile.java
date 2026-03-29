package io.quarkiverse.messaginghub.pooled.jms.it;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class JmsDefaultDisabledPoolTestProfile implements QuarkusTestProfile {

    @Override
    public String getConfigProfile() {
        return "named-config";
    }

    @Override
    public Map<String, String> getConfigOverrides() {
        HashMap<String, String> props = new HashMap<>();

        // Default pool config with pooling disabled
        props.put("quarkus.pooled-jms.pooling.enabled", "false");

        // Named "broker1" with pooling enabled and custom settings
        props.put("quarkus.pooled-jms.\"broker1\".max-connections", "10");
        props.put("quarkus.pooled-jms.\"broker1\".max-sessions-per-connection", "200");

        return props;
    }
}
