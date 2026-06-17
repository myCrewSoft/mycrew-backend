package com.mycrewsoft.domain.approval.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycrewsoft.ai.chatbot.service.ApprovalAiPromptService;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.approval.dto.request.ApprovalAiDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalStepRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiApproverCandidateDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiDraftJobResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiDraftResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalTemplateResponse;
import com.mycrewsoft.domain.approval.mapper.ApprovalAiMapper;
import com.mycrewsoft.domain.approval.mapper.ApprovalDraftMapper;
import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.security.util.SecurityUtil;

@Service
public class ApprovalAiDraftServiceImpl implements ApprovalAiDraftService {

    private static final String DEFAULT_APPROVAL_METHOD = "01";
    private static final Pattern SIGN_TOKEN_PATTERN =
            Pattern.compile("\\{\\{\\s*SIGN\\s*:\\s*(\\d+)\\s*\\}\\}");

    private final ApprovalDraftMapper approvalDraftMapper;
    private final ApprovalAiMapper approvalAiMapper;
    private final EmployeeMapper employeeMapper;
    private final ApprovalDraftWriteService approvalDraftWriteService;
    private final ApprovalAiChatClient approvalAiChatClient;
    private final ApprovalAiPromptService approvalAiPromptService;
    private final TaskExecutor approvalAiTaskExecutor;
    private final ObjectMapper objectMapper;
    private final AtomicLong jobIdGenerator = new AtomicLong(System.currentTimeMillis());
    private final ConcurrentMap<Long, ApprovalAiDraftJob> jobs = new ConcurrentHashMap<>();

    public ApprovalAiDraftServiceImpl(
            ApprovalDraftMapper approvalDraftMapper,
            ApprovalAiMapper approvalAiMapper,
            EmployeeMapper employeeMapper,
            ApprovalDraftWriteService approvalDraftWriteService,
            ApprovalAiChatClient approvalAiChatClient,
            ApprovalAiPromptService approvalAiPromptService,
            @Qualifier("approvalAiTaskExecutor") TaskExecutor approvalAiTaskExecutor,
            ObjectMapper objectMapper) {
        this.approvalDraftMapper = approvalDraftMapper;
        this.approvalAiMapper = approvalAiMapper;
        this.employeeMapper = employeeMapper;
        this.approvalDraftWriteService = approvalDraftWriteService;
        this.approvalAiChatClient = approvalAiChatClient;
        this.approvalAiPromptService = approvalAiPromptService;
        this.approvalAiTaskExecutor = approvalAiTaskExecutor;
        this.objectMapper = objectMapper;
    }

    @Override
    public ApprovalAiDraftResponseDTO createDraft(ApprovalAiDraftRequestDTO request) {
        DraftContext context = loadDraftContext(request);

        String contentPrompt = approvalAiPromptService.buildDraftContentPrompt(
                request,
                context.empId(),
                context.drafter(),
                context.templates());
        AiDraftContentPayload contentPayload =
                parseAiPayload(approvalAiChatClient.complete(contentPrompt), AiDraftContentPayload.class);

        List<String> warnings = new ArrayList<>();
        addWarnings(warnings, contentPayload.warnings);

        ApprovalTemplateResponse selectedTemplate =
                selectTemplate(request, contentPayload.templateCode, context.templates(), warnings);

        String docTitle = StringUtils.hasText(contentPayload.docTitle)
                ? contentPayload.docTitle.trim()
                : makeFallbackTitle(request, selectedTemplate);
        String selectedTemplateCode = selectedTemplate == null ? contentPayload.templateCode : selectedTemplate.getTmplatCd();

        String approvalLinePrompt = approvalAiPromptService.buildApprovalLinePrompt(
                request,
                context.empId(),
                context.drafter(),
                docTitle,
                selectedTemplateCode,
                context.candidates());
        AiApprovalLineResponsePayload approvalLinePayload =
                parseAiPayload(approvalAiChatClient.complete(approvalLinePrompt), AiApprovalLineResponsePayload.class);
        addWarnings(warnings, approvalLinePayload.warnings);

        List<ApprovalStepRequestDTO> approvalLines =
                normalizeApprovalLines(approvalLinePayload, context.candidates(), warnings);

        String html = StringUtils.hasText(contentPayload.html)
                ? contentPayload.html
                : selectedTemplate == null ? "" : selectedTemplate.getTmplatCn();
        html = ensureSignatureTokens(html, approvalLines, context.candidates());

        ApprovalDraftRequestDTO draftRequest = new ApprovalDraftRequestDTO();
        draftRequest.setDocTtl(docTitle);
        draftRequest.setTmplatCd(selectedTemplate == null ? null : selectedTemplate.getTmplatCd());
        draftRequest.setAprvlFullCn(html);
        draftRequest.setApprovalLines(approvalLines);

        Long drftDocSn = approvalDraftWriteService.saveTemporaryDraft(draftRequest);
        return new ApprovalAiDraftResponseDTO(
                drftDocSn,
                docTitle,
                draftRequest.getTmplatCd(),
                warnings);
    }

    @Override
    public ApprovalAiDraftJobResponseDTO createDraftJob(ApprovalAiDraftRequestDTO request) {
        validateRequest(request);

        Long empId = SecurityUtil.getCurrentEmpId();
        ApprovalAiDraftRequestDTO jobRequest = copyRequest(request);
        ApprovalAiDraftJob job = new ApprovalAiDraftJob(jobIdGenerator.incrementAndGet(), empId);
        jobs.put(job.jobId(), job);

        try {
            approvalAiTaskExecutor.execute(() -> runJob(job.jobId(), jobRequest));
        } catch (RuntimeException e) {
            job.fail("AI 기안서 생성 작업을 시작하지 못했습니다.");
        }
        return toJobResponse(job);
    }

    @Override
    public ApprovalAiDraftJobResponseDTO getDraftJob(Long jobId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        ApprovalAiDraftJob job = jobs.get(jobId);
        if (job == null || !Objects.equals(job.empId(), empId)) {
            throw new CustomException(ErrorCode.APPROVAL_AI_JOB_NOT_FOUND);
        }
        return toJobResponse(job);
    }

    private void runJob(Long jobId, ApprovalAiDraftRequestDTO request) {
        ApprovalAiDraftJob job = jobs.get(jobId);
        if (job == null) {
            return;
        }
        job.running();
        try {
            job.succeed(createDraft(request));
        } catch (Exception e) {
            job.fail("AI 기안서 초안 생성에 실패했습니다.");
        }
    }

    private DraftContext loadDraftContext(ApprovalAiDraftRequestDTO request) {
        validateRequest(request);

        Long empId = SecurityUtil.getCurrentEmpId();
        EmployeeProfileDTO drafter = employeeMapper.selectEmployeeProfileByEmpId(empId);
        if (drafter == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        List<ApprovalTemplateResponse> templates =
                reorderTemplates(approvalDraftMapper.selectAllUsableTemplates(empId), request.getTmplatCd());
        if (StringUtils.hasText(request.getTmplatCd()) && templates.stream()
                .noneMatch(template -> request.getTmplatCd().equals(template.getTmplatCd()))) {
            throw new CustomException(ErrorCode.TEMPLATE_NOT_FOUND);
        }

        String deptCd = drafter.getDepartment() == null ? null : drafter.getDepartment().getDeptCd();
        List<ApprovalAiApproverCandidateDTO> candidates =
                approvalAiMapper.selectApproverCandidates(empId, deptCd);
        if (candidates == null || candidates.isEmpty()) {
            throw new CustomException(ErrorCode.APPROVAL_LINE_INVALID);
        }

        return new DraftContext(empId, drafter, templates, candidates);
    }

    private void validateRequest(ApprovalAiDraftRequestDTO request) {
        if (request == null || !StringUtils.hasText(request.getUserPrompt())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private ApprovalAiDraftRequestDTO copyRequest(ApprovalAiDraftRequestDTO request) {
        ApprovalAiDraftRequestDTO copy = new ApprovalAiDraftRequestDTO();
        copy.setUserPrompt(request.getUserPrompt());
        copy.setTmplatCd(request.getTmplatCd());
        return copy;
    }

    private List<ApprovalTemplateResponse> reorderTemplates(
            List<ApprovalTemplateResponse> templates,
            String preferredTemplateCode) {
        if (templates == null || templates.isEmpty() || !StringUtils.hasText(preferredTemplateCode)) {
            return templates == null ? List.of() : templates;
        }
        return templates.stream()
                .sorted(Comparator.comparing(
                        template -> preferredTemplateCode.equals(template.getTmplatCd()) ? 0 : 1))
                .toList();
    }

    private <T> T parseAiPayload(String rawResponse, Class<T> payloadType) {
        if (!StringUtils.hasText(rawResponse)) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        String json = extractJson(rawResponse);
        try {
            return objectMapper.readValue(json, payloadType);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String extractJson(String rawResponse) {
        String text = rawResponse.trim();
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```[a-zA-Z]*\\s*", "");
            text = text.replaceFirst("\\s*```$", "");
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return text.substring(start, end + 1);
    }

    private ApprovalTemplateResponse selectTemplate(
            ApprovalAiDraftRequestDTO request,
            String aiTemplateCode,
            List<ApprovalTemplateResponse> templates,
            List<String> warnings) {
        if (templates == null || templates.isEmpty()) {
            return null;
        }
        Map<String, ApprovalTemplateResponse> byCode = templates.stream()
                .filter(template -> StringUtils.hasText(template.getTmplatCd()))
                .collect(Collectors.toMap(
                        ApprovalTemplateResponse::getTmplatCd,
                        template -> template,
                        (left, right) -> left,
                        LinkedHashMap::new));

        if (StringUtils.hasText(aiTemplateCode) && byCode.containsKey(aiTemplateCode)) {
            return byCode.get(aiTemplateCode);
        }
        if (StringUtils.hasText(request.getTmplatCd()) && byCode.containsKey(request.getTmplatCd())) {
            if (StringUtils.hasText(aiTemplateCode)) {
                warnings.add("AI가 선택한 결재 양식이 유효하지 않아 요청한 양식으로 대체했습니다.");
            }
            return byCode.get(request.getTmplatCd());
        }
        if (StringUtils.hasText(aiTemplateCode)) {
            warnings.add("AI가 선택한 결재 양식이 유효하지 않아 사용 가능한 첫 번째 양식으로 대체했습니다.");
        }
        return templates.get(0);
    }

    private List<ApprovalStepRequestDTO> normalizeApprovalLines(
            AiApprovalLineResponsePayload linePayload,
            List<ApprovalAiApproverCandidateDTO> candidates,
            List<String> warnings) {
        Set<Long> validCandidateIds = candidates.stream()
                .map(ApprovalAiApproverCandidateDTO::getEmpId)
                .collect(Collectors.toSet());
        List<Long> selectedApproverIds = new ArrayList<>();
        boolean invalidSelected = false;

        if (linePayload.approvalLines != null) {
            List<AiApprovalLinePayload> lines = new ArrayList<>(linePayload.approvalLines);
            lines.sort(Comparator.comparing(line -> line.aprvlOrd == null ? Long.MAX_VALUE : line.aprvlOrd));
            Set<Long> seen = new HashSet<>();
            for (AiApprovalLinePayload line : lines) {
                if (line.aprvrEmpIds == null) {
                    continue;
                }
                for (Long approverId : line.aprvrEmpIds) {
                    if (approverId == null || !validCandidateIds.contains(approverId)) {
                        invalidSelected = true;
                        continue;
                    }
                    if (seen.add(approverId)) {
                        selectedApproverIds.add(approverId);
                    }
                }
            }
        }

        if (selectedApproverIds.isEmpty()) {
            selectedApproverIds.add(candidates.get(0).getEmpId());
            warnings.add("AI가 선택한 결재자가 유효하지 않아 후보 목록의 첫 번째 결재자로 대체했습니다.");
        } else if (invalidSelected) {
            warnings.add("AI가 선택한 결재자 중 유효하지 않은 후보는 제외했습니다.");
        }

        List<ApprovalStepRequestDTO> approvalLines = new ArrayList<>();
        for (int i = 0; i < selectedApproverIds.size(); i++) {
            ApprovalStepRequestDTO step = new ApprovalStepRequestDTO();
            step.setAprvlOrd((long) i + 1);
            step.setAprvlMthdCd(DEFAULT_APPROVAL_METHOD);
            step.setAprvrEmpIds(List.of(selectedApproverIds.get(i)));
            approvalLines.add(step);
        }
        return approvalLines;
    }

    private String ensureSignatureTokens(
            String html,
            List<ApprovalStepRequestDTO> approvalLines,
            List<ApprovalAiApproverCandidateDTO> candidates) {
        String normalizedHtml = StringUtils.hasText(html)
                ? html
                : "<!DOCTYPE html><html lang=\"ko\"><body></body></html>";
        Set<Long> existingOrders = new HashSet<>();
        java.util.regex.Matcher matcher = SIGN_TOKEN_PATTERN.matcher(normalizedHtml);
        while (matcher.find()) {
            existingOrders.add(Long.valueOf(matcher.group(1)));
        }

        List<ApprovalStepRequestDTO> missing = approvalLines.stream()
                .filter(step -> !existingOrders.contains(step.getAprvlOrd()))
                .toList();
        if (missing.isEmpty()) {
            return normalizedHtml;
        }

        String signBlock = buildSignatureBlock(missing, candidates);
        int bodyEnd = normalizedHtml.toLowerCase().lastIndexOf("</body>");
        if (bodyEnd >= 0) {
            return normalizedHtml.substring(0, bodyEnd)
                    + signBlock
                    + normalizedHtml.substring(bodyEnd);
        }
        return normalizedHtml + signBlock;
    }

    private String buildSignatureBlock(
            List<ApprovalStepRequestDTO> approvalLines,
            List<ApprovalAiApproverCandidateDTO> candidates) {
        Map<Long, ApprovalAiApproverCandidateDTO> candidateById = candidates.stream()
                .collect(Collectors.toMap(
                        ApprovalAiApproverCandidateDTO::getEmpId,
                        candidate -> candidate,
                        (left, right) -> left));
        StringBuilder header = new StringBuilder();
        StringBuilder body = new StringBuilder();
        for (ApprovalStepRequestDTO step : approvalLines) {
            Long approverId = step.getAprvrEmpIds().get(0);
            ApprovalAiApproverCandidateDTO candidate = candidateById.get(approverId);
            header.append("<th style=\"border:1px solid #334155;padding:6px 12px;background:#f1f5f9;\">")
                    .append(candidate == null ? "결재" : escapeHtml(candidate.getEmpNm()))
                    .append("</th>");
            body.append("<td style=\"border:1px solid #334155;width:96px;height:64px;text-align:center;vertical-align:middle;\">")
                    .append("{{SIGN:")
                    .append(step.getAprvlOrd())
                    .append("}}")
                    .append("</td>");
        }
        return """
                <table class="approval-ai-sign-table" style="border-collapse:collapse;float:right;margin:0 0 16px 16px;font-size:12px;text-align:center;">
                  <tr>%s</tr>
                  <tr>%s</tr>
                </table>
                <div style="clear:both;"></div>
                """.formatted(header, body);
    }

    private String makeFallbackTitle(ApprovalAiDraftRequestDTO request, ApprovalTemplateResponse selectedTemplate) {
        if (selectedTemplate != null && StringUtils.hasText(selectedTemplate.getTmplatNm())) {
            return selectedTemplate.getTmplatNm();
        }
        String prompt = request.getUserPrompt().trim();
        return limit(prompt, 40);
    }

    private void addWarnings(List<String> warnings, List<String> nextWarnings) {
        if (nextWarnings == null) {
            return;
        }
        warnings.addAll(nextWarnings.stream()
                .filter(StringUtils::hasText)
                .toList());
    }

    private ApprovalAiDraftJobResponseDTO toJobResponse(ApprovalAiDraftJob job) {
        ApprovalAiDraftResponseDTO result = job.result();
        return new ApprovalAiDraftJobResponseDTO(
                job.jobId(),
                job.status().name(),
                result == null ? null : result.getDrftDocSn(),
                result == null ? null : result.getDocTtl(),
                result == null ? null : result.getTmplatCd(),
                result == null ? null : result.getWarnings(),
                job.errorMessage(),
                job.createdAt(),
                job.updatedAt());
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value == null ? "" : value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private record DraftContext(
            Long empId,
            EmployeeProfileDTO drafter,
            List<ApprovalTemplateResponse> templates,
            List<ApprovalAiApproverCandidateDTO> candidates) {
    }

    private enum ApprovalAiDraftJobStatus {
        PENDING,
        RUNNING,
        SUCCEEDED,
        FAILED
    }

    private static class ApprovalAiDraftJob {
        private final Long jobId;
        private final Long empId;
        private final LocalDateTime createdAt;
        private volatile LocalDateTime updatedAt;
        private volatile ApprovalAiDraftJobStatus status;
        private volatile ApprovalAiDraftResponseDTO result;
        private volatile String errorMessage;

        private ApprovalAiDraftJob(Long jobId, Long empId) {
            this.jobId = jobId;
            this.empId = empId;
            this.createdAt = LocalDateTime.now();
            this.updatedAt = this.createdAt;
            this.status = ApprovalAiDraftJobStatus.PENDING;
        }

        private Long jobId() {
            return jobId;
        }

        private Long empId() {
            return empId;
        }

        private LocalDateTime createdAt() {
            return createdAt;
        }

        private LocalDateTime updatedAt() {
            return updatedAt;
        }

        private ApprovalAiDraftJobStatus status() {
            return status;
        }

        private ApprovalAiDraftResponseDTO result() {
            return result;
        }

        private String errorMessage() {
            return errorMessage;
        }

        private void running() {
            this.status = ApprovalAiDraftJobStatus.RUNNING;
            this.updatedAt = LocalDateTime.now();
        }

        private void succeed(ApprovalAiDraftResponseDTO result) {
            this.result = result;
            this.errorMessage = null;
            this.status = ApprovalAiDraftJobStatus.SUCCEEDED;
            this.updatedAt = LocalDateTime.now();
        }

        private void fail(String errorMessage) {
            this.errorMessage = errorMessage;
            this.status = ApprovalAiDraftJobStatus.FAILED;
            this.updatedAt = LocalDateTime.now();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AiDraftContentPayload {
        public String docTitle;
        public String templateCode;
        public String html;
        public List<String> warnings;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AiApprovalLineResponsePayload {
        public List<AiApprovalLinePayload> approvalLines;
        public List<String> warnings;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class AiApprovalLinePayload {
        public Long aprvlOrd;
        public String aprvlMthdCd;
        public List<Long> aprvrEmpIds;
    }
}
