package br.com.pointdapizza.service;

import br.com.pointdapizza.dto.CardapioCategoriaDto;
import br.com.pointdapizza.dto.CategoriaDto;
import br.com.pointdapizza.dto.ProdutoBordaDto;
import br.com.pointdapizza.dto.ProdutoDto;
import br.com.pointdapizza.dto.ProdutoTamanhoDto;
import br.com.pointdapizza.entity.Categoria;
import br.com.pointdapizza.entity.Produto;
import br.com.pointdapizza.entity.ProdutoBorda;
import br.com.pointdapizza.entity.ProdutoTamanho;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CardapioService {

    @Transactional
    public List<CardapioCategoriaDto> obterCardapio() {
        List<Categoria> categorias = Categoria.list("ativa = true ORDER BY ordem ASC");
        List<Produto> produtos = Produto.list("categoria.ativa = true ORDER BY ordemExibicao ASC, nome ASC");
        List<ProdutoTamanho> tamanhos = ProdutoTamanho.list("ORDER BY ordem ASC");
        List<ProdutoBorda> bordas = ProdutoBorda.list("ORDER BY ordem ASC");

        Map<String, List<ProdutoTamanhoDto>> tamanhosPorProduto = new LinkedHashMap<>();
        for (ProdutoTamanho tamanho : tamanhos) {
            tamanhosPorProduto
                .computeIfAbsent(tamanho.produto.id, k -> new ArrayList<>())
                .add(new ProdutoTamanhoDto(tamanho.codigo, tamanho.nome, tamanho.preco));
        }

        Map<String, List<ProdutoBordaDto>> bordasPorProduto = new LinkedHashMap<>();
        for (ProdutoBorda borda : bordas) {
            bordasPorProduto
                .computeIfAbsent(borda.produto.id, k -> new ArrayList<>())
                .add(new ProdutoBordaDto(borda.codigo, borda.nome, borda.acrescimo));
        }

        Map<String, List<Produto>> produtosPorCategoria = new LinkedHashMap<>();
        for (Produto produto : produtos) {
            produtosPorCategoria
                .computeIfAbsent(produto.categoria.id, k -> new ArrayList<>())
                .add(produto);
        }

        List<CardapioCategoriaDto> resultado = new ArrayList<>();
        for (Categoria categoria : categorias) {
            List<Produto> produtosDaCategoria = produtosPorCategoria.get(categoria.id);
            if (produtosDaCategoria == null || produtosDaCategoria.isEmpty()) {
                continue;
            }

            CategoriaDto categoriaDto = new CategoriaDto(
                categoria.id,
                categoria.nome,
                categoria.ordem,
                categoria.imagemUrl
            );

            List<ProdutoDto> produtosDto = produtosDaCategoria.stream()
                .map(produto -> {
                    List<ProdutoTamanhoDto> tamanhosDto = tamanhosPorProduto.get(produto.id);
                    List<ProdutoBordaDto> bordasDto = bordasPorProduto.get(produto.id);
                    return new ProdutoDto(
                        produto.id,
                        categoria.id,
                        categoria.nome,
                        produto.nome,
                        produto.descricao,
                        produto.preco,
                        produto.precoOriginal,
                        produto.imagemUrl,
                        produto.disponivel,
                        produto.destaque,
                        produto.ordemDestaque,
                        produto.seloDestaque,
                        (tamanhosDto == null || tamanhosDto.isEmpty()) ? null : tamanhosDto,
                        (bordasDto == null || bordasDto.isEmpty()) ? null : bordasDto
                    );
                })
                .toList();

            resultado.add(new CardapioCategoriaDto(categoriaDto, produtosDto));
        }

        return resultado;
    }
}
