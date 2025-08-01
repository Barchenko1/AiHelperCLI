package in.demon.helper.util;

import java.util.HashMap;
import java.util.Map;

public final class Constant {
    public static final String PROMPT_1 = "This is a screenshot wish task. " +
            "Please read the image carefully and do the following: " +
            "1. Understand the problem requirements (don't write it)" +
            "2. Extract the input/output format. (don't write it)" +
            "3. Write a clear and correct solution in Java. (write it)" +
            "4. Explain the time and space complexity. (write it)" +
            "5. Provide a brief explanation of the approach. (write it)" +
            "6. Try to be short but complex";

    public static final String PROMPT_2 = "This is a screenshot wish task. " +
            "Also for understanding question can be highlighted " +
            "Please read the image carefully and do the following: " +
            "1. Understand the problem requirements (don't write it)" +
            "2. Extract the input/output format. (don't write it)" +
            "3. Write a clear and correct solution. (write it)" +
            "4. Try to be short but complex.";

    public static final String PROMPT_3 = "This is a text of voice from talk. " +
            "Try to find question sentences in text" +
            " remember you are at software engineering interview read text carefully and do the following: " +
            "1. Understand the question (don't write it)" +
            "2. Extract the input/output format. (don't write it)" +
            "3. Write a clear and correct answer. (write it)" +
            "4. Try to be short but complex. " +
            "5. Try to create an example on Java.";

    public static final String PROMPT_4 = "This is a text with task. Find task and resolve it.";

    public static final Map<String, String> SUB_PROMPT_MAP = new HashMap<>() {{
        put("algorithm", PROMPT_1);
        put("flex", PROMPT_2);
        put("voice", PROMPT_3);
        put("copyHtml", PROMPT_4);
    }};
}
