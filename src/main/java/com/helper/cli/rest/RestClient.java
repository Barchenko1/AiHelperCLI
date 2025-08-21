package com.helper.cli.rest;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class RestClient implements IRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();

    public HttpResponse<String> postJson(String url, Object payload, String subPrompt, Map<String, String> headers) {
        String json = GSON.toJson(payload);
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .header("X-Api-Secret", System.getenv("WEBSOCKET_API_TOKEN"))
                .POST(HttpRequest.BodyPublishers.ofString(json));

        if (headers != null) {
            headers.forEach(b::header);
        }
        try {
            return CLIENT.send(b.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public HttpResponse<String> postMultipartPng(String url,
                                                 byte[] pngBytes,
                                                 String subPrompt,
                                                 Map<String, String> headers) {
        try {
            Objects.requireNonNull(pngBytes, "pngBytes");

            String boundary = "----JavaBoundary" + UUID.randomUUID();
            byte[] body = buildMultipartBody(boundary, "image.png", pngBytes, "image/png", subPrompt);
            return getHttpResponse(url, headers, boundary, body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public HttpResponse<String> postMultipartWav(
            String url,
            byte[] wavBytes,
            String subPrompt,
            Map<String, String> headers
    ) {
        try {
            Objects.requireNonNull(wavBytes, "wavBytes");

            String boundary = "----JavaBoundary" + UUID.randomUUID();
            byte[] body = buildMultipartBody(boundary, "audio.wav", wavBytes, "audio/wav", subPrompt);
            return getHttpResponse(url, headers, boundary, body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HttpResponse<String> getHttpResponse(String url,
                                                 Map<String, String> headers,
                                                 String boundary,
                                                 byte[] body) throws IOException, InterruptedException {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                // NOTE: send your RAW secret unless your server decodes base64
                .header("X-Api-Secret", System.getenv("WEBSOCKET_API_TOKEN"))
                .POST(HttpRequest.BodyPublishers.ofByteArray(body));

        if (headers != null) headers.forEach(b::header);

        return CLIENT.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private byte[] buildMultipartBody(String boundary,
                            String filename,
                            byte[] bytes,
                            String contentType,
                            String subPrompt) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String CRLF = "\r\n";

        // file part
        out.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"" + CRLF).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: " + contentType + CRLF + CRLF).getBytes(StandardCharsets.UTF_8));

        out.write(bytes);
        out.write(CRLF.getBytes(StandardCharsets.UTF_8));

        if (subPrompt != null && !subPrompt.isBlank()) {
            out.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Disposition: form-data; name=\"subPrompt\"" + CRLF)
                    .getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Type: text/plain; charset=UTF-8" + CRLF + CRLF)
                    .getBytes(StandardCharsets.UTF_8));
            out.write(subPrompt.getBytes(StandardCharsets.UTF_8));
            out.write(CRLF.getBytes(StandardCharsets.UTF_8));
        }

        // end
        out.write(("--" + boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }
}
