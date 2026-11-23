package br.com.pointdapizza.resource;

import br.com.pointdapizza.dto.CardapioCategoriaDto;
import br.com.pointdapizza.service.CardapioService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/cardapio")
@Produces(MediaType.APPLICATION_JSON)
public class CardapioResource {

    @Inject
    CardapioService cardapioService;

    @GET
    public List<CardapioCategoriaDto> obterCardapio() {
        return cardapioService.obterCardapio();
    }
}
