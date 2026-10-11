package com.helper.cli.propertie;

import java.util.Map;

public interface IPropertiesProvider {
    void loadProperties();
    String getProperty(String key);
    Map<String, String> getPropertyMap();

    /** Full URL of a server API route, e.g. apiUrl("/screens"); aiHelperServerUrl may carry a routing prefix. */
    default String apiUrl(String path) {
        return getProperty("aiHelperServerUrl").replaceAll("/+$", "") + "/api/v1" + path;
    }
}
