package dashboard.database;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:3000";
    private static final HttpClient client = HttpClient.newHttpClient();

    public static String getData(String endpoint, Map<String, String> params) throws Exception {
        StringBuilder url = new StringBuilder(BASE_URL + "/" + endpoint);

        if (params != null && !params.isEmpty()) {
            url.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!first) url.append("&");
                url.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                url.append("=");
                url.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                first = false;
            }
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url.toString()))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Server returned status " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }
}