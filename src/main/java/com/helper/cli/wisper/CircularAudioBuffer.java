package com.helper.cli.wisper;

import java.io.ByteArrayOutputStream;

public class CircularAudioBuffer {
    private final byte[] buffer;
    private int writePos = 0;
    private boolean filled = false;

    public CircularAudioBuffer(int sizeBytes) {
        this.buffer = new byte[sizeBytes];
    }

    public synchronized void write(byte[] data, int offset, int length) {
        int remaining = buffer.length - writePos;
        if (length <= remaining) {
            System.arraycopy(data, offset, buffer, writePos, length);
        } else {
            System.arraycopy(data, offset, buffer, writePos, remaining);
            System.arraycopy(data, offset + remaining, buffer, 0, length - remaining);
        }
        writePos = (writePos + length) % buffer.length;
        if (length > 0) filled = true;
    }

    public synchronized byte[] getSnapshot() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (!filled) {
            out.write(buffer, 0, writePos);
        } else {
            out.write(buffer, writePos, buffer.length - writePos);
            out.write(buffer, 0, writePos);
        }
        return out.toByteArray();
    }

    public int getBufferLength() {
        return buffer.length;
    }
}
