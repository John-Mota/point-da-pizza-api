package br.com.pointdapizza.shared.exception;

import br.com.pointdapizza.shared.dto.response.ErroCampoDto;
import br.com.pointdapizza.shared.dto.response.ErroResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ApiExceptionMapper {

    private static final Logger LOG = Logger.getLogger(ApiExceptionMapper.class);

    @ServerExceptionMapper(priority = Priorities.USER)
    public Response mapApiException(ApiException ex) {
        return Response.status(ex.getStatus())
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(new ErroResponseDto(ex.getMessage(), ex.getCampos()))
            .build();
    }

    @ServerExceptionMapper(priority = Priorities.USER)
    public Response mapConstraintViolation(ConstraintViolationException ex) {
        List<ErroCampoDto> campos = ex.getConstraintViolations().stream()
            .map(violation -> new ErroCampoDto(extrairNomeCampo(violation), violation.getMessage()))
            .toList();

        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(new ErroResponseDto("Dados inválidos. Verifique os campos informados.", campos))
            .build();
    }

    @ServerExceptionMapper(priority = Priorities.USER)
    public Response mapJsonProcessing(JsonProcessingException ex) {
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(new ErroResponseDto("Formato de JSON inválido na requisição."))
            .build();
    }

    @ServerExceptionMapper(priority = Priorities.USER)
    public Response mapWebApplication(WebApplicationException ex) {
        int status = ex.getResponse() != null ? ex.getResponse().getStatus() : 500;
        String mensagem = switch (status) {
            case 400 -> "Requisição inválida.";
            case 401 -> "Não autorizado.";
            case 403 -> "Acesso negado.";
            case 404 -> "Recurso não encontrado.";
            case 405 -> "Método HTTP não permitido.";
            case 415 -> "Tipo de conteúdo não suportado.";
            default -> status >= 500
                ? "Ocorreu um erro interno inesperado. Tente novamente mais tarde."
                : "Não foi possível processar a requisição.";
        };
        return Response.status(status)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(new ErroResponseDto(mensagem))
            .build();
    }

    @ServerExceptionMapper(priority = Priorities.USER + 100)
    public Response mapUnexpected(Throwable ex) {
        LOG.error("Erro inesperado na API", ex);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .entity(new ErroResponseDto("Ocorreu um erro interno inesperado. Tente novamente mais tarde."))
            .build();
    }

    private String extrairNomeCampo(ConstraintViolation<?> violation) {
        List<String> partes = new ArrayList<>();
        for (Path.Node node : violation.getPropertyPath()) {
            switch (node.getKind()) {
                case PROPERTY, CONTAINER_ELEMENT -> {
                    if (node.getName() != null) {
                        partes.add(node.getName());
                    }
                }
                case PARAMETER -> {
                    if (partes.isEmpty() && node.getName() != null) {
                        partes.add(node.getName());
                    }
                }
                default -> {
                }
            }
        }
        return partes.isEmpty() ? "requisicao" : partes.get(partes.size() - 1);
    }
}
