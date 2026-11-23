package br.com.pointdapizza.pedido.service;

import br.com.pointdapizza.auth.entity.Usuario;
import br.com.pointdapizza.cardapio.entity.Produto;
import br.com.pointdapizza.cardapio.entity.ProdutoBorda;
import br.com.pointdapizza.cardapio.entity.ProdutoTamanho;
import br.com.pointdapizza.pedido.dto.request.PedidoItemRequestDto;
import br.com.pointdapizza.pedido.dto.request.PedidoNovoRequestDto;
import br.com.pointdapizza.pedido.dto.response.PedidoCriadoResponseDto;
import br.com.pointdapizza.pedido.dto.response.PedidoItemCriadoDto;
import br.com.pointdapizza.pedido.entity.Pedido;
import br.com.pointdapizza.pedido.entity.PedidoItem;
import br.com.pointdapizza.pedido.exception.PedidoException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PedidoService {

    @Transactional
    public PedidoCriadoResponseDto criarPedido(
        String idempotencyKey,
        PedidoNovoRequestDto request,
        UUID usuarioId
    ) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw PedidoException.campoInvalido(
                "O cabeçalho Idempotency-Key é obrigatório.",
                "Idempotency-Key",
                "Informe uma chave de idempotência válida."
            );
        }

        String chaveNormalizada = idempotencyKey.trim();
        if (chaveNormalizada.length() > 100) {
            throw PedidoException.campoInvalido(
                "O cabeçalho Idempotency-Key deve ter no máximo 100 caracteres.",
                "Idempotency-Key",
                "Máximo de 100 caracteres."
            );
        }

        Pedido pedidoExistente = Pedido.find("idempotencyKey", chaveNormalizada).firstResult();
        if (pedidoExistente != null) {
            return mapearResposta(pedidoExistente);
        }

        if (request == null) {
            throw PedidoException.requisicaoInvalida("Informe os dados do pedido.");
        }

        String tipoEntrega = request.tipoEntrega().trim();
        String endereco = trimToNull(request.endereco());
        if ("entrega".equals(tipoEntrega) && endereco == null) {
            throw PedidoException.campoInvalido(
                "Informe o endereço para entrega.",
                "endereco",
                "O endereço é obrigatório para pedidos de entrega."
            );
        }

        Pedido pedido = new Pedido();
        pedido.idempotencyKey = chaveNormalizada;
        pedido.clienteNome = request.clienteNome().trim();
        pedido.clienteTelefone = request.clienteTelefone().trim();
        pedido.tipoEntrega = tipoEntrega;
        pedido.endereco = endereco;
        pedido.complemento = trimToNull(request.complemento());
        pedido.formaPagamento = request.formaPagamento().trim();
        pedido.trocoPara = request.trocoPara() != null
            ? request.trocoPara().setScale(2, RoundingMode.HALF_UP)
            : null;
        pedido.status = "recebido";

        if (usuarioId != null) {
            pedido.usuario = Usuario.findById(usuarioId);
        }

        BigDecimal totalPedido = BigDecimal.ZERO;
        List<PedidoItemRequestDto> itensReq = request.itens();

        for (int i = 0; i < itensReq.size(); i++) {
            PedidoItemRequestDto itemReq = itensReq.get(i);
            Produto produto = Produto.findById(itemReq.produtoId().trim());
            if (produto == null) {
                throw PedidoException.campoInvalido(
                    "Produto não encontrado: " + itemReq.produtoId(),
                    "itens",
                    "Produto não encontrado: " + itemReq.produtoId()
                );
            }
            if (!produto.disponivel) {
                throw PedidoException.campoInvalido(
                    "O produto '" + produto.nome + "' está indisponível no momento.",
                    "itens",
                    "O produto '" + produto.nome + "' está indisponível no momento."
                );
            }

            BigDecimal precoBase;
            String tamanhoId = null;
            String tamanhoNome = null;
            String tamanhoReq = trimToNull(itemReq.tamanhoId());
            if (tamanhoReq != null) {
                ProdutoTamanho tamanho = ProdutoTamanho
                    .find("produto.id = ?1 AND codigo = ?2", produto.id, tamanhoReq)
                    .firstResult();
                if (tamanho == null) {
                    throw PedidoException.campoInvalido(
                        "Tamanho inválido para o produto " + produto.nome + ".",
                        "itens",
                        "Tamanho '" + tamanhoReq + "' não encontrado para " + produto.nome + "."
                    );
                }
                precoBase = tamanho.preco;
                tamanhoId = tamanho.codigo;
                tamanhoNome = tamanho.nome;
            } else {
                precoBase = produto.preco;
            }

            BigDecimal acrescimoBorda = BigDecimal.ZERO;
            String bordaId = null;
            String bordaNome = null;
            String bordaReq = trimToNull(itemReq.bordaId());
            if (bordaReq != null) {
                ProdutoBorda borda = ProdutoBorda
                    .find("produto.id = ?1 AND codigo = ?2", produto.id, bordaReq)
                    .firstResult();
                if (borda == null) {
                    throw PedidoException.campoInvalido(
                        "Borda inválida para o produto " + produto.nome + ".",
                        "itens",
                        "Borda '" + bordaReq + "' não encontrada para " + produto.nome + "."
                    );
                }
                acrescimoBorda = borda.acrescimo;
                bordaId = borda.codigo;
                bordaNome = borda.nome;
            }

            BigDecimal precoUnitario = precoBase
                .add(acrescimoBorda)
                .setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalItem = precoUnitario
                .multiply(BigDecimal.valueOf(itemReq.quantidade()))
                .setScale(2, RoundingMode.HALF_UP);

            PedidoItem pedidoItem = new PedidoItem();
            pedidoItem.pedido = pedido;
            pedidoItem.produto = produto;
            pedidoItem.nome = produto.nome;
            pedidoItem.quantidade = itemReq.quantidade();
            pedidoItem.tamanhoId = tamanhoId;
            pedidoItem.tamanhoNome = tamanhoNome;
            pedidoItem.bordaId = bordaId;
            pedidoItem.bordaNome = bordaNome;
            pedidoItem.observacao = trimToNull(itemReq.observacao());
            pedidoItem.precoUnitario = precoUnitario;
            pedidoItem.total = totalItem;
            pedidoItem.ordem = (short) (i + 1);

            pedido.itens.add(pedidoItem);
            totalPedido = totalPedido.add(totalItem);
        }

        pedido.total = totalPedido.setScale(2, RoundingMode.HALF_UP);
        pedido.persistAndFlush();

        return mapearResposta(pedido);
    }

    private PedidoCriadoResponseDto mapearResposta(Pedido pedido) {
        List<PedidoItemCriadoDto> itens = pedido.itens.stream()
            .sorted(Comparator.comparing(item -> item.ordem))
            .map(item -> {
                List<String> opcoes = new ArrayList<>();
                if (item.tamanhoNome != null && !item.tamanhoNome.isBlank()) {
                    opcoes.add("Tamanho: " + item.tamanhoNome);
                }
                if (item.bordaNome != null && !item.bordaNome.isBlank()) {
                    opcoes.add("Borda: " + item.bordaNome);
                }
                if (item.observacao != null && !item.observacao.isBlank()) {
                    opcoes.add("Obs: " + item.observacao);
                }
                return new PedidoItemCriadoDto(item.nome, item.quantidade, opcoes);
            })
            .toList();

        return new PedidoCriadoResponseDto(
            pedido.id,
            pedido.numero,
            pedido.clienteNome,
            pedido.clienteTelefone,
            pedido.tipoEntrega,
            pedido.endereco,
            pedido.formaPagamento,
            pedido.trocoPara,
            itens,
            pedido.total,
            pedido.status,
            pedido.criadoEm
        );
    }

    private String trimToNull(String valor) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
