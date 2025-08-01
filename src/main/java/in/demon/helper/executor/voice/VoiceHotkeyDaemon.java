package in.demon.helper.executor.voice;

import in.demon.helper.openaiclient.IOpenAIClient;
import in.demon.helper.openaiclient.OpenAITextClient;
import in.demon.helper.openaiclient.wisper.IOpenAITranscribeClient;
import in.demon.helper.openaiclient.wisper.OpenAITranscribeClient;
import in.demon.helper.propertie.IPropertiesProvider;
import in.demon.helper.template.ITemplateJsonService;
import in.demon.helper.template.TemplateJsonService;
import in.demon.helper.voice.BackgroundMicrophone;
import in.demon.helper.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.File;

public class VoiceHotkeyDaemon implements IVoiceHotKeyDaemon {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceHotkeyDaemon.class);

    private static final String AUDIO_FILE = "recorded.wav";
    private static final String TEXT_REQ = "/textRequest.json";
    private static final int DURATION_IN_SEC = 30;

    private final IOpenAITranscribeClient transcribeClient;
    private final ITemplateJsonService templateJsonService;
    private final IOpenAIClient openAIClient;
    private final WebSocketClient webSocketClient;
    private final AudioFormat format;
    private final String subPrompt;

    private final BackgroundMicrophone backgroundMicrophone;

    public VoiceHotkeyDaemon(IPropertiesProvider propertiesProvider) {
        this.transcribeClient = new OpenAITranscribeClient(propertiesProvider);
        this.subPrompt = propertiesProvider.getPropertyMap().get("prompt.3");
        this.openAIClient = new OpenAITextClient(propertiesProvider);
        this.templateJsonService = new TemplateJsonService();
        this.webSocketClient = WebSocketClient.getInstance(propertiesProvider);
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
            LOGGER.info("✅ Final Transcript: {}", transcript);
            String jsonPayload = templateJsonService.buildJsonPayload(TEXT_REQ, transcript, subPrompt);
            LOGGER.info(jsonPayload);
            String response = openAIClient.sendToOpenAI(jsonPayload);
            webSocketClient.send(response);
        } else {
            LOGGER.info("⚠️ No speech detected.");
        }
    }
}
