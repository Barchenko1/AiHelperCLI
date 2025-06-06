package in.demon.helper.executor.voice;

public interface IVoiceHotKeyDaemon {
    void startBackgroundCapture();
    void captureAndProcess();
    void requestStop();
}
