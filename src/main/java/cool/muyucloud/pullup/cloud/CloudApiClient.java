package cool.muyucloud.pullup.cloud;

import com.google.gson.Gson;
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.Config;
import cool.muyucloud.pullup.util.PlatformAccess;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class CloudApiClient {
    private static final Gson GSON = new Gson();

    private final HttpClient httpClient;
    private final String baseUrl;

    public CloudApiClient(String baseUrl) {
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    public static CloudApiClient fromConfig() {
        Config config = Pullup.getConfig();
        return new CloudApiClient(config.getAsString("cloudBaseUrl"));
    }

    public CloudApiModels.SessionCreateResponse createSession(String sourceSetName, String sourceJson) throws CloudApiException {
        CloudApiModels.SessionCreateRequest request = new CloudApiModels.SessionCreateRequest();
        request.client = "pullup-mod";
        request.modVersion = Pullup.getModVersion();
        request.minecraftVersion = PlatformAccess.getMinecraftVersion();
        request.loader = PlatformAccess.getLoaderName();
        request.language = CloudClientContextCollector.detectLanguage();
        request.schemaVersion = CloudApiModels.SUPPORTED_SCHEMA_VERSION;
        request.sourceSetName = sourceSetName;
        request.sourceJson = sourceJson;
        request.clientContext = CloudClientContextCollector.collect();
        return sendJson("POST", "/api/v1/sessions", request, CloudApiModels.SessionCreateResponse.class);
    }

    public CloudApiModels.PackResponse fetchPack(String code) throws CloudApiException {
        String normalizedCode = code == null ? "" : code.trim().toUpperCase();
        if (normalizedCode.isEmpty()) {
            throw new CloudApiException("INVALID_REQUEST", "Import code can not be empty.");
        }
        return sendJson("GET", "/api/v1/packs/" + URLEncoder.encode(normalizedCode, StandardCharsets.UTF_8), null, CloudApiModels.PackResponse.class);
    }

    private <T> T sendJson(String method, String path, Object requestBody, Class<T> responseType) throws CloudApiException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(resolve(path))
            .timeout(Duration.ofSeconds(20))
            .header("Accept", "application/json");

        if (requestBody != null) {
            builder.header("Content-Type", "application/json; charset=utf-8");
            builder.method(method, HttpRequest.BodyPublishers.ofString(GSON.toJson(requestBody), StandardCharsets.UTF_8));
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        HttpResponse<String> response;
        try {
            response = this.httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new CloudApiException("NETWORK_ERROR", "Failed to reach the PullUp cloud service: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CloudApiException("NETWORK_INTERRUPTED", "The PullUp cloud request was interrupted.");
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw parseError(response);
        }

        try {
            return GSON.fromJson(response.body(), responseType);
        } catch (Exception e) {
            throw new CloudApiException("INVALID_RESPONSE", "The PullUp cloud service returned invalid JSON.");
        }
    }

    private CloudApiException parseError(HttpResponse<String> response) {
        try {
            CloudApiModels.ErrorEnvelope envelope = GSON.fromJson(response.body(), CloudApiModels.ErrorEnvelope.class);
            if (envelope != null && envelope.error != null) {
                return new CloudApiException(envelope.error.code, envelope.error.message);
            }
        } catch (Exception ignored) {
        }
        return new CloudApiException("HTTP_" + response.statusCode(), "The PullUp cloud service returned HTTP " + response.statusCode() + ".");
    }

    private URI resolve(String path) {
        return URI.create(this.baseUrl + path);
    }

    private static String normalizeBaseUrl(String baseUrl) {
        String value = baseUrl == null ? "" : baseUrl.trim();
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
