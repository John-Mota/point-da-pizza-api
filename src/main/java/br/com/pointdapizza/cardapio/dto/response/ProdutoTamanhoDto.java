package br.com.pointdapizza.cardapio.dto.response;

import java.math.BigDecimal;

public record ProdutoTamanhoDto(
    String id,
    String nome,
    BigDecimal preco
) {
}
