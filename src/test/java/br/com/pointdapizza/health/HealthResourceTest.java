package br.com.pointdapizza.health;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class HealthResourceTest {

    @Test
    void deveRetornarApiOnlineEmGetApiHealth() {
        given()
            .when().get("/api/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
            .body("online", equalTo(true))
            .body("servico", equalTo("point-da-pizza-api"))
            .body("$", not(hasKey("banco")))
            .body("timestamp", notNullValue());
    }

    @Test
    void deveValidarBancoQuandoSolicitadoEmGetApiHealth() {
        given()
            .queryParam("verificarBanco", true)
            .when().get("/api/health")
            .then()
            .statusCode(200)
            .body("status", equalTo("UP"))
            .body("online", equalTo(true))
            .body("banco", equalTo("UP"))
            .body("timestamp", notNullValue());
    }
}
