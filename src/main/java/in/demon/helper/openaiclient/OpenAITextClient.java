package in.demon.helper.openaiclient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static in.demon.helper.util.Constant.COMPLETIONS_API_URL;

public class OpenAITextClient implements IOpenAIClient{

    private final String apiKey;

    public OpenAITextClient(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String sendToOpenAI(String json) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(COMPLETIONS_API_URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(json.getBytes());
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();
                return jsonObject.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString().trim();
            }
        } catch (Exception e) {
            System.err.println("❌ Error sending to GPT: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
