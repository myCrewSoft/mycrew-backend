package com.mycrewsoft.domain.approval.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycrewsoft.ai.chatbot.service.ApprovalAiPromptService;
import com.mycrewsoft.domain.approval.dto.request.ApprovalAiDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiApproverCandidateDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiDraftJobResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiDraftResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalTemplateResponse;
import com.mycrewsoft.domain.approval.mapper.ApprovalAiMapper;
import com.mycrewsoft.domain.approval.mapper.ApprovalDraftMapper;
import com.mycrewsoft.domain.department.vo.DepartmentVO;
import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.domain.jobgrade.vo.JobGradeVO;
import com.mycrewsoft.domain.jobposition.vo.JobPositionVO;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class ApprovalAiDraftServiceImplTest {

    @Mock
    private ApprovalDraftMapper approvalDraftMapper;

    @Mock
    private ApprovalAiMapper approvalAiMapper;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private ApprovalDraftWriteService approvalDraftWriteService;

    @Mock
    private ApprovalAiChatClient approvalAiChatClient;

    @Mock
    private ApprovalAiPromptService approvalAiPromptService;

    @Test
    void createDraftSplitsContentAndApprovalLinePromptsThenSavesTemporaryDraft() {
        ApprovalAiDraftServiceImpl service = newService(Runnable::run);
        Long drafterEmpId = 1234L;
        Long draftDocSn = 700L;
        ApprovalAiDraftRequestDTO request = request("Write a vacation draft for June 20 to June 21.");
        EmployeeProfileDTO profile = profile();
        List<ApprovalTemplateResponse> templates = List.of(template("TMPL_VACATION", "Vacation Request"));
        List<ApprovalAiApproverCandidateDTO> candidates = List.of(candidate(1111L, "Manager"));

        when(employeeMapper.selectEmployeeProfileByEmpId(drafterEmpId)).thenReturn(profile);
        when(approvalDraftMapper.selectAllUsableTemplates(drafterEmpId)).thenReturn(templates);
        when(approvalAiMapper.selectApproverCandidates(drafterEmpId, "DEPT_023")).thenReturn(candidates);
        when(approvalAiPromptService.buildDraftContentPrompt(
                any(ApprovalAiDraftRequestDTO.class), eq(drafterEmpId), eq(profile), eq(templates)))
                .thenReturn("content prompt");
        when(approvalAiPromptService.buildApprovalLinePrompt(
                any(ApprovalAiDraftRequestDTO.class),
                eq(drafterEmpId),
                eq(profile),
                eq("Vacation Request"),
                eq("TMPL_VACATION"),
                eq(candidates)))
                .thenReturn("approval line prompt");
        when(approvalAiChatClient.complete("content prompt")).thenReturn("""
                {
                  "docTitle": "Vacation Request",
                  "templateCode": "TMPL_VACATION",
                  "html": "<!DOCTYPE html><html><body><h1>Vacation Request</h1></body></html>",
                  "warnings": []
                }
                """);
        when(approvalAiChatClient.complete("approval line prompt")).thenReturn("""
                {
                  "approvalLines": [
                    {"aprvlOrd": 1, "aprvlMthdCd": "01", "aprvrEmpIds": [1111]}
                  ],
                  "warnings": []
                }
                """);
        when(approvalDraftWriteService.saveTemporaryDraft(any(ApprovalDraftRequestDTO.class)))
                .thenReturn(draftDocSn);

        ApprovalAiDraftResponseDTO result;
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(drafterEmpId);
            result = service.createDraft(request);
        }

        assertThat(result.getDrftDocSn()).isEqualTo(draftDocSn);
        assertThat(result.getDocTtl()).isEqualTo("Vacation Request");
        assertThat(result.getTmplatCd()).isEqualTo("TMPL_VACATION");
        verify(approvalAiChatClient, times(1)).complete("content prompt");
        verify(approvalAiChatClient, times(1)).complete("approval line prompt");

        ArgumentCaptor<ApprovalDraftRequestDTO> draftCaptor =
                ArgumentCaptor.forClass(ApprovalDraftRequestDTO.class);
        verify(approvalDraftWriteService).saveTemporaryDraft(draftCaptor.capture());
        ApprovalDraftRequestDTO savedDraft = draftCaptor.getValue();
        assertThat(savedDraft.getAprvlFullCn()).contains("{{SIGN:1}}");
        assertThat(savedDraft.getApprovalLines()).hasSize(1);
        assertThat(savedDraft.getApprovalLines().get(0).getAprvrEmpIds()).containsExactly(1111L);
    }

    @Test
    void createDraftJobRunsInExecutorAndCanBeQueriedByOwner() {
        ApprovalAiDraftServiceImpl service = newService(Runnable::run);
        Long drafterEmpId = 1234L;
        ApprovalAiDraftRequestDTO request = request("Write a purchase draft.");
        EmployeeProfileDTO profile = profile();
        List<ApprovalTemplateResponse> templates = List.of(template("TMPL_PURCHASE", "Purchase Request"));
        List<ApprovalAiApproverCandidateDTO> candidates = List.of(candidate(1111L, "Manager"));

        when(employeeMapper.selectEmployeeProfileByEmpId(drafterEmpId)).thenReturn(profile);
        when(approvalDraftMapper.selectAllUsableTemplates(drafterEmpId)).thenReturn(templates);
        when(approvalAiMapper.selectApproverCandidates(drafterEmpId, "DEPT_023")).thenReturn(candidates);
        when(approvalAiPromptService.buildDraftContentPrompt(
                any(ApprovalAiDraftRequestDTO.class), eq(drafterEmpId), eq(profile), eq(templates)))
                .thenReturn("content prompt");
        when(approvalAiPromptService.buildApprovalLinePrompt(
                any(ApprovalAiDraftRequestDTO.class),
                eq(drafterEmpId),
                eq(profile),
                eq("Purchase Request"),
                eq("TMPL_PURCHASE"),
                eq(candidates)))
                .thenReturn("approval line prompt");
        when(approvalAiChatClient.complete("content prompt")).thenReturn("""
                {
                  "docTitle": "Purchase Request",
                  "templateCode": "TMPL_PURCHASE",
                  "html": "<html><body><p>Purchase laptop</p></body></html>",
                  "warnings": ["content warning"]
                }
                """);
        when(approvalAiChatClient.complete("approval line prompt")).thenReturn("""
                {
                  "approvalLines": [
                    {"aprvlOrd": 1, "aprvlMthdCd": "01", "aprvrEmpIds": [1111]}
                  ],
                  "warnings": ["line warning"]
                }
                """);
        when(approvalDraftWriteService.saveTemporaryDraft(any(ApprovalDraftRequestDTO.class)))
                .thenReturn(701L);

        ApprovalAiDraftJobResponseDTO started;
        ApprovalAiDraftJobResponseDTO queried;
        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(drafterEmpId);
            started = service.createDraftJob(request);
            queried = service.getDraftJob(started.getJobId());
        }

        assertThat(started.getJobId()).isNotNull();
        assertThat(started.getStatus()).isEqualTo("SUCCEEDED");
        assertThat(queried.getStatus()).isEqualTo("SUCCEEDED");
        assertThat(queried.getDrftDocSn()).isEqualTo(701L);
        assertThat(queried.getDocTtl()).isEqualTo("Purchase Request");
        assertThat(queried.getWarnings()).containsExactly("content warning", "line warning");
    }

    private ApprovalAiDraftServiceImpl newService(TaskExecutor taskExecutor) {
        return new ApprovalAiDraftServiceImpl(
                approvalDraftMapper,
                approvalAiMapper,
                employeeMapper,
                approvalDraftWriteService,
                approvalAiChatClient,
                approvalAiPromptService,
                taskExecutor,
                new ObjectMapper());
    }

    private ApprovalAiDraftRequestDTO request(String prompt) {
        ApprovalAiDraftRequestDTO request = new ApprovalAiDraftRequestDTO();
        request.setUserPrompt(prompt);
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
