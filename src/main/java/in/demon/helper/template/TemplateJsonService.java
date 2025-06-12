package in.demon.helper.template;

import java.io.IOException;
import java.io.InputStream;

public class TemplateJsonService implements ITemplateJsonService {

    @Override
    public String buildJsonPayload(String templateFile, String prompt, String subPrompt) {
        try (InputStream inputStream = this.getClass().getResourceAsStream(templateFile)) {
            if (inputStream == null) {
                throw new RuntimeException("JSON template not found: " + templateFile);
            }

            String template = new String(inputStream.readAllBytes());
            return template.formatted(prompt, subPrompt.replace("\"", "\\\""));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON template", e);
        }
    }
}
