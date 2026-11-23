package br.com.pointdapizza.pedido.resource;

import br.com.pointdapizza.pedido.dto.request.PedidoNovoRequestDto;
import br.com.pointdapizza.pedido.dto.response.PedidoCriadoResponseDto;
import br.com.pointdapizza.pedido.service.PedidoService;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api/pedidos")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PedidoResource {

    @Inject
    PedidoService pedidoService;

    @Inject
    JWTParser jwtParser;

    @ConfigProperty(name = "pointdapizza.jwt.secret")
    String jwtSecret;

    @POST
    public Response criarPedido(
        @HeaderParam("Idempotency-Key") String idempotencyKey,
        @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
        @Valid PedidoNovoRequestDto request
    ) {
        UUID usuarioId = extrairUsuarioIdOpcional(authorization);
        PedidoCriadoResponseDto pedidoCriado = pedidoService.criarPedido(idempotencyKey, request, usuarioId);
        return Response.status(Response.Status.CREATED).entity(pedidoCriado).build();
    }

    private UUID extrairUsuarioIdOpcional(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        String token = authorization.substring(7).trim();
        if (token.isEmpty()) {
            return null;
        }
        try {
            JsonWebToken jwt = jwtParser.verify(token, jwtSecret);
            String subject = jwt.getSubject();
            return subject != null ? UUID.fromString(subject) : null;
        } catch (ParseException | IllegalArgumentException e) {
            return null;
        }
    }
}
