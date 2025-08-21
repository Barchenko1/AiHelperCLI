package com.helper.cli.executor.sceen;

import com.helper.cli.propertie.IPropertiesProvider;
import com.helper.cli.rest.IRestClient;
import com.helper.cli.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ScreenHotkeyExecutor implements IScreenHotKeyExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenHotkeyExecutor.class);

    private static final String FOLDER_PREFIX = "/Users/pbarchenko/Downloads/helper";
    private final IPropertiesProvider propertiesProvider;
    private final IRestClient restClient;

    public ScreenHotkeyExecutor(IPropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
        this.restClient = new RestClient();
    }

    @Override
    public void execute(String subPrompt) {
        callScreenHelper(subPrompt);
    }

    private void callScreenHelper(String subPrompt) {
        LOGGER.info("fn + F1 detected");
        try {
            long handle1 = System.currentTimeMillis();
            BufferedImage screenshot = captureScreen();
            BufferedImage resized = resizeImage(screenshot, 0.8); // 50% smaller
            saveScreenshotToLocal(resized);
            long timeDiff1 = System.currentTimeMillis() - handle1;
            LOGGER.info("time gets {}", timeDiff1);

            File latestPng = getLatestPng(new File(FOLDER_PREFIX));
            if (latestPng == null) {
                LOGGER.error("No PNG files found in folder: " + FOLDER_PREFIX);
                return;
            }
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "image/png");

            HttpResponse<String> response = restClient.postMultipartPng(
                    propertiesProvider.getProperty("aiHelperServerUrl") + "/api/v1/screen",
                    Files.readAllBytes(latestPng.toPath()),
                    subPrompt,
                    headers);

            int status = response.statusCode();
            if (status == 200) {
                LOGGER.info("Upload successful: " + status);
            } else {
                LOGGER.error("Upload failed {}: {}%n", status, response.body());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        LOGGER.info("Saved local screenshot to: {}", filename);
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
        if (pngFiles == null || pngFiles.length == 0) {
            return null;
        }

        return Stream.of(pngFiles)
                .filter(File::isFile)
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);
    }
}
