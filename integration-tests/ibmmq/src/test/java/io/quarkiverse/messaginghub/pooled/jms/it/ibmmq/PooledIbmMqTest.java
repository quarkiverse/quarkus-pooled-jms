package io.quarkiverse.messaginghub.pooled.jms.it.ibmmq;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@WithTestResource(ArtemisArtemisTestResource.class)
@WithTestResource(IbmMqTestContainer.class)
public class PooledIbmMqTest {

    @Test
    public void testArtemisSend() {
        String body = "hello from artemis";
        given()
                .contentType("text/plain")
                .body(body)
                .when().post("/ibmmq/artemis-send")
                .then()
                .statusCode(200)
                .body(is(body));
    }

    @Test
    public void testIbmmqSend() {
        String body = "hello from ibmmq";
        given()
                .contentType("text/plain")
                .body(body)
                .when().post("/ibmmq/ibmmq-send")
                .then()
                .statusCode(200)
                .body(is(body));
    }

    @Test
    public void testPoolInfo() {
        given()
                .when().get("/ibmmq/pool-info")
                .then()
                .statusCode(200)
                .body(containsString("DelegatingJmsPoolConnectionFactory"));
    }
}
