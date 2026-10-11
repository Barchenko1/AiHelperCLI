package com.helper.cli.propertie;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IPropertiesProviderTest {

    private static IPropertiesProvider serverAt(String url) {
        return new IPropertiesProvider() {
            public void loadProperties() {}
            public String getProperty(String key) { return "aiHelperServerUrl".equals(key) ? url : null; }
            public Map<String, String> getPropertyMap() { return Map.of(); }
        };
    }

    @Test
    void apiUrlKeepsTheRoutingPrefix() {
        assertEquals("https://host/aihelper/api/v1/screens", serverAt("https://host/aihelper").apiUrl("/screens"));
    }

    @Test
    void apiUrlIgnoresTrailingSlash() {
        assertEquals("http://localhost:8080/api/v1/ping", serverAt("http://localhost:8080/").apiUrl("/ping"));
    }
}
