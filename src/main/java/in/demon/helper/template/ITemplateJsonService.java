package in.demon.helper.template;

public interface ITemplateJsonService {
    String buildJsonPayload(String templateFile, String prompt, String subPrompt);
}
