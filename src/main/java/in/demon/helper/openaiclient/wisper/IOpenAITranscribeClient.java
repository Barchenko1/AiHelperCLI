package in.demon.helper.openaiclient.wisper;

import java.io.File;

public interface IOpenAITranscribeClient {
    String transcribeWithOpenAI(File audioFile);
}
