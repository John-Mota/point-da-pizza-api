package br.com.pointdapizza.imagem.service;

import br.com.pointdapizza.imagem.dto.response.UploadImagemResponseDto;
import br.com.pointdapizza.imagem.exception.ImagemException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CloudinaryStorageService {

    private static final Logger LOG = Logger.getLogger(CloudinaryStorageService.class);
    private static final long MAX_FOTO_BYTES = 5L * 1024L * 1024L;
    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build();

    @Inject
    ObjectMapper objectMapper;

    @ConfigProperty(name = "pointdapizza.cloudinary.cloud-name")
    Optional<String> cloudName;

    @ConfigProperty(name = "pointdapizza.cloudinary.api-key")
    Optional<String> apiKey;

    @ConfigProperty(name = "pointdapizza.cloudinary.api-secret")
    Optional<String> apiSecret;

    @ConfigProperty(name = "pointdapizza.cloudinary.api-base-url", defaultValue = "https://api.cloudinary.com/v1_1")
    String apiBaseUrl;

    @ConfigProperty(name = "pointdapizza.cloudinary.folder-prefix", defaultValue = "point-da-pizza")
    String folderPrefix;

    public UploadImagemResponseDto uploadImagem(FotoUpload foto, PastaImagem pasta) {
        if (foto == null || foto.arquivo() == null) {
            return null;
        }

        Path origem = foto.arquivo();
        byte[] bytesArquivo;
        try {
            if (!Files.exists(origem) || Files.size(origem) == 0) {
                return null;
            }
            if (Files.size(origem) > MAX_FOTO_BYTES) {
                throw ImagemException.arquivoInvalido(
                    "A foto deve ter no máximo 5 MB.",
                    "A imagem enviada excede o limite de 5 MB."
                );
            }
            bytesArquivo = Files.readAllBytes(origem);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao ler arquivo de imagem enviado.", e);
        }

        String extensao = extrairExtensao(foto.nomeArquivo(), foto.contentType());
        if (!EXTENSOES_PERMITIDAS.contains(extensao)) {
            throw ImagemException.arquivoInvalido(
                "Formato de imagem não suportado.",
                "Envie uma imagem nos formatos JPG, PNG, WEBP ou GIF."
            );
        }

        String resolvedCloudName = exigirConfig(cloudName, "CLOUD_NAME");
        String resolvedApiKey = exigirConfig(apiKey, "CLOUD_API_KEY");
        String resolvedApiSecret = exigirConfig(apiSecret, "CLOUD_API_SECRET");

        String folder = montarPasta(pasta);
        String publicId = UUID.randomUUID().toString();
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        TreeMap<String, String> parametrosAssinados = new TreeMap<>();
        parametrosAssinados.put("folder", folder);
        parametrosAssinados.put("public_id", publicId);
        parametrosAssinados.put("timestamp", timestamp);

        String signature = gerarAssinaturaSha1(parametrosAssinados, resolvedApiSecret);

        String nomeArquivoEnvio = (foto.nomeArquivo() != null && !foto.nomeArquivo().isBlank())
            ? foto.nomeArquivo()
            : publicId + extensao;
        String mimeType = resolverMimeType(extensao, foto.contentType());

        String boundary = "----PointDaPizzaBoundary" + UUID.randomUUID().toString().replace("-", "");
        byte[] corpoMultipart = montarMultipart(
            boundary,
            resolvedApiKey,
            folder,
            publicId,
            timestamp,
            signature,
            nomeArquivoEnvio,
            mimeType,
            bytesArquivo
        );

        String baseUrlNormalizada = apiBaseUrl.endsWith("/")
            ? apiBaseUrl.substring(0, apiBaseUrl.length() - 1)
            : apiBaseUrl;
        URI uploadUri = URI.create(baseUrlNormalizada + "/" + resolvedCloudName + "/image/upload");

        HttpRequest request = HttpRequest.newBuilder(uploadUri)
            .timeout(Duration.ofSeconds(30))
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(HttpRequest.BodyPublishers.ofByteArray(corpoMultipart))
            .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                LOG.errorf("Erro no upload ao Cloudinary (HTTP %d): %s", response.statusCode(), response.body());
                throw ImagemException.falhaUpload("Não foi possível salvar a imagem no Cloudinary. Tente novamente.");
            }

            JsonNode json = objectMapper.readTree(response.body());
            String secureUrl = json.path("secure_url").asText(null);
            if (secureUrl == null || secureUrl.isBlank()) {
                secureUrl = json.path("url").asText(null);
            }
            String returnedPublicId = json.path("public_id").asText(folder + "/" + publicId);

            if (secureUrl == null || secureUrl.isBlank()) {
                LOG.errorf("Resposta do Cloudinary sem secure_url: %s", response.body());
                throw ImagemException.falhaUpload("Resposta inválida do serviço de imagens Cloudinary.");
            }

            return new UploadImagemResponseDto(secureUrl, returnedPublicId);
        } catch (ImagemException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ImagemException.falhaUpload("Envio da imagem ao Cloudinary foi interrompido.");
        } catch (IOException e) {
            LOG.error("Falha de comunicação com o Cloudinary", e);
            throw ImagemException.falhaUpload("Não foi possível conectar ao Cloudinary para salvar a imagem.");
        }
    }

    String gerarAssinaturaSha1(Map<String, String> parametrosOrdenados, String secret) {
        StringBuilder sb = new StringBuilder();
        boolean primeiro = true;
        for (Map.Entry<String, String> entry : parametrosOrdenados.entrySet()) {
            if (!primeiro) {
                sb.append('&');
            }
            sb.append(entry.getKey()).append('=').append(entry.getValue());
            primeiro = false;
        }
        sb.append(secret);

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-1 não disponível.", e);
        }
    }

    private byte[] montarMultipart(
        String boundary,
        String apiKeyValue,
        String folder,
        String publicId,
        String timestamp,
        String signature,
        String fileName,
        String mimeType,
        byte[] fileBytes
    ) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        escreverCampoTexto(out, boundary, "api_key", apiKeyValue);
        escreverCampoTexto(out, boundary, "folder", folder);
        escreverCampoTexto(out, boundary, "public_id", publicId);
        escreverCampoTexto(out, boundary, "timestamp", timestamp);
        escreverCampoTexto(out, boundary, "signature", signature);
        escreverCampoArquivo(out, boundary, "file", fileName, mimeType, fileBytes);
        escreverString(out, "--" + boundary + "--\r\n");
        return out.toByteArray();
    }

    private void escreverCampoTexto(ByteArrayOutputStream out, String boundary, String nome, String valor) {
        escreverString(out, "--" + boundary + "\r\n");
        escreverString(out, "Content-Disposition: form-data; name=\"" + nome + "\"\r\n\r\n");
        escreverString(out, valor + "\r\n");
    }

    private void escreverCampoArquivo(
        ByteArrayOutputStream out,
        String boundary,
        String nomeCampo,
        String fileName,
        String mimeType,
        byte[] bytes
    ) {
        String nomeSeguro = fileName.replace("\"", "_").replace("\r", "").replace("\n", "");
        escreverString(out, "--" + boundary + "\r\n");
        escreverString(out, "Content-Disposition: form-data; name=\"" + nomeCampo + "\"; filename=\"" + nomeSeguro + "\"\r\n");
        escreverString(out, "Content-Type: " + mimeType + "\r\n\r\n");
        out.writeBytes(bytes);
        escreverString(out, "\r\n");
    }

    private void escreverString(ByteArrayOutputStream out, String texto) {
        out.writeBytes(texto.getBytes(StandardCharsets.UTF_8));
    }

    private String montarPasta(PastaImagem pasta) {
        String prefixo = folderPrefix != null ? folderPrefix.trim().replaceAll("^/+|/+$", "") : "";
        if (prefixo.isEmpty()) {
            return pasta.getSubpasta();
        }
        return prefixo + "/" + pasta.getSubpasta();
    }

    private String exigirConfig(Optional<String> valor, String nomeEnv) {
        if (valor == null || valor.isEmpty() || valor.get().isBlank()) {
            throw ImagemException.configuracaoAusente(nomeEnv);
        }
        return valor.get().trim();
    }

    private String extrairExtensao(String fileName, String contentType) {
        if (fileName != null) {
            int ponto = fileName.lastIndexOf('.');
            if (ponto >= 0 && ponto < fileName.length() - 1) {
                String ext = fileName.substring(ponto).toLowerCase(Locale.ROOT);
                if (EXTENSOES_PERMITIDAS.contains(ext)) {
                    return ext;
                }
            }
        }
        if (contentType != null) {
            String mime = contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
            return switch (mime) {
                case "image/jpeg", "image/jpg" -> ".jpg";
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                case "image/gif" -> ".gif";
                default -> "";
            };
        }
        return "";
    }

    private String resolverMimeType(String extensao, String contentTypeOriginal) {
        if (contentTypeOriginal != null && contentTypeOriginal.toLowerCase(Locale.ROOT).startsWith("image/")) {
            return contentTypeOriginal.split(";")[0].trim();
        }
        return switch (extensao) {
            case ".jpg", ".jpeg" -> "image/jpeg";
            case ".png" -> "image/png";
            case ".webp" -> "image/webp";
            case ".gif" -> "image/gif";
            default -> "application/octet-stream";
        };
    }
}
