package br.com.pointdapizza.resource;

import br.com.pointdapizza.dto.CadastroRequestDto;
import br.com.pointdapizza.dto.CadastroResponseDto;
import br.com.pointdapizza.dto.LoginRequestDto;
import br.com.pointdapizza.dto.LoginResponseDto;
import br.com.pointdapizza.exception.ApiException;
import br.com.pointdapizza.service.AuthService;
import br.com.pointdapizza.service.FotoUpload;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
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

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthService authService;

    @Inject
    ObjectMapper objectMapper;

    @POST
    @Path("/cadastro")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response cadastrarJson(@Valid CadastroRequestDto request) {
        CadastroResponseDto resposta = authService.cadastrar(request, null);
        return Response.status(Response.Status.CREATED).entity(resposta).build();
    }

    @POST
    @Path("/cadastro")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response cadastrarMultipart(MultipartFormDataInput input) throws IOException {
        if (input == null || input.getValues() == null) {
            throw ApiException.badRequest(
                "A parte 'dados' é obrigatória.",
                "dados",
                "Informe os dados de cadastro."
            );
        }

        FormValue dadosPart = primeiroValor(input.getValues().get("dados"));
        if (dadosPart == null) {
            throw ApiException.badRequest(
                "A parte 'dados' é obrigatória.",
                "dados",
                "Informe os dados de cadastro."
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
            throw ApiException.badRequest(
                "A parte 'dados' é obrigatória.",
                "dados",
                "Informe os dados de cadastro."
            );
        }

        CadastroRequestDto request = objectMapper.readValue(dadosBytes, CadastroRequestDto.class);

        FotoUpload foto = null;
        FormValue fotoPart = primeiroValor(input.getValues().get("foto"));
        if (fotoPart != null && fotoPart.isFileItem()) {
            String contentType = fotoPart.getHeaders() != null
                ? fotoPart.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)
                : null;
            foto = new FotoUpload(
                fotoPart.getFileItem().getFile(),
                fotoPart.getFileName(),
                contentType
            );
        }

        CadastroResponseDto resposta = authService.cadastrar(request, foto);
        return Response.status(Response.Status.CREATED).entity(resposta).build();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public LoginResponseDto login(@Valid LoginRequestDto request) {
        return authService.login(request);
    }

    private FormValue primeiroValor(Collection<FormValue> valores) {
        if (valores == null || valores.isEmpty()) {
            return null;
        }
        return valores.iterator().next();
    }
}
