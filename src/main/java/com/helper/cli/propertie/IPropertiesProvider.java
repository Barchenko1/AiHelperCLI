package com.helper.cli.propertie;

import java.util.Map;

public interface IPropertiesProvider {
    void loadProperties();
    String getProperty(String key);
    Map<String, String> getPropertyMap();
}
