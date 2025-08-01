package in.demon.helper;

import com.github.kwhat.jnativehook.GlobalScreen;
import in.demon.helper.event.GlobalEventListener;
import in.demon.helper.propertie.IPropertiesProvider;
import in.demon.helper.propertie.PropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final IPropertiesProvider propertiesProvider = new PropertiesProvider();
//    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");

    public static void main(String[] args) {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new GlobalEventListener());
            LOGGER.info("Listening for global fn + F1 key...");
        } catch (Exception e) {
            LOGGER.error("Failed to register global hook.", e);
            throw new RuntimeException(e);
        }
    }
}
