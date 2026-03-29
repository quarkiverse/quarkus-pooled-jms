package io.quarkiverse.messaginghub.pooled.jms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(JmsNamedDisabledPoolTestProfile.class)
public class PooledNamedDisabledPoolTest {

    @Test
    public void testDefaultPoolIsEnabled() {
        given()
                .when().get("/named-pooled-jms/info/default")
                .then()
                .statusCode(200)
                .body("poolType", is("JmsPoolLocalTransactionConnectionFactory"));
    }

    @Test
    public void testNamedPoolIsDisabled() {
        // "broker1" has pooling disabled, should return the raw CF (passthrough)
        given()
                .when().get("/named-pooled-jms/info/broker1")
                .then()
                .statusCode(200)
                .body("pooled", is(false));
    }
}
