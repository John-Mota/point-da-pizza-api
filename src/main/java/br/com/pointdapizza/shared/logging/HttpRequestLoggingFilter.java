package br.com.pointdapizza.shared.logging;

import br.com.pointdapizza.shared.dto.response.ErroResponseDto;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.jboss.resteasy.reactive.server.ServerResponseFilter;

public class HttpRequestLoggingFilter {

    private static final Logger LOG = Logger.getLogger(HttpRequestLoggingFilter.class);
    private static final String START_TIME_PROP = "pointdapizza.request.startTimeNanos";

    @ServerRequestFilter
    public void logarEntrada(ContainerRequestContext requestContext) {
        String path = extrairCaminho(requestContext);
        if (deveIgnorar(path)) {
            return;
        }
        requestContext.setProperty(START_TIME_PROP, System.nanoTime());
        LOG.infof("--> %s %s", requestContext.getMethod(), path);
    }

    @ServerResponseFilter
    public void logarSaida(
        ContainerRequestContext requestContext,
        ContainerResponseContext responseContext
    ) {
        String path = extrairCaminho(requestContext);
        if (deveIgnorar(path)) {
            return;
        }

        long duracaoMs = 0L;
        Object inicio = requestContext.getProperty(START_TIME_PROP);
        if (inicio instanceof Long inicioNanos) {
            duracaoMs = (System.nanoTime() - inicioNanos) / 1_000_000L;
        }

        String method = requestContext.getMethod();
        int status = responseContext.getStatus();
        String detalheErro = extrairDetalheErro(responseContext);

        if (status >= 500) {
            LOG.errorf("<-- %s %s | status=%d (ERRO) | %dms%s", method, path, status, duracaoMs, detalheErro);
        } else if (status >= 400) {
            LOG.warnf("<-- %s %s | status=%d (ERRO) | %dms%s", method, path, status, duracaoMs, detalheErro);
        } else {
            LOG.infof("<-- %s %s | status=%d (OK) | %dms", method, path, status, duracaoMs);
        }
    }

    private String extrairCaminho(ContainerRequestContext requestContext) {
        if (requestContext.getUriInfo() == null) {
            return "/";
        }
        String path = requestContext.getUriInfo().getPath();
        String query = requestContext.getUriInfo().getRequestUri().getRawQuery();
        String normalizado = path.startsWith("/") ? path : "/" + path;
        return (query != null && !query.isBlank()) ? normalizado + "?" + query : normalizado;
    }

    private boolean deveIgnorar(String path) {
        return path.startsWith("/q/");
    }

    private String extrairDetalheErro(ContainerResponseContext responseContext) {
        Object entity = responseContext.getEntity();
        if (entity instanceof ErroResponseDto erro && erro.mensagem() != null) {
            return " | motivo=\"" + erro.mensagem() + "\"";
        }
        return "";
    }
}
