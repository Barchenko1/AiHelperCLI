package com.helper.cli.util;

public final class Constant {
    public static final String LANGUAGE_TEXT = "Write a clear and correct solution in %s. (write it) ";
    /** programmingLanguage value that leaves the language sentence out of the prompt. */
    public static final String NO_LANGUAGE = "None";

    private Constant() {}

    /** The prompt plus the "solution in &lt;language&gt;" sentence, unless the language is None/empty. */
    public static String withLanguage(String prompt, String programmingLanguage) {
        String base = prompt == null ? "" : prompt.trim();
        if (programmingLanguage == null || programmingLanguage.isBlank()
                || NO_LANGUAGE.equalsIgnoreCase(programmingLanguage.trim())) {
            return base;
        }
        return (base + " " + LANGUAGE_TEXT.formatted(programmingLanguage.trim())).trim();
    }
}
