package com.helper.cli.executor.voice;

import com.helper.cli.propertie.IPropertiesProvider;
import com.helper.cli.rest.RestClient;
import com.helper.cli.voice.BackgroundMicrophone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static com.helper.cli.util.Constant.LANGUAGE_TEXT;

public class VoiceHotkeyExecutor implements IVoiceHotKeyExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(VoiceHotkeyExecutor.class);

    private static final String AUDIO_FILE = "recorded.wav";
    private static final int DURATION_IN_SEC = 15;
    private final IPropertiesProvider propertiesProvider;
    private final AudioFormat format;

    private final BackgroundMicrophone backgroundMicrophone;
    private final RestClient restClient;

    public VoiceHotkeyExecutor(IPropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
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
    public void captureAndProcess(String prompt, String programmingLanguage) {
        backgroundMicrophone.captureDuration(format, AUDIO_FILE);
        File file = new File(AUDIO_FILE);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "audio/wav");
        String finalPrompt = prompt + " " + LANGUAGE_TEXT.formatted(programmingLanguage);
        try {
            HttpResponse<String> response = restClient.postMultipartWav(
                    propertiesProvider.getProperty("aiHelperServerUrl") + "/api/v1/voice",
                    Files.readAllBytes(file.toPath()),
                    finalPrompt,
                    headers);

            int status = response.statusCode();
            if (status == 200) {
                LOGGER.info("Upload successful: {}", status);
            } else {
                LOGGER.error("Upload failed {}: {}", status, response.body());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void requestStop() {
        backgroundMicrophone.stop();
        backgroundMicrophone.close();
    }
}
