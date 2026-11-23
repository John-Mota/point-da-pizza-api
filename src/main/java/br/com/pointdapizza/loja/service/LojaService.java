package br.com.pointdapizza.loja.service;

import br.com.pointdapizza.loja.dto.response.HorarioFuncionamentoDto;
import br.com.pointdapizza.loja.dto.response.LojaResponseDto;
import br.com.pointdapizza.loja.entity.Loja;
import br.com.pointdapizza.loja.exception.LojaException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class LojaService {

    @Transactional
    public LojaResponseDto obterLoja() {
        Loja loja = Loja.<Loja>find("ORDER BY id ASC").firstResult();
        if (loja == null) {
            throw LojaException.naoEncontrada();
        }

        List<HorarioFuncionamentoDto> horarios = loja.horarios.stream()
            .map(h -> new HorarioFuncionamentoDto(
                h.dia,
                h.abre != null ? h.abre.trim() : null,
                h.fecha != null ? h.fecha.trim() : null
            ))
            .toList();

        List<String> formasPagamento = loja.formasPagamento.stream()
            .map(fp -> fp.descricao)
            .toList();

        return new LojaResponseDto(
            loja.nome,
            loja.slogan,
            loja.logoUrl,
            loja.capaUrl,
            loja.aberta,
            horarios,
            loja.endereco,
            loja.telefone,
            formasPagamento
        );
    }
}
