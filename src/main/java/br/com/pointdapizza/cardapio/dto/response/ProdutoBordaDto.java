package br.com.pointdapizza.cardapio.dto.response;

import java.math.BigDecimal;

public record ProdutoBordaDto(
    String id,
    String nome,
    BigDecimal acrescimo
) {
}
