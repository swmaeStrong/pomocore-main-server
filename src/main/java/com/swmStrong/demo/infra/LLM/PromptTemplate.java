package com.swmStrong.demo.infra.LLM;

import org.springframework.stereotype.Component;

@Component
public class PromptTemplate {
    
    private static final String CLASSIFICATION_PROMPT_TEMPLATE = """
Which category does the usage of this app with the given URL and title belong to?

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
4. If user use youtube but title is not about entertainment, should categorize properly
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
        return getBehaviorClassificationPromptV1(query);
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

    private static final String SUMMARIZE_PROMPT_TEMPLATE = """
사용자가 사용한 앱들의 정보를 바탕으로, 해당 세션 동안 사용자가 무엇을 했는지 30자 이내로 요약해주세요.

**분석할 데이터:**
- App: 사용한 애플리케이션 이름
- Title: 창 제목이나 콘텐츠 설명
- URL: 웹 주소 (해당하는 경우)
- Duration: 사용 시간 (초)

**요약 지침:**
1. 가장 많이 사용한 앱과 활동을 중심으로 요약
2. 구체적인 작업 내용이나 주제가 있다면 포함
3. 한국어로 자연스러운 한 문장으로 작성
4. "사용자가" 같은 불필요한 주어는 생략하고 핵심 위주로 간결하게 작성
5. 명사형으로 문장을 마무리 (예시: "Java 프로젝트 코드 작성", "pomocore 팀 관리", "백준 문제 풀이 및 블로그 포스팅")
6. afk, loginwindow를 포함하는 경우 분석에 사용하지 않고 무시.
7. 단, 전체 duration의 합산 대비 loginwindow, afk 의 비중이 절반 이상의 경우 "(특정 행동) 중 잠시 자리비움" 으로 표시
8. 마지막에 문장 부호 붙이지 않기 (예시: '.' ',' '?' '!')

**데이터:**
%s
""";

    public String getSummarizeTemplate(String query) {
        return SUMMARIZE_PROMPT_TEMPLATE.formatted(query.replace("%", "%%"));
    }
}
