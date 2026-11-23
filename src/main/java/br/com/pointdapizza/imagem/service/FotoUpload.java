package br.com.pointdapizza.imagem.service;

import java.nio.file.Path;

public record FotoUpload(
    Path arquivo,
    String nomeArquivo,
    String contentType
) {
}
