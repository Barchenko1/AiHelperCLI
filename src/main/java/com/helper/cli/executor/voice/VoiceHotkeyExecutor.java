package com.helper.cli.executor.voice;

import com.helper.cli.wisper.IOpenAITranscribeClient;
import com.helper.cli.wisper.OpenAITranscribeClient;
import com.helper.cli.propertie.IPropertiesProvider;
import com.helper.cli.rest.RestClient;
import com.helper.cli.voice.BackgroundMicrophone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class VoiceHotkeyExecutor implements IVoiceHotKeyExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceHotkeyExecutor.class);

    private static final String AUDIO_FILE = "recorded.wav";
    private static final int DURATION_IN_SEC = 30;

    private final IOpenAITranscribeClient transcribeClient;
    private final AudioFormat format;
    private final String subPrompt;

    private final BackgroundMicrophone backgroundMicrophone;
    private final RestClient restClient;

    public VoiceHotkeyExecutor(IPropertiesProvider propertiesProvider) {
        this.transcribeClient = new OpenAITranscribeClient(propertiesProvider);
        this.subPrompt = propertiesProvider.getPropertyMap().get("prompt.3");
        this.backgroundMicrophone = new BackgroundMicrophone();
        this.format = new AudioFormat(44100, 16, 1, true, false);
        this.restClient = new RestClient();
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
        File file = new File(AUDIO_FILE);
        String transcript = transcribeClient.transcribeWithOpenAI(file);
        Map<String, String> headers = new HashMap<>();
        try {
            restClient.postMultipartPng(
                    "http://localhost:8080/api/v1/voice",
                    Files.readAllBytes(file.toPath()),
                    "",
                    "",
                    headers);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        restClient.postJson(
//                "http://localhost:8080/api/v1/voice",
//                transcript,
//                headers);
    }

    @Override
    public void requestStop() {
        backgroundMicrophone.stop();
        backgroundMicrophone.close();
    }
}
