package in.demon.helper;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.google.gson.Gson;
import in.demon.helper.event.GlobalEventListener;

public class Main {
    private static final String PARAM = "param";
    private static final String OPENAI_API_KEY = System.getenv("OPENAI_API_KEY");
    private static final Gson GSON = new Gson();

    public static void main(String[] args) {
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new GlobalEventListener(OPENAI_API_KEY));
            System.out.println("Listening for global fn + F1 key...");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
