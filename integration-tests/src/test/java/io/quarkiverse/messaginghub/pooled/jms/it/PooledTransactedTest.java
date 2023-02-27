package io.quarkiverse.messaginghub.pooled.jms.it;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class PooledTransactedTest extends BasePooledJmsTest {
    @Override
    boolean isTransacted() {
        return true;
    }
}
