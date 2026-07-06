package com.mycrewsoft.domain.mtng.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mycrewsoft.ai.chatbot.service.PromptService;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DateUtil;
import com.mycrewsoft.domain.mtng.mapper.MtngMapper;
import com.mycrewsoft.domain.mtng.mapper.MtngMomMapper;
import com.mycrewsoft.domain.mtng.vo.MtngDetailVO;
import com.mycrewsoft.domain.mtng.vo.MtngPtcptDetailVO;
import com.mycrewsoft.domain.mtng.vo.mom.MtngMomHistVO;
import com.mycrewsoft.domain.mtng.vo.mom.MtngMomVO;
import com.mycrewsoft.domain.video.mapper.VideoConfMapper;
import com.mycrewsoft.domain.video.vo.VideoChatLogVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MtngMomAiService {

	private final ChatClient chatClient;
	private final MtngMapper mtngMapper;
	private final MtngMomMapper mtngMomMapper;
	private final VideoConfMapper videoConfMapper;

	@Autowired
	@Qualifier("promptMeetingService")
	private PromptService promptMeetingService;

	// AI 초안 생성 후 draftCn 반환 (최초 생성, 재생성 공통 사용)
	public String generateDraft(Long vconfId, Long mtngId) {
		MtngDetailVO detailVO = mtngMapper.selectMtngDetailByVconfId(vconfId);
		List<MtngPtcptDetailVO> ptcptList = mtngMapper.selectMtngPtcptDetailList(mtngId);

		String reference = buildReference(vconfId, detailVO, ptcptList);
		String question = "위 대화 로그를 기반으로 회의록 내용을 작성하십시오.";

		String aiFragment = chatClient.prompt().user(promptMeetingService.build(question, reference)).call().content();

		return assembleAiMomHtml(aiFragment, detailVO, ptcptList);
	}

	// 회의록 AI 초안 재생성 (실패 시 수동 재시도, 수정 이력 저장 포함)
	@Async
	public void regenerateAiDraft(Long mtngId, Long empId) {

		MtngDetailVO mtngVO = mtngMapper.selectMtngDetail(mtngId);
		if (mtngVO == null)
			throw new CustomException(ErrorCode.MTNG_NOT_FOUND);
		if (!mtngVO.getCrtrId().equals(empId))
			throw new CustomException(ErrorCode.ACCESS_DENIED);

		String draftCn = generateDraft(mtngVO.getVconfId(), mtngId);

		MtngMomVO momVO = mtngMomMapper.selectMomByMtngId(mtngId);
		if (momVO == null) {
			// 회의록 없으면 새로 INSERT
			MtngMomVO newMomVO = MtngMomVO.builder().mtngId(mtngId).momCn(draftCn).momSttusCd("01").build();
			mtngMomMapper.createMtngMom(newMomVO);
		} else {
			// 기존 회의록 있으면 이력 저장 후 UPDATE
			MtngMomHistVO histVO = MtngMomHistVO.builder().momId(momVO.getMomId()).momCn(momVO.getMomCn()).edtrId(empId)
					.build();
			mtngMomMapper.createMtngMomHist(histVO);

			momVO.setMomCn(draftCn);
			mtngMomMapper.updateMtngMom(momVO);
		}
	}

	// 대화 로그 + 회의 정보를 AI 프롬프트용 텍스트로 조합
	private String buildReference(Long vconfId, MtngDetailVO detailVO, List<MtngPtcptDetailVO> ptcptList) {
		List<VideoChatLogVO> chatLogs = videoConfMapper.selectChatLogsByVconfId(vconfId);

		String ptcptNames = ptcptList.stream().map(MtngPtcptDetailVO::getEmpNm).collect(Collectors.joining(", "));

		// 1단계: 대화 로그를 30줄씩 청크로 분할
		List<String> logLines = chatLogs.stream().map(log -> "[" + log.getMbrId() + "] " + log.getSpkngCn())
				.collect(Collectors.toList());

		int chunkSize = 30;
		StringBuilder summaries = new StringBuilder();

		// 2단계: 청크별 LLM 요약 호출
		for (int i = 0; i < logLines.size(); i += chunkSize) {
			List<String> chunk = logLines.subList(i, Math.min(i + chunkSize, logLines.size()));
			String chunkText = String.join("\n", chunk);

			String summary = chatClient.prompt().user(promptMeetingService.buildChunkSummary(chunkText)).call()
					.content();

			summaries.append("[요약 ").append(i / chunkSize + 1).append("]\n");
			summaries.append(summary).append("\n\n");
		}

		// 3단계: 요약 결과로 reference 구성
		return """
				회의명: %s
				일시: %s ~ %s
				장소: %s
				주재자: %s
				참석자: %s

				[대화 로그 요약]
				%s
				""".formatted(detailVO.getMtngNm(), detailVO.getBeginDt(), detailVO.getEndDt(),
				detailVO.getConfRmNm() != null ? detailVO.getConfRmNm() : "온라인", detailVO.getCrtrNm(), ptcptNames,
				summaries.toString());
	}

	private String assembleAiMomHtml(String aiFragment, MtngDetailVO detailVO, List<MtngPtcptDetailVO> ptcptList) {

		String purpose = extractBlock(aiFragment, "PURPOSE");
		String agenda = extractBlock(aiFragment, "AGENDA");
		String decisions = extractBlock(aiFragment, "DECISIONS");
		String actionRows = sanitizeActionRows(extractBlock(aiFragment, "ACTION_ROWS"));
		String notes = extractBlock(aiFragment, "NOTES");

		if (agenda.isBlank()) {
			agenda = "<div class=\"agenda-item\"><div class=\"title\">안건 1</div><div class=\"content-box\"><span class=\"empty-text\"></span></div></div>";
		}
		if (actionRows.isBlank()) {
			actionRows = "<tr><td class=\"assignee\"><span class=\"empty-text\">-</span></td><td><span class=\"empty-text\"></span></td><td class=\"due-date\"><span class=\"empty-text\">-</span></td></tr>";
		}

		List<MtngPtcptDetailVO> signTargetList = ptcptList.stream()
				.filter(p -> !Objects.equals(p.getEmpId(), detailVO.getCrtrId())).toList();

		String signHeaders = signTargetList.stream()
				.map(p -> "<td class=\"header\">" + escapeHtml(p.getEmpNm()) + "</td>").collect(Collectors.joining());

		String signBodies = IntStream.range(0, signTargetList.size())
				.mapToObj(i -> "<td class=\"body\">{{SIGN:" + (i + 1) + "}}</td>").collect(Collectors.joining());

		String ptcptNames = ptcptList.stream().map(p -> {
			String deptNm = defaultText(p.getDeptNm(), "-");
			String jobGrdNm = defaultText(p.getJobGrdNm(), "-");
			return escapeHtml(p.getEmpNm()) + " (" + escapeHtml(deptNm) + " / " + escapeHtml(jobGrdNm) + ")";
		}).collect(Collectors.joining(", "));

		String location = defaultText(detailVO.getConfRmNm(), "미정");
		String beginDt = formatDateTime(detailVO.getBeginDt());
		String endDt = formatDateTime(detailVO.getEndDt());

		return AI_MOM_TEMPLATE.replace("{{SIGN_HEADERS}}", signHeaders).replace("{{SIGN_BODIES}}", signBodies)
				.replace("{{MEETING_NAME}}", escapeHtml(detailVO.getMtngNm())).replace("{{BEGIN_DT}}", beginDt)
				.replace("{{END_DT}}", endDt).replace("{{LOCATION}}", escapeHtml(location))
				.replace("{{CREATOR_NAME}}", escapeHtml(detailVO.getCrtrNm()))
				.replace("{{PARTICIPANT_NAMES}}", ptcptNames).replace("{{PURPOSE}}", purpose)
				.replace("{{AGENDA_ITEMS}}", agenda).replace("{{DECISIONS}}", decisions)
				.replace("{{ACTION_ROWS}}", actionRows).replace("{{NOTES}}", notes);
	}

	private String extractBlock(String text, String tag) {
		Pattern pattern = Pattern.compile("\\{\\{" + tag + "\\}\\}(.*?)\\{\\{/" + tag + "\\}\\}", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(text);
		return matcher.find() ? matcher.group(1).trim() : "";
	}

	private String formatDateTime(LocalDateTime dateTime) {
		return dateTime == null ? "미정" : dateTime.format(DateUtil.DATETIME_FORMAT);
	}

	private String defaultText(String value, String defaultValue) {
		return value == null || value.isBlank() ? defaultValue : value;
	}

	private String escapeHtml(String value) {
		if (value == null)
			return "";
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&#39;");
	}

	private static final String AI_MOM_TEMPLATE = """
			<!DOCTYPE html>
			<html lang="ko">
			<head>
			<meta charset="UTF-8">
			<style>
			  * { box-sizing: border-box; }
			  body { margin: 0; padding: 40px; font-family: 'Malgun Gothic', 'Apple SD Gothic Neo', Arial, sans-serif; font-size: 13px; color: #111827; background: #ffffff; }
			  .document { max-width: 920px; margin: 0 auto; }
			  .top-bar { display: flex; align-items: flex-start; justify-content: space-between; gap: 24px; width: 100%; margin-bottom: 24px; }
			  .title-area { flex: 1 1 auto; min-width: 0; padding-right: 12px; }
			  h1 { margin: 0; font-size: 28px; font-weight: 800; letter-spacing: 8px; color: #0f172a; }
			  .sign-area { flex: 0 0 320px; text-align: right; }
			  .sign-table { display: inline-table; border-collapse: collapse; table-layout: fixed; min-width: 260px; font-size: 12px; text-align: center; color: #0f172a; }
			  .sign-table td { border: 1px solid #cbd5e1; }
			  .sign-table .header { height: 30px; padding: 6px 10px; font-weight: bold; background: #f8fafc; }
			  .sign-table .body { width: 86px; height: 64px; padding: 8px; vertical-align: middle; background: #ffffff; }
			  .meta-card { border: 1px solid #cbd5e1; border-radius: 10px; overflow: hidden; margin-bottom: 22px; }
			  .meta-table { width: 100%; border-collapse: collapse; table-layout: fixed; }
			  .meta-table th { width: 120px; padding: 11px 14px; text-align: left; font-weight: bold; color: #334155; background: #f8fafc; border-bottom: 1px solid #e2e8f0; border-right: 1px solid #e2e8f0; }
			  .meta-table td { padding: 11px 14px; color: #111827; border-bottom: 1px solid #e2e8f0; }
			  .meta-table tr:last-child th, .meta-table tr:last-child td { border-bottom: none; }
			  .section { margin-top: 20px; page-break-inside: avoid; }
			  .section-title { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; font-size: 15px; font-weight: 800; color: #0f172a; }
			  .section-title .number { display: inline-block; min-width: 24px; height: 24px; line-height: 24px; text-align: center; font-size: 12px; color: #ffffff; background: #2563eb; border-radius: 50%; }
			  .content-box { min-height: 64px; padding: 14px 16px; line-height: 1.75; color: #1f2937; background: #ffffff; border: 1px solid #e2e8f0; border-radius: 10px; }
			  .agenda-list { border: 1px solid #e2e8f0; border-radius: 10px; overflow: hidden; }
			  .agenda-item { padding: 14px 16px; border-bottom: 1px solid #e2e8f0; }
			  .agenda-item:last-child { border-bottom: none; }
			  .agenda-item .title { margin-bottom: 6px; font-size: 14px; font-weight: 800; color: #111827; }
			  .agenda-item .content-box { border: none; padding: 0; min-height: auto; }
			  .action-table { width: 100%; border-collapse: separate; border-spacing: 0; overflow: hidden; border: 1px solid #cbd5e1; border-radius: 10px; table-layout: fixed; }
			  .action-table th { padding: 10px 12px; font-weight: bold; text-align: center; color: #334155; background: #f8fafc; border-bottom: 1px solid #cbd5e1; border-right: 1px solid #e2e8f0; }
			  .action-table th:last-child { border-right: none; }
			  .action-table td { padding: 10px 12px; line-height: 1.6; vertical-align: top; border-bottom: 1px solid #e2e8f0; border-right: 1px solid #e2e8f0; }
			  .action-table td:last-child { border-right: none; }
			  .action-table tr:last-child td { border-bottom: none; }
			  .action-table .assignee { width: 120px; text-align: center; font-weight: bold; }
			  .action-table .due-date { width: 130px; text-align: center; color: #475569; }
			  .empty-text { color: #94a3b8; }
			  .footer { margin-top: 28px; padding-top: 12px; border-top: 1px solid #e2e8f0; font-size: 11px; color: #94a3b8; text-align: right; }
			  @media print { body { padding: 24px; } .document { max-width: none; } .section { page-break-inside: avoid; } }
			</style>
			</head>
			<body>
			<div class="document">

			  <div class="top-bar">
			    <div class="title-area">
			      <h1>회의록</h1>
			    </div>
			    <div class="sign-area">
			      <table class="sign-table">
			        <tr>{{SIGN_HEADERS}}</tr>
			        <tr>{{SIGN_BODIES}}</tr>
			      </table>
			    </div>
			  </div>

			  <div class="meta-card">
			    <table class="meta-table">
			      <tr><th>회의명</th><td>{{MEETING_NAME}}</td></tr>
			      <tr><th>일시</th><td>{{BEGIN_DT}} ~ {{END_DT}}</td></tr>
			      <tr><th>장소</th><td>{{LOCATION}}</td></tr>
			      <tr><th>주재자</th><td>{{CREATOR_NAME}}</td></tr>
			      <tr><th>참석자</th><td>{{PARTICIPANT_NAMES}}</td></tr>
			    </table>
			  </div>

			  <div class="section">
			    <div class="section-title"><span class="number">1</span><span>회의 목적</span></div>
			    <div class="content-box">{{PURPOSE}}</div>
			  </div>

			  <div class="section">
			    <div class="section-title"><span class="number">2</span><span>안건별 논의 내용</span></div>
			    <div class="agenda-list">{{AGENDA_ITEMS}}</div>
			  </div>

			  <div class="section">
			    <div class="section-title"><span class="number">3</span><span>결정 사항</span></div>
			    <div class="content-box">{{DECISIONS}}</div>
			  </div>

			  <div class="section">
			    <div class="section-title"><span class="number">4</span><span>액션 아이템</span></div>
			    <table class="action-table">
			      <tr>
			        <th class="assignee">담당자</th>
			        <th>내용</th>
			        <th class="due-date">기한</th>
			      </tr>
			      {{ACTION_ROWS}}
			    </table>
			  </div>

			  <div class="section">
			    <div class="section-title"><span class="number">5</span><span>특이사항 / 기타</span></div>
			    <div class="content-box">{{NOTES}}</div>
			  </div>

			  <div class="footer">본 문서는 그룹웨어 회의록 양식에 따라 자동 생성되었습니다.</div>

			</div>
			</body>
			</html>
			""";

	private static final String DEFAULT_ACTION_ROW = "<tr><td class=\"assignee\"><span class=\"empty-text\">-</span></td><td><span class=\"empty-text\"></span></td><td class=\"due-date\"><span class=\"empty-text\">-</span></td></tr>";

	// LLM이 생성한 액션 아이템 중 <tr>...</tr> 형태로 올바르게 닫힌 것만 추려서 사용
	// 깨진 태그(미완성 tr/td)가 섞여도 화면이 깨지지 않도록 방어
	private String sanitizeActionRows(String raw) {
		if (raw == null || raw.isBlank()) {
			return DEFAULT_ACTION_ROW;
		}

		Pattern rowPattern = Pattern.compile("<tr>.*?</tr>", Pattern.DOTALL);
		Matcher matcher = rowPattern.matcher(raw);

		StringBuilder valid = new StringBuilder();
		while (matcher.find()) {
			valid.append(matcher.group());
		}

		return valid.length() > 0 ? valid.toString() : DEFAULT_ACTION_ROW;
	}
}