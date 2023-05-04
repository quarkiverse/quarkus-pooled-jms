package io.quarkiverse.messaginghub.pooled.jms.it;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(JmsXATestProfile.class)
public class PooledXATest extends BasePooledJmsTest {

    @Override
    boolean isTransacted() {
        return false;
    }
}
