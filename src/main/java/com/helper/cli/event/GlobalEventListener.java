package com.helper.cli.event;

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

    private final ScreenHotkeyExecutor algorithmScreenHotkeyDaemon;
    private final ScreenHotkeyExecutor flexTaskScreenHotkeyDaemon;
    private final VoiceHotkeyExecutor voiceHotkeyDaemon;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public GlobalEventListener() {
        IPropertiesProvider propertiesProvider = new PropertiesProvider();
        this.algorithmScreenHotkeyDaemon = new ScreenHotkeyExecutor(propertiesProvider, "prompt.1");
        this.flexTaskScreenHotkeyDaemon = new ScreenHotkeyExecutor(propertiesProvider, "prompt.2");
        this.voiceHotkeyDaemon = new VoiceHotkeyExecutor(propertiesProvider);
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

    }
}
