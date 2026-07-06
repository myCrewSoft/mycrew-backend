package com.mycrewsoft.ai.chatbot.service.impl;

import org.springframework.stereotype.Service;

import com.mycrewsoft.ai.chatbot.service.PromptService;
import com.mycrewsoft.domain.board.dto.response.BoardResponse;
import com.mycrewsoft.domain.board.service.BoardService;

import lombok.RequiredArgsConstructor;

@Service("promptPostRiskService")
@RequiredArgsConstructor
public class PromptPostRiskServiceImpl implements PromptService {

	private final BoardService boardservice;
	
	@Override
	public String build(String question, String boardId) {
		
		BoardResponse boardRp = boardservice.getBoard(null, Long.parseLong(boardId));
		String boardCn = boardRp.getBoardCn();
		
		return """
				[ROLE]
				        당신은 사내 게시물의 위험도를 정량적으로 분석하는 AI입니다.
				        제시된 [위험도 산정 기준]을 엄격히 준수하여 게시물의 위험 점수를 백분율(%%)로 수취화하십시오.

				        [위험도 산정 기준]
				        - 0%% ~ 20%% (안전/단순 불만): 단순한 업무적 아쉬움 토로, 일상적인 투덜거림, 욕설 없음.
				        - 21%% ~ 50%% (주의/불편 식별): 특정 부서나 제도에 대한 강한 불만 표출, 또는 가벼운 비속어/은어 1~2회 사용.
				        - 51%% ~ 80%% (경고/심각): 사내 구성원에 대한 직접적인 비방, 심각한 욕설 포함, 또는 퇴사/이탈 의사를 강력하게 표출.
				        - 81%% ~ 100%% (위험/최고조): 특정인에 대한 심각한 인신공격, 지속적인 저주/협박, 또는 회사 내부 기밀 유출 위험성 인지.

				        [RULES]
				        - 반드시 제공된 [REFERENCE] 내용만을 근거로 위 [위험도 산정 기준]에 매칭하여 점수를 부여하십시오.
				        - 추측하거나 과장하지 마십시오. 위험 요소를 판단할 정보가 전혀 없다면 risk_score는 0입니다.
				        - 출력은 반드시 아래 지정된 JSON 형식만 허용하며, 앞뒤 설명이나 인사말 등 딴소리는 일절 금지합니다.

				        [REFERENCE]
				        %s

				        [QUESTION]
				        %s

				        [OUTPUT FORMAT]
				        {
				          "risk_score": 0에서 100 사이의 정수 (위험도 %% 의미),
				          "reason": "위험도 점수를 산정하게 된 핵심 이유 (한글로 1줄 요약)"
				        }
				"""
				// ✔ Java 15+ Text Block 사용
				// ✔ String.format 대신 formatted() 사용 (가독성 + 유지보수성)
				.formatted(boardCn, question);
	}

	@Override
	public String build(String question) {
		return """
				[ROLE]
				당신은 게시물의 위험도 분석 AI입니다.

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
