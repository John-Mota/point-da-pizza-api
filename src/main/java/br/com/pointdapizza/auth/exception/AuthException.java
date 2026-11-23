package br.com.pointdapizza.auth.exception;

import br.com.pointdapizza.shared.dto.response.ErroCampoDto;
import br.com.pointdapizza.shared.exception.ApiException;
import jakarta.ws.rs.core.Response;
import java.util.List;

public class AuthException extends ApiException {

    public AuthException(Response.Status status, String mensagem) {
        super(status, mensagem);
    }

    public AuthException(Response.Status status, String mensagem, List<ErroCampoDto> campos) {
        super(status, mensagem, campos);
    }

    public static AuthException dadosObrigatorios(String mensagem) {
        return new AuthException(Response.Status.BAD_REQUEST, mensagem);
    }

    public static AuthException campoInvalido(String mensagem, String campo, String mensagemCampo) {
        return new AuthException(
            Response.Status.BAD_REQUEST,
            mensagem,
            List.of(new ErroCampoDto(campo, mensagemCampo))
        );
    }

    public static AuthException emailOuTelefoneObrigatorio(String mensagem, String detalheEmail, String detalheTelefone) {
        return new AuthException(
            Response.Status.BAD_REQUEST,
            mensagem,
            List.of(
                new ErroCampoDto("email", detalheEmail),
                new ErroCampoDto("telefone", detalheTelefone)
            )
        );
    }

    public static AuthException duplicado(String campo, String mensagem) {
        return new AuthException(
            Response.Status.CONFLICT,
            mensagem,
            List.of(new ErroCampoDto(campo, mensagem))
        );
    }

    public static AuthException credenciaisInvalidas() {
        return new AuthException(
            Response.Status.UNAUTHORIZED,
            "Credenciais inválidas. Verifique seus dados e tente novamente."
        );
    }

    public static AuthException permissaoNegada(String mensagem) {
        return new AuthException(Response.Status.FORBIDDEN, mensagem);
    }
}
