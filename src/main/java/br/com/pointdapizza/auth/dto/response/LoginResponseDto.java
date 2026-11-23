package br.com.pointdapizza.auth.dto.response;

public record LoginResponseDto(
    String mensagem,
    UsuarioLogadoDto usuario,
    String token
) {
}
