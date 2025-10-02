package com.helper.cli.executor.sceen;

public interface IScreenHotKeyExecutor {
    void execute(String prompt, String programmingLanguage);
    void commitScreenShots();
    void pushScreenShots(String prompt, String programmingLanguage);
}
