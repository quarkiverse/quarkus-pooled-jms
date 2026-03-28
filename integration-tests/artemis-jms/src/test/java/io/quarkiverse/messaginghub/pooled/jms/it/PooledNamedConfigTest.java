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
                .when().get("/named-pooled-jms/default-info")
                .then()
                .statusCode(200)
                .body("maxConnections", is(5))
                .body("maxSessionsPerConnection", is(100))
                .body("useAnonymousProducers", is(true));
    }

    @Test
    public void testNamedPoolConfig() {
        given()
                .when().get("/named-pooled-jms/named-info")
                .then()
                .statusCode(200)
                .body("maxConnections", is(15))
                .body("maxSessionsPerConnection", is(300))
                .body("useAnonymousProducers", is(false));
    }
}
