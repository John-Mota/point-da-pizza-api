package br.com.pointdapizza.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
    String email,
    String telefone,
    @NotBlank(message = "Informe a senha.")
    String senha,
    Boolean lembrarMe
) {
}
