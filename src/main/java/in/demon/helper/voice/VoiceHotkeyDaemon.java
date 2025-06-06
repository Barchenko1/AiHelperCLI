package in.demon.helper.voice;

import in.demon.helper.openaiclient.IOpenAIClient;
import in.demon.helper.openaiclient.OpenAITextClient;
import in.demon.helper.openaiclient.wisper.IOpenAITranscribeClient;
import in.demon.helper.openaiclient.wisper.OpenAITranscribeClient;
import in.demon.helper.websocket.WebSocketClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static in.demon.helper.util.Constant.WEBSOCKET_API_URL;

public class VoiceHotkeyDaemon {

    private static final String AUDIO_FILE = "recorded.wav";
    private static final String TEXT_REQ = "/textRequest.json";
    private final IOpenAITranscribeClient transcribeClient;
    private final IOpenAIClient openAIClient;
    private final WebSocketClient webSocketClient;
    private final Microphone microphone;

    public VoiceHotkeyDaemon(String apiKey) {
        this.transcribeClient = new OpenAITranscribeClient(apiKey);
        this.openAIClient = new OpenAITextClient(apiKey);
        this.microphone = new Microphone();
        this.webSocketClient = new WebSocketClient(WEBSOCKET_API_URL);
    }

    public void execute() {
        microphone.transcribeToFile(AUDIO_FILE);
        String transcript = transcribeClient.transcribeWithOpenAI(new File(AUDIO_FILE));
        if (transcript != null && !transcript.isEmpty()) {
            System.out.println("✅ Final Transcript: " + transcript);
            String jsonPayload = buildJsonPayload(transcript);
            System.out.println(jsonPayload);
            String response = openAIClient.sendToOpenAI(jsonPayload);
            webSocketClient.send(response);
        } else {
            System.out.println("⚠️ No speech detected.");
        }
    }

    public void requestStop() {
        microphone.stop();
    }

    private String buildJsonPayload(String transcript) {
        try (InputStream inputStream = this.getClass().getResourceAsStream(TEXT_REQ)) {
            if (inputStream == null) {
                throw new RuntimeException("JSON template not found: " + TEXT_REQ);
            }

            String template = new String(inputStream.readAllBytes());
            return template.formatted(transcript);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON template", e);
        }
    }
}
