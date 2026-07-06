package com.mycrewsoft.ai.chatbot.service.impl;

import org.springframework.stereotype.Service;

import com.mycrewsoft.ai.chatbot.service.PromptService;

@Service("promptChatbotService")
public class PromptChatbotServiceImpl implements PromptService {

	@Override
	public String build(String question, String reference) {
		return """
				[ROLE]
				당신은 그룹웨어의 챗봇 AI입니다.

				[RULES]
				- 반드시 [REFERENCE]만 사용하십시오.
				- 추측하지 마십시오.
				- 정보가 없으면 "확인 불가"라고 답변하십시오.
				- [REFERENCE]도 한글로 해석해서 답변하십시오.

				[REFERENCE]
				%s

				[QUESTION]
				%s

				[OUTPUT FORMAT]
				- 핵심 요약 3줄 이내
				"""
				// ✔ Java 15+ Text Block 사용
				// ✔ String.format 대신 formatted() 사용 (가독성 + 유지보수성)
				.formatted(reference, question);
	}

	@Override
	public String build(String question) {
		return """
				[ROLE]
				당신은 그룹웨어의 챗봇 AI입니다.
				사용자의 요청에 따라 필요한 도구를 호출하여 정확한 정보를 제공하십시오.

				[RULES]
				- 도구로 조회한 실제 데이터만 사용하십시오.
				- 도구 호출 결과를 자연스러운 한국어로 답변하십시오.
				- 도구가 없거나 조회 결과가 없으면 "확인할 수 없습니다. 죄송합니다."라고 답변하십시오.

				[QUESTION]
				%s
				""".formatted(question);
	}

}
