package br.com.pointdapizza.pedido.dto.response;

import java.util.List;

public record PedidoItemCriadoDto(
    String nome,
    int quantidade,
    List<String> opcoes
) {
}
