package com.helper.cli.executor.sceen;

import com.helper.cli.heartbeat.IPresenceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** F6: first press sends a screenshot (like F1) every N seconds, second press stops. Drives the UI's Auto screen light. */
public class AutoScreen {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoScreen.class);

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final IScreenHotKeyExecutor screen;
    private final IPresenceClient presence;
    private final long intervalSeconds;
    private ScheduledFuture<?> task;

    public AutoScreen(IScreenHotKeyExecutor screen, IPresenceClient presence, long intervalSeconds) {
        this.screen = screen;
        this.presence = presence;
        this.intervalSeconds = intervalSeconds;
    }

    public synchronized void toggle(String prompt, String programmingLanguage) {
        if (task != null) {
            stop();
            return;
        }
        task = scheduler.scheduleAtFixedRate(() -> sendOnce(prompt, programmingLanguage), 0, intervalSeconds, TimeUnit.SECONDS);
        LOGGER.info("▶️  Auto screen ON: every {}s, press F6 again to stop", intervalSeconds);
    }

    public synchronized void stop() {
        if (task == null) return;
        task.cancel(false);
        task = null;
        presence.offline(IPresenceClient.AUTO_SCREEN);
        LOGGER.info("⏹️  Auto screen OFF");
    }

    /** Light green when a round starts, red if the screenshot was not delivered. */
    void sendOnce(String prompt, String programmingLanguage) {
        presence.ping(IPresenceClient.AUTO_SCREEN);
        if (!screen.captureAndSend(prompt, programmingLanguage)) {
            presence.offline(IPresenceClient.AUTO_SCREEN);
        }
    }
}
