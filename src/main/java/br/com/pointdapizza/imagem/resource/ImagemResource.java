package br.com.pointdapizza.imagem.resource;

import br.com.pointdapizza.imagem.dto.response.UploadImagemResponseDto;
import br.com.pointdapizza.imagem.exception.ImagemException;
import br.com.pointdapizza.imagem.service.CloudinaryStorageService;
import br.com.pointdapizza.imagem.service.FotoUpload;
import br.com.pointdapizza.imagem.service.PastaImagem;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collection;
import org.jboss.resteasy.reactive.server.multipart.FormValue;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;

@Path("/api/imagens")
@Produces(MediaType.APPLICATION_JSON)
public class ImagemResource {

    @Inject
    CloudinaryStorageService cloudinaryStorageService;

    @POST
    @Path("/{pasta}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadImagem(
        @PathParam("pasta") String pastaParam,
        MultipartFormDataInput input
    ) {
        PastaImagem pasta = PastaImagem.fromParametro(pastaParam);

        FormValue fotoPart = extrairArquivo(input);
        if (fotoPart == null || !fotoPart.isFileItem()) {
            throw ImagemException.arquivoInvalido(
                "Envie o arquivo de imagem no campo 'foto'.",
                "Arquivo de imagem obrigatório."
            );
        }

        String contentType = fotoPart.getHeaders() != null
            ? fotoPart.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)
            : null;
        FotoUpload foto = new FotoUpload(
            fotoPart.getFileItem().getFile(),
            fotoPart.getFileName(),
            contentType
        );

        UploadImagemResponseDto resultado = cloudinaryStorageService.uploadImagem(foto, pasta);
        if (resultado == null) {
            throw ImagemException.arquivoInvalido(
                "O arquivo de imagem enviado está vazio.",
                "Envie um arquivo de imagem válido."
            );
        }

        return Response.status(Response.Status.CREATED).entity(resultado).build();
    }

    private FormValue extrairArquivo(MultipartFormDataInput input) {
        if (input == null || input.getValues() == null) {
            return null;
        }
        FormValue foto = primeiroValor(input.getValues().get("foto"));
        if (foto != null) {
            return foto;
        }
        return primeiroValor(input.getValues().get("file"));
    }

    private FormValue primeiroValor(Collection<FormValue> valores) {
        if (valores == null || valores.isEmpty()) {
            return null;
        }
        return valores.iterator().next();
    }
}
