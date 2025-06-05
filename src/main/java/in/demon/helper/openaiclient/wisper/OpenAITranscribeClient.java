package in.demon.helper.openaiclient.wisper;

import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static in.demon.helper.util.Constant.TRANSCRIPTION_API_URL;

public class OpenAITranscribeClient implements IOpenAITranscribeClient {
    private final String apiKey;

    public OpenAITranscribeClient(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String transcribeWithOpenAI(File audioFile) {
        try {
            String boundary = "----OpenAIFormBoundary" + System.currentTimeMillis();

            URL url = new URL(TRANSCRIPTION_API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream out = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"), true)) {

                // file part
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"audio.wav\"\r\n");
                writer.append("Content-Type: audio/wav\r\n\r\n").flush();
                try (FileInputStream inputStream = new FileInputStream(audioFile)) {
                    inputStream.transferTo(out);
                }
                out.flush();
                writer.append("\r\n").flush();

                // model param
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"model\"\r\n\r\n");
                writer.append("whisper-1").append("\r\n").flush();

                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"language\"\r\n\r\n");
                writer.append("en").append("\r\n").flush();

                // close boundary
                writer.append("--").append(boundary).append("--").append("\r\n").flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    return JsonParser
                            .parseString(response.toString())
                            .getAsJsonObject()
                            .get("text")
                            .getAsString();
                }
            } else {
                System.err.println("❌ OpenAI API failed: " + responseCode);
                return null;
            }

        } catch (IOException e) {
            System.err.println("❌ Error sending to OpenAI: " + e.getMessage());
            return null;
        }
    }
}
