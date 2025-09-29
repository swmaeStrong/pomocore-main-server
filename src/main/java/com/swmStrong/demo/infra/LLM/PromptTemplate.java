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
사용자의 앱 사용 데이터를 분석하여 30자 이내로 활동을 요약합니다.

**데이터 구조:**
- App: 애플리케이션명
- Title: 창 제목/콘텐츠
- URL: 웹 주소 (해당하는 경우)
- Duration: 사용 시간(초)

**요약 생성 규칙:**

[필수 규칙]
- 형식: 명사구로 종결 (예: "개발", "작업", "관리")
- 길이: 한글 30자 이내, 영어는 한글을 번역
- 구조: [구체적 활동] + [대상/도구] + [행위명사]
예: "Java 프로젝트 코드 작성"

[우선순위 기준]
1순위: Duration 최상위 2개 앱의 핵심 활동
2순위: 전체 시간의 30퍼센트 이상 차지하는 활동
3순위: 의미있는 작업 패턴

[특별 처리]
- afk/loginwindow
- 기본: 무시
- 전체 50퍼센트 이상: "(주요활동) 중 자리비움" 형식

- YouTube 음악 (URL이 'youtube.com' 으로 시작하고, title이 노래와 관련이 있을 경우)
- 기본: 무시
- 전체 30퍼센트 이상: "음악 감상하며 (주요활동)" 형식

**좋은 예시:**
- "백준 문제 풀이 및 블로그 작성"
- "pomocore 팀 관리 업무"
- "Spring 프로젝트 개발"

**데이터:**
%s

**출력:**
{
    summaryKor: string,
    summaryEng: string
}
""";

    public String getSummarizeTemplate(String query) {
        return SUMMARIZE_PROMPT_TEMPLATE.formatted(query.replace("%", "%%"));
    }
}
