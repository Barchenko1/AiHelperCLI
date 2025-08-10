package com.helper.cli.rest;

import com.google.gson.Gson;

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
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();

    public HttpResponse<String> postJson(String url, Object payload, Map<String, String> headers) {
        String json = GSON.toJson(payload);
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .header("X-Api-Secret", "bXktc3VwZXItc2VjdXJlLXNlY3JldC1rZXktMTIzNDU2")
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
                                                        String filename,
                                                        String descriptionOrNull,
                                                        Map<String, String> headers) {
        try {
            Objects.requireNonNull(pngBytes, "pngBytes");
            if (filename == null || filename.isBlank()) filename = "image.png";

            String boundary = "----JavaBoundary" + UUID.randomUUID();
            byte[] body = buildMultipartBody(boundary, pngBytes, filename, descriptionOrNull);

            HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .header("X-Api-Secret", "bXktc3VwZXItc2VjdXJlLXNlY3JldC1rZXktMTIzNDU2")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body));

            if (headers != null) {
                headers.forEach(b::header);
            }
            return CLIENT.send(b.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] buildMultipartBody(String boundary, byte[] pngBytes, String filename, String description) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String CRLF = "\r\n";

        // file part
        out.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"" + CRLF).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: image/png" + CRLF + CRLF).getBytes(StandardCharsets.UTF_8));
        out.write(pngBytes);
        out.write(CRLF.getBytes(StandardCharsets.UTF_8));

        // optional text part
        if (description != null && !description.isBlank()) {
            out.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Disposition: form-data; name=\"description\"" + CRLF).getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Type: text/plain; charset=UTF-8" + CRLF + CRLF).getBytes(StandardCharsets.UTF_8));
            out.write(description.getBytes(StandardCharsets.UTF_8));
            out.write(CRLF.getBytes(StandardCharsets.UTF_8));
        }

        // end
        out.write(("--" + boundary + "--" + CRLF).getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }
}
