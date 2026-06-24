package com.mycrewsoft.ai.chatbot.service.impl;

import org.springframework.stereotype.Service;

import com.mycrewsoft.ai.chatbot.service.PromptService;

@Service("promptReportService")
public class PromptReportServiceImpl implements PromptService {

	@Override
	public String build(String question, String reference) {
		return """
				[ROLE]
				당신은 프로젝트의 보고서를 작성하는 AI입니다.

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
		        아래 구조로 한국어 보고서를 작성하십시오.

		        # 프로젝트 현황 보고서

		        ## 1. 프로젝트 개요
		        (프로젝트명, 기간, 상태, 진척률 요약)

		        ## 2. 업무 현황
		        (전체/완료/진행중/중단 건수와 간단한 분석)

		        ## 3. 마감 임박 업무
		        (7일 이내 마감 업무 목록, 없으면 "없음"으로 표기)

		        ## 4. 종합 의견
		        (현재 진척률과 업무 현황을 바탕으로 프로젝트 상태를 3~5문장으로 분석)
		        """.formatted(reference, question);
	}

	@Override
	public String build(String question) {
		return """
				[ROLE]
				당신은 프로젝트의 보고서를 작성하는 AI입니다.

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
