package com.mycrewsoft.domain.mtng.service;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.DateUtil;
import com.mycrewsoft.domain.approval.dto.request.ApprovalDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalStepRequestDTO;
import com.mycrewsoft.domain.approval.service.ApprovalDraftWriteService;
import com.mycrewsoft.domain.approval.service.ApprovalRequestService;
import com.mycrewsoft.domain.mtng.dto.mom.request.MtngMomUpdateRequest;
import com.mycrewsoft.domain.mtng.dto.mom.response.MtngMomResponse;
import com.mycrewsoft.domain.mtng.mapper.MtngMapper;
import com.mycrewsoft.domain.mtng.mapper.MtngMomDtoMapper;
import com.mycrewsoft.domain.mtng.mapper.MtngMomMapper;
import com.mycrewsoft.domain.mtng.vo.MtngDetailVO;
import com.mycrewsoft.domain.mtng.vo.MtngPtcptDetailVO;
import com.mycrewsoft.domain.mtng.vo.mom.MtngMomHistVO;
import com.mycrewsoft.domain.mtng.vo.mom.MtngMomVO;
import com.mycrewsoft.security.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MtngMomServiceImpl implements MtngMomService {

	private final MtngMomMapper mtngMomMapper;
	private final MtngMapper mtngMapper;
	private final MtngMomDtoMapper mtngMomDtoMapper;
	private final ApprovalDraftWriteService approvalDraftWriteService;
	private final ApprovalRequestService approvalRequestService;
	private final MtngMomAiService mtngMomAiService;

	@Override
	@Transactional
	public Long createEmptyMom(Long mtngId) {
		Long empId = SecurityUtil.getCurrentEmpId();
		validatePtcpt(mtngId, empId);

		// 회의 기본정보 조회 (회의명, 일시, 장소, 참여자)
		MtngDetailVO detailVO = mtngMapper.selectMtngDetail(mtngId);
		List<MtngPtcptDetailVO> ptcptList = mtngMapper.selectMtngPtcptDetailList(mtngId);

		// 회의 기본정보가 채워진 HTML 양식 생성
		String initialHtml = buildInitialMomHtml(detailVO, ptcptList);

		MtngMomVO momVO = MtngMomVO.builder().mtngId(mtngId).momCn(initialHtml).momSttusCd("02") // 편집중
				.edtrId(empId).build();

		mtngMomMapper.createMtngMom(momVO);
		return momVO.getMomId();
	}

	@Override
	@Transactional
	public Long createAiDraftMom(Long mtngId, String draftCn) {

		MtngMomVO momVO = MtngMomVO.builder().mtngId(mtngId).momCn(draftCn).momSttusCd("01") // AI 초안
				.build();

		mtngMomMapper.createMtngMom(momVO);
		return momVO.getMomId();
	}

	@Override
	public MtngMomResponse getMom(Long mtngId) {
		Long empId = SecurityUtil.getCurrentEmpId();
		validatePtcpt(mtngId, empId);

		MtngMomVO momVO = mtngMomMapper.selectMomByMtngId(mtngId);
		if (momVO == null) {
			throw new CustomException(ErrorCode.MTNG_MOM_NOT_FOUND);
		}

		return mtngMomDtoMapper.toMomResponse(momVO);
	}

	@Override
	@Transactional
	public void updateMom(Long mtngId, MtngMomUpdateRequest request) {
		Long empId = SecurityUtil.getCurrentEmpId();
		validatePtcpt(mtngId, empId);

		MtngMomVO momVO = mtngMomMapper.selectMomByMtngId(mtngId);
		if (momVO == null) {
			throw new CustomException(ErrorCode.MTNG_MOM_NOT_FOUND);
		}

		// 결재 진행 중/완료된 회의록은 수정 불가
		if ("03".equals(momVO.getMomSttusCd()) || "04".equals(momVO.getMomSttusCd())) {
			throw new CustomException(ErrorCode.MTNG_MOM_NOT_EDITABLE);
		}

		// 수정 전 내용을 이력으로 적재
		MtngMomHistVO histVO = MtngMomHistVO.builder().momId(momVO.getMomId()).momCn(momVO.getMomCn())
				.edtrId(momVO.getEdtrId()).build();
		mtngMomMapper.createMtngMomHist(histVO);

		// 회의록 갱신 (편집중 상태 유지)
		MtngMomVO updateVO = MtngMomVO.builder().momId(momVO.getMomId()).momCn(request.getMomCn()).momSttusCd("02")
				.edtrId(empId).build();
		mtngMomMapper.updateMtngMom(updateVO);
	}

	@Override
	@Transactional
	public void requestApproval(Long mtngId) {
		Long empId = SecurityUtil.getCurrentEmpId();
		validatePtcpt(mtngId, empId);

		MtngMomVO momVO = mtngMomMapper.selectMomByMtngId(mtngId);
		if (momVO == null) {
			throw new CustomException(ErrorCode.MTNG_MOM_NOT_FOUND);
		}

		// 이미 결재 요청된 회의록은 중복 요청 불가
		if (momVO.getDrftDocSn() != null) {
			throw new CustomException(ErrorCode.MTNG_MOM_ALREADY_REQUESTED);
		}

		// 결재 진행 중/완료된 회의록은 결재 요청 불가
		if ("03".equals(momVO.getMomSttusCd()) || "04".equals(momVO.getMomSttusCd())) {
			throw new CustomException(ErrorCode.MTNG_MOM_NOT_EDITABLE);
		}

		// 참여자 목록 조회 (작성자 본인 제외한 나머지가 결재자)
		// 회의록 결재는 참여자 전원이 하므로, 1단계에 전원을 넣는다
		List<MtngPtcptDetailVO> ptcptList = mtngMapper.selectMtngPtcptDetailList(mtngId);
		List<Long> aprvrEmpIds = ptcptList.stream().map(MtngPtcptDetailVO::getEmpId).filter(id -> !id.equals(empId)) // 작성자
																														// 본인
																														// 제외
				.toList();

		if (aprvrEmpIds.isEmpty()) {
			throw new CustomException(ErrorCode.MTNG_MOM_NO_APPROVER);
		}

		// 결재선 구성: 1단계, 참여자 전원 동시 결재
		ApprovalStepRequestDTO approvalStep = new ApprovalStepRequestDTO();
		approvalStep.setAprvlOrd(1L);
		approvalStep.setAprvrEmpIds(aprvrEmpIds);

		// 기안 문서 생성 요청 DTO 구성
		// aprvlFullCn: AI가 생성한 회의록 HTML ({{SIGN:N}} 토큰 포함)
		// tmplatCd: null (템플릿 없이 AI HTML을 직접 사용)
		ApprovalDraftRequestDTO draftRequest = new ApprovalDraftRequestDTO();
		draftRequest.setDrftDocSn(null); // null이면 신규 INSERT
		draftRequest.setDocTtl("회의록 - " + momVO.getMtngId());
		draftRequest.setAprvlFullCn(momVO.getMomCn()); // AI 생성 HTML 그대로 사용
		draftRequest.setAtchFileId(null);
		draftRequest.setApprovalLines(List.of(approvalStep));

		// 1단계: 기안 문서 저장 (TB_APRVL_DOC INSERT)
		Long drftDocSn = approvalDraftWriteService.saveTemporaryDraft(draftRequest);

		// 2단계: 결재 요청 (상태 "진행중"으로 변경 + 결재자 알림)
		approvalRequestService.submitApproval(drftDocSn);

		// 3단계: TB_MTNG_MOM에 drftDocSn 연결 + 상태 "결재요청"으로 변경
		mtngMomMapper.updateMtngMomDrftDocSn(momVO.getMomId(), drftDocSn, "03");
	}

	@Override
	@Transactional
	public void regenerateAiDraft(Long mtngId) {
		
		Long empId = SecurityUtil.getCurrentEmpId();
		
		mtngMomAiService.regenerateAiDraft(mtngId, empId);
	}

	// 회의 참여자인지 검증 (회의록 조회/작성/수정 공통)
	private void validatePtcpt(Long mtngId, Long empId) {
		boolean isPtcpt = mtngMapper.selectMtngPtcptDetailList(mtngId).stream()
				.anyMatch(p -> p.getEmpId().equals(empId));

		if (!isPtcpt) {
			throw new CustomException(ErrorCode.MTNG_PTCPT_FORBIDDEN);
		}
	}

	// 오프라인 회의록 초기 HTML 양식 생성
	// AI 없이 회의 기본정보만 채워서 반환하고 작성자가 이후 내용을 직접 입력한다.
	private String buildInitialMomHtml(MtngDetailVO detailVO, List<MtngPtcptDetailVO> ptcptList) {

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

		String html = """
				<!DOCTYPE html>
				<html lang="ko">
				<head>
				<meta charset="UTF-8">
				<style>
				  * {
				    box-sizing: border-box;
				  }

				  body {
				    margin: 0;
				    padding: 40px;
				    font-family: 'Malgun Gothic', 'Apple SD Gothic Neo', Arial, sans-serif;
				    font-size: 13px;
				    color: #111827;
				    background: #ffffff;
				  }

				  .document {
				    max-width: 920px;
				    margin: 0 auto;
				  }

				  .top-bar {
				    display: flex;
				    align-items: flex-start;
				    justify-content: space-between;
				    gap: 24px;
				    width: 100%;
				    margin-bottom: 24px;
				  }

				  .title-area {
				    flex: 1 1 auto;
				    min-width: 0;
				    padding-right: 12px;
				  }

				  .document-label {
				    display: inline-block;
				    padding: 4px 10px;
				    margin-bottom: 10px;
				    font-size: 11px;
				    font-weight: bold;
				    color: #2563eb;
				    background: #eff6ff;
				    border: 1px solid #bfdbfe;
				    border-radius: 999px;
				    letter-spacing: -0.2px;
				  }

				  h1 {
				    margin: 0;
				    font-size: 28px;
				    font-weight: 800;
				    letter-spacing: 8px;
				    color: #0f172a;
				  }

				  .subtitle {
				    margin-top: 8px;
				    font-size: 12px;
				    color: #64748b;
				  }

				  .sign-area {
				    flex: 0 0 320px;
				    text-align: right;
				  }

				  .sign-table {
				    display: inline-table;
				    border-collapse: collapse;
				    table-layout: fixed;
				    min-width: 260px;
				    font-size: 12px;
				    text-align: center;
				    color: #0f172a;
				  }

				  .sign-table td {
				    border: 1px solid #cbd5e1;
				  }

				  .sign-table .header {
				    height: 30px;
				    padding: 6px 10px;
				    font-weight: bold;
				    background: #f8fafc;
				  }

				  .sign-table .body {
				    width: 86px;
				    height: 64px;
				    padding: 8px;
				    vertical-align: middle;
				    background: #ffffff;
				  }

				  .meta-card {
				    border: 1px solid #cbd5e1;
				    border-radius: 10px;
				    overflow: hidden;
				    margin-bottom: 22px;
				  }

				  .meta-table {
				    width: 100%;
				    border-collapse: collapse;
				    table-layout: fixed;
				  }

				  .meta-table th {
				    width: 120px;
				    padding: 11px 14px;
				    text-align: left;
				    font-weight: bold;
				    color: #334155;
				    background: #f8fafc;
				    border-bottom: 1px solid #e2e8f0;
				    border-right: 1px solid #e2e8f0;
				  }

				  .meta-table td {
				    padding: 11px 14px;
				    color: #111827;
				    border-bottom: 1px solid #e2e8f0;
				  }

				  .meta-table tr:last-child th,
				  .meta-table tr:last-child td {
				    border-bottom: none;
				  }

				  .section {
				    margin-top: 20px;
				    page-break-inside: avoid;
				  }

				  .section-title {
				    display: flex;
				    align-items: center;
				    gap: 8px;
				    margin-bottom: 8px;
				    font-size: 15px;
				    font-weight: 800;
				    color: #0f172a;
				  }

				  .section-title .number {
				    display: inline-block;
				    min-width: 24px;
				    height: 24px;
				    line-height: 24px;
				    text-align: center;
				    font-size: 12px;
				    color: #ffffff;
				    background: #2563eb;
				    border-radius: 50%;
				  }

				  .content-box {
				    min-height: 64px;
				    padding: 14px 16px;
				    line-height: 1.75;
				    color: #1f2937;
				    background: #ffffff;
				    border: 1px solid #e2e8f0;
				    border-radius: 10px;
				  }

				  .agenda-list {
				    border: 1px solid #e2e8f0;
				    border-radius: 10px;
				    overflow: hidden;
				  }

				  .agenda-item {
				    padding: 14px 16px;
				    border-bottom: 1px solid #e2e8f0;
				  }

				  .agenda-item:last-child {
				    border-bottom: none;
				  }

				  .agenda-item .title {
				    margin-bottom: 6px;
				    font-size: 14px;
				    font-weight: 800;
				    color: #111827;
				  }

				  .agenda-item .description {
				    line-height: 1.7;
				    color: #374151;
				  }

				  .decision-list {
				    margin: 0;
				    padding-left: 20px;
				    line-height: 1.8;
				  }

				  .decision-list li {
				    margin-bottom: 4px;
				  }

				  .action-table {
				    width: 100%;
				    border-collapse: separate;
				    border-spacing: 0;
				    overflow: hidden;
				    border: 1px solid #cbd5e1;
				    border-radius: 10px;
				    table-layout: fixed;
				  }

				  .action-table th {
				    padding: 10px 12px;
				    font-weight: bold;
				    text-align: center;
				    color: #334155;
				    background: #f8fafc;
				    border-bottom: 1px solid #cbd5e1;
				    border-right: 1px solid #e2e8f0;
				  }

				  .action-table th:last-child {
				    border-right: none;
				  }

				  .action-table td {
				    padding: 10px 12px;
				    line-height: 1.6;
				    vertical-align: top;
				    border-bottom: 1px solid #e2e8f0;
				    border-right: 1px solid #e2e8f0;
				  }

				  .action-table td:last-child {
				    border-right: none;
				  }

				  .action-table tr:last-child td {
				    border-bottom: none;
				  }

				  .action-table .assignee {
				    width: 120px;
				    text-align: center;
				    font-weight: bold;
				  }

				  .action-table .due-date {
				    width: 130px;
				    text-align: center;
				    color: #475569;
				  }

				  .empty-text {
				    color: #94a3b8;
				  }

				  .footer {
				    margin-top: 28px;
				    padding-top: 12px;
				    border-top: 1px solid #e2e8f0;
				    font-size: 11px;
				    color: #94a3b8;
				    text-align: right;
				  }

				  @media print {
				    body {
				      padding: 24px;
				    }

				    .document {
				      max-width: none;
				    }

				    .section {
				      page-break-inside: avoid;
				    }
				  }
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
				        <tr>
				          {{SIGN_HEADERS}}
				        </tr>
				        <tr>
				          {{SIGN_BODIES}}
				        </tr>
				      </table>
				    </div>
				  </div>

				  <div class="meta-card">
				    <table class="meta-table">
				      <tr>
				        <th>회의명</th>
				        <td>{{MEETING_NAME}}</td>
				      </tr>
				      <tr>
				        <th>일시</th>
				        <td>{{BEGIN_DT}} ~ {{END_DT}}</td>
				      </tr>
				      <tr>
				        <th>장소</th>
				        <td>{{LOCATION}}</td>
				      </tr>
				      <tr>
				        <th>주재자</th>
				        <td>{{CREATOR_NAME}}</td>
				      </tr>
				      <tr>
				        <th>참석자</th>
				        <td>{{PARTICIPANT_NAMES}}</td>
				      </tr>
				    </table>
				  </div>

				  <div class="section">
				    <div class="section-title">
				      <span class="number">1</span>
				      <span>회의 목적</span>
				    </div>
				    <div class="content-box">
				      <span class="empty-text"></span>
				    </div>
				  </div>

				  <div class="section">
				    <div class="section-title">
				      <span class="number">2</span>
				      <span>안건별 논의 내용</span>
				    </div>

				    <div class="agenda-list">
				      <div class="agenda-item">
				        <div class="title">안건 1</div>
				        <div class="description"><span class="empty-text"></span></div>
				      </div>
				    </div>
				  </div>

				  <div class="section">
				    <div class="section-title">
				      <span class="number">3</span>
				      <span>결정 사항</span>
				    </div>
				    <div class="content-box">
				      <ol class="decision-list">
				        <li><span class="empty-text"></span></li>
				      </ol>
				    </div>
				  </div>

				  <div class="section">
				    <div class="section-title">
				      <span class="number">4</span>
				      <span>액션 아이템</span>
				    </div>

				    <table class="action-table">
				      <tr>
				        <th class="assignee">담당자</th>
				        <th>내용</th>
				        <th class="due-date">기한</th>
				      </tr>
				      <tr>
				        <td class="assignee"><span class="empty-text">-</span></td>
				        <td><span class="empty-text"></span></td>
				        <td class="due-date"><span class="empty-text">-</span></td>
				      </tr>
				    </table>
				  </div>

				  <div class="section">
				    <div class="section-title">
				      <span class="number">5</span>
				      <span>특이사항 / 기타</span>
				    </div>
				    <div class="content-box">
				      <span class="empty-text"></span>
				    </div>
				  </div>

				</div>
				</body>
				</html>
				""";

		return html.replace("{{SIGN_HEADERS}}", signHeaders).replace("{{SIGN_BODIES}}", signBodies)
				.replace("{{MEETING_NAME}}", escapeHtml(detailVO.getMtngNm())).replace("{{BEGIN_DT}}", beginDt)
				.replace("{{END_DT}}", endDt).replace("{{LOCATION}}", escapeHtml(location))
				.replace("{{CREATOR_NAME}}", escapeHtml(detailVO.getCrtrNm()))
				.replace("{{PARTICIPANT_NAMES}}", ptcptNames);
	}

	private String formatDateTime(LocalDateTime dateTime) {
		return dateTime == null ? "미정" : dateTime.format(DateUtil.DATETIME_FORMAT);
	}

	private String defaultText(String value, String defaultValue) {
		return value == null || value.isBlank() ? defaultValue : value;
	}

	private String escapeHtml(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
}
