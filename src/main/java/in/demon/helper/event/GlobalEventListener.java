package in.demon.helper.event;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import in.demon.helper.executor.sceen.ScreenHotkeyDaemon;
import in.demon.helper.executor.voice.VoiceHotkeyDaemon;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static in.demon.helper.util.Constant.SUB_PROMPT_MAP;

public class GlobalEventListener implements NativeKeyListener {

    private final ScreenHotkeyDaemon algorithmScreenHotkeyDaemon;
    private final ScreenHotkeyDaemon flexTaskScreenHotkeyDaemon;
    private final VoiceHotkeyDaemon voiceHotkeyDaemon;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public GlobalEventListener(String apiKey) {
        this.algorithmScreenHotkeyDaemon = new ScreenHotkeyDaemon(apiKey, SUB_PROMPT_MAP.get("algorithm"));
        this.flexTaskScreenHotkeyDaemon = new ScreenHotkeyDaemon(apiKey, SUB_PROMPT_MAP.get("flex"));
        this.voiceHotkeyDaemon = new VoiceHotkeyDaemon(apiKey, SUB_PROMPT_MAP.get("voice"));
        this.voiceHotkeyDaemon.startBackgroundCapture();
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_F1) {
            System.out.println("📸 Trigger screen capture");
            executor.submit(algorithmScreenHotkeyDaemon::execute);
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_F2) {
            System.out.println("📸 Trigger screen capture");
            executor.submit(flexTaskScreenHotkeyDaemon::execute);
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_F3) {
            System.out.println("🎤 Trigger voice capture (last 30s)");
            executor.submit(voiceHotkeyDaemon::captureAndProcess);
        }

    }
}
