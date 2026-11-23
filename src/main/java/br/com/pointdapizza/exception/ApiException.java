package br.com.pointdapizza.exception;

import br.com.pointdapizza.dto.ErroCampoDto;
import jakarta.ws.rs.core.Response;
import java.util.List;

public class ApiException extends RuntimeException {

    private final Response.Status status;
    private final List<ErroCampoDto> campos;

    public ApiException(Response.Status status, String mensagem) {
        this(status, mensagem, List.of());
    }

    public ApiException(Response.Status status, String mensagem, List<ErroCampoDto> campos) {
        super(mensagem);
        this.status = status;
        this.campos = campos == null ? List.of() : List.copyOf(campos);
    }

    public static ApiException badRequest(String mensagem) {
        return new ApiException(Response.Status.BAD_REQUEST, mensagem);
    }

    public static ApiException badRequest(String mensagem, String campo, String mensagemCampo) {
        return new ApiException(
            Response.Status.BAD_REQUEST,
            mensagem,
            List.of(new ErroCampoDto(campo, mensagemCampo))
        );
    }

    public static ApiException badRequest(String mensagem, List<ErroCampoDto> campos) {
        return new ApiException(Response.Status.BAD_REQUEST, mensagem, campos);
    }

    public static ApiException unauthorized(String mensagem) {
        return new ApiException(Response.Status.UNAUTHORIZED, mensagem);
    }

    public static ApiException notFound(String mensagem) {
        return new ApiException(Response.Status.NOT_FOUND, mensagem);
    }

    public static ApiException conflict(String campo, String mensagem) {
        return new ApiException(
            Response.Status.CONFLICT,
            mensagem,
            List.of(new ErroCampoDto(campo, mensagem))
        );
    }

    public Response.Status getStatus() {
        return status;
    }

    public List<ErroCampoDto> getCampos() {
        return campos;
    }
}
