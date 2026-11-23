package br.com.pointdapizza.loja.dto.response;

import java.util.List;

public record LojaResponseDto(
    String nome,
    String slogan,
    String logoUrl,
    String capaUrl,
    boolean aberta,
    List<HorarioFuncionamentoDto> horarios,
    String endereco,
    String telefone,
    List<String> formasPagamento
) {
}
