package in.demon.helper.event;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import in.demon.helper.executor.extension.ClipboardExtensionKeyDaemon;
import in.demon.helper.executor.extension.IClipboardExtensionKeyDaemon;
import in.demon.helper.executor.sceen.ScreenHotkeyDaemon;
import in.demon.helper.executor.voice.VoiceHotkeyDaemon;
import in.demon.helper.propertie.IPropertiesProvider;
import in.demon.helper.propertie.PropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlobalEventListener implements NativeKeyListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalEventListener.class);

    private final ScreenHotkeyDaemon algorithmScreenHotkeyDaemon;
    private final ScreenHotkeyDaemon flexTaskScreenHotkeyDaemon;
//    private final IClipboardKeyDaemon clipboardKeyDaemon;
    private final IClipboardExtensionKeyDaemon clipboardExtensionKeyDaemon;
    private final VoiceHotkeyDaemon voiceHotkeyDaemon;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public GlobalEventListener() {
        IPropertiesProvider propertiesProvider = new PropertiesProvider();
        this.algorithmScreenHotkeyDaemon = new ScreenHotkeyDaemon(propertiesProvider, "prompt.1");
        this.flexTaskScreenHotkeyDaemon = new ScreenHotkeyDaemon(propertiesProvider, "prompt.2");
//        this.clipboardKeyDaemon = new ClipboardKeyDaemon(propertiesProvider);
        this.clipboardExtensionKeyDaemon = new ClipboardExtensionKeyDaemon(propertiesProvider);
        this.voiceHotkeyDaemon = new VoiceHotkeyDaemon(propertiesProvider);
        this.voiceHotkeyDaemon.startBackgroundCapture();
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_F1) {
            LOGGER.info("📸 Trigger screen capture");
            executor.submit(algorithmScreenHotkeyDaemon::execute);
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_F2) {
            LOGGER.info("📸 Trigger screen capture");
            executor.submit(flexTaskScreenHotkeyDaemon::execute);
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_F3) {
            LOGGER.info("🎤 Trigger voice capture (last 30s)");
            executor.submit(voiceHotkeyDaemon::captureAndProcess);
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_F4) {
            LOGGER.info("Trigger clipboard capture");
            executor.submit(clipboardExtensionKeyDaemon::execute);
        }

    }
}
