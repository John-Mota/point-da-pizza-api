package br.com.pointdapizza.cardapio;

import br.com.pointdapizza.CloudinaryMockTestResource;
import br.com.pointdapizza.TestSeedHelper;
import br.com.pointdapizza.cardapio.entity.Produto;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

@QuarkusTest
@QuarkusTestResource(CloudinaryMockTestResource.class)
class ProdutoResourceTest {

    private static final byte[] PNG_FAKE = new byte[] {
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D
    };

    @Inject
    TestSeedHelper testSeedHelper;

    private final List<String> produtosCriados = new ArrayList<>();

    @BeforeEach
    void prepararDados() {
        testSeedHelper.garantirDadosBase();
    }

    @AfterEach
    void limparProdutosCriados() {
        removerProdutos(produtosCriados);
        produtosCriados.clear();
    }

    @Transactional
    void removerProdutos(List<String> ids) {
        for (String id : ids) {
            Produto p = Produto.findById(id);
            if (p != null) {
                p.delete();
            }
        }
    }

    @Test
    void deveCriarPizzaComFotoNoCloudinaryTamanhosEBordas() {
        String dadosJson = """
            {
              "categoriaId": "pizzas",
              "nome": "Pizza Portuguesa Especial",
              "descricao": "Molho artesanal, mussarela, presunto, ovos, cebola, azeitonas e orégano.",
              "preco": 38.90,
              "precoOriginal": 44.90,
              "destaque": true,
              "ordemDestaque": 10,
              "seloDestaque": "Novidade",
              "ordemExibicao": 20,
              "tamanhos": [
                { "id": "P", "nome": "Pequena (P)", "preco": 32.90 },
                { "id": "G", "nome": "Grande (G)", "preco": 38.90 }
              ],
              "bordas": [
                { "id": "requeijao", "nome": "Requeijão", "acrescimo": 4.00 }
              ]
            }
            """;

        String idCriado = given()
            .multiPart(new MultiPartSpecBuilder(dadosJson.getBytes(StandardCharsets.UTF_8))
                .controlName("dados")
                .fileName("dados.json")
                .mimeType("application/json")
                .build())
            .multiPart("foto", "pizza-portuguesa.png", PNG_FAKE, "image/png")
            .when().post("/api/produtos")
            .then()
            .statusCode(201)
            .body("id", equalTo("pizza-portuguesa-especial"))
            .body("categoriaId", equalTo("pizzas"))
            .body("categoria", equalTo("Pizzas"))
            .body("nome", equalTo("Pizza Portuguesa Especial"))
            .body("preco", equalTo(38.9f))
            .body("precoOriginal", equalTo(44.9f))
            .body("imagemUrl", startsWith("https://res.cloudinary.com/test-cloud/image/upload/"))
            .body("imagemUrl", containsString("point-da-pizza/produtos/"))
            .body("disponivel", equalTo(true))
            .body("destaque", equalTo(true))
            .body("seloDestaque", equalTo("Novidade"))
            .body("tamanhos", hasSize(2))
            .body("tamanhos[1].id", equalTo("G"))
            .body("tamanhos[1].preco", equalTo(38.9f))
            .body("bordas", hasSize(1))
            .body("bordas[0].id", equalTo("requeijao"))
            .body("bordas[0].acrescimo", equalTo(4.0f))
            .extract().path("id");

        produtosCriados.add(idCriado);
    }

    @Test
    void deveCriarProdutoSemFotoEAtualizarFotoDepois() {
        String idCriado = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "categoriaId": "pizzas",
                  "nome": "Pizza Frango com Catupiry",
                  "descricao": "Frango desfiado temperado com catupiry cremoso e orégano.",
                  "preco": 36.90
                }
                """)
            .when().post("/api/produtos")
            .then()
            .statusCode(201)
            .body("id", equalTo("pizza-frango-com-catupiry"))
            .body("imagemUrl", nullValue())
            .body("disponivel", equalTo(true))
            .extract().path("id");

        produtosCriados.add(idCriado);

        given()
            .multiPart("foto", "frango-catupiry.png", PNG_FAKE, "image/png")
            .when().put("/api/produtos/" + idCriado + "/foto")
            .then()
            .statusCode(200)
            .body("id", equalTo(idCriado))
            .body("imagemUrl", startsWith("https://res.cloudinary.com/test-cloud/image/upload/"))
            .body("imagemUrl", containsString("point-da-pizza/produtos/"));
    }

    @Test
    void deveValidarCategoriaInexistenteEPrecoOriginalMenorQuePreco() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "categoriaId": "categoria-que-nao-existe",
                  "nome": "Pizza Teste",
                  "descricao": "Descrição teste",
                  "preco": 30.00
                }
                """)
            .when().post("/api/produtos")
            .then()
            .statusCode(400)
            .body("mensagem", notNullValue())
            .body("campos[0].campo", equalTo("categoriaId"));

        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "categoriaId": "pizzas",
                  "nome": "Pizza Preco Invalido",
                  "descricao": "Descrição teste",
                  "preco": 40.00,
                  "precoOriginal": 30.00
                }
                """)
            .when().post("/api/produtos")
            .then()
            .statusCode(400)
            .body("mensagem", notNullValue())
            .body("campos[0].campo", equalTo("precoOriginal"));
    }
}
