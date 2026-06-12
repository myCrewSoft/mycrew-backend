package com.mycrewsoft.ai.chatbot.service;

public interface PromptService {
 
    /**
     * RAG 기반 프롬프트 생성 메서드
     *
     * ✔ 검색된 문서 컨텍스트(context)를 포함하여
     *   LLM에게 질문을 전달하는 경우 사용
     *
     * 사용 시나리오
     * --------------------------------------------------
     * - 벡터 DB / 문서 검색 결과가 존재할 때
     * - "다음 문서를 참고하여 질문에 답하라" 형태의 프롬프트
     *
     * 내부 동작
     * --------------------------------------------------
     * - context : 검색된 문서 chunk들의 요약 또는 원문
     * - question : 사용자 입력 질문
     * - provider.systemPrompt(context, question) 호출
     *
     * @param question 사용자 질문
     * @param context  RAG 검색을 통해 확보한 문서 컨텍스트
     * @return LLM에 전달할 최종 프롬프트 문자열
     */
    public String build(String question, String reference);

    /**
     * 일반 질문용 프롬프트 생성 메서드 (Non-RAG)
     *
     * ✔ 문서 검색 없이 순수 질의응답(QA) 형태로
     *   LLM을 사용하는 경우
     *
     * 사용 시나리오
     * --------------------------------------------------
     * - 간단한 FAQ
     * - 대화형 챗봇
     * - 컨텍스트가 필요 없는 질문
     *
     * 설계 포인트
     * --------------------------------------------------
     * - 메서드 오버로딩을 통해 호출부 단순화
     * - context 여부에 따른 분기 로직을
     *   호출자가 아닌 Builder 내부에서 흡수
     *
     * @param question 사용자 질문
     * @return LLM에 전달할 최종 프롬프트 문자열
     */
    public String build(String question);
}
