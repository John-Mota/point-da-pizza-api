package br.com.pointdapizza.pedido.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PedidoCriadoResponseDto(
    UUID id,
    String numero,
    String cliente,
    String telefone,
    String tipoEntrega,
    String endereco,
    String formaPagamento,
    BigDecimal trocoPara,
    List<PedidoItemCriadoDto> itens,
    BigDecimal total,
    String status,
    OffsetDateTime criadoEm
) {
}
