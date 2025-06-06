package in.demon.helper.executor.sceen;

import in.demon.helper.openaiclient.IOpenAIClient;
import in.demon.helper.openaiclient.OpenAIScreenClient;
import in.demon.helper.websocket.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Comparator;
import java.util.stream.Stream;

import static in.demon.helper.util.Constant.PROMPT_1;
import static in.demon.helper.util.Constant.WEBSOCKET_API_URL;

public class ScreenHotkeyDaemon implements IScreenHotKeyDaemon {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenHotkeyDaemon.class);

    private static final String FOLDER_PREFIX = "/Users/pbarchenko/Downloads/helper";
    private static final String PICTURE_REQ = "/pictureRequest.json";

    private final IOpenAIClient openAIClient;
    private final WebSocketClient webSocketClient;
    private static long lastTriggerTime = 0;

    public ScreenHotkeyDaemon(String apiKey) {
        this.openAIClient = new OpenAIScreenClient(apiKey);
        this.webSocketClient = new WebSocketClient(WEBSOCKET_API_URL);
    }

    @Override
    public void execute() {
        callScreenHelper();
    }

    private void callScreenHelper() {
        lastTriggerTime = System.currentTimeMillis();

        System.out.println("fn + F1 detected:");

        try {
            long handle1 = System.currentTimeMillis();
            BufferedImage screenshot = captureScreen();
            BufferedImage resized = resizeImage(screenshot, 0.8); // 50% smaller
            saveScreenshotToLocal(resized);
            long timeStamp1 = System.currentTimeMillis();
            System.out.println(timeStamp1 - handle1);

            File latestPng = getLatestPng(new File(FOLDER_PREFIX));
            if (latestPng == null) {
                System.err.println("No PNG files found in folder: " + FOLDER_PREFIX);
                return;
            }
            long handle2 = System.currentTimeMillis();
            String base64 = encodeImageToBase64Jpeg(resized, 0.5f);
            String jsonPayload = buildJsonPayload(base64, PROMPT_1);
            long timeStamp2 = System.currentTimeMillis();
            System.out.println(timeStamp2 - handle2);

            long handle3 = System.currentTimeMillis();
            String response = openAIClient.sendToOpenAI(jsonPayload);
            long timeStamp3 = System.currentTimeMillis();
            System.out.println(timeStamp3 - handle3);
            long handle4 = System.currentTimeMillis();
            webSocketClient.send(response);
            long timeStamp4 = System.currentTimeMillis();
            System.out.println(timeStamp4 - handle4);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String buildJsonPayload(String base64Image, String userPrompt) {
        try (InputStream inputStream = this.getClass().getResourceAsStream(PICTURE_REQ)) {
            if (inputStream == null) {
                throw new RuntimeException("JSON template not found: " + PICTURE_REQ);
            }

            String template = new String(inputStream.readAllBytes());
            return template.formatted(base64Image, userPrompt.replace("\"", "\\\""));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON template", e);
        }
    }

    private static BufferedImage captureScreen() throws Exception {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        Rectangle bounds = gd.getDefaultConfiguration().getBounds();

        Robot robot = new Robot(gd);
        return robot.createScreenCapture(bounds);
    }

    private static void saveScreenshotToLocal(BufferedImage image) throws IOException {
        String filename = FOLDER_PREFIX + "/screenshot-" + System.currentTimeMillis() + ".png";
        ImageIO.write(image, "png", new File(filename));
        System.out.println("Saved local screenshot to: " + filename);
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, double scale) {
        int width = (int) (originalImage.getWidth() * scale);
        int height = (int) (originalImage.getHeight() * scale);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        resized.getGraphics().drawImage(originalImage, 0, 0, width, height, null);
        return resized;
    }

    private static String encodeImageToBase64Jpeg(BufferedImage image, float quality) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        writer.setOutput(ios);

        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality); // e.g. 0.5 for good balance

        writer.write(null, new IIOImage(image, null, null), param);
        ios.close();
        writer.dispose();

        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private static File getLatestPng(File folder) {
        File[] pngFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (pngFiles == null || pngFiles.length == 0) return null;

        return Stream.of(pngFiles)
                .filter(File::isFile)
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);
    }
}
