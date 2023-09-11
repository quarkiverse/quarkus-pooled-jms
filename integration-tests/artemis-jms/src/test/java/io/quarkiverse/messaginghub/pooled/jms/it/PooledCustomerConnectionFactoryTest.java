package io.quarkiverse.messaginghub.pooled.jms.it;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(JmsArtemisDisabled.class)
public class PooledCustomerConnectionFactoryTest extends BasePooledJmsTest {
    @Override
    boolean isTransacted() {
        return true;
    }
}
