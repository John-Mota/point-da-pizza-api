package br.com.pointdapizza.cardapio.resource;

import br.com.pointdapizza.cardapio.dto.request.ProdutoNovoRequestDto;
import br.com.pointdapizza.cardapio.dto.response.ProdutoDto;
import br.com.pointdapizza.cardapio.exception.CardapioException;
import br.com.pointdapizza.cardapio.service.ProdutoService;
import br.com.pointdapizza.imagem.service.FotoUpload;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;

@Path("/api/produtos")
@Produces(MediaType.APPLICATION_JSON)
public class ProdutoResource {

    @Inject
    ProdutoService produtoService;

    @Inject
    ObjectMapper objectMapper;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response criarProdutoJson(@Valid ProdutoNovoRequestDto request) {
        ProdutoDto criado = produtoService.criarProduto(request, null);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response criarProdutoMultipart(MultipartFormDataInput input) throws IOException {
        if (input == null || input.getValues() == null) {
            throw CardapioException.campoInvalido(
                "A parte 'dados' é obrigatória.",
                "dados",
                "Informe os dados do produto."
            );
        }

        FormValue dadosPart = primeiroValor(input.getValues().get("dados"));
        if (dadosPart == null) {
            throw CardapioException.campoInvalido(
                "A parte 'dados' é obrigatória.",
                "dados",
                "Informe os dados do produto."
            );
        }

        byte[] dadosBytes;
        if (dadosPart.isFileItem()) {
            dadosBytes = Files.readAllBytes(dadosPart.getFileItem().getFile());
        } else {
            String valor = dadosPart.getValue();
            dadosBytes = valor != null ? valor.getBytes(StandardCharsets.UTF_8) : new byte[0];
        }

        if (dadosBytes.length == 0) {
            throw CardapioException.campoInvalido(
                "A parte 'dados' é obrigatória.",
                "dados",
                "Informe os dados do produto."
            );
        }

        ProdutoNovoRequestDto request = objectMapper.readValue(dadosBytes, ProdutoNovoRequestDto.class);
        FotoUpload foto = extrairFotoOpcional(input);

        ProdutoDto criado = produtoService.criarProduto(request, foto);
        return Response.status(Response.Status.CREATED).entity(criado).build();
    }

    @PUT
    @Path("/{id}/foto")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public ProdutoDto atualizarFoto(
        @PathParam("id") String produtoId,
        MultipartFormDataInput input
    ) {
        FotoUpload foto = extrairFotoOpcional(input);
        if (foto == null) {
            throw CardapioException.campoInvalido(
                "Envie o arquivo de imagem no campo 'foto'.",
                "foto",
                "Arquivo de imagem obrigatório."
            );
        }
        return produtoService.atualizarFoto(produtoId, foto);
    }

    private FotoUpload extrairFotoOpcional(MultipartFormDataInput input) {
        if (input == null || input.getValues() == null) {
            return null;
        }
        FormValue fotoPart = primeiroValor(input.getValues().get("foto"));
        if (fotoPart == null) {
            fotoPart = primeiroValor(input.getValues().get("imagem"));
        }
        if (fotoPart == null || !fotoPart.isFileItem()) {
            return null;
        }
        String contentType = fotoPart.getHeaders() != null
            ? fotoPart.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)
            : null;
        return new FotoUpload(
            fotoPart.getFileItem().getFile(),
            fotoPart.getFileName(),
            contentType
        );
    }

    private FormValue primeiroValor(Collection<FormValue> valores) {
        if (valores == null || valores.isEmpty()) {
            return null;
        }
        return valores.iterator().next();
    }
}
