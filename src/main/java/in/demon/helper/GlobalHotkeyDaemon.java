package in.demon.helper;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static in.demon.helper.Constant.PROMT_1;

public class GlobalHotkeyDaemon implements NativeKeyListener {

    private static final Gson GSON = new Gson();
    private static final String FOLDER_PREFIX = "/Users/pbarchenko/Downloads/helper";

    private static final String CHAT_GPT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String CHAT_GPT_API_KEY = System.getenv("OPENAI_API_KEY");;
    private static final String PICTURE_REQ = "/pictureRequest.json";

    private static WebSocketClient webSocketClient = new WebSocketClient("ws://192.168.1.22:8080/ws");
    private static long lastTriggerTime = 0;

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new GlobalHotkeyDaemon());
            System.out.println("Listening for global fn + F1 key...");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_F1) {
            long now = System.currentTimeMillis();
            boolean isDouble = now - lastTriggerTime < 3000;
            lastTriggerTime = now;

            System.out.println("fn + F1 detected: " + (isDouble ? "double" : "single"));

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
                String jsonPayload = buildJsonPayload(base64, PROMT_1);
                long timeStamp2 = System.currentTimeMillis();
                System.out.println(timeStamp2 - handle2);

                long handle3 = System.currentTimeMillis();
                String response = sendToOpenAI(jsonPayload);
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

    private static String encodeFileToBase64(File file) throws IOException {
        byte[] imageBytes = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    private static String sendToOpenAI(String json) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(CHAT_GPT_API_URL).openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Bearer " + CHAT_GPT_API_KEY);
        connection.setRequestProperty("Content-Type", "application/json");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(json.getBytes());
        }

        int code = connection.getResponseCode();
        InputStream responseStream = (code >= 200 && code < 300)
                ? connection.getInputStream()
                : connection.getErrorStream();

        JsonObject jsonResponse = JsonParser.parseReader(new InputStreamReader(responseStream)).getAsJsonObject();

        String content = jsonResponse
                .getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .getAsJsonObject("message")
                .get("content").getAsString();

        jsonResponse.addProperty("timestamp", System.currentTimeMillis());

        return GSON.toJson(content);
    }
}
