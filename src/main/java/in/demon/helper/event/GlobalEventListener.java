package in.demon.helper.event;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import in.demon.helper.executor.sceen.ScreenHotkeyDaemon;
import in.demon.helper.executor.voice.VoiceHotkeyDaemon;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlobalEventListener implements NativeKeyListener {

    private final ScreenHotkeyDaemon screenHotkeyDaemon;
    private final VoiceHotkeyDaemon voiceHotkeyDaemon;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public GlobalEventListener(String apiKey) {
        this.screenHotkeyDaemon = new ScreenHotkeyDaemon(apiKey);
        this.voiceHotkeyDaemon = new VoiceHotkeyDaemon(apiKey);
        this.voiceHotkeyDaemon.startBackgroundCapture();
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_F1) {
            System.out.println("📸 Trigger screen capture");
            executor.submit(screenHotkeyDaemon::execute);
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_F2) {
            System.out.println("🎤 Trigger voice capture (last 30s)");
            executor.submit(voiceHotkeyDaemon::captureAndProcess);
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_F3) {
            System.out.println("🛑 Pause voice capture");
            executor.submit(voiceHotkeyDaemon::requestStop);
        }
    }
}
