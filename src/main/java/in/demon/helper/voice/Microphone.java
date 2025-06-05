package in.demon.helper.voice;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Microphone implements AutoCloseable {
    private final TargetDataLine line;
    private final int bufferSize;

    public Microphone(float sampleRate, int bufferSize) throws LineUnavailableException {
        this.bufferSize = bufferSize;
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            throw new LineUnavailableException("Microphone line not supported");
        }

        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
    }

    public void start() {
        line.start();
    }

    public void stop() {
        line.stop();
    }

    public byte[] readChunk() {
        byte[] buffer = new byte[bufferSize];
        int bytesRead = line.read(buffer, 0, buffer.length);
        return bytesRead > 0 ? buffer : new byte[0];
    }

    @Override
    public void close() {
        line.close();
    }
}
