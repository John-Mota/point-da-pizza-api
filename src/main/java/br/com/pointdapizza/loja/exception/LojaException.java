package br.com.pointdapizza.loja.exception;

import br.com.pointdapizza.shared.exception.ApiException;
import jakarta.ws.rs.core.Response;

public class LojaException extends ApiException {

    public LojaException(Response.Status status, String mensagem) {
        super(status, mensagem);
    }

    public static LojaException naoEncontrada() {
        return new LojaException(Response.Status.NOT_FOUND, "Informações da loja não encontradas.");
    }
}
