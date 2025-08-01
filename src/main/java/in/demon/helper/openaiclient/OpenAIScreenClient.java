package in.demon.helper.openaiclient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.demon.helper.propertie.IPropertiesProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenAIScreenClient implements IOpenAIClient {
    private static final Gson GSON = new Gson();
    private final IPropertiesProvider propertiesProvider;

    public OpenAIScreenClient(IPropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
    }

    @Override
    public String sendToOpenAI(String json) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(propertiesProvider.getProperty("COMPLETIONS_API_URL")).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + propertiesProvider.getProperty("OPENAI_TOKEN"));
            connection.setRequestProperty("Content-Type", "application/json");

            try (OutputStream os = connection.getOutputStream()) {
                os.write(json.getBytes());
            }

            int code = connection.getResponseCode();
            InputStream responseStream = (code >= 200 && code < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            JsonObject jsonResponse = JsonParser.parseReader(new InputStreamReader(responseStream)).getAsJsonObject();

            String content = jsonResponse
                    .getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            jsonResponse.addProperty("timestamp", System.currentTimeMillis());

            return GSON.toJson(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
