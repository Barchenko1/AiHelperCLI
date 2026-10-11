package com.helper.cli.executor.sceen;

import com.helper.cli.heartbeat.IPresenceClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutoScreenTest {

    /** Records what the UI light would see. */
    static class FakePresence implements IPresenceClient {
        final List<String> calls = new CopyOnWriteArrayList<>();
        public boolean ping(String client) { calls.add("ping"); return true; }
        public void offline(String client) { calls.add("offline"); }
    }

    static class FakeScreen implements IScreenHotKeyExecutor {
        final CountDownLatch firstSend = new CountDownLatch(1);
        volatile boolean accept = true;
        volatile RuntimeException failure;
        public boolean captureAndSend(String prompt, String lang) {
            firstSend.countDown();
            if (failure != null) throw failure;
            return accept;
        }
        public void execute(String prompt, String lang) {}
        public void commitScreenShots() {}
        public void pushScreenShots(String prompt, String lang) {}
    }

    private final FakePresence presence = new FakePresence();
    private final FakeScreen screen = new FakeScreen();
    private final AutoScreen auto = new AutoScreen(screen, presence, 3600);

    @AfterEach
    void stop() {
        auto.stop();
    }

    /** F6 once, then wait for the immediate first screenshot. */
    private void started() throws InterruptedException {
        auto.toggle("p", "Java");
        assertTrue(screen.firstSend.await(5, TimeUnit.SECONDS));
        while (presence.calls.isEmpty()) Thread.onSpinWait();
        presence.calls.clear();
    }

    @Test
    void firstPressSendsRightAwayAndLightGoesGreen() throws Exception {
        auto.toggle("p", "Java");

        assertTrue(screen.firstSend.await(5, TimeUnit.SECONDS));
        while (presence.calls.isEmpty()) Thread.onSpinWait();
        assertEquals(List.of("ping"), presence.calls);
    }

    @Test
    void secondPressStopsAndLightGoesRed() throws Exception {
        started();

        auto.toggle("p", "Java");

        assertEquals(List.of("offline"), presence.calls);
    }

    @Test
    void rejectedScreenshotTurnsLightRed() throws Exception {
        started();
        screen.accept = false;

        auto.sendOnce("p", "Java");

        assertEquals(List.of("offline"), presence.calls);
    }

    @Test
    void crashingScreenshotDoesNotThrowAndTurnsLightRed() throws Exception {
        started();
        screen.failure = new RuntimeException("no display");

        auto.sendOnce("p", "Java");

        assertEquals(List.of("offline"), presence.calls);
    }

    @Test
    void screenshotFinishingAfterStopKeepsLightRed() throws Exception {
        started();
        auto.stop();
        presence.calls.clear();

        auto.sendOnce("p", "Java");

        assertEquals(List.of("offline"), presence.calls);
    }

    @Test
    void stopWhenNotRunningDoesNothing() {
        auto.stop();
        assertTrue(presence.calls.isEmpty());
    }
}
