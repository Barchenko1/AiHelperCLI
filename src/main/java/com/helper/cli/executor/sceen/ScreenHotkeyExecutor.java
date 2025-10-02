package com.helper.cli.executor.sceen;

import com.helper.cli.propertie.IPropertiesProvider;
import com.helper.cli.rest.IRestClient;
import com.helper.cli.rest.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.helper.cli.util.Constant.LANGUAGE_TEXT;

public class ScreenHotkeyExecutor implements IScreenHotKeyExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenHotkeyExecutor.class);
    private static final String COMMIT_FOLDER = "commit";

    private final IPropertiesProvider propertiesProvider;
    private final IRestClient restClient;

    public ScreenHotkeyExecutor(IPropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
        this.restClient = new RestClient();
    }

    @Override
    public void execute(String prompt, String programmingLanguage) {
        makeScreenAndPush(prompt, programmingLanguage);
    }

    @Override
    public void commitScreenShots() {
        Path commitFolderPath = getCommitFolderPath();
        makeScreen(commitFolderPath);
    }

    @Override
    public void pushScreenShots(String prompt, String programmingLanguage) {
        Path commitFolderPath = getCommitFolderPath();
        sendScreenshotsToAi(commitFolderPath, prompt, programmingLanguage);
        deleteCommitFolder(commitFolderPath);
    }

    private void makeScreen(Path commitFolderPath) {
        try {
            long handle1 = System.currentTimeMillis();
            BufferedImage screenshot = captureScreen();
            BufferedImage resized = resizeImage(screenshot, 0.8);
            saveScreenshotToLocal(commitFolderPath, "screenshot", resized);
            long timeDiff1 = System.currentTimeMillis() - handle1;
            LOGGER.info("time gets {}", timeDiff1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendScreenshotsToAi(Path folderPrefixPath, String prompt, String programmingLanguage) {
        File[] filesArr = getCommittedFiles(folderPrefixPath.toFile()); // return [] when none

        List<File> files = Arrays.stream(filesArr)
                .sorted(Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();

        HttpResponse<String> resp = uploadScreens(files, buildFinalPrompt(prompt, programmingLanguage));
        logUploadResult(resp);

    }

    private void makeScreenAndPush(String prompt, String programmingLanguage) {
        try {
            long t0 = System.currentTimeMillis();
            BufferedImage shot = captureScreen();
            BufferedImage resized = resizeImage(shot, 0.8);
            Path rootFolderPath = Paths.get(propertiesProvider.getProperty("screenFolder"));
            saveScreenshotToLocal(rootFolderPath, "screenshot", resized);
            LOGGER.info("screenshot captured+saved in {} ms", (System.currentTimeMillis() - t0));
            File latestPng = getLatestPng(new File(propertiesProvider.getProperty("screenFolder")));
            HttpResponse<String> resp = uploadScreens(List.of(latestPng), buildFinalPrompt(prompt, programmingLanguage));
            logUploadResult(resp);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private String buildFinalPrompt(String prompt, String programmingLanguage) {
        String base = prompt == null ? "" : prompt.trim();
        return (base + " " + LANGUAGE_TEXT.formatted(programmingLanguage)).trim();
    }

    private HttpResponse<String> uploadScreens(List<File> files, String finalPrompt) {
        Map<String, String> headers = getHeaders();
        String url = propertiesProvider.getProperty("aiHelperServerUrl") + "/api/v1/screens";
        return restClient.postMultipartPngs(url, files, finalPrompt, headers);
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        String apiSecret = propertiesProvider.getProperty("apiSecret");
        if (apiSecret != null && !apiSecret.isBlank()) {
            headers.put("X-Api-Secret", apiSecret);
        }
        return headers;
    }

    private void logUploadResult(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status == 200) {
            LOGGER.info("Upload successful: {}", status);
        } else {
            LOGGER.error("Upload failed {}: {}", status, response.body());
        }
    }

    private BufferedImage captureScreen() throws Exception {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        Rectangle bounds = gd.getDefaultConfiguration().getBounds();

        Robot robot = new Robot(gd);
        return robot.createScreenCapture(bounds);
    }

    private void saveScreenshotToLocal(Path folderPrefixPath, String filePrefix, BufferedImage image) throws IOException {
        if (!Files.exists(folderPrefixPath)) {
            Files.createDirectories(folderPrefixPath);
        }
        String filename = folderPrefixPath
                + "/" + filePrefix + "-" + System.currentTimeMillis() + ".png";
        ImageIO.write(image, "png", new File(filename));
        LOGGER.info("Saved local screenshot to: {}", filename);
    }

    private void deleteCommitFolder(Path dir) {
        if (dir == null) return;
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()) // delete children first
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); }
                        catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
            LOGGER.info("Deleted folder {}", dir);
        } catch (IOException | UncheckedIOException e) {
            LOGGER.error("Failed to delete {}: {}", dir, e.getMessage(), e);
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, double scale) {
        int width = (int) (originalImage.getWidth() * scale);
        int height = (int) (originalImage.getHeight() * scale);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        resized.getGraphics().drawImage(originalImage, 0, 0, width, height, null);
        return resized;
    }

    private Path getCommitFolderPath() {
        return Paths.get(propertiesProvider.getProperty("screenFolder")
                + "/" + COMMIT_FOLDER);
    }

    private File getLatestPng(File folder) {
        File[] pngFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (pngFiles == null || pngFiles.length == 0) {
            return null;
        }

        return Stream.of(pngFiles)
                .filter(File::isFile)
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);
    }

    private File[] getCommittedFiles(File folder) {
        if (!folder.exists() && !folder.isDirectory()) {
            return new File[]{};
        }
        File[] pngFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (pngFiles == null || pngFiles.length == 0) {
            return pngFiles;
        }

        return Arrays.stream(pngFiles)
                .filter(File::isFile)
                .toArray(File[]::new);
    }
}
