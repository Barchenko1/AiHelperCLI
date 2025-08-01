package in.demon.helper.propertie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesProvider implements IPropertiesProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesProvider.class);

    private final Properties properties = new Properties();
    private final Map<String, String> propertiesMap = new HashMap<>();

    public PropertiesProvider() {
        loadProperties();
    }

    @Override
    public void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new FileNotFoundException("transcription.properties not found in classpath");
            }
            properties.load(input);
            for (String name : properties.stringPropertyNames()) {
                propertiesMap.put(name, properties.getProperty(name));
            }
        } catch (IOException e) {
            LOGGER.error("❌ Failed to load transcription URL from properties: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public Map<String, String> getPropertyMap() {
        return propertiesMap;
    }
}
