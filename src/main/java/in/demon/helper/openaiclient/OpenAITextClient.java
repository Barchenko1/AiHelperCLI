package in.demon.helper.openaiclient;

public class OpenAITextClient implements IOpenAIClient{

    private final String apiKey;

    public OpenAITextClient(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String sendToOpenAI(String json) {
        return "";
    }
}
