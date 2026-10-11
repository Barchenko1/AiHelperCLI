package com.helper.cli.heartbeat;

import com.helper.cli.propertie.PropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Tells the server "this CLI is running with this code" every few seconds (and "offline" on exit), so the UI can show a green light.
 * A rejected code (expired session / wrong code) gets 401 and the UI keeps the light red.
 */
public class Heartbeat {

    private static final Logger LOGGER = LoggerFactory.getLogger(Heartbeat.class);
    private static final int INTERVAL_SECONDS = 5;

    private final PresenceClient presence;
    private Boolean lastOk;

    public Heartbeat() {
        this.presence = new PresenceClient(new PropertiesProvider());
    }

    public void start() {
        if (!presence.hasCode()) {
            LOGGER.warn("VERIFICATION_CODE is not set - the UI will show the CLI as not connected.");
            return;
        }
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "heartbeat");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(this::ping, 0, INTERVAL_SECONDS, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::goOffline, "heartbeat-offline"));
    }

    private void goOffline() {
        // exiting anyway; the server marks the CLI offline after a few missed heartbeats
        presence.offline(IPresenceClient.CLI);
    }

    private void ping() {
        try {
            int status = presence.pingStatus(IPresenceClient.CLI);
            report(status >= 200 && status < 300, "server answered HTTP " + status
                    + (status == 401 ? " - the code is wrong or the session expired, log in on the UI and restart with the new code" : ""));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            report(false, "cannot reach the server: " + e.getMessage());
        }
    }

    /** Logs only when the state changes, so a 20s heartbeat doesn't flood the console. */
    private void report(boolean ok, String problem) {
        if (lastOk == null || lastOk != ok) {
            if (ok) {
                LOGGER.info("Connected to the server with this code.");
            } else {
                LOGGER.warn("Not connected: {}", problem);
            }
        }
        lastOk = ok;
    }
}
