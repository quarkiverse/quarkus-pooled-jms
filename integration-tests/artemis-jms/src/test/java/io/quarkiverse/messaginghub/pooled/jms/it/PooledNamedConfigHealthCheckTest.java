package io.quarkiverse.messaginghub.pooled.jms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(JmsNamedConfigTestProfile.class)
public class PooledNamedConfigHealthCheckTest {

    @Test
    public void testHealthCheckWithNamedConnectionFactories() {
        given()
                .when().get("/q/health/ready")
                .then()
                .statusCode(200)
                .body("status", is("UP"))
                .body("checks.find { it.name == 'Artemis JMS health check' }.status", is("UP"))
                .body("checks.find { it.name == 'Artemis JMS health check' }.data.'<default>'", is("UP"))
                .body("checks.find { it.name == 'Artemis JMS health check' }.data.broker1", is("UP"))
                .body("checks.find { it.name == 'Artemis JMS health check' }.data.broker2", is("UP"));
    }

    @Test
    public void testDefaultPoolInfoAfterHealthCheck() {
        given()
                .when().get("/named-pooled-jms/info/default")
                .then()
                .statusCode(200)
                .body("maxConnections", is(5))
                .body("maxSessionsPerConnection", is(100));
    }

    @Test
    public void testNamedPoolInfoAfterHealthCheck() {
        given()
                .when().get("/named-pooled-jms/info/broker1")
                .then()
                .statusCode(200)
                .body("maxConnections", is(15))
                .body("maxSessionsPerConnection", is(300))
                .body("useAnonymousProducers", is(false));
    }

    @Test
    public void testFallbackPoolInfoAfterHealthCheck() {
        given()
                .when().get("/named-pooled-jms/info/broker2")
                .then()
                .statusCode(200)
                .body("maxConnections", is(5))
                .body("maxSessionsPerConnection", is(100));
    }
}
