package be.codesolutions.pwm.boundary;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class PwmResourceTest {

    @Test
    void shouldGetAllOutputs() {
        given()
            .when().get("/pwm")
            .then()
            .statusCode(200)
            .body("size()", is(5))
            .body("id", hasItems("zk", "mb", "lk", "kk", "bk"));
    }

    @Test
    void shouldGetSingleOutput() {
        given()
            .when().get("/pwm/mb")
            .then()
            .statusCode(200)
            .body("id", is("mb"))
            .body("pin", is(16));
    }

    @Test
    void shouldReturn404ForUnknownOutput() {
        given()
            .when().get("/pwm/unknown")
            .then()
            .statusCode(404);
    }

    @Test
    void shouldSetPwmValue() {
        given()
            .queryParam("value", 0.75)
            .when().put("/pwm/zk")
            .then()
            .statusCode(200)
            .body("id", is("zk"))
            .body("value", is(0.75f));
    }

    @Test
    void shouldRejectMissingValue() {
        given()
            .when().put("/pwm/zk")
            .then()
            .statusCode(400)
            .body(containsString("Missing required query parameter"));
    }

    @Test
    void shouldRejectInvalidValue() {
        given()
            .queryParam("value", 1.5)
            .when().put("/pwm/zk")
            .then()
            .statusCode(400)
            .body(containsString("must be between 0.0 and 1.0"));
    }

    @Test
    void shouldRejectUnknownOutputId() {
        given()
            .queryParam("value", 0.5)
            .when().put("/pwm/unknown")
            .then()
            .statusCode(400)
            .body(containsString("Unknown output ID"));
    }
}