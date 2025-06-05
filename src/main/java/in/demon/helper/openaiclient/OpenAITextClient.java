package in.demon.helper.openaiclient;

public class OpenAITextClient implements IOpenAIClient{

    private IOpenAIClient openAIClient;

    public OpenAITextClient(String apiKey) {
        this.openAIClient = new OpenAITextClient(apiKey);
    }

    @Override
    public String sendToOpenAI(String json) {
        return "";
    }
}
