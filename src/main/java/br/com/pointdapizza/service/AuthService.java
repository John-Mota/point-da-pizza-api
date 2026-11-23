package br.com.pointdapizza.service;

import br.com.pointdapizza.dto.CadastroRequestDto;
import br.com.pointdapizza.dto.CadastroResponseDto;
import br.com.pointdapizza.dto.ErroCampoDto;
import br.com.pointdapizza.dto.LoginRequestDto;
import br.com.pointdapizza.dto.LoginResponseDto;
import br.com.pointdapizza.dto.UploadImagemResponseDto;
import br.com.pointdapizza.dto.UsuarioLogadoDto;
import br.com.pointdapizza.entity.RoleUsuario;
import br.com.pointdapizza.entity.Usuario;
import br.com.pointdapizza.exception.ApiException;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.ws.rs.core.Response;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AuthService {

    @Inject
    EntityManager entityManager;

    @Inject
    Validator validator;

    @Inject
    CloudinaryStorageService cloudinaryStorageService;

    @ConfigProperty(name = "pointdapizza.jwt.issuer", defaultValue = "point-da-pizza")
    String jwtIssuer;

    @ConfigProperty(name = "pointdapizza.jwt.secret")
    String jwtSecret;

    @Transactional
    public CadastroResponseDto cadastrar(CadastroRequestDto request, FotoUpload foto) {
        return criarUsuario(request, foto, RoleUsuario.USER, null);
    }

    @Transactional
    public CadastroResponseDto cadastrarPrestador(CadastroRequestDto request, FotoUpload foto, UUID adminId) {
        if (adminId == null) {
            throw new ApiException(
                Response.Status.FORBIDDEN,
                "Apenas um administrador pode cadastrar prestadores de serviço."
            );
        }
        Usuario admin = Usuario.findById(adminId);
        if (admin == null || !admin.ativo || admin.role != RoleUsuario.ADMIN) {
            throw new ApiException(
                Response.Status.FORBIDDEN,
                "Apenas um administrador ativo pode cadastrar prestadores de serviço."
            );
        }
        return criarUsuario(request, foto, RoleUsuario.PRESTADOR, admin);
    }

    private CadastroResponseDto criarUsuario(
        CadastroRequestDto request,
        FotoUpload foto,
        RoleUsuario role,
        Usuario cadastradoPorAdmin
    ) {
        if (request == null) {
            throw ApiException.badRequest("Informe os dados de cadastro.");
        }

        Set<ConstraintViolation<CadastroRequestDto>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        String email = trimToNull(request.email());
        if (email != null) {
            email = email.toLowerCase(Locale.ROOT);
        }

        String telefone = trimToNull(request.telefone());
        String telefoneDigitos = normalizarDigitos(telefone);
        if (telefone != null && telefoneDigitos.isEmpty()) {
            throw ApiException.badRequest(
                "Informe um número de telefone válido.",
                "telefone",
                "Informe um número de telefone válido."
            );
        }

        if (email == null && telefone == null) {
            throw ApiException.badRequest(
                "Informe pelo menos um e-mail ou telefone.",
                List.of(
                    new ErroCampoDto("email", "Informe um e-mail ou telefone."),
                    new ErroCampoDto("telefone", "Informe um telefone ou e-mail.")
                )
            );
        }

        if (email != null && Usuario.count("LOWER(email) = ?1", email) > 0) {
            throw ApiException.conflict("email", "Este e-mail já está cadastrado.");
        }

        if (telefone != null && existeUsuarioComTelefone(telefoneDigitos)) {
            throw ApiException.conflict("telefone", "Este telefone já está cadastrado.");
        }

        UploadImagemResponseDto upload = cloudinaryStorageService.uploadImagem(foto, PastaImagem.USUARIOS);
        String fotoUrl = upload != null ? upload.url() : null;

        Usuario usuario = new Usuario();
        usuario.nome = request.nome().trim();
        usuario.telefone = telefone;
        usuario.telefoneWhatsapp = request.telefoneWhatsapp() == null || request.telefoneWhatsapp();
        usuario.email = email;
        usuario.endereco = trimToNull(request.endereco());
        usuario.complemento = trimToNull(request.complemento());
        usuario.senhaHash = BcryptUtil.bcryptHash(request.senha());
        usuario.fotoUrl = fotoUrl;
        usuario.role = role;
        usuario.cadastradoPorAdmin = role == RoleUsuario.PRESTADOR ? cadastradoPorAdmin : null;
        usuario.ativo = true;

        usuario.persistAndFlush();

        return new CadastroResponseDto("Conta criada com sucesso!");
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        if (request == null) {
            throw ApiException.badRequest("Informe as credenciais de acesso.");
        }

        String email = trimToNull(request.email());
        String telefone = trimToNull(request.telefone());

        if (email == null && telefone == null) {
            throw ApiException.badRequest(
                "Informe o e-mail ou telefone para entrar.",
                List.of(
                    new ErroCampoDto("email", "Informe o e-mail ou telefone."),
                    new ErroCampoDto("telefone", "Informe o telefone ou e-mail.")
                )
            );
        }

        Usuario usuario = null;
        if (email != null) {
            usuario = Usuario.find("LOWER(email) = ?1 AND ativo = true", email.toLowerCase(Locale.ROOT)).firstResult();
        } else {
            String digitos = normalizarDigitos(telefone);
            if (!digitos.isEmpty()) {
                usuario = buscarUsuarioAtivoPorTelefone(digitos);
            }
        }

        if (usuario == null || !BcryptUtil.matches(request.senha(), usuario.senhaHash)) {
            throw ApiException.unauthorized("Credenciais inválidas. Verifique seus dados e tente novamente.");
        }

        Duration expiracao = Boolean.TRUE.equals(request.lembrarMe())
            ? Duration.ofDays(30)
            : Duration.ofDays(1);

        String roleNome = usuario.role != null ? usuario.role.name() : RoleUsuario.USER.name();

        String token = Jwt.issuer(jwtIssuer)
            .subject(usuario.id.toString())
            .upn(usuario.email != null ? usuario.email : usuario.telefone)
            .groups(Set.of(roleNome))
            .claim("nome", usuario.nome)
            .claim("role", roleNome)
            .expiresIn(expiracao)
            .signWithSecret(jwtSecret);

        UsuarioLogadoDto usuarioDto = new UsuarioLogadoDto(
            usuario.nome,
            usuario.telefone,
            usuario.email,
            usuario.endereco,
            usuario.complemento,
            usuario.fotoUrl
        );

        return new LoginResponseDto("Login realizado com sucesso!", usuarioDto, token);
    }

    private boolean existeUsuarioComTelefone(String digitos) {
        Number count = (Number) entityManager.createNativeQuery(
            "SELECT COUNT(1) FROM usuario WHERE telefone IS NOT NULL AND regexp_replace(telefone, '[^0-9]', '', 'g') = :digitos"
        )
            .setParameter("digitos", digitos)
            .getSingleResult();
        return count != null && count.longValue() > 0;
    }

    @SuppressWarnings("unchecked")
    private Usuario buscarUsuarioAtivoPorTelefone(String digitos) {
        List<Usuario> encontrados = entityManager.createNativeQuery(
            "SELECT * FROM usuario WHERE ativo = true AND telefone IS NOT NULL AND regexp_replace(telefone, '[^0-9]', '', 'g') = :digitos LIMIT 1",
            Usuario.class
        )
            .setParameter("digitos", digitos)
            .getResultList();
        return encontrados.isEmpty() ? null : encontrados.get(0);
    }

    private String normalizarDigitos(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replaceAll("[^0-9]", "");
    }

    private String trimToNull(String valor) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
