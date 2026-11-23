package br.com.pointdapizza.auth.dto.response;

public record UsuarioLogadoDto(
    String nome,
    String telefone,
    String email,
    String endereco,
    String complemento,
    String fotoUrl
) {
}
