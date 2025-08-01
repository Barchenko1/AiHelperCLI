package in.demon.helper.executor.clipboard;

import in.demon.helper.openaiclient.IOpenAIClient;
import in.demon.helper.openaiclient.OpenAIScreenClient;
import in.demon.helper.propertie.IPropertiesProvider;
import in.demon.helper.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public class ClipboardKeyDaemon implements IClipboardKeyDaemon {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClipboardKeyDaemon.class);

    private final IPropertiesProvider propertiesProvider;
    private final String subPrompt;

    private final IOpenAIClient openAIClient;
    private final WebSocketClient webSocketClient;

    public ClipboardKeyDaemon(IPropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
        this.subPrompt = propertiesProvider.getPropertyMap().get("prompt.4");
        this.openAIClient = new OpenAIScreenClient(propertiesProvider);
        this.webSocketClient = WebSocketClient.getInstance(propertiesProvider);
    }

    @Override
    public void execute() {
        String text = getFocusedElementText();
        LOGGER.info("Executing clipboard key daemon, {}", text);

    }

    private String getClipboardText() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable data = clipboard.getContents(null);
            if (data != null && data.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) data.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read clipboard", e);
            return "";
        }
        return null;
    }

    private String getFocusedElementText() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection empty = new StringSelection("");
            clipboard.setContents(empty, null);

            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("mac")) {
                Process process = new ProcessBuilder("osascript",
                        "-e",
                        "tell application \"System Events\" to keystroke \"a\" using {command down}",
                        "-e",
                        "tell application \"System Events\" to keystroke \"c\" using {command down}"
                ).start();
                process.waitFor();
            } else if (os.contains("linux")) {
                new ProcessBuilder("sh", "-c", "xdotool key ctrl+a; xdotool key ctrl+c").start().waitFor();
            }
            Thread.sleep(300);
            return getClipboardText();
        } catch (Exception e) {
            LOGGER.error("Failed to copy focused text", e);
            return "";
        }
    }
}
