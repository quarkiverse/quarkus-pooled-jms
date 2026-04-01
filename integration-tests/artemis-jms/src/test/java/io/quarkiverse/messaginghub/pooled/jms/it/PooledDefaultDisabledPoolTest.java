package io.quarkiverse.messaginghub.pooled.jms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(JmsDefaultDisabledPoolTestProfile.class)
public class PooledDefaultDisabledPoolTest {

    @Test
    public void testDefaultPoolIsDisabled() {
        given()
                .when().get("/named-pooled-jms/info/default")
                .then()
                .statusCode(200)
                .body("pooled", is(false));
    }

    @Test
    public void testNamedPoolIsEnabled() {
        given()
                .when().get("/named-pooled-jms/info/broker1")
                .then()
                .statusCode(200)
                .body("maxConnections", is(10))
                .body("maxSessionsPerConnection", is(200));
    }

    @Test
    public void testUnconfiguredNamedPoolFallsBackToDisabledDefault() {
        // "broker2" has no explicit pooled-jms config, falls back to default which is disabled
        given()
                .when().get("/named-pooled-jms/info/broker2")
                .then()
                .statusCode(200)
                .body("pooled", is(false));
    }
}
