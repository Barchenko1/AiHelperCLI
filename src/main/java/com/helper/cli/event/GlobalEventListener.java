package com.helper.cli.event;

import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.helper.cli.executor.sceen.ScreenHotkeyExecutor;
import com.helper.cli.executor.voice.VoiceHotkeyExecutor;
import com.helper.cli.propertie.IPropertiesProvider;
import com.helper.cli.propertie.PropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GlobalEventListener implements NativeKeyListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalEventListener.class);
    private final IPropertiesProvider propertiesProvider;
    private final ScreenHotkeyExecutor screenHotkeyExecutor;
    private final VoiceHotkeyExecutor voiceHotkeyExecutor;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public GlobalEventListener() {
        propertiesProvider = new PropertiesProvider();
        this.screenHotkeyExecutor = new ScreenHotkeyExecutor(propertiesProvider);
        this.voiceHotkeyExecutor = new VoiceHotkeyExecutor(propertiesProvider);
        this.voiceHotkeyExecutor.startBackgroundCapture();
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        final String lang = propertiesProvider.getProperty("programmingLanguage");
        final String promptText = propertiesProvider.getPropertyMap().get("prompt.text");
        final String promptVoice = propertiesProvider.getPropertyMap().get("prompt.voice");

        final boolean requireAlt = isWindows() || isLinux();

        if (e.getKeyCode() == NativeKeyEvent.VC_F1 && matches(e, requireAlt)) {
            LOGGER.info("📸 Trigger screen capture");
            executor.submit(() -> screenHotkeyExecutor.execute(promptText, lang));
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_F2 && matches(e, requireAlt)) {
            LOGGER.info("🎤 Trigger voice capture (last 30s)");
            executor.submit(() -> voiceHotkeyExecutor.captureAndProcess(promptVoice, lang));
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_F3 && matches(e, requireAlt)) {
            LOGGER.info("📸 Commit screenshots");
            executor.submit(screenHotkeyExecutor::commitScreenShots);
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_F4 && matches(e, requireAlt)) {
            LOGGER.info("⬆️  Push screen folder");
            executor.submit(() -> screenHotkeyExecutor.pushScreenShots(promptText, lang));
        }
    }

    private boolean matches(NativeKeyEvent e, boolean requireAlt) {
        return !requireAlt || isAltDown(e);
    }

    private boolean isAltDown(NativeKeyEvent e) {
        return (e.getModifiers() & NativeInputEvent.ALT_MASK) != 0;
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }
    private boolean isMac() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("mac");
    }
    private boolean isLinux() {
        String os = System.getProperty("os.name");
        return os != null && (os.toLowerCase().contains("nux") || os.toLowerCase().contains("nix"));
    }

}
