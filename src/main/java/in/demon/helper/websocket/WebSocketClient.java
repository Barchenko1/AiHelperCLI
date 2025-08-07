package in.demon.helper.websocket;

import in.demon.helper.propertie.IPropertiesProvider;
import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WebSocketClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketClient.class);
    private static WebSocketClient instance;

    private Session userSession;
    private final BlockingQueue<String> pendingMessages = new LinkedBlockingQueue<>();
    private final IPropertiesProvider propertiesProvider;

    private WebSocketClient(IPropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
        connect();
    }

    public static synchronized WebSocketClient getInstance(IPropertiesProvider propertiesProvider) {
        if (instance == null) {
            instance = new WebSocketClient(propertiesProvider);
        }
        return instance;
    }

    private void connect() {
        new Thread(() -> {
            while (userSession == null || !userSession.isOpen()) {
                try {
                    ClientManager client = ClientManager.createClient();

                    // Get JWT token
                    String jwtToken = propertiesProvider.getProperty("WEBSOCKET_API_TOKEN");

                    // Build config with Sec-WebSocket-Protocol header
                    ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
                        @Override
                        public void beforeRequest(java.util.Map<String, java.util.List<String>> headers) {
                            headers.put("Sec-WebSocket-Protocol", java.util.Collections.singletonList(jwtToken));
                        }
                    };

                    ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                            .configurator(configurator)
                            .build();

                    client.connectToServer(new Endpoint() {
                        @Override
                        public void onOpen(Session session, EndpointConfig config) {
                            LOGGER.info("✅ WebSocket connected.");
                            userSession = session;

                            session.addMessageHandler(String.class, message -> {
                                LOGGER.info("\uD83D\uDCE9 Received from server: {}", message);
                            });
                        }

                        @Override
                        public void onClose(Session session, CloseReason closeReason) {
                            LOGGER.info("⚠️ WebSocket closed: {}", closeReason);
                            userSession = null;
                            connect(); // Reconnect on close
                        }

                        @Override
                        public void onError(Session session, Throwable thr) {
                            LOGGER.error("\uD83D\uDCA5 WebSocket error: {}", thr.getMessage());
                            userSession = null;
                            connect();
                        }
                    }, config, new URI(propertiesProvider.getProperty("WEBSOCKET_API_URL")));

                    break;

                } catch (Exception e) {
                    LOGGER.error("❌ WebSocket connection failed: {}", e.getMessage());
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
            LOGGER.warn("WebSocket not open. Buffering message.");
            pendingMessages.add(message);
        }
    }

    public void close() {
        if (userSession != null) {
            try {
                userSession.close();
            } catch (IOException e) {
                LOGGER.error("WebSocket close error: {}", e.getMessage());
            }
        }
    }
}
