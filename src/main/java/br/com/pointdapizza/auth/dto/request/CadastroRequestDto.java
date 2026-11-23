package br.com.pointdapizza.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastroRequestDto(
    @NotBlank(message = "Informe o nome.")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
    String nome,

    @Size(max = 20, message = "O telefone deve ter no máximo 20 caracteres.")
    String telefone,

    Boolean telefoneWhatsapp,

    @Email(message = "Informe um e-mail válido.")
    @Size(max = 180, message = "O e-mail deve ter no máximo 180 caracteres.")
    String email,

    @Size(max = 300, message = "O endereço deve ter no máximo 300 caracteres.")
    String endereco,

    @Size(max = 150, message = "O complemento deve ter no máximo 150 caracteres.")
    String complemento,

    @NotBlank(message = "Informe a senha.")
    @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres.")
    @Pattern(
        regexp = "^(?=.*\\p{Lu})(?=.*\\p{Ll}).+$",
        message = "A senha deve conter pelo menos uma letra maiúscula e uma letra minúscula."
    )
    String senha
) {
}
