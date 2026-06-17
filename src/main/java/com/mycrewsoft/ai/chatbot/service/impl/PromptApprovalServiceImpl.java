package com.mycrewsoft.ai.chatbot.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mycrewsoft.ai.chatbot.service.ApprovalAiPromptService;
import com.mycrewsoft.ai.chatbot.service.PromptService;
import com.mycrewsoft.domain.approval.dto.request.ApprovalAiDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiApproverCandidateDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalTemplateResponse;
import com.mycrewsoft.domain.department.vo.DepartmentVO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;
import com.mycrewsoft.domain.jobgrade.vo.JobGradeVO;
import com.mycrewsoft.domain.jobposition.vo.JobPositionVO;

@Service("promptApprovalService")
public class PromptApprovalServiceImpl implements PromptService, ApprovalAiPromptService {

	private static final int MAX_TEMPLATE_COUNT = 3;
	private static final int MAX_TEMPLATE_LENGTH = 3000;
	private static final int MAX_CANDIDATE_COUNT = 15;

	@Override
	public String buildDraftContentPrompt(
			ApprovalAiDraftRequestDTO request,
			Long drafterEmpId,
			EmployeeProfileDTO drafter,
			List<ApprovalTemplateResponse> templates) {
		return """
				[ROLE]
				당신은 사내 전자결재 기안서 본문 작성 AI입니다.

				[STRICT RULES]
				- 반드시 [TEMPLATES]에 있는 양식만 활용하십시오.
				- templateCode는 [TEMPLATES]의 tmplatCd 중 하나이거나 null이어야 합니다.
				- 결재선은 작성하지 마십시오. 결재선 필드를 출력하지 마십시오.
				- HTML에는 결재 서명 영역이 이미 있으면 유지하고, {{SIGN:1}} 같은 서명 토큰은 삭제하지 마십시오.
				- 출력은 JSON 하나만 허용합니다. 마크다운 코드블록과 설명 문장을 쓰지 마십시오.

				[OUTPUT JSON SCHEMA]
				{
				  "docTitle": "기안서 제목",
				  "templateCode": "선택한 tmplatCd 또는 null",
				  "html": "기안 본문 전체 HTML",
				  "warnings": []
				}

				[USER_REQUEST]
				%s

				[DRAFTER]
				%s

				[TEMPLATES]
				%s
				""".formatted(
				value(request.getUserPrompt()).trim(),
				formatDrafter(drafterEmpId, drafter),
				formatTemplates(templates));
	}

	@Override
	public String buildApprovalLinePrompt(
			ApprovalAiDraftRequestDTO request,
			Long drafterEmpId,
			EmployeeProfileDTO drafter,
			String docTitle,
			String templateCode,
			List<ApprovalAiApproverCandidateDTO> candidates) {
		return """
				[ROLE]
				당신은 사내 전자결재 결재선을 추천하는 AI입니다.

				[STRICT RULES]
				- approvalLines.aprvrEmpIds에는 [APPROVER_CANDIDATES]에 있는 empId만 넣으십시오.
				- 기안자 본인 empId=%d는 결재자로 넣지 마십시오.
				- 한 결재 단계에는 결재자 1명만 넣으십시오.
				- 결재 단계는 업무 중요도에 맞게 1~4단계로 구성하십시오.
				- 같은 부서, 부서장, 직무 관련성, 직급, 전자서명 보유 여부를 우선 고려하십시오.
				- 출력은 JSON 하나만 허용합니다. 마크다운 코드블록과 설명 문장을 쓰지 마십시오.

				[OUTPUT JSON SCHEMA]
				{
				  "approvalLines": [
				    {"aprvlOrd": 1, "aprvlMthdCd": "01", "aprvrEmpIds": [1111]}
				  ],
				  "warnings": []
				}

				[USER_REQUEST]
				%s

				[DRAFT_SUMMARY]
				docTitle=%s, templateCode=%s

				[DRAFTER]
				%s

				[APPROVER_CANDIDATES]
				%s
				""".formatted(
				drafterEmpId,
				value(request.getUserPrompt()).trim(),
				value(docTitle),
				value(templateCode),
				formatDrafter(drafterEmpId, drafter),
				formatCandidates(candidates));
	}

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

	private String formatDrafter(Long empId, EmployeeProfileDTO drafter) {
		if (drafter == null) {
			return "empId=%d".formatted(empId);
		}
		DepartmentVO department = drafter.getDepartment();
		JobGradeVO jobGrade = drafter.getJobGrade();
		JobPositionVO jobPosition = drafter.getJobPosition();
		return """
				empId=%d, empNm=%s, deptCd=%s, deptNm=%s, jobGrdCd=%s, jobGrdNm=%s, jobPstnCd=%s, jobPstnNm=%s
				""".formatted(
				empId,
				value(drafter.getEmpNm()),
				department == null ? "" : value(department.getDeptCd()),
				department == null ? "" : value(department.getDeptNm()),
				jobGrade == null ? "" : value(jobGrade.getJobGrdCd()),
				jobGrade == null ? "" : value(jobGrade.getJobGrdNm()),
				jobPosition == null ? "" : value(jobPosition.getJobPstnCd()),
				jobPosition == null ? "" : value(jobPosition.getJobPstnNm()));
	}

	private String formatTemplates(List<ApprovalTemplateResponse> templates) {
		if (templates == null || templates.isEmpty()) {
			return "사용 가능한 결재 양식 없음";
		}
		return templates.stream()
				.limit(MAX_TEMPLATE_COUNT)
				.map(template -> """
						- tmplatCd=%s, tmplatNm=%s, favoriteYn=%s, html=%s
						""".formatted(
						value(template.getTmplatCd()),
						value(template.getTmplatNm()),
						value(template.getFavoriteYn()),
						limit(value(template.getTmplatCn()), MAX_TEMPLATE_LENGTH)))
				.collect(Collectors.joining("\n"));
	}

	private String formatCandidates(List<ApprovalAiApproverCandidateDTO> candidates) {
		if (candidates == null || candidates.isEmpty()) {
			return "사용 가능한 결재자 후보 없음";
		}
		return candidates.stream()
				.limit(MAX_CANDIDATE_COUNT)
				.map(candidate -> """
						- empId=%d, empNm=%s, deptCd=%s, deptNm=%s, parentDeptCd=%s, jobGrdCd=%s, jobGrdNm=%s, jobGrdSortOrder=%s, jobPstnCd=%s, jobPstnNm=%s, jobDutyCn=%s, execYn=%s, hasSignatureYn=%s, deptLeaderYn=%s
						""".formatted(
						candidate.getEmpId(),
						value(candidate.getEmpNm()),
						value(candidate.getDeptCd()),
						value(candidate.getDeptNm()),
						value(candidate.getParentDeptCd()),
						value(candidate.getJobGrdCd()),
						value(candidate.getJobGrdNm()),
						candidate.getJobGrdSortOrder() == null ? "" : candidate.getJobGrdSortOrder(),
						value(candidate.getJobPstnCd()),
						value(candidate.getJobPstnNm()),
						value(candidate.getJobDutyCn()),
						value(candidate.getExecYn()),
						value(candidate.getHasSignatureYn()),
						value(candidate.getDeptLeaderYn())))
				.collect(Collectors.joining("\n"));
	}

	private String value(String value) {
		return value == null ? "" : value;
	}

	private String limit(String value, int maxLength) {
		if (value == null || value.length() <= maxLength) {
			return value == null ? "" : value;
		}
		return value.substring(0, maxLength) + "...";
	}

}
