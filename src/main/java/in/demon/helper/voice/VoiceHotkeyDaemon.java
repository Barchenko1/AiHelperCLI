package in.demon.helper.voice;

import in.demon.helper.openaiclient.IOpenAIClient;
import in.demon.helper.openaiclient.OpenAITextClient;
import in.demon.helper.openaiclient.wisper.IOpenAITranscribeClient;
import in.demon.helper.openaiclient.wisper.OpenAITranscribeClient;
import in.demon.helper.websocket.WebSocketClient;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import static in.demon.helper.util.Constant.WEBSOCKET_API_URL;

public class VoiceHotkeyDaemon {

    private static final String AUDIO_FILE = "recorded.wav";
    private final IOpenAITranscribeClient transcribeClient;
    private final IOpenAIClient openAIClient;
    private final WebSocketClient webSocketClient;

    public VoiceHotkeyDaemon(String apiKey) {
        this.transcribeClient = new OpenAITranscribeClient(apiKey);
        this.openAIClient = new OpenAITextClient(apiKey);
        this.webSocketClient = new WebSocketClient(WEBSOCKET_API_URL);
    }

    public void execute() {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("🚫 Microphone not supported.");
                return;
            }

            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
            byte[] buffer = new byte[16384];

            Scanner scanner = new Scanner(System.in);
            System.out.println("🎤 Press [Enter] to start speaking. Ctrl+C to stop.");

            while (true) {
                scanner.nextLine(); // Wait for Enter
                System.out.println("🎙️ Listening... Press [Enter] again to stop.");

                microphone.start();

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Thread recordingThread = new Thread(() -> {
                    try {
                        while (!Thread.currentThread().isInterrupted()) {
                            int count = microphone.read(buffer, 0, buffer.length);
                            if (count > 0) {
                                out.write(buffer, 0, count);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error while recording: " + e.getMessage());
                    }
                });

                recordingThread.start();
                scanner.nextLine(); // Wait for second Enter
                recordingThread.interrupt();
                microphone.stop();

                // Save to WAV file
                byte[] audioBytes = out.toByteArray();
                try (ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
                     AudioInputStream audioStream = new AudioInputStream(bais, format, audioBytes.length / format.getFrameSize())) {
                    File wavFile = new File(AUDIO_FILE);
                    AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, wavFile);
                }

                // Send to OpenAI Whisper
                String transcript = transcribeClient.transcribeWithOpenAI(new File(AUDIO_FILE));
                if (transcript != null && !transcript.isEmpty()) {
                    System.out.println("✅ Final Transcript: " + transcript);
//                    openAIClient.sendToOpenAI(transcript);
                } else {
                    System.out.println("⚠️ No speech detected.");
                }

                System.out.println("🔁 Ready for next input. Press Enter...");
            }
        } catch (IOException | LineUnavailableException ex) {
            throw new RuntimeException(ex);
        }
    }
}
