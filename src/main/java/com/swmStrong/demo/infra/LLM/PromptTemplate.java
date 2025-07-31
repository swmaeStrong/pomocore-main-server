package com.swmStrong.demo.infra.LLM;

import org.springframework.stereotype.Component;

@Component
public class PromptTemplate {
    
    private static final String CLASSIFICATION_PROMPT_TEMPLATE = """
Which category does the usage of thie app with the given URL and title belong to?

Respond with **only one** of the following categories. **Do not explain. Do not say anything else.**
                
# category
SNS, Documentation, Design, Communication, LLM, Development, Productivity, Video Editing, Entertainment, File Management, System & Utilities, Game, Education, Finance, Browsing, Marketing, Music, E-commerce & Shopping

# input
%s
""";

    private static final String BEHAVIOR_CLASSIFICATION_PROMPT_V1 = """
Based on the user's active application usage pattern, categorize their current behavior into one of the predefined categories.

**Analysis Context:**
- App Name: The specific application the user is currently using
- Title: The window title or content description
- URL: The web address or application context (if applicable)

**Instructions:**
1. Analyze the user's digital behavior pattern from the provided app usage data
2. Consider the app's primary function and the specific context (title/URL)
3. Infer the user's intent and activity type
4. Respond with **exactly one** category from the list below
5. **Do not provide explanations or additional text**

**Categories:**
SNS, Documentation, Design, Communication, LLM, Development, Productivity, Video Editing, Entertainment, File Management, System & Utilities, Game, Education, Finance, Browsing, Marketing, Music, E-commerce & Shopping

**Input Data:**
%s
""";

    private static final String BEHAVIOR_CLASSIFICATION_PROMPT_V2 = """
Classify the user's digital activity based on their current app usage pattern.

**Task:** Determine what the user is doing based on their active application and context.

**Analysis Approach:**
- Primary: What is the main purpose of this application?
- Secondary: What specific activity does the title/URL suggest?
- Behavioral Intent: What is the user likely trying to accomplish?

**Response Format:** Return only one category name from the list below.

**Categories:**
SNS, Documentation, Design, Communication, LLM, Development, Productivity, Video Editing, Entertainment, File Management, System & Utilities, Game, Education, Finance, Browsing, Marketing, Music, E-commerce & Shopping

**Data:**
%s
""";

    public String getClassificationPrompt(String query) {
        return CLASSIFICATION_PROMPT_TEMPLATE.formatted(query);
    }

    public String getBehaviorClassificationPromptV0(String query) {
        return CLASSIFICATION_PROMPT_TEMPLATE.formatted(query);
    }

    public String getBehaviorClassificationPromptV1(String query) {
        return BEHAVIOR_CLASSIFICATION_PROMPT_V1.formatted(query);
    }

    public String getBehaviorClassificationPromptV2(String query) {
        return BEHAVIOR_CLASSIFICATION_PROMPT_V2.formatted(query);
    }
}
