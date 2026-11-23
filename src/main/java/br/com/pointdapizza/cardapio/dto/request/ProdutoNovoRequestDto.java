package br.com.pointdapizza.cardapio.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record ProdutoNovoRequestDto(
    @Size(max = 64, message = "O identificador do produto deve ter no máximo 64 caracteres.")
    String id,

    @NotBlank(message = "Informe a categoria do produto.")
    @Size(max = 64, message = "Identificador da categoria inválido.")
    String categoriaId,

    @NotBlank(message = "Informe o nome do produto.")
    @Size(max = 160, message = "O nome do produto deve ter no máximo 160 caracteres.")
    String nome,

    @NotBlank(message = "Informe a descrição do produto.")
    String descricao,

    @NotNull(message = "Informe o preço do produto.")
    @DecimalMin(value = "0.00", message = "O preço deve ser maior ou igual a zero.")
    BigDecimal preco,

    @DecimalMin(value = "0.00", message = "O preço original deve ser maior ou igual a zero.")
    BigDecimal precoOriginal,

    @Size(max = 500, message = "A URL da imagem deve ter no máximo 500 caracteres.")
    String imagemUrl,

    Boolean disponivel,

    Boolean destaque,

    Integer ordemDestaque,

    @Size(max = 60, message = "O selo de destaque deve ter no máximo 60 caracteres.")
    String seloDestaque,

    Integer ordemExibicao,

    List<@Valid ProdutoTamanhoRequestDto> tamanhos,

    List<@Valid ProdutoBordaRequestDto> bordas
) {
    public record ProdutoTamanhoRequestDto(
        @NotBlank(message = "Informe o código do tamanho.")
        @Size(max = 32, message = "O código do tamanho deve ter no máximo 32 caracteres.")
        String id,

        @NotBlank(message = "Informe o nome do tamanho.")
        @Size(max = 80, message = "O nome do tamanho deve ter no máximo 80 caracteres.")
        String nome,

        @NotNull(message = "Informe o preço do tamanho.")
        @DecimalMin(value = "0.00", message = "O preço do tamanho deve ser maior ou igual a zero.")
        BigDecimal preco
    ) {
    }

    public record ProdutoBordaRequestDto(
        @NotBlank(message = "Informe o código da borda.")
        @Size(max = 32, message = "O código da borda deve ter no máximo 32 caracteres.")
        String id,

        @NotBlank(message = "Informe o nome da borda.")
        @Size(max = 80, message = "O nome da borda deve ter no máximo 80 caracteres.")
        String nome,

        @NotNull(message = "Informe o acréscimo da borda.")
        @DecimalMin(value = "0.00", message = "O acréscimo da borda deve ser maior ou igual a zero.")
        BigDecimal acrescimo
    ) {
    }
}
