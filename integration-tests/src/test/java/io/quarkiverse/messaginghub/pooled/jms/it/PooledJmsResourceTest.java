package io.quarkiverse.messaginghub.pooled.jms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class PooledJmsResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/pooled-jms")
                .then()
                .statusCode(200)
                .body(is("Hello pooled-jms"));
    }
}
