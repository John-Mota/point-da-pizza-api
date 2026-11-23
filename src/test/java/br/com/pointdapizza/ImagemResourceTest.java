package br.com.pointdapizza;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
@QuarkusTestResource(CloudinaryMockTestResource.class)
class ImagemResourceTest {

    private static final byte[] PNG_FAKE = new byte[] {
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D
    };

    @Test
    void deveEnviarFotoDeUsuarioParaCloudinary() {
        given()
            .multiPart("foto", "avatar.png", PNG_FAKE, "image/png")
            .when().post("/api/imagens/usuarios")
            .then()
            .statusCode(201)
            .body("url", startsWith("https://res.cloudinary.com/test-cloud/image/upload/"))
            .body("url", containsString("point-da-pizza/usuarios/"))
            .body("publicId", startsWith("point-da-pizza/usuarios/"));
    }

    @Test
    void deveEnviarFotoDeProdutoParaCloudinary() {
        given()
            .multiPart("foto", "pizza.png", PNG_FAKE, "image/png")
            .when().post("/api/imagens/produtos")
            .then()
            .statusCode(201)
            .body("url", startsWith("https://res.cloudinary.com/test-cloud/image/upload/"))
            .body("url", containsString("point-da-pizza/produtos/"))
            .body("publicId", startsWith("point-da-pizza/produtos/"));
    }

    @Test
    void deveRejeitarPastaInvalidaEFormatoNaoSuportado() {
        given()
            .multiPart("foto", "arquivo.png", PNG_FAKE, "image/png")
            .when().post("/api/imagens/desconhecida")
            .then()
            .statusCode(400)
            .body("mensagem", notNullValue())
            .body("campos[0].campo", equalTo("pasta"));

        given()
            .multiPart("foto", "documento.pdf", "conteudo pdf".getBytes(), "application/pdf")
            .when().post("/api/imagens/produtos")
            .then()
            .statusCode(400)
            .body("mensagem", notNullValue())
            .body("campos[0].campo", equalTo("foto"));
    }
}
