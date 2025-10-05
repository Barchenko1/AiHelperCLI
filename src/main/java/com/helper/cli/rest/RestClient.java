package com.helper.cli.rest;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class RestClient implements IRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Gson GSON = new Gson();

    public HttpResponse<String> postJson(String url, Object payload, String prompt, Map<String, String> headers) {
        String json = GSON.toJson(payload);
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));

        if (headers != null) {
            headers.forEach(b::header);
        }
        String code = System.getenv("VERIFICATION_CODE");
        if (code != null && !code.isEmpty()) {
            b.header("X-Auth-Code", code);
        }
        try {
            return CLIENT.send(b.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public HttpResponse<String> postMultipartPng(String url,
                                                 byte[] pngBytes,
                                                 String prompt,
                                                 Map<String, String> headers) {
        try {
            Objects.requireNonNull(pngBytes, "pngBytes");


            String boundary = "----JavaBoundary" + UUID.randomUUID();
            byte[] body = buildMultipartBody(boundary, "image.png", pngBytes, "image/png", prompt);
            return getHttpResponse(url, headers, boundary, body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpResponse<String> postMultipartPngs(String url,
                                                  List<File> files,
                                                  String prompt,
                                                  Map<String, String> headers) {
        try {
            String boundary = "----JavaBoundary" + UUID.randomUUID();
            byte[] body = buildMultipartBody(boundary, files, "image/png", prompt);

            return getHttpResponse(url, headers, boundary, body);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public HttpResponse<String> postMultipartWav(
            String url,
            byte[] wavBytes,
            String prompt,
            Map<String, String> headers
    ) {
        try {
            Objects.requireNonNull(wavBytes, "wavBytes");

            String boundary = "----JavaBoundary" + UUID.randomUUID();
            byte[] body = buildMultipartBody(boundary, "audio.wav", wavBytes, "audio/wav", prompt);
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
                .POST(HttpRequest.BodyPublishers.ofByteArray(body));

        String code = System.getenv("VERIFICATION_CODE");
        if (code != null && !code.isEmpty()) {
            b.header("X-Auth-Code", code);
        }

        if (headers != null) headers.forEach(b::header);

        return CLIENT.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private byte[] buildMultipartBody(String boundary,
                            String filename,
                            byte[] bytes,
                            String contentType,
                            String prompt) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String CRLF = "\r\n";

        out.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"" + CRLF).getBytes(StandardCharsets.UTF_8));
        out.write(("Content-Type: " + contentType + CRLF + CRLF).getBytes(StandardCharsets.UTF_8));

        out.write(bytes);
        out.write(CRLF.getBytes(StandardCharsets.UTF_8));

        return getByteArrayOutputStreamWithPrompt(prompt, out, boundary, CRLF).toByteArray();
    }

    private byte[] buildMultipartBody(String boundary,
                                      List<File> files,
                                      String contentType,
                                      String prompt) throws IOException {
        final String CRLF = "\r\n";
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (File f : files) {
            byte[] bytes = Files.readAllBytes(f.toPath());

            out.write(("--" + boundary + CRLF).getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Disposition: form-data; name=\"files\"; filename=\"" + f.getName() + "\"" + CRLF).getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Type: " + contentType + CRLF + CRLF).getBytes(StandardCharsets.UTF_8));

            out.write(bytes);
            out.write(CRLF.getBytes(StandardCharsets.UTF_8));
        }

        return getByteArrayOutputStreamWithPrompt(prompt, out, boundary, CRLF).toByteArray();
    }

    private ByteArrayOutputStream getByteArrayOutputStreamWithPrompt(String prompt,
                                                                     ByteArrayOutputStream out,
                                                                     String boundary,
                                                                     String crlf) throws IOException {
        if (prompt != null && !prompt.isBlank()) {
            out.write(("--" + boundary + crlf).getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Disposition: form-data; name=\"prompt\"" + crlf)
                    .getBytes(StandardCharsets.UTF_8));
            out.write(("Content-Type: text/plain; charset=UTF-8" + crlf + crlf)
                    .getBytes(StandardCharsets.UTF_8));
            out.write(prompt.getBytes(StandardCharsets.UTF_8));
            out.write(crlf.getBytes(StandardCharsets.UTF_8));
        }

        out.write(("--" + boundary + "--" + crlf).getBytes(StandardCharsets.UTF_8));
        return out;
    }
}
