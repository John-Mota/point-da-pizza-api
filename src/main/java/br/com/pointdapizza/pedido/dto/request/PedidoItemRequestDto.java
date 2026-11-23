package br.com.pointdapizza.pedido.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PedidoItemRequestDto(
    @NotBlank(message = "Informe o produto.")
    @Size(max = 64, message = "Identificador do produto inválido.")
    String produtoId,

    String nome,

    @NotNull(message = "Informe a quantidade.")
    @Min(value = 1, message = "A quantidade mínima é 1.")
    Integer quantidade,

    @Size(max = 32, message = "Tamanho inválido.")
    String tamanhoId,

    String tamanhoNome,

    @Size(max = 32, message = "Borda inválida.")
    String bordaId,

    String bordaNome,

    @Size(max = 140, message = "A observação deve ter no máximo 140 caracteres.")
    String observacao,

    BigDecimal precoUnitario,

    BigDecimal total
) {
}
