package br.com.pointdapizza.pedido.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record PedidoNovoRequestDto(
    @NotBlank(message = "Informe o nome do cliente.")
    @Size(max = 150, message = "O nome do cliente deve ter no máximo 150 caracteres.")
    String clienteNome,

    @NotBlank(message = "Informe o telefone do cliente.")
    @Size(max = 25, message = "O telefone deve ter no máximo 25 caracteres.")
    String clienteTelefone,

    @NotBlank(message = "Informe o tipo de entrega.")
    @Pattern(regexp = "entrega|retirada", message = "O tipo de entrega deve ser 'entrega' ou 'retirada'.")
    String tipoEntrega,

    @Size(max = 300, message = "O endereço deve ter no máximo 300 caracteres.")
    String endereco,

    @Size(max = 150, message = "O complemento deve ter no máximo 150 caracteres.")
    String complemento,

    @NotBlank(message = "Informe a forma de pagamento.")
    @Size(max = 60, message = "A forma de pagamento deve ter no máximo 60 caracteres.")
    String formaPagamento,

    @DecimalMin(value = "0.01", message = "O valor de troco deve ser maior que zero.")
    BigDecimal trocoPara,

    @NotEmpty(message = "O pedido deve conter pelo menos um item.")
    List<@Valid PedidoItemRequestDto> itens,

    BigDecimal total
) {
}
