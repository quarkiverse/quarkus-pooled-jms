package io.quarkiverse.messaginghub.pooled.jms.it;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class JmsXATestProfile implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        HashMap<String, String> props = new HashMap<>();

        props.put("quarkus.pooled-jms.transaction", "xa");
        props.put("quarkus.transaction-manager.enable-recovery", "true");
        return props;
    }
}
