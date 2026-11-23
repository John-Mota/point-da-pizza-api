package br.com.pointdapizza.shared.dto.response;

public record ErroCampoDto(
    String campo,
    String mensagem
) {
}
