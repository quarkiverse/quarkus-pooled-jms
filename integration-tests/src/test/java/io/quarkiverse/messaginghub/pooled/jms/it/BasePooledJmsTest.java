package io.quarkiverse.messaginghub.pooled.jms.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.util.UUID;

import org.junit.jupiter.api.Test;

abstract public class BasePooledJmsTest {

    @Test
    public void testCommit() {
        String body = UUID.randomUUID().toString();

        given().body(body).queryParam("transacted", isTransacted())
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
        given().body("fail").queryParam("transacted", isTransacted())
                .when().post("/pooled-jms")
                .then()
                .statusCode(204);

        given()
                .when().get("/pooled-jms")
                .then()
                .statusCode(204);
    }

    abstract boolean isTransacted();
}
