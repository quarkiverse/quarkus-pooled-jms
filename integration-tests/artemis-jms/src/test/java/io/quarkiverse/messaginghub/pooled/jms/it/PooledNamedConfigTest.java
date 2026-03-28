package io.quarkiverse.messaginghub.pooled.jms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(JmsNamedConfigTestProfile.class)
public class PooledNamedConfigTest {

    @Test
    public void testDefaultPoolConfig() {
        given()
                .when().get("/named-pooled-jms/info/default")
                .then()
                .statusCode(200)
                .body("maxConnections", is(5))
                .body("maxSessionsPerConnection", is(100))
                .body("useAnonymousProducers", is(true));
    }

    @Test
    public void testNamedPoolConfig() {
        given()
                .when().get("/named-pooled-jms/info/broker1")
                .then()
                .statusCode(200)
                .body("maxConnections", is(15))
                .body("maxSessionsPerConnection", is(300))
                .body("useAnonymousProducers", is(false));
    }

    @Test
    public void testFallbackToDefaultPoolConfig() {
        // "broker2" has an artemis config but no pooled-jms config,
        // so it should fall back to the default pool settings
        given()
                .when().get("/named-pooled-jms/info/broker2")
                .then()
                .statusCode(200)
                .body("maxConnections", is(5))
                .body("maxSessionsPerConnection", is(100))
                .body("useAnonymousProducers", is(true));
    }
}
