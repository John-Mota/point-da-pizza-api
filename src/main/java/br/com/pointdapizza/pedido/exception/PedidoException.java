package br.com.pointdapizza.pedido.exception;

import br.com.pointdapizza.shared.dto.response.ErroCampoDto;
import br.com.pointdapizza.shared.exception.ApiException;
import jakarta.ws.rs.core.Response;
import java.util.List;

public class PedidoException extends ApiException {

    public PedidoException(Response.Status status, String mensagem) {
        super(status, mensagem);
    }

    public PedidoException(Response.Status status, String mensagem, List<ErroCampoDto> campos) {
        super(status, mensagem, campos);
    }

    public static PedidoException requisicaoInvalida(String mensagem) {
        return new PedidoException(Response.Status.BAD_REQUEST, mensagem);
    }

    public static PedidoException campoInvalido(String mensagem, String campo, String mensagemCampo) {
        return new PedidoException(
            Response.Status.BAD_REQUEST,
            mensagem,
            List.of(new ErroCampoDto(campo, mensagemCampo))
        );
    }
}
