package in.demon.helper.executor.extension;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import in.demon.helper.openaiclient.IOpenAIClient;
import in.demon.helper.openaiclient.OpenAITextClient;
import in.demon.helper.propertie.IPropertiesProvider;
import in.demon.helper.template.ITemplateJsonService;
import in.demon.helper.template.TemplateJsonService;
import in.demon.helper.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class ClipboardExtensionKeyDaemon implements IClipboardExtensionKeyDaemon {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClipboardExtensionKeyDaemon.class);

    private static final String TEXT_REQ = "/textRequest.json";
    private final String subPrompt;

    private final IOpenAIClient openAIClient;
    private final ITemplateJsonService templateJsonService;
    private final WebSocketClient webSocketClient;

    public ClipboardExtensionKeyDaemon(IPropertiesProvider propertiesProvider) {
        this.subPrompt = propertiesProvider.getPropertyMap().get("prompt.4");
        this.openAIClient = new OpenAITextClient(propertiesProvider);
        this.templateJsonService = new TemplateJsonService();
        this.webSocketClient = WebSocketClient.getInstance(propertiesProvider);
    }

    private void callExtension() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/text", (HttpExchange exchange) -> {
                if ("POST".equals(exchange.getRequestMethod())) {
                    InputStream is = exchange.getRequestBody();
                    String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    LOGGER.info("Received from extension: {}", body);
                    long handle3 = System.currentTimeMillis();
                    String sanitizedBody = body
                            .replaceAll("\\s+", " ")
                            .replace("\"", "\\\"");
                    String jsonPayload = templateJsonService.buildJsonPayload(TEXT_REQ, sanitizedBody, subPrompt);
                    String response = openAIClient.sendToOpenAI(jsonPayload);
                    long timeDiff3 = System.currentTimeMillis() - handle3;
                    LOGGER.info("time gets response {}", timeDiff3);
                    long handle4 = System.currentTimeMillis();
                    webSocketClient.send(response);
                    long timeDiff4 = System.currentTimeMillis() - handle4;
                    LOGGER.info("time gets socket {}", timeDiff4);
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            });
            server.start();
            LOGGER.info("Java server listening on http://localhost:8080/text");
        } catch (IOException e) {
            LOGGER.error("Java server listening on http://localhost:8080/text", e);
            throw new RuntimeException(e);
        }
    }



    @Override
    public void execute() {
        callExtension();
    }
}
