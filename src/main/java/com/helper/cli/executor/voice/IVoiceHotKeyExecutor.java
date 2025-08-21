package com.helper.cli.executor.voice;

public interface IVoiceHotKeyExecutor {
    void startBackgroundCapture();
    void captureAndProcess(String subPrompt);
    void requestStop();
}
