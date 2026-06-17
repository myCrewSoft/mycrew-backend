package com.mycrewsoft.ai.chatbot.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.mycrewsoft.domain.approval.dto.request.ApprovalAiDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiApproverCandidateDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalTemplateResponse;
import com.mycrewsoft.domain.department.vo.DepartmentVO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;
import com.mycrewsoft.domain.jobgrade.vo.JobGradeVO;
import com.mycrewsoft.domain.jobposition.vo.JobPositionVO;

class PromptApprovalServiceImplTest {

    private final PromptApprovalServiceImpl promptService = new PromptApprovalServiceImpl();

    @Test
    void buildDraftContentPromptContainsTemplateContextButNotApproverCandidates() {
        ApprovalAiDraftRequestDTO request = request("Write a vacation request.");
        String prompt = promptService.buildDraftContentPrompt(
                request,
                1234L,
                profile(),
                List.of(template("TMPL_VACATION", "Vacation Request")));

        assertThat(prompt).contains("docTitle", "templateCode", "html", "TMPL_VACATION");
        assertThat(prompt).doesNotContain("approvalLines", "APPROVER_CANDIDATES");
    }

    @Test
    void buildApprovalLinePromptContainsCandidateContextButNotTemplateHtml() {
        ApprovalAiDraftRequestDTO request = request("Write a purchase request.");
        String prompt = promptService.buildApprovalLinePrompt(
                request,
                1234L,
                profile(),
                "Purchase Request",
                "TMPL_PURCHASE",
                List.of(candidate(1111L, "Manager")));

        assertThat(prompt).contains("approvalLines", "APPROVER_CANDIDATES", "1111", "Manager");
        assertThat(prompt).doesNotContain("<html><body>");
    }

    private ApprovalAiDraftRequestDTO request(String userPrompt) {
        ApprovalAiDraftRequestDTO request = new ApprovalAiDraftRequestDTO();
        request.setUserPrompt(userPrompt);
        return request;
    }

    private EmployeeProfileDTO profile() {
        DepartmentVO department = new DepartmentVO();
        department.setDeptCd("DEPT_023");
        department.setDeptNm("Management");

        JobGradeVO jobGrade = new JobGradeVO();
        jobGrade.setJobGrdCd("JOB01");
        jobGrade.setJobGrdNm("Director");
        jobGrade.setSortOrder(10);

        JobPositionVO jobPosition = new JobPositionVO();
        jobPosition.setJobPstnCd("PSTN01");
        jobPosition.setJobPstnNm("Team Lead");

        EmployeeProfileDTO profile = new EmployeeProfileDTO();
        profile.setEmpNm("Drafter");
        profile.setDepartment(department);
        profile.setJobGrade(jobGrade);
        profile.setJobPosition(jobPosition);
        return profile;
    }

    private ApprovalTemplateResponse template(String tmplatCd, String tmplatNm) {
        ApprovalTemplateResponse template = new ApprovalTemplateResponse();
        template.setTmplatCd(tmplatCd);
        template.setTmplatNm(tmplatNm);
        template.setTmplatCn("<html><body><h1>" + tmplatNm + "</h1></body></html>");
        template.setUseYn("Y");
        template.setFavoriteYn("N");
        return template;
    }

    private ApprovalAiApproverCandidateDTO candidate(Long empId, String empNm) {
        ApprovalAiApproverCandidateDTO candidate = new ApprovalAiApproverCandidateDTO();
        candidate.setEmpId(empId);
        candidate.setEmpNm(empNm);
        candidate.setDeptCd("DEPT_023");
        candidate.setDeptNm("Management");
        candidate.setJobGrdCd("JOB02");
        candidate.setJobGrdNm("Manager");
        candidate.setJobGrdSortOrder(20);
        candidate.setJobPstnCd("PSTN02");
        candidate.setJobPstnNm("Approver");
        candidate.setJobDutyCn("Approval owner");
        candidate.setExecYn("N");
        candidate.setHasSignatureYn("Y");
        candidate.setDeptLeaderYn("Y");
        return candidate;
    }
}
