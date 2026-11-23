package br.com.pointdapizza;

import com.sun.net.httpserver.HttpServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CloudinaryMockTestResource implements QuarkusTestResourceLifecycleManager {

    private static final String TEST_CLOUD_NAME = "test-cloud";
    private static final String TEST_API_KEY = "test-api-key";
    private static final String TEST_API_SECRET = "test-api-secret";

    private HttpServer server;

    @Override
    public Map<String, String> start() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/" + TEST_CLOUD_NAME + "/image/upload", exchange -> {
                byte[] requestBody = exchange.getRequestBody().readAllBytes();
                String bodyStr = new String(requestBody, StandardCharsets.ISO_8859_1);

                String apiKey = extrairCampo(bodyStr, "api_key");
                String folder = extrairCampo(bodyStr, "folder");
                String publicId = extrairCampo(bodyStr, "public_id");
                String timestamp = extrairCampo(bodyStr, "timestamp");
                String signature = extrairCampo(bodyStr, "signature");

                String esperado = sha1("folder=" + folder + "&public_id=" + publicId + "&timestamp=" + timestamp + TEST_API_SECRET);

                int status;
                String jsonResposta;
                if (!TEST_API_KEY.equals(apiKey) || !esperado.equals(signature)) {
                    status = 401;
                    jsonResposta = "{\"error\":{\"message\":\"Invalid Signature\"}}";
                } else {
                    status = 200;
                    String fullPublicId = folder + "/" + publicId;
                    String secureUrl = "https://res.cloudinary.com/" + TEST_CLOUD_NAME + "/image/upload/v1/" + fullPublicId + ".png";
                    jsonResposta = "{\"secure_url\":\"" + secureUrl + "\",\"public_id\":\"" + fullPublicId + "\"}";
                }

                byte[] bytes = jsonResposta.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(status, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            });
            server.start();
            int port = server.getAddress().getPort();

            return Map.of(
                "pointdapizza.cloudinary.cloud-name", TEST_CLOUD_NAME,
                "pointdapizza.cloudinary.api-key", TEST_API_KEY,
                "pointdapizza.cloudinary.api-secret", TEST_API_SECRET,
                "pointdapizza.cloudinary.api-base-url", "http://127.0.0.1:" + port
            );
        } catch (IOException e) {
            throw new RuntimeException("Falha ao iniciar servidor mock do Cloudinary", e);
        }
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private String extrairCampo(String multipart, String nome) {
        Pattern pattern = Pattern.compile("name=\"" + Pattern.quote(nome) + "\"\\r\\n\\r\\n([^\\r\\n]*)");
        Matcher matcher = pattern.matcher(multipart);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String sha1(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return HexFormat.of().formatHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
