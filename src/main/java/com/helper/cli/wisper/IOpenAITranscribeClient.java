package com.helper.cli.wisper;

import java.io.File;

public interface IOpenAITranscribeClient {
    String transcribeWithOpenAI(File audioFile);
}
