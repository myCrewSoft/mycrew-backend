package com.mycrewsoft.ai.chatbot.service.impl;

import org.springframework.stereotype.Service;

import com.mycrewsoft.ai.chatbot.service.PromptService;

@Service("promptMeetingService")
public class PromptMeetingServiceImpl implements PromptService {

	@Override
	public String build(String question, String reference) {
		return """
				[ROLE]
				당신은 회의록 자동 작성 AI입니다.
				대화 로그를 분석하여 회의록 본문 내용을 생성합니다.

				[RULES]
				- 반드시 [REFERENCE]에 제공된 정보만 사용하십시오.
				- 대화 로그에 없는 내용은 추측하지 마십시오.
				- 정보가 불충분한 항목은 "확인 불가"로 작성하십시오.
				- 모든 내용은 한국어로 작성하십시오.
				- 구어체는 문어체로 변환하십시오.
				- 반복되거나 불필요한 발언은 제거하십시오.

				[REFERENCE]
				%s

				[OUTPUT FORMAT]
				- 아래 5개 블록을 반드시 순서대로 출력하십시오.
				- 블록 태그 외에 다른 텍스트는 절대 출력하지 마십시오.
				- 마크다운, 코드블록, 설명 텍스트를 절대 포함하지 마십시오.
				- HTML 태그는 반드시 올바르게 열고 닫으십시오. 태그 속성은 시작 태그 안에만 작성하십시오.

				1. 회의 목적 (텍스트만)
				{{PURPOSE}}회의 목적 내용{{/PURPOSE}}

				2. 안건별 논의 내용 (안건 수만큼 div 반복)
				{{AGENDA}}<div class="agenda-item"><div class="title">안건명</div><div class="content-box">논의 내용</div></div>{{/AGENDA}}

				3. 결정 사항 (텍스트만)
				{{DECISIONS}}결정 사항 내용{{/DECISIONS}}

				4. 액션 아이템
				- 액션 아이템이 있으면 항목 수만큼 <tr><td class="assignee">담당자</td><td>내용</td><td class="due-date">기한</td></tr> 를 반복하십시오.
				- 액션 아이템이 없으면 아래 줄을 한 글자도 변경하지 말고 그대로 출력하십시오.
				{{ACTION_ROWS}}<tr><td class="assignee">확인 불가</td><td>진행되지 않음</td><td class="due-date">확인 불가</td></tr>{{/ACTION_ROWS}}

				5. 특이사항 / 기타 (텍스트만)
				{{NOTES}}기타 내용 또는 없음{{/NOTES}}
				"""
				.formatted(reference);
	}

	@Override
	public String buildChunkSummary(String chatLogChunk) {
		return """
				[ROLE]
				당신은 회의 대화 로그 요약 AI입니다.

				[RULES]
				- 주어진 대화 로그 일부를 간결하게 요약하십시오.
				- 핵심 내용, 결정 사항, 액션 아이템만 추출하십시오.
				- 불필요한 인사말, 반복 표현은 제거하십시오.
				- 한국어 문어체로 작성하십시오.
				- 요약은 3~5 문장으로 작성하십시오.

				[대화 로그]
				%s

				[OUTPUT FORMAT]
				- 요약 텍스트만 출력하십시오.
				- 마크다운이나 HTML을 사용하지 마십시오.
				""".formatted(chatLogChunk);
	}

	@Override
	public String build(String question) {
		return """
				[ROLE]
				당신은 회의록 작성 AI입니다.

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
