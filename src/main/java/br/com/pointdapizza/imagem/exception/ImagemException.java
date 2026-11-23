package br.com.pointdapizza.imagem.exception;

import br.com.pointdapizza.shared.dto.response.ErroCampoDto;
import br.com.pointdapizza.shared.exception.ApiException;
import jakarta.ws.rs.core.Response;
import java.util.List;

public class ImagemException extends ApiException {

    public ImagemException(Response.Status status, String mensagem) {
        super(status, mensagem);
    }

    public ImagemException(Response.Status status, String mensagem, List<ErroCampoDto> campos) {
        super(status, mensagem, campos);
    }

    public static ImagemException pastaInvalida(String mensagem) {
        return new ImagemException(
            Response.Status.BAD_REQUEST,
            mensagem,
            List.of(new ErroCampoDto("pasta", "Use 'usuarios' ou 'produtos'."))
        );
    }

    public static ImagemException arquivoInvalido(String mensagem, String detalheCampo) {
        return new ImagemException(
            Response.Status.BAD_REQUEST,
            mensagem,
            List.of(new ErroCampoDto("foto", detalheCampo))
        );
    }

    public static ImagemException configuracaoAusente(String nomeEnv) {
        return new ImagemException(
            Response.Status.INTERNAL_SERVER_ERROR,
            "Configuração do Cloudinary ausente: defina " + nomeEnv + " no ambiente."
        );
    }

    public static ImagemException falhaUpload(String mensagem) {
        return new ImagemException(Response.Status.BAD_GATEWAY, mensagem);
    }
}
