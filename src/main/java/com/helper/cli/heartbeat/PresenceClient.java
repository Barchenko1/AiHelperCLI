package com.helper.cli.heartbeat;

import com.helper.cli.propertie.IPropertiesProvider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PresenceClient implements IPresenceClient {

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final String pingUrl;
    private final String offlineUrl;
    private final String code;

    public PresenceClient(IPropertiesProvider properties) {
        this.pingUrl = properties.apiUrl("/ping");
        this.offlineUrl = properties.apiUrl("/ping/offline");
        this.code = System.getenv("VERIFICATION_CODE");
    }

    public boolean hasCode() {
        return code != null && !code.isBlank();
    }

    @Override
    public boolean ping(String client) {
        try {
            int status = pingStatus(client);
            return status >= 200 && status < 300;
        } catch (Exception e) {
            return false;
        }
    }

    /** Last status code of a ping, for callers that want to explain a failure. */
    public int pingStatus(String client) throws Exception {
        return send(pingUrl, client, Duration.ofSeconds(10));
    }

    @Override
    public void offline(String client) {
        try {
            send(offlineUrl, client, Duration.ofSeconds(2));
        } catch (Exception ignored) {
            // best effort: the light goes red anyway once check-ins stop
        }
    }

    private int send(String url, String clientType, Duration timeout) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(timeout)
                .header("X-Auth-Code", code)
                .header("X-Client", clientType)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }
}
