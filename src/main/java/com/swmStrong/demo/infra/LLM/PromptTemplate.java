package com.swmStrong.demo.infra.LLM;

import org.springframework.stereotype.Component;

@Component
public class PromptTemplate {
    
    private static final String CLASSIFICATION_PROMPT_TEMPLATE = """
Which category does the usage of this app with the given URL and title belong to?
Respond with **only one** of the following categories. **Do not explain. Do not say anything else.**
                
# category
SNS, Documentation, Design, Communication, LLM, Development, Productivity, Video Editing, Entertainment, File Management, System & Utilities, Game, Education, Finance

# input
%s
""";

    public String getClassificationPrompt(String query) {
        return CLASSIFICATION_PROMPT_TEMPLATE.formatted(query);
    }
}
