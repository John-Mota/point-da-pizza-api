package br.com.pointdapizza.loja.resource;

import br.com.pointdapizza.loja.dto.response.LojaResponseDto;
import br.com.pointdapizza.loja.service.LojaService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/loja")
@Produces(MediaType.APPLICATION_JSON)
public class LojaResource {

    @Inject
    LojaService lojaService;

    @GET
    public LojaResponseDto obterLoja() {
        return lojaService.obterLoja();
    }
}
