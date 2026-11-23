package br.com.pointdapizza.shared.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public record ErroResponseDto(
    String mensagem,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<ErroCampoDto> campos
) {
    public ErroResponseDto(String mensagem) {
        this(mensagem, List.of());
    }
}
