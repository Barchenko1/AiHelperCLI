package com.helper.cli.executor.sceen;

public interface IScreenHotKeyExecutor {
    void execute(String prompt, String programmingLanguage);
    /** Same as execute (F1), but tells whether the server accepted the screenshot. */
    boolean captureAndSend(String prompt, String programmingLanguage);
    void commitScreenShots();
    void pushScreenShots(String prompt, String programmingLanguage);
}
