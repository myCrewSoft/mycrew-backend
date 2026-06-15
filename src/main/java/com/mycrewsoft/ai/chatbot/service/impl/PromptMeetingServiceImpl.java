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
                대화 로그를 분석하여 공식 회의록 HTML을 생성합니다.

                [RULES]
                - 반드시 [REFERENCE]에 제공된 정보만 사용하십시오.
                - 대화 로그에 없는 내용은 추측하지 마십시오.
                - 정보가 불충분한 항목은 "확인 불가"로 작성하십시오.
                - 모든 내용은 한국어로 작성하십시오.
                - 구어체(~했어요, ~인데요 등)는 문어체(~하였음, ~임)로 변환하십시오.
                - 반복되거나 불필요한 발언(음, 어, 잠깐만요 등)은 제거하십시오.

                [REFERENCE]
                %s

                [QUESTION]
                %s

                [OUTPUT FORMAT]
                - 반드시 완전한 HTML 문서로만 출력하십시오.
                - ```html 코드블록, 마크다운, 설명 텍스트를 절대 포함하지 마십시오.
                - <!DOCTYPE html>부터 </html>까지만 출력하십시오.
                - 서명란은 [REFERENCE]의 참석자 중 작성자(주재자)를 제외한 인원수에 맞게 열(column)을 생성하십시오.
                - 아래 HTML 양식의 {{...}} 플레이스홀더를 실제 내용으로 채우십시오.
                - 안건별 논의 내용은 대화 로그에서 파악되는 주제별로 구분하여 작성하십시오.
                - 액션 아이템은 대화 로그에서 "~하겠습니다", "~해주세요", "~까지 처리" 등의 표현을 찾아 추출하십시오.
                - 서명란의 각 칸에는 반드시 {{SIGN:순서}} 형식의 토큰을 삽입하십시오. (예: {{SIGN:1}}, {{SIGN:2}})
                
                [HTML 양식]
                %s
                """
                .formatted(reference, question, HTML_TEMPLATE);
    }

    private static final String HTML_TEMPLATE = """
            <!DOCTYPE html>
            <html lang="ko">
            <head>
            <meta charset="UTF-8">
            <style>
              body { font-family: 'Malgun Gothic', sans-serif; font-size: 13px; color: #1e293b; margin: 40px; }
              h1 { font-size: 20px; text-align: center; font-weight: bold; margin-bottom: 24px; border-bottom: 2px solid #334155; padding-bottom: 12px; overflow: hidden;}
              .info-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
              .info-table td { border: 1px solid #334155; padding: 6px 12px; }
              .info-table td:first-child { background: #f1f5f9; font-weight: bold; width: 100px; text-align: center; }
              .sign-table { border-collapse: collapse; font-size: 12px; text-align: center; float: right; margin: 0 0 12px 12px; }
              .sign-table td { border: 1px solid #334155; padding: 4px 14px; }
              .sign-table .header { background: #f1f5f9; font-weight: bold; }
              .sign-table .body { width: 84px; height: 60px; vertical-align: middle; }
              .section-title { font-weight: bold; font-size: 14px; background: #f1f5f9; border-left: 4px solid #3b82f6; padding: 6px 12px; margin: 20px 0 8px 0; }
              .content-box { border: 1px solid #e2e8f0; padding: 12px 16px; min-height: 60px; line-height: 1.8; }
              .agenda-item { margin-bottom: 12px; }
              .agenda-item .title { font-weight: bold; margin-bottom: 4px; }
              .action-table { width: 100%; border-collapse: collapse; margin-top: 8px; }
              .action-table th { background: #f1f5f9; border: 1px solid #334155; padding: 6px; text-align: center; }
              .action-table td { border: 1px solid #e2e8f0; padding: 6px 10px; }
              .clearfix::after { content: ""; display: table; clear: both; }
            </style>
            </head>
            <body>
            <div class="clearfix">
              <table class="sign-table">
                <tr>
                  <!-- 참석자(작성자 제외) 이름을 <td class="header">이름</td> 형태로 반복 -->
                </tr>
                <tr>
                  <!-- 각 칸에 {{SIGN:순서}} 토큰을 삽입. 예: {{SIGN:1}}, {{SIGN:2}} -->
                  <!-- 순서는 1부터 시작, 참석자(작성자 제외) 수만큼 -->
                </tr>
              </table>
              <h1>회 의 록</h1>
            </div>
            <table class="info-table">
              <tr><td>회의명</td><td>{{회의명}}</td></tr>
              <tr><td>일시</td><td>{{시작일시}} ~ {{종료일시}}</td></tr>
              <tr><td>장소</td><td>{{회의실명 또는 온라인}}</td></tr>
              <tr><td>주재자</td><td>{{작성자명}}</td></tr>
              <tr><td>참석자</td><td>{{참석자 전원 이름, 쉼표 구분}}</td></tr>
            </table>
            <div class="section-title">1. 회의 목적</div>
            <div class="content-box">{{회의 목적 요약}}</div>
            <div class="section-title">2. 안건별 논의 내용</div>
            {{안건별 agenda-item 블록 반복}}
            <div class="section-title">3. 결정 사항</div>
            <div class="content-box">{{결정 내용 번호 목록}}</div>
            <div class="section-title">4. 액션 아이템</div>
            <table class="action-table">
              <tr><th>담당자</th><th>내용</th><th>기한</th></tr>
              {{액션 아이템 tr 반복}}
            </table>
            <div class="section-title">5. 특이사항 / 기타</div>
            <div class="content-box">{{기타 내용 또는 없음}}</div>
            </body>
            </html>
            """;

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
