package com.helper.cli;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.helper.cli.event.GlobalEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new GlobalEventListener());
            LOGGER.info("Listening for global fn + F1 key...");
        } catch (Exception e) {
            LOGGER.error("Failed to register global hook.", e);
            throw new RuntimeException(e);
        }
    }
}
