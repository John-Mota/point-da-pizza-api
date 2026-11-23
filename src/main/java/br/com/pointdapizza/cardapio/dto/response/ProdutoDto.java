package br.com.pointdapizza.cardapio.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.List;

public record ProdutoDto(
    String id,
    String categoriaId,
    String categoria,
    String nome,
    String descricao,
    BigDecimal preco,
    BigDecimal precoOriginal,
    String imagemUrl,
    boolean disponivel,
    boolean destaque,
    Integer ordemDestaque,
    String seloDestaque,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<ProdutoTamanhoDto> tamanhos,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<ProdutoBordaDto> bordas
) {
}
