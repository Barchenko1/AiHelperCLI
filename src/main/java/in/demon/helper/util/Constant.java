package in.demon.helper.util;

import java.util.HashMap;
import java.util.Map;

public final class Constant {
    public static final String COMPLETIONS_API_URL = "https://api.openai.com/v1/chat/completions";
    public static final String TRANSCRIPTION_API_URL = "https://api.openai.com/v1/audio/transcriptions";
    public static final String WEBSOCKET_API_URL = "ws://192.168.1.22:8080/ws";

    public static final String PROMPT_1 = "This is a screenshot wish task. " +
            "Please read the image carefully and do the following: " +
            "1. Understand the problem requirements (don't write it)" +
            "2. Extract the input/output format. (don't write it)" +
            "3. Write a clear and correct solution in Java. (write it)" +
            "4. Explain the time and space complexity. (write it)" +
            "5. Provide a brief explanation of the approach. (write it)" +
            "6. Try to be short but complex";

    public static final String PROMPT_2 = "This is a screenshot wish task. " +
            "Please read the image carefully and do the following: " +
            "1. Understand the problem requirements (don't write it)" +
            "2. Extract the input/output format. (don't write it)" +
            "3. Write a clear and correct solution in Java. (write it)" +
            "4. Explain the time and space complexity. (write it)" +
            "5. Provide a brief explanation of the approach. (write it)" +
            "6. Try to be short but complex";

    public static final Map<String, String> PARAM_MAP = new HashMap<>() {{
        put("prompt1", PROMPT_1);
        put("prompt2", PROMPT_2);
    }};
}
