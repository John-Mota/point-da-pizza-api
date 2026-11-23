package br.com.pointdapizza.cardapio.dto.response;

import java.util.List;

public record CardapioCategoriaDto(
    CategoriaDto categoria,
    List<ProdutoDto> produtos
) {
}
