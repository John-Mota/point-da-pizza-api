package br.com.pointdapizza.pedido;

import br.com.pointdapizza.TestSeedHelper;
import br.com.pointdapizza.pedido.entity.Pedido;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class PedidoResourceTest {

    @Inject
    TestSeedHelper testSeedHelper;

    @BeforeEach
    void prepararDados() {
        testSeedHelper.garantirDadosBase();
    }

    @Test
    void deveCriarPedidoRecalculandoPrecosNoServidorERespeitandoIdempotencyKey() {
        String idempotencyKey = "pedido-test-" + UUID.randomUUID();
        String payload = """
            {
              "clienteNome": "Carlos Mendes",
              "clienteTelefone": "(85) 99999-1122",
              "tipoEntrega": "entrega",
              "endereco": "Rua das Flores, 120 - Aldeota",
              "complemento": "Apto 202",
              "formaPagamento": "Dinheiro",
              "trocoPara": 100.0,
              "itens": [
                {
                  "produtoId": "prod-trad-1",
                  "nome": "Nome Adulterado",
                  "quantidade": 2,
                  "tamanhoId": "G",
                  "tamanhoNome": "Tamanho Adulterado",
                  "bordaId": "requeijao",
                  "bordaNome": "Borda Adulterada",
                  "observacao": "Sem cebola",
                  "precoUnitario": 0.01,
                  "total": 0.02
                },
                {
                  "produtoId": "prod-beb-2",
                  "nome": "Refri",
                  "quantidade": 1,
                  "precoUnitario": 0.01,
                  "total": 0.01
                }
              ],
              "total": 0.03
            }
            """;

        var primeiraResposta = given()
            .contentType(ContentType.JSON)
            .header("Idempotency-Key", idempotencyKey)
            .body(payload)
            .when().post("/api/pedidos")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("numero", startsWith("#"))
            .body("cliente", equalTo("Carlos Mendes"))
            .body("telefone", equalTo("(85) 99999-1122"))
            .body("tipoEntrega", equalTo("entrega"))
            .body("endereco", equalTo("Rua das Flores, 120 - Aldeota"))
            .body("formaPagamento", equalTo("Dinheiro"))
            .body("trocoPara", equalTo(100.0f))
            .body("total", equalTo(82.8f))
            .body("status", equalTo("recebido"))
            .body("criadoEm", notNullValue())
            .body("itens", hasSize(2))
            .body("itens[0].nome", equalTo("Pizza Calabresa Acebolada"))
            .body("itens[0].quantidade", equalTo(2))
            .body("itens[0].opcoes", contains("Tamanho: Grande (G)", "Borda: Requeijão", "Obs: Sem cebola"))
            .body("itens[1].nome", equalTo("Refrigerante 2 Litros"))
            .body("itens[1].quantidade", equalTo(1))
            .body("itens[1].opcoes", hasSize(0))
            .extract();

        String pedidoId = primeiraResposta.path("id");
        String pedidoNumero = primeiraResposta.path("numero");

        given()
            .contentType(ContentType.JSON)
            .header("Idempotency-Key", idempotencyKey)
            .body(payload)
            .when().post("/api/pedidos")
            .then()
            .statusCode(201)
            .body("id", equalTo(pedidoId))
            .body("numero", equalTo(pedidoNumero))
            .body("total", equalTo(82.8f));

        assertEquals(1L, Pedido.count("idempotencyKey", idempotencyKey));
    }

    @Test
    void deveRetornarErroPadraoQuandoIdempotencyKeyAusente() {
        String payload = """
            {
              "clienteNome": "Ana Paula",
              "clienteTelefone": "(85) 98888-0000",
              "tipoEntrega": "retirada",
              "formaPagamento": "Pix",
              "itens": [
                {
                  "produtoId": "prod-beb-1",
                  "quantidade": 1
                }
              ]
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when().post("/api/pedidos")
            .then()
            .statusCode(400)
            .body("mensagem", notNullValue())
            .body("campos[0].campo", equalTo("Idempotency-Key"));
    }

    @Test
    void deveRetornarErroPadraoQuandoValidacaoFalhar() {
        String payload = """
            {
              "clienteNome": "",
              "clienteTelefone": "",
              "tipoEntrega": "drone",
              "formaPagamento": "",
              "itens": []
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("Idempotency-Key", "invalid-" + UUID.randomUUID())
            .body(payload)
            .when().post("/api/pedidos")
            .then()
            .statusCode(400)
            .body("mensagem", notNullValue())
            .body("campos", hasSize(greaterThanOrEqualTo(3)));
    }
}
