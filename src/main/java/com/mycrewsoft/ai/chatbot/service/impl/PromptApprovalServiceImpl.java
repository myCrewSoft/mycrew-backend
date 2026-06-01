package com.mycrewsoft.ai.chatbot.service.impl;

import org.springframework.stereotype.Service;

import com.mycrewsoft.ai.chatbot.service.PromptService;

@Service("promptApprovalService")
public class PromptApprovalServiceImpl implements PromptService {

	@Override
	public String build(String question, String reference) {
		return """
				[ROLE]
				당신은 결재 문서 작성 및 결재선 분석 AI입니다.

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
				당신은 결재 문서 작성 및 결재선 분석 AI입니다.

				[RULES]
				- 추측하지 마십시오.
				- 정보가 없으면 "확인 불가"라고 답변하십시오.

				[QUESTION]
				%s

				[OUTPUT FORMAT]
				- 상세하게 설명을 하고 예시를 10개 들어주십시오.
				""".formatted(question);
	}

}
