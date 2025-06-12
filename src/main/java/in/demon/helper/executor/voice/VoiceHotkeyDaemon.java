package in.demon.helper.executor.voice;

import in.demon.helper.openaiclient.IOpenAIClient;
import in.demon.helper.openaiclient.OpenAITextClient;
import in.demon.helper.openaiclient.wisper.IOpenAITranscribeClient;
import in.demon.helper.openaiclient.wisper.OpenAITranscribeClient;
import in.demon.helper.template.ITemplateJsonService;
import in.demon.helper.template.TemplateJsonService;
import in.demon.helper.voice.BackgroundMicrophone;
import in.demon.helper.websocket.WebSocketClient;

import javax.sound.sampled.AudioFormat;
import java.io.File;

import static in.demon.helper.util.Constant.WEBSOCKET_API_URL;

public class VoiceHotkeyDaemon implements IVoiceHotKeyDaemon {

    private static final String AUDIO_FILE = "recorded.wav";
    private static final String TEXT_REQ = "/textRequest.json";
    private static final int DURATION_IN_SEC = 30;

    private final IOpenAITranscribeClient transcribeClient;
    private final ITemplateJsonService transformJsonService;
    private final IOpenAIClient openAIClient;
    private final WebSocketClient webSocketClient;
    private final AudioFormat format;
    private final String subPrompt;

    private final BackgroundMicrophone backgroundMicrophone;

    public VoiceHotkeyDaemon(String apiKey, String subPrompt) {
        this.transcribeClient = new OpenAITranscribeClient(apiKey);
        this.subPrompt = subPrompt;
        this.openAIClient = new OpenAITextClient(apiKey);
        this.transformJsonService = new TemplateJsonService();
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

    private void sendToOpenAITranscript(String transcript) {
        if (transcript != null && !transcript.isEmpty()) {
            System.out.println("✅ Final Transcript: " + transcript);
            String jsonPayload = transformJsonService.buildJsonPayload(TEXT_REQ, transcript, subPrompt);
            System.out.println(jsonPayload);
            String response = openAIClient.sendToOpenAI(jsonPayload);
            webSocketClient.send(response);
        } else {
            System.out.println("⚠️ No speech detected.");
        }
    }
}
