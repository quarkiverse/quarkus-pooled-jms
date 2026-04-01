package io.quarkiverse.messaginghub.pooled.jms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(JmsNamedXAConfigTestProfile.class)
public class PooledNamedXAConfigTest {

    @Test
    public void testDefaultPoolUsesLocalTransaction() {
        given()
                .when().get("/named-pooled-jms/info/default")
                .then()
                .statusCode(200)
                .body("maxConnections", is(5))
                .body("maxSessionsPerConnection", is(100))
                .body("poolType", is("JmsPoolLocalTransactionConnectionFactory"));
    }

    @Test
    public void testNamedPoolUsesXA() {
        given()
                .when().get("/named-pooled-jms/info/broker1")
                .then()
                .statusCode(200)
                .body("maxConnections", is(10))
                .body("maxSessionsPerConnection", is(200))
                .body("poolType", is("JmsPoolXAConnectionFactory"));
    }

    @Test
    public void testUnconfiguredNamedPoolFallsBackToDefault() {
        // "broker2" has artemis config but no pooled-jms config,
        // so it should use the default pool settings (local transaction)
        given()
                .when().get("/named-pooled-jms/info/broker2")
                .then()
                .statusCode(200)
                .body("maxConnections", is(5))
                .body("maxSessionsPerConnection", is(100))
                .body("poolType", is("JmsPoolLocalTransactionConnectionFactory"));
    }
}
