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
//        if (e.getKeyCode() == NativeKeyEvent.VC_F1) {
//            LOGGER.info("📸 Trigger screen capture");
//            executor.submit(() ->
//                    screenHotkeyExecutor.execute(
//                            propertiesProvider.getPropertyMap().get("prompt.text"),
//                            propertiesProvider.getProperty("programmingLanguage")));
//        }
        int mods = e.getModifiers();
        if (e.getKeyCode() == NativeKeyEvent.VC_F1 &&
                (mods & NativeInputEvent.ALT_MASK) != 0) {
            LOGGER.info("📸 Trigger screen capture");
            executor.submit(() ->
                    screenHotkeyExecutor.execute(
                            propertiesProvider.getPropertyMap().get("prompt.text"),
                            propertiesProvider.getProperty("programmingLanguage")));}
//        if (e.getKeyCode() == NativeKeyEvent.Alt) {
//            LOGGER.info("📸 Trigger screen capture");
//            executor.submit(() ->
//                    screenHotkeyExecutor.execute(
//                            propertiesProvider.getPropertyMap().get("prompt.text"),
//                            propertiesProvider.getProperty("programmingLanguage")));
//        }

        if (e.getKeyCode() == NativeKeyEvent.VC_F2) {
            LOGGER.info("🎤 Trigger voice capture (last 30s)");
            executor.submit(() ->
                    voiceHotkeyExecutor.captureAndProcess(
                            propertiesProvider.getPropertyMap().get("prompt.voice"),
                            propertiesProvider.getProperty("programmingLanguage")));
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_F3) {
            LOGGER.info("📸 Trigger screen capture");
            executor.submit(screenHotkeyExecutor::commitScreenShots);
        }

        if (e.getKeyCode() == NativeKeyEvent.VC_F4) {
            LOGGER.info("push screen folder");
            executor.submit(() ->
                    screenHotkeyExecutor.pushScreenShots(
                            propertiesProvider.getPropertyMap().get("prompt.text"),
                            propertiesProvider.getProperty("programmingLanguage")));
        }

    }
}
