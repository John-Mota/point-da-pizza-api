package br.com.pointdapizza.cardapio;

import br.com.pointdapizza.TestSeedHelper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@QuarkusTest
class LojaCardapioResourceTest {

    @Inject
    TestSeedHelper testSeedHelper;

    @BeforeEach
    void prepararDados() {
        testSeedHelper.garantirDadosBase();
    }

    @Test
    void deveRetornarDadosDaLojaNoContratoExato() {
        given()
            .when().get("/api/loja")
            .then()
            .statusCode(200)
            .body("nome", equalTo("Point da Pizza"))
            .body("slogan", equalTo("Sabor que te leva ao ponto!"))
            .body("logoUrl", equalTo("/img/logo.png"))
            .body("capaUrl", equalTo("/img/categoria-pizzas.jpg"))
            .body("aberta", equalTo(true))
            .body("endereco", equalTo("Av. Principal, 450 - Centro, Fortaleza/CE"))
            .body("telefone", equalTo("85988776655"))
            .body("horarios", hasSize(6))
            .body("horarios.dia", contains(0, 2, 3, 4, 5, 6))
            .body("horarios[0].abre", equalTo("18:00"))
            .body("horarios[0].fecha", equalTo("23:30"))
            .body("formasPagamento", contains("Pix", "Cartão de crédito", "Cartão de débito", "Dinheiro"));
    }

    @Test
    void deveRetornarCardapioApenasComCategoriasAtivasQuePossuemProdutos() {
        given()
            .when().get("/api/cardapio")
            .then()
            .statusCode(200)
            .body("$", hasSize(6))
            .body("categoria.id", contains("pizzas", "pasteis", "calzones", "bebidas", "combos", "lasanhas"))
            .body("categoria.ordem", contains(1, 2, 4, 6, 7, 8))
            .body("[0].categoria.nome", equalTo("Pizzas"))
            .body("[0].categoria.imagemUrl", equalTo("/img/categoria-pizzas.jpg"))
            .body("[0].produtos", hasSize(7))
            .body("[0].produtos[0].id", equalTo("prod-trad-1"))
            .body("[0].produtos[0].categoriaId", equalTo("pizzas"))
            .body("[0].produtos[0].categoria", equalTo("Pizzas"))
            .body("[0].produtos[0].nome", equalTo("Pizza Calabresa Acebolada"))
            .body("[0].produtos[0].preco", equalTo(32.9f))
            .body("[0].produtos[0].precoOriginal", nullValue())
            .body("[0].produtos[0].disponivel", equalTo(true))
            .body("[0].produtos[0].destaque", equalTo(true))
            .body("[0].produtos[0].ordemDestaque", equalTo(5))
            .body("[0].produtos[0].seloDestaque", equalTo("Favorita da Casa"))
            .body("[0].produtos[0].tamanhos", hasSize(2))
            .body("[0].produtos[0].tamanhos[0].id", equalTo("P"))
            .body("[0].produtos[0].tamanhos[0].nome", equalTo("Pequena (P)"))
            .body("[0].produtos[0].tamanhos[0].preco", equalTo(29.9f))
            .body("[0].produtos[0].tamanhos[1].id", equalTo("G"))
            .body("[0].produtos[0].tamanhos[1].nome", equalTo("Grande (G)"))
            .body("[0].produtos[0].tamanhos[1].preco", equalTo(34.9f))
            .body("[0].produtos[0].bordas", hasSize(1))
            .body("[0].produtos[0].bordas[0].id", equalTo("requeijao"))
            .body("[0].produtos[0].bordas[0].nome", equalTo("Requeijão"))
            .body("[0].produtos[0].bordas[0].acrescimo", equalTo(0.0f))
            .body("[3].produtos[0].id", equalTo("prod-beb-1"))
            .body("[3].produtos[0]", not(hasKey("tamanhos")))
            .body("[3].produtos[0]", not(hasKey("bordas")))
            .body("[4].produtos", hasSize(4))
            .body("[4].produtos[0].id", equalTo("prod-promo-1"))
            .body("[4].produtos[0].preco", equalTo(62.0f))
            .body("[4].produtos[0].precoOriginal", equalTo(79.8f))
            .body("[4].produtos[0].seloDestaque", equalTo("Super Oferta"));
    }
}
