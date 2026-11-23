package br.com.pointdapizza.cardapio.dto.response;

public record CategoriaDto(
    String id,
    String nome,
    Integer ordem,
    String imagemUrl
) {
}
