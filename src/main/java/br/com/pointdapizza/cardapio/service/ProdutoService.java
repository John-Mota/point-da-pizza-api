package br.com.pointdapizza.cardapio.service;

import br.com.pointdapizza.cardapio.dto.request.ProdutoNovoRequestDto;
import br.com.pointdapizza.cardapio.dto.response.ProdutoBordaDto;
import br.com.pointdapizza.cardapio.dto.response.ProdutoDto;
import br.com.pointdapizza.cardapio.dto.response.ProdutoTamanhoDto;
import br.com.pointdapizza.cardapio.entity.Categoria;
import br.com.pointdapizza.cardapio.entity.Produto;
import br.com.pointdapizza.cardapio.entity.ProdutoBorda;
import br.com.pointdapizza.cardapio.entity.ProdutoTamanho;
import br.com.pointdapizza.cardapio.exception.CardapioException;
import br.com.pointdapizza.imagem.dto.response.UploadImagemResponseDto;
import br.com.pointdapizza.imagem.service.CloudinaryStorageService;
import br.com.pointdapizza.imagem.service.FotoUpload;
import br.com.pointdapizza.imagem.service.PastaImagem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@ApplicationScoped
public class ProdutoService {

    @Inject
    Validator validator;

    @Inject
    CloudinaryStorageService cloudinaryStorageService;

    @Transactional
    public ProdutoDto criarProduto(ProdutoNovoRequestDto request, FotoUpload foto) {
        if (request == null) {
            throw CardapioException.requisicaoInvalida("Informe os dados do produto.");
        }

        Set<ConstraintViolation<ProdutoNovoRequestDto>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        Categoria categoria = Categoria.findById(request.categoriaId().trim());
        if (categoria == null) {
            throw CardapioException.campoInvalido(
                "Categoria não encontrada: " + request.categoriaId(),
                "categoriaId",
                "Categoria não encontrada."
            );
        }

        if (request.precoOriginal() != null && request.precoOriginal().compareTo(request.preco()) < 0) {
            throw CardapioException.campoInvalido(
                "O preço original deve ser maior ou igual ao preço do produto.",
                "precoOriginal",
                "O preço original não pode ser menor que o preço atual."
            );
        }

        String produtoId = resolverProdutoId(request.id(), request.nome());

        String imagemUrl = trimToNull(request.imagemUrl());
        UploadImagemResponseDto upload = cloudinaryStorageService.uploadImagem(foto, PastaImagem.PRODUTOS);
        if (upload != null) {
            imagemUrl = upload.url();
        }

        Produto produto = new Produto();
        produto.id = produtoId;
        produto.categoria = categoria;
        produto.nome = request.nome().trim();
        produto.descricao = request.descricao().trim();
        produto.preco = request.preco().setScale(2, RoundingMode.HALF_UP);
        produto.precoOriginal = request.precoOriginal() != null
            ? request.precoOriginal().setScale(2, RoundingMode.HALF_UP)
            : null;
        produto.imagemUrl = imagemUrl;
        produto.disponivel = request.disponivel() == null || request.disponivel();
        produto.destaque = Boolean.TRUE.equals(request.destaque());
        produto.ordemDestaque = request.ordemDestaque();
        produto.seloDestaque = trimToNull(request.seloDestaque());
        produto.ordemExibicao = request.ordemExibicao() != null ? request.ordemExibicao() : 1;

        if (request.tamanhos() != null && !request.tamanhos().isEmpty()) {
            Set<String> codigosUsados = new HashSet<>();
            for (int i = 0; i < request.tamanhos().size(); i++) {
                var tReq = request.tamanhos().get(i);
                String codigo = tReq.id().trim();
                if (!codigosUsados.add(codigo.toLowerCase(Locale.ROOT))) {
                    throw CardapioException.campoInvalido(
                        "Código de tamanho duplicado: " + codigo,
                        "tamanhos",
                        "O código de tamanho '" + codigo + "' foi informado mais de uma vez."
                    );
                }
                ProdutoTamanho tamanho = new ProdutoTamanho();
                tamanho.produto = produto;
                tamanho.codigo = codigo;
                tamanho.nome = tReq.nome().trim();
                tamanho.preco = tReq.preco().setScale(2, RoundingMode.HALF_UP);
                tamanho.ordem = (short) (i + 1);
                produto.tamanhos.add(tamanho);
            }
        }

        if (request.bordas() != null && !request.bordas().isEmpty()) {
            Set<String> codigosUsados = new HashSet<>();
            for (int i = 0; i < request.bordas().size(); i++) {
                var bReq = request.bordas().get(i);
                String codigo = bReq.id().trim();
                if (!codigosUsados.add(codigo.toLowerCase(Locale.ROOT))) {
                    throw CardapioException.campoInvalido(
                        "Código de borda duplicado: " + codigo,
                        "bordas",
                        "O código de borda '" + codigo + "' foi informado mais de uma vez."
                    );
                }
                ProdutoBorda borda = new ProdutoBorda();
                borda.produto = produto;
                borda.codigo = codigo;
                borda.nome = bReq.nome().trim();
                borda.acrescimo = bReq.acrescimo().setScale(2, RoundingMode.HALF_UP);
                borda.ordem = (short) (i + 1);
                produto.bordas.add(borda);
            }
        }

        produto.persistAndFlush();

        return mapearParaDto(produto);
    }

    @Transactional
    public ProdutoDto atualizarFoto(String produtoId, FotoUpload foto) {
        if (produtoId == null || produtoId.isBlank()) {
            throw CardapioException.requisicaoInvalida("Informe o identificador do produto.");
        }

        Produto produto = Produto.findById(produtoId.trim());
        if (produto == null) {
            throw CardapioException.produtoNaoEncontrado(produtoId);
        }

        UploadImagemResponseDto upload = cloudinaryStorageService.uploadImagem(foto, PastaImagem.PRODUTOS);
        if (upload == null) {
            throw CardapioException.campoInvalido(
                "Envie o arquivo de imagem no campo 'foto'.",
                "foto",
                "Arquivo de imagem obrigatório."
            );
        }

        produto.imagemUrl = upload.url();
        produto.persistAndFlush();

        return mapearParaDto(produto);
    }

    private ProdutoDto mapearParaDto(Produto produto) {
        List<ProdutoTamanhoDto> tamanhos = produto.tamanhos.stream()
            .sorted(Comparator.comparing(t -> t.ordem))
            .map(t -> new ProdutoTamanhoDto(t.codigo, t.nome, t.preco))
            .toList();

        List<ProdutoBordaDto> bordas = produto.bordas.stream()
            .sorted(Comparator.comparing(b -> b.ordem))
            .map(b -> new ProdutoBordaDto(b.codigo, b.nome, b.acrescimo))
            .toList();

        return new ProdutoDto(
            produto.id,
            produto.categoria.id,
            produto.categoria.nome,
            produto.nome,
            produto.descricao,
            produto.preco,
            produto.precoOriginal,
            produto.imagemUrl,
            produto.disponivel,
            produto.destaque,
            produto.ordemDestaque,
            produto.seloDestaque,
            tamanhos.isEmpty() ? null : tamanhos,
            bordas.isEmpty() ? null : bordas
        );
    }

    private String resolverProdutoId(String idInformado, String nome) {
        String idLimpo = trimToNull(idInformado);
        if (idLimpo != null) {
            if (Produto.findById(idLimpo) != null) {
                throw CardapioException.produtoDuplicado();
            }
            return idLimpo;
        }

        String baseSlug = gerarSlug(nome);
        if (baseSlug.isEmpty()) {
            baseSlug = "prod-" + UUID.randomUUID().toString().substring(0, 8);
        }
        if (baseSlug.length() > 56) {
            baseSlug = baseSlug.substring(0, 56).replaceAll("-+$", "");
        }

        String candidato = baseSlug;
        int sufixo = 2;
        while (Produto.findById(candidato) != null) {
            candidato = baseSlug + "-" + sufixo;
            sufixo++;
        }
        return candidato;
    }

    private String gerarSlug(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return semAcento.toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("^-+|-+$", "");
    }

    private String trimToNull(String valor) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
