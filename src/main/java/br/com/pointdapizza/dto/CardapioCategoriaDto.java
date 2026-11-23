package br.com.pointdapizza.dto;

import java.util.List;

public record CardapioCategoriaDto(
    CategoriaDto categoria,
    List<ProdutoDto> produtos
) {
}
