package com.helper.cli.wisper;

import com.google.gson.JsonParser;
import com.helper.cli.propertie.IPropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.nio.charset.StandardCharsets;

public class OpenAITranscribeClient implements IOpenAITranscribeClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAITranscribeClient.class);
    private final IPropertiesProvider propertiesProvider;

    public OpenAITranscribeClient(IPropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
    }

    @Override
    public String transcribeWithOpenAI(File audioFile) {
        try {
            String boundary = "----OpenAIFormBoundary" + System.currentTimeMillis();

            URL url = new URL(propertiesProvider.getProperty("TRANSCRIPTION_API_URL"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + propertiesProvider.getProperty("OPENAI_API_KEY"));
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream out = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true)) {

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
                LOGGER.error("❌ OpenAI API failed: {}", responseCode);
                return null;
            }

        } catch (IOException e) {
            LOGGER.error("❌ Error sending to OpenAI: {}", e.getMessage());
            return null;
        }
    }
}
