package br.com.pointdapizza.imagem.service;

import br.com.pointdapizza.imagem.exception.ImagemException;
import java.util.Locale;

public enum PastaImagem {
    USUARIOS("usuarios"),
    PRODUTOS("produtos");

    private final String subpasta;

    PastaImagem(String subpasta) {
        this.subpasta = subpasta;
    }

    public String getSubpasta() {
        return subpasta;
    }

    public static PastaImagem fromParametro(String valor) {
        if (valor == null || valor.isBlank()) {
            throw ImagemException.pastaInvalida("Pasta de destino inválida.");
        }
        return switch (valor.trim().toLowerCase(Locale.ROOT)) {
            case "usuarios", "usuario" -> USUARIOS;
            case "produtos", "produto" -> PRODUTOS;
            default -> throw ImagemException.pastaInvalida("Pasta de destino inválida: " + valor);
        };
    }
}
