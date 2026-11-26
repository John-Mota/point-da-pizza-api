package br.com.pointdapizza.health.resource;

import br.com.pointdapizza.health.dto.response.HealthResponseDto;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.OffsetDateTime;

@Path("/api/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    private static final String SERVICO = "point-da-pizza-api";

    @Inject
    EntityManager entityManager;

    @GET
    public Response verificarStatus(
        @QueryParam("verificarBanco") @DefaultValue("false") boolean verificarBanco
    ) {
        if (!verificarBanco) {
            return Response.ok(new HealthResponseDto(
                "UP",
                true,
                SERVICO,
                null,
                OffsetDateTime.now()
            )).build();
        }

        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            return Response.ok(new HealthResponseDto(
                "UP",
                true,
                SERVICO,
                "UP",
                OffsetDateTime.now()
            )).build();
        } catch (Exception e) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(new HealthResponseDto(
                    "DOWN",
                    false,
                    SERVICO,
                    "DOWN",
                    OffsetDateTime.now()
                ))
                .build();
        }
    }
}
