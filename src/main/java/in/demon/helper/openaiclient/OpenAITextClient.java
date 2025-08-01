package in.demon.helper.openaiclient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import in.demon.helper.propertie.IPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenAITextClient implements IOpenAIClient{

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAITextClient.class);
    private final IPropertiesProvider propertiesProvider;

    public OpenAITextClient(IPropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
    }

    @Override
    public String sendToOpenAI(String json) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(propertiesProvider.getProperty("COMPLETIONS_API_URL")).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + propertiesProvider.getProperty("OPENAI_API_KEY"));
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
            LOGGER.error("❌ Error sending to GPT: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
