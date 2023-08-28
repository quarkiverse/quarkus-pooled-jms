package io.quarkiverse.messaginghub.pooled.jms.it;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class JmsArtemisDisabled implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        HashMap<String, String> props = new HashMap<>();

        props.put("quarkus.artemis.enabled", "false");
        //props.put("quarkus.pooled-jms.transaction", "xa");
        return props;
    }

    @Override
    public List<TestResourceEntry> testResources() {
        return List.of(new TestResourceEntry(CustomArtemisTestResource.class));
    }
}
