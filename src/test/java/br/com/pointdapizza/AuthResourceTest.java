package br.com.pointdapizza;

import br.com.pointdapizza.dto.CadastroRequestDto;
import br.com.pointdapizza.entity.RoleUsuario;
import br.com.pointdapizza.entity.Usuario;
import br.com.pointdapizza.exception.ApiException;
import br.com.pointdapizza.service.AuthService;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.MultiPartSpecBuilder;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(CloudinaryMockTestResource.class)
class AuthResourceTest {

    @Inject
    AuthService authService;

    @Inject
    EntityManager entityManager;

    @Test
    void deveCadastrarViaJsonFazerLoginPorEmailETelefoneEValidarDuplicidades() {
        String cadastroJson = """
            {
              "nome": "Maria Silva",
              "telefone": "(85) 99999-9999",
              "telefoneWhatsapp": true,
              "email": "Maria@Exemplo.com",
              "endereco": "Rua das Flores, 100 - Centro",
              "complemento": "Apto 101",
              "senha": "segredo123"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(cadastroJson)
            .when().post("/api/auth/cadastro")
            .then()
            .statusCode(201)
            .body("mensagem", equalTo("Conta criada com sucesso!"));

        // Verifica hash BCrypt e role padrao USER gravados no banco
        Usuario salvo = Usuario.find("LOWER(email) = ?1", "maria@exemplo.com").firstResult();
        assertNotNull(salvo);
        assertEquals(RoleUsuario.USER, salvo.role);
        assertNull(salvo.cadastradoPorAdmin);
        assertNotEquals("segredo123", salvo.senhaHash);
        assertTrue(salvo.senhaHash.startsWith("$2"));

        // Duplicidade de e-mail (case-insensitive) -> 409
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "nome": "Outra Pessoa",
                  "email": "MARIA@EXEMPLO.COM",
                  "senha": "outrasenha123"
                }
                """)
            .when().post("/api/auth/cadastro")
            .then()
            .statusCode(409)
            .body("mensagem", notNullValue())
            .body("campos[0].campo", equalTo("email"));

        // Duplicidade de telefone (somente digitos) -> 409
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "nome": "Outra Pessoa",
                  "telefone": "85999999999",
                  "senha": "outrasenha123"
                }
                """)
            .when().post("/api/auth/cadastro")
            .then()
            .statusCode(409)
            .body("mensagem", notNullValue())
            .body("campos[0].campo", equalTo("telefone"));

        // Login por e-mail (case-insensitive)
        String tokenEmail = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "email": "MARIA@EXEMPLO.COM",
                  "senha": "segredo123",
                  "lembrarMe": true
                }
                """)
            .when().post("/api/auth/login")
            .then()
            .statusCode(200)
            .body("mensagem", equalTo("Login realizado com sucesso!"))
            .body("usuario.nome", equalTo("Maria Silva"))
            .body("usuario.telefone", equalTo("(85) 99999-9999"))
            .body("usuario.email", equalTo("maria@exemplo.com"))
            .body("usuario.endereco", equalTo("Rua das Flores, 100 - Centro"))
            .body("usuario.complemento", equalTo("Apto 101"))
            .body("usuario.fotoUrl", nullValue())
            .body("token", notNullValue())
            .extract().path("token");

        assertTrue(tokenEmail.split("\\.").length == 3);

        // Login por telefone normalizado (apenas digitos)
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "telefone": "85999999999",
                  "senha": "segredo123"
                }
                """)
            .when().post("/api/auth/login")
            .then()
            .statusCode(200)
            .body("mensagem", equalTo("Login realizado com sucesso!"))
            .body("usuario.nome", equalTo("Maria Silva"))
            .body("token", notNullValue());

        // Login com senha invalida -> 401
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "email": "maria@exemplo.com",
                  "senha": "senhaerrada"
                }
                """)
            .when().post("/api/auth/login")
            .then()
            .statusCode(401)
            .body("mensagem", notNullValue());
    }

    @Test
    void deveCadastrarViaMultipartComFotoEAutenticar() {
        byte[] pngFake = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D
        };

        String dadosJson = """
            {
              "nome": "João Souza",
              "telefone": "(85) 98765-4321",
              "telefoneWhatsapp": false,
              "email": "joao.multipart@exemplo.com",
              "endereco": "Av. Beira Mar, 500",
              "senha": "minhasenha123"
            }
            """;

        given()
            .multiPart(new MultiPartSpecBuilder(dadosJson.getBytes(StandardCharsets.UTF_8))
                .controlName("dados")
                .fileName("dados.json")
                .mimeType("application/json")
                .build())
            .multiPart("foto", "avatar.png", pngFake, "image/png")
            .when().post("/api/auth/cadastro")
            .then()
            .statusCode(201)
            .body("mensagem", equalTo("Conta criada com sucesso!"));

        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "email": "joao.multipart@exemplo.com",
                  "senha": "minhasenha123"
                }
                """)
            .when().post("/api/auth/login")
            .then()
            .statusCode(200)
            .body("usuario.nome", equalTo("João Souza"))
            .body("usuario.fotoUrl", startsWith("https://res.cloudinary.com/test-cloud/image/upload/"))
            .body("usuario.fotoUrl", containsString("point-da-pizza/usuarios/"))
            .body("token", notNullValue());
    }

    @Test
    void deveVincularAdminCadastradorAoPrestadorEBloquearCadastrosInvalidos() {
        UUID adminId = criarUsuarioDireto("Admin Master", "admin.master@pointdapizza.com", RoleUsuario.ADMIN);
        UUID userComumId = criarUsuarioDireto("Cliente Comum", "cliente.comum@pointdapizza.com", RoleUsuario.USER);

        CadastroRequestDto prestadorReq = new CadastroRequestDto(
            "Carlos Entregador",
            "(85) 97777-1234",
            true,
            "prestador@pointdapizza.com",
            "Rua dos Entregadores, 10",
            null,
            "senhaPrestador123"
        );

        // Usuario comum (USER) nao pode cadastrar PRESTADOR
        assertThrows(
            ApiException.class,
            () -> authService.cadastrarPrestador(prestadorReq, null, userComumId)
        );

        // ADMIN cadastra PRESTADOR com sucesso e registra quem cadastrou
        var resposta = authService.cadastrarPrestador(prestadorReq, null, adminId);
        assertEquals("Conta criada com sucesso!", resposta.mensagem());

        Usuario prestador = buscarPrestadorComAdmin("prestador@pointdapizza.com");
        assertNotNull(prestador);
        assertEquals(RoleUsuario.PRESTADOR, prestador.role);
        assertNotNull(prestador.cadastradoPorAdmin);
        assertEquals(adminId, prestador.cadastradoPorAdmin.id);

        // Banco (PostgreSQL CHECK) deve rejeitar PRESTADOR sem cadastrado_por_admin_id
        assertThrows(
            Exception.class,
            () -> tentarInserirPrestadorNoBanco("prestador.sem.admin@pointdapizza.com", null)
        );

        // Banco (PostgreSQL trigger) deve rejeitar PRESTADOR cujo cadastrado_por_admin_id nao eh ADMIN
        assertThrows(
            Exception.class,
            () -> tentarInserirPrestadorNoBanco("prestador.com.user@pointdapizza.com", userComumId)
        );
    }

    @Transactional
    UUID criarUsuarioDireto(String nome, String email, RoleUsuario role) {
        Usuario u = new Usuario();
        u.nome = nome;
        u.email = email;
        u.senhaHash = BcryptUtil.bcryptHash("senha123");
        u.role = role;
        u.ativo = true;
        u.persistAndFlush();
        return u.id;
    }

    @Transactional
    Usuario buscarPrestadorComAdmin(String email) {
        return entityManager.createQuery(
            "SELECT u FROM Usuario u LEFT JOIN FETCH u.cadastradoPorAdmin WHERE LOWER(u.email) = :email",
            Usuario.class
        )
            .setParameter("email", email.toLowerCase())
            .getSingleResult();
    }

    @Transactional
    void tentarInserirPrestadorNoBanco(String email, UUID adminId) {
        entityManager.createNativeQuery("""
            INSERT INTO usuario (nome, email, senha_hash, role, cadastrado_por_admin_id)
            VALUES ('Prestador Teste', :email, :hash, 'PRESTADOR', :adminId)
            """)
            .setParameter("email", email)
            .setParameter("hash", BcryptUtil.bcryptHash("senha123"))
            .setParameter("adminId", adminId)
            .executeUpdate();
    }
}
