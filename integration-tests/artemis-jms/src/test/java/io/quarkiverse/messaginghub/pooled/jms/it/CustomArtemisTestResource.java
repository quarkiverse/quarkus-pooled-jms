package io.quarkiverse.messaginghub.pooled.jms.it;

import io.quarkus.artemis.test.ArtemisTestResource;

public class CustomArtemisTestResource extends ArtemisTestResource {
    public CustomArtemisTestResource() {
        super("artemis", "custom");
    }
}
