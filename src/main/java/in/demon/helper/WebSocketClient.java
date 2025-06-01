package in.demon.helper;

import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WebSocketClient {

    private Session userSession;
    private final BlockingQueue<String> pendingMessages = new LinkedBlockingQueue<>();
    private final String uri;

    public WebSocketClient(String uri) {
        this.uri = uri;
        connect();
    }

    private void connect() {
        new Thread(() -> {
            while (userSession == null || !userSession.isOpen()) {
                try {
                    ClientManager client = ClientManager.createClient();
                    client.connectToServer(new Endpoint() {
                        @Override
                        public void onOpen(Session session, EndpointConfig config) {
                            System.out.println("✅ WebSocket connected.");
                            userSession = session;

                            session.addMessageHandler(String.class, message -> {
                                System.out.println("📩 Received from server: " + message);
                            });
                        }

                        @Override
                        public void onClose(Session session, CloseReason closeReason) {
                            System.out.println("⚠️ WebSocket closed: " + closeReason);
                            userSession = null;
                            connect(); // Reconnect on close
                        }

                        @Override
                        public void onError(Session session, Throwable thr) {
                            System.err.println("💥 WebSocket error: " + thr.getMessage());
                            userSession = null;
                            connect();
                        }
                    }, new URI(uri));
                    break;
                } catch (Exception e) {
                    System.err.println("❌ WebSocket connection failed: " + e.getMessage());
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }).start();
    }

    public void send(String message) {
        if (userSession != null && userSession.isOpen()) {
            userSession.getAsyncRemote().sendText(message);
        } else {
            System.err.println("WebSocket not open. Buffering message.");
            pendingMessages.offer(message);
        }
    }

    public void close() {
        if (userSession != null) {
            try {
                userSession.close();
            } catch (IOException e) {
                System.err.println("WebSocket close error: " + e.getMessage());
            }
        }
    }
}
