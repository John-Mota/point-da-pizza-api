package br.com.pointdapizza.cardapio.exception;

import br.com.pointdapizza.shared.dto.response.ErroCampoDto;
import br.com.pointdapizza.shared.exception.ApiException;
import jakarta.ws.rs.core.Response;
import java.util.List;

public class CardapioException extends ApiException {

    public CardapioException(Response.Status status, String mensagem) {
        super(status, mensagem);
    }

    public CardapioException(Response.Status status, String mensagem, List<ErroCampoDto> campos) {
        super(status, mensagem, campos);
    }

    public static CardapioException requisicaoInvalida(String mensagem) {
        return new CardapioException(Response.Status.BAD_REQUEST, mensagem);
    }

    public static CardapioException campoInvalido(String mensagem, String campo, String mensagemCampo) {
        return new CardapioException(
            Response.Status.BAD_REQUEST,
            mensagem,
            List.of(new ErroCampoDto(campo, mensagemCampo))
        );
    }

    public static CardapioException produtoNaoEncontrado(String produtoId) {
        return new CardapioException(Response.Status.NOT_FOUND, "Produto não encontrado: " + produtoId);
    }

    public static CardapioException produtoDuplicado() {
        String mensagem = "Já existe um produto cadastrado com este identificador.";
        return new CardapioException(
            Response.Status.CONFLICT,
            mensagem,
            List.of(new ErroCampoDto("id", mensagem))
        );
    }
}
