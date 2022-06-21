package io.quarkiverse.messaginghub.pooled.jms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.quarkus.artemis.test.ArtemisTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(ArtemisTestResource.class)
public class PooledJmsResourceTest {

    @Test
    public void testXA() {
        String body = UUID.randomUUID().toString();

        given().body(body)
                .when().post("/pooled-jms")
                .then()
                .statusCode(204);

        given()
                .when().get("/pooled-jms")
                .then()
                .statusCode(200)
                .body(is(body));
    }

    @Test
    public void testRollback() {
        given().body("fail")
                .when().post("/pooled-jms")
                .then()
                .statusCode(204);

        given()
                .when().get("/pooled-jms")
                .then()
                .statusCode(204);
    }
}
