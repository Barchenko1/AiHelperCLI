package in.demon.helper.executor.voice;

import in.demon.helper.openaiclient.IOpenAIClient;
import in.demon.helper.openaiclient.OpenAITextClient;
import in.demon.helper.openaiclient.wisper.IOpenAITranscribeClient;
import in.demon.helper.openaiclient.wisper.OpenAITranscribeClient;
import in.demon.helper.voice.BackgroundMicrophone;
import in.demon.helper.websocket.WebSocketClient;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static in.demon.helper.util.Constant.WEBSOCKET_API_URL;

public class VoiceHotkeyDaemon implements IVoiceHotKeyDaemon {

    private static final String AUDIO_FILE = "recorded.wav";
    private static final String TEXT_REQ = "/textRequest.json";
    private static final int DURATION_IN_SEC = 30;

    private final IOpenAITranscribeClient transcribeClient;
    private final IOpenAIClient openAIClient;
    private final WebSocketClient webSocketClient;
    private final AudioFormat format;

    private final BackgroundMicrophone backgroundMicrophone;

    public VoiceHotkeyDaemon(String apiKey) {
        this.transcribeClient = new OpenAITranscribeClient(apiKey);
        this.openAIClient = new OpenAITextClient(apiKey);
        this.webSocketClient = new WebSocketClient(WEBSOCKET_API_URL);
        this.backgroundMicrophone = new BackgroundMicrophone();
        this.format = new AudioFormat(44100, 16, 1, true, false);
    }

    @Override
    public void startBackgroundCapture() {
        backgroundMicrophone.createMicrophone(format);
        backgroundMicrophone.start();
        backgroundMicrophone.readChunk(format, DURATION_IN_SEC);
    }

    @Override
    public void captureAndProcess() {
        backgroundMicrophone.captureDuration(format, AUDIO_FILE);
        String transcript = transcribeClient.transcribeWithOpenAI(new File(AUDIO_FILE));
        sendToOpenAITranscript(transcript);
    }

    @Override
    public void requestStop() {
        backgroundMicrophone.stop();
        backgroundMicrophone.close();
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

    private void sendToOpenAITranscript(String transcript) {
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
}
