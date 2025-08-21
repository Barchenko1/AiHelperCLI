package com.helper.cli.voice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

public class BackgroundMicrophone implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundMicrophone.class);

    private TargetDataLine microphone;
    private CircularAudioBuffer circularBuffer;
    private Thread captureThread;

    public void createMicrophone(AudioFormat format) {
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                LOGGER.error("🚫 Microphone not supported.");
                return;
            }

            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() {
        microphone.start();
    }

    public void stop() {
        if (captureThread != null && captureThread.isAlive()) {
            captureThread.interrupt();
            try {
                captureThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        microphone.stop();
    }

    public void readChunk(AudioFormat format, int durationInSec) {
        int bufferByteSize = durationInSec * (int) format.getSampleRate() * format.getFrameSize();
        circularBuffer = new CircularAudioBuffer(bufferByteSize);

        captureThread = new Thread(() -> {
            byte[] buffer = new byte[16384];
            while (!Thread.currentThread().isInterrupted()) {
                int count = microphone.read(buffer, 0, buffer.length);
                if (count > 0) {
                    circularBuffer.write(buffer, 0, count);
                }
            }
        });
        captureThread.setDaemon(true);
        captureThread.start();
    }

    public void captureDuration(AudioFormat format, String fileName) {
        byte[] snapshot = circularBuffer.getSnapshot();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(snapshot);
             AudioInputStream audioStream = new AudioInputStream(bais, format, snapshot.length / format.getFrameSize())) {
            File wavFile = new File(fileName);
            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, wavFile);
        } catch (IOException e) {
            throw new RuntimeException("Error during capture and processing", e);
        }
    }

    @Override
    public void close() {
        microphone.close();
    }
}
