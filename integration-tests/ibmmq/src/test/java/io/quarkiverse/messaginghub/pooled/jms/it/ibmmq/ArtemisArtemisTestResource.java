package io.quarkiverse.messaginghub.pooled.jms.it.ibmmq;

import io.quarkus.artemis.test.ArtemisTestResource;

public class ArtemisArtemisTestResource extends ArtemisTestResource {
    public ArtemisArtemisTestResource() {
        super("artemis");
    }
}
