package com.helper.cli.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConstantTest {

    @Test
    void languageSentenceIsAppended() {
        assertEquals("Solve it. Write a clear and correct solution in Java. (write it)",
                Constant.withLanguage("Solve it.", "Java"));
    }

    @Test
    void noneLeavesTheLanguageOut() {
        assertEquals("Solve it.", Constant.withLanguage("Solve it.", "None"));
        assertEquals("Solve it.", Constant.withLanguage("  Solve it. ", " none "));
    }

    @Test
    void missingLanguageLeavesItOut() {
        assertEquals("Solve it.", Constant.withLanguage("Solve it.", null));
        assertEquals("Solve it.", Constant.withLanguage("Solve it.", "  "));
    }

    @Test
    void nullPromptStillGetsTheLanguage() {
        assertEquals("Write a clear and correct solution in Go. (write it)", Constant.withLanguage(null, "Go"));
        assertEquals("", Constant.withLanguage(null, "None"));
    }
}
