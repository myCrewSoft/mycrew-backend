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
import com.mycrewsoft.domain.approval.dto.request.ApprovalAiApprovalLineRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalAiDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalStepRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiApprovalLineJobResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiApprovalLineResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiContentJobResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiContentResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiApproverCandidateDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiDraftJobResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAiDraftResponseDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalTemplateResponse;
import com.mycrewsoft.domain.approval.mapper.ApprovalAiMapper;
import com.mycrewsoft.domain.approval.mapper.ApprovalDraftMapper;
import com.mycrewsoft.domain.employee.dto.response.EmployeeProfileDTO;
import com.mycrewsoft.domain.employee.mapper.EmployeeMapper;
import com.mycrewsoft.domain.notification.service.NotificationService;
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
    private final NotificationService notificationService;
    private final TaskExecutor approvalAiTaskExecutor;
    private final ObjectMapper objectMapper;
    private final AtomicLong jobIdGenerator = new AtomicLong(System.currentTimeMillis());
    private final ConcurrentMap<Long, AiJob<ApprovalAiDraftResponseDTO>> jobs = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, AiJob<ApprovalAiDraftResponseDTO>> contentSaveJobs = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, AiJob<ApprovalAiContentResponseDTO>> contentJobs = new ConcurrentHashMap<>();
    private final ConcurrentMap<Long, AiJob<ApprovalAiApprovalLineResponseDTO>> approvalLineJobs = new ConcurrentHashMap<>();

    public ApprovalAiDraftServiceImpl(
            ApprovalDraftMapper approvalDraftMapper,
            ApprovalAiMapper approvalAiMapper,
            EmployeeMapper employeeMapper,
            ApprovalDraftWriteService approvalDraftWriteService,
            ApprovalAiChatClient approvalAiChatClient,
            ApprovalAiPromptService approvalAiPromptService,
            NotificationService notificationService,
            @Qualifier("approvalAiTaskExecutor") TaskExecutor approvalAiTaskExecutor,
            ObjectMapper objectMapper) {
        this.approvalDraftMapper = approvalDraftMapper;
        this.approvalAiMapper = approvalAiMapper;
        this.employeeMapper = employeeMapper;
        this.approvalDraftWriteService = approvalDraftWriteService;
        this.approvalAiChatClient = approvalAiChatClient;
        this.approvalAiPromptService = approvalAiPromptService;
        this.notificationService = notificationService;
        this.approvalAiTaskExecutor = approvalAiTaskExecutor;
        this.objectMapper = objectMapper;
    }

    @Override
    public ApprovalAiDraftResponseDTO createDraft(ApprovalAiDraftRequestDTO request) {
        DraftContext context = loadDraftContext(request);
        List<String> warnings = new ArrayList<>();
        ApprovalAiContentResponseDTO content =
                generateDraftContent(request, context.empId(), context.drafter(), context.templates());
        addWarnings(warnings, content.getWarnings());

        ApprovalAiApprovalLineRequestDTO lineRequest = new ApprovalAiApprovalLineRequestDTO();
        lineRequest.setUserPrompt(request.getUserPrompt());
        lineRequest.setDocTtl(content.getDocTtl());
        lineRequest.setTmplatCd(content.getTmplatCd());
        lineRequest.setAprvlFullCn(content.getAprvlFullCn());
        ApprovalAiApprovalLineResponseDTO line = generateApprovalLine(lineRequest, context);
        addWarnings(warnings, line.getWarnings());

        String html = ensureSignatureTokens(
                content.getAprvlFullCn(),
                line.getApprovalLines(),
                line.getApprovers());

        ApprovalDraftRequestDTO draftRequest = new ApprovalDraftRequestDTO();
        draftRequest.setDocTtl(content.getDocTtl());
        draftRequest.setTmplatCd(content.getTmplatCd());
        draftRequest.setAprvlFullCn(html);
        draftRequest.setApprovalLines(line.getApprovalLines());

        Long drftDocSn = approvalDraftWriteService.saveTemporaryDraft(draftRequest);
        return new ApprovalAiDraftResponseDTO(
                drftDocSn,
                content.getDocTtl(),
                draftRequest.getTmplatCd(),
                warnings);
    }

    @Override
    public ApprovalAiDraftJobResponseDTO createDraftJob(ApprovalAiDraftRequestDTO request) {
        validateRequest(request);

        Long empId = SecurityUtil.getCurrentEmpId();
        ApprovalAiDraftRequestDTO jobRequest = copyRequest(request);
        AiJob<ApprovalAiDraftResponseDTO> job = new AiJob<>(jobIdGenerator.incrementAndGet(), empId);
        jobs.put(job.jobId(), job);

        try {
            approvalAiTaskExecutor.execute(() -> runDraftJob(job.jobId(), jobRequest));
        } catch (RuntimeException e) {
            job.fail("AI 기안서 생성 작업을 시작하지 못했습니다.");
        }
        return toJobResponse(job);
    }

    @Override
    public ApprovalAiDraftJobResponseDTO getDraftJob(Long jobId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        AiJob<ApprovalAiDraftResponseDTO> job = jobs.get(jobId);
        if (job == null || !Objects.equals(job.empId(), empId)) {
            throw new CustomException(ErrorCode.APPROVAL_AI_JOB_NOT_FOUND);
        }
        return toJobResponse(job);
    }

    @Override
    public ApprovalAiContentJobResponseDTO createDraftContentJob(ApprovalAiDraftRequestDTO request) {
        validateRequest(request);

        Long empId = SecurityUtil.getCurrentEmpId();
        ApprovalAiDraftRequestDTO jobRequest = copyRequest(request);
        AiJob<ApprovalAiContentResponseDTO> job = new AiJob<>(jobIdGenerator.incrementAndGet(), empId);
        contentJobs.put(job.jobId(), job);

        try {
            approvalAiTaskExecutor.execute(() -> runDraftContentJob(job.jobId(), jobRequest));
        } catch (RuntimeException e) {
            job.fail("AI 기안서 본문 생성 작업을 시작하지 못했습니다.");
        }
        return toContentJobResponse(job);
    }

    @Override
    public ApprovalAiContentJobResponseDTO getDraftContentJob(Long jobId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        AiJob<ApprovalAiContentResponseDTO> job = contentJobs.get(jobId);
        if (job == null || !Objects.equals(job.empId(), empId)) {
            throw new CustomException(ErrorCode.APPROVAL_AI_JOB_NOT_FOUND);
        }
        return toContentJobResponse(job);
    }

    @Override
    public ApprovalAiDraftJobResponseDTO createDraftContentSaveJob(ApprovalAiDraftRequestDTO request) {
        validateRequest(request);

        Long empId = SecurityUtil.getCurrentEmpId();
        ApprovalAiDraftRequestDTO jobRequest = copyRequest(request);
        AiJob<ApprovalAiDraftResponseDTO> job = new AiJob<>(jobIdGenerator.incrementAndGet(), empId);
        contentSaveJobs.put(job.jobId(), job);

        try {
            approvalAiTaskExecutor.execute(() -> runDraftContentSaveJob(job.jobId(), jobRequest));
        } catch (RuntimeException e) {
            job.fail("AI 기안서 양식 임시저장 작업을 시작하지 못했습니다.");
            notifyAiDraftContentSaveFailure(empId);
        }
        return toJobResponse(job);
    }

    @Override
    public ApprovalAiDraftJobResponseDTO getDraftContentSaveJob(Long jobId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        AiJob<ApprovalAiDraftResponseDTO> job = contentSaveJobs.get(jobId);
        if (job == null || !Objects.equals(job.empId(), empId)) {
            throw new CustomException(ErrorCode.APPROVAL_AI_JOB_NOT_FOUND);
        }
        return toJobResponse(job);
    }

    @Override
    public ApprovalAiApprovalLineJobResponseDTO createApprovalLineJob(ApprovalAiApprovalLineRequestDTO request) {
        validateApprovalLineRequest(request);

        Long empId = SecurityUtil.getCurrentEmpId();
        ApprovalAiApprovalLineRequestDTO jobRequest = copyApprovalLineRequest(request);
        AiJob<ApprovalAiApprovalLineResponseDTO> job = new AiJob<>(jobIdGenerator.incrementAndGet(), empId);
        approvalLineJobs.put(job.jobId(), job);

        try {
            approvalAiTaskExecutor.execute(() -> runApprovalLineJob(job.jobId(), jobRequest));
        } catch (RuntimeException e) {
            job.fail("AI 결재선 자동 지정 작업을 시작하지 못했습니다.");
        }
        return toApprovalLineJobResponse(job);
    }

    @Override
    public ApprovalAiApprovalLineJobResponseDTO getApprovalLineJob(Long jobId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        AiJob<ApprovalAiApprovalLineResponseDTO> job = approvalLineJobs.get(jobId);
        if (job == null || !Objects.equals(job.empId(), empId)) {
            throw new CustomException(ErrorCode.APPROVAL_AI_JOB_NOT_FOUND);
        }
        return toApprovalLineJobResponse(job);
    }

    private void runDraftJob(Long jobId, ApprovalAiDraftRequestDTO request) {
        AiJob<ApprovalAiDraftResponseDTO> job = jobs.get(jobId);
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

    private void runDraftContentJob(Long jobId, ApprovalAiDraftRequestDTO request) {
        AiJob<ApprovalAiContentResponseDTO> job = contentJobs.get(jobId);
        if (job == null) {
            return;
        }
        job.running();
        try {
            DraftContentContext context = loadDraftContentContext(request);
            job.succeed(generateDraftContent(request, context.empId(), context.drafter(), context.templates()));
        } catch (Exception e) {
            job.fail("AI 기안서 본문 생성에 실패했습니다.");
        }
    }

    private void runDraftContentSaveJob(Long jobId, ApprovalAiDraftRequestDTO request) {
        AiJob<ApprovalAiDraftResponseDTO> job = contentSaveJobs.get(jobId);
        if (job == null) {
            return;
        }
        job.running();
        try {
            DraftContentContext context = loadDraftContentContext(request);
            ApprovalAiContentResponseDTO content =
                    generateDraftContent(request, context.empId(), context.drafter(), context.templates());

            ApprovalDraftRequestDTO draftRequest = new ApprovalDraftRequestDTO();
            draftRequest.setDocTtl(content.getDocTtl());
            draftRequest.setTmplatCd(content.getTmplatCd());
            draftRequest.setAprvlFullCn(content.getAprvlFullCn());

            Long drftDocSn = approvalDraftWriteService.saveTemporaryDraft(draftRequest);
            ApprovalAiDraftResponseDTO result = new ApprovalAiDraftResponseDTO(
                    drftDocSn,
                    content.getDocTtl(),
                    content.getTmplatCd(),
                    content.getWarnings());
            job.succeed(result);
            notifyAiDraftContentSaved(job.empId(), result);
        } catch (Exception e) {
            job.fail("AI 기안서 양식 임시저장에 실패했습니다.");
            notifyAiDraftContentSaveFailure(job.empId());
        }
    }

    private void runApprovalLineJob(Long jobId, ApprovalAiApprovalLineRequestDTO request) {
        AiJob<ApprovalAiApprovalLineResponseDTO> job = approvalLineJobs.get(jobId);
        if (job == null) {
            return;
        }
        job.running();
        try {
            DraftContext context = loadDraftContext(toPromptRequest(request));
            job.succeed(generateApprovalLine(request, context));
        } catch (Exception e) {
            job.fail("AI 결재선 자동 지정에 실패했습니다.");
        }
    }

    private void notifyAiDraftContentSaved(Long empId, ApprovalAiDraftResponseDTO result) {
        if (empId == null || result == null) {
            return;
        }
        try {
            String title = StringUtils.hasText(result.getDocTtl())
                    ? result.getDocTtl()
                    : "AI 기안서";
            notificationService.sendAlrm(
                    "AI 기안서 양식 생성 완료",
                    "02",
                    title + " 문서가 임시저장되었습니다. 임시저장함에서 확인하세요.",
                    List.of(empId));
        } catch (Exception ignored) {
            // AI 작업 완료 자체가 알림 저장 실패로 실패 처리되지 않도록 한다.
        }
    }

    private void notifyAiDraftContentSaveFailure(Long empId) {
        if (empId == null) {
            return;
        }
        try {
            notificationService.sendAlrm(
                    "AI 기안서 양식 생성 실패",
                    "02",
                    "AI 기안서 양식 생성에 실패했습니다. 다시 시도해 주세요.",
                    List.of(empId));
        } catch (Exception ignored) {
            // 실패 알림 전송 실패는 원래 실패 원인을 가리지 않는다.
        }
    }

    private DraftContext loadDraftContext(ApprovalAiDraftRequestDTO request) {
        DraftContentContext base = loadDraftContentContext(request);
        String deptCd = base.drafter().getDepartment() == null ? null : base.drafter().getDepartment().getDeptCd();
        List<ApprovalAiApproverCandidateDTO> candidates =
                approvalAiMapper.selectApproverCandidates(base.empId(), deptCd);
        if (candidates == null || candidates.isEmpty()) {
            throw new CustomException(ErrorCode.APPROVAL_LINE_INVALID);
        }

        return new DraftContext(base.empId(), base.drafter(), base.templates(), candidates);
    }

    private DraftContentContext loadDraftContentContext(ApprovalAiDraftRequestDTO request) {
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

        return new DraftContentContext(empId, drafter, templates);
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

    private ApprovalAiApprovalLineRequestDTO copyApprovalLineRequest(ApprovalAiApprovalLineRequestDTO request) {
        ApprovalAiApprovalLineRequestDTO copy = new ApprovalAiApprovalLineRequestDTO();
        copy.setUserPrompt(request.getUserPrompt());
        copy.setDocTtl(request.getDocTtl());
        copy.setTmplatCd(request.getTmplatCd());
        copy.setAprvlFullCn(request.getAprvlFullCn());
        return copy;
    }

    private void validateApprovalLineRequest(ApprovalAiApprovalLineRequestDTO request) {
        if (request == null
                || (!StringUtils.hasText(request.getUserPrompt())
                && !StringUtils.hasText(request.getDocTtl())
                && !StringUtils.hasText(request.getTmplatCd())
                && !StringUtils.hasText(request.getAprvlFullCn()))) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private ApprovalAiDraftRequestDTO toPromptRequest(ApprovalAiApprovalLineRequestDTO request) {
        validateApprovalLineRequest(request);
        ApprovalAiDraftRequestDTO promptRequest = new ApprovalAiDraftRequestDTO();
        promptRequest.setUserPrompt(StringUtils.hasText(request.getUserPrompt())
                ? request.getUserPrompt().trim()
                : buildApprovalLineUserPrompt(request));
        promptRequest.setTmplatCd(request.getTmplatCd());
        return promptRequest;
    }

    private String buildApprovalLineUserPrompt(ApprovalAiApprovalLineRequestDTO request) {
        List<String> parts = new ArrayList<>();
        if (StringUtils.hasText(request.getDocTtl())) {
            parts.add("문서 제목: " + request.getDocTtl().trim());
        }
        if (StringUtils.hasText(request.getTmplatCd())) {
            parts.add("양식 코드: " + request.getTmplatCd().trim());
        }
        if (StringUtils.hasText(request.getAprvlFullCn())) {
            parts.add("본문 내용: " + limit(stripHtml(request.getAprvlFullCn()), 1000));
        }
        String prompt = String.join("\n", parts).trim();
        if (!StringUtils.hasText(prompt)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        return prompt;
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

    private ApprovalAiContentResponseDTO generateDraftContent(
            ApprovalAiDraftRequestDTO request,
            Long empId,
            EmployeeProfileDTO drafter,
            List<ApprovalTemplateResponse> templates) {
        String contentPrompt = approvalAiPromptService.buildDraftContentPrompt(
                request,
                empId,
                drafter,
                templates);
        AiDraftContentPayload contentPayload =
                parseAiPayload(approvalAiChatClient.complete(contentPrompt), AiDraftContentPayload.class);

        List<String> warnings = new ArrayList<>();
        addWarnings(warnings, contentPayload.warnings);

        ApprovalTemplateResponse selectedTemplate =
                selectTemplate(request, contentPayload.templateCode, templates, warnings);

        String docTitle = StringUtils.hasText(contentPayload.docTitle)
                ? contentPayload.docTitle.trim()
                : makeFallbackTitle(request, selectedTemplate);
        String selectedTemplateCode = selectedTemplate == null ? null : selectedTemplate.getTmplatCd();
        String html = StringUtils.hasText(contentPayload.html)
                ? contentPayload.html
                : selectedTemplate == null ? "" : selectedTemplate.getTmplatCn();
        html = ensureMinimumSignatureToken(html);

        return new ApprovalAiContentResponseDTO(
                docTitle,
                selectedTemplateCode,
                html,
                warnings);
    }

    private ApprovalAiApprovalLineResponseDTO generateApprovalLine(
            ApprovalAiApprovalLineRequestDTO request,
            DraftContext context) {
        ApprovalAiDraftRequestDTO promptRequest = toPromptRequest(request);
        String docTitle = StringUtils.hasText(request.getDocTtl())
                ? request.getDocTtl().trim()
                : makeFallbackTitle(promptRequest, null);
        String templateCode = StringUtils.hasText(request.getTmplatCd())
                ? request.getTmplatCd().trim()
                : null;

        String approvalLinePrompt = approvalAiPromptService.buildApprovalLinePrompt(
                promptRequest,
                context.empId(),
                context.drafter(),
                docTitle,
                templateCode,
                context.candidates());
        AiApprovalLineResponsePayload approvalLinePayload =
                parseAiPayload(approvalAiChatClient.complete(approvalLinePrompt), AiApprovalLineResponsePayload.class);

        List<String> warnings = new ArrayList<>();
        addWarnings(warnings, approvalLinePayload.warnings);
        List<ApprovalStepRequestDTO> approvalLines =
                normalizeApprovalLines(approvalLinePayload, context.candidates(), warnings);
        List<ApprovalAiApproverCandidateDTO> approvers = selectApprovers(approvalLines, context.candidates());

        return new ApprovalAiApprovalLineResponseDTO(
                approvalLines,
                approvers,
                warnings);
    }

    private <T> T parseAiPayload(String rawResponse, Class<T> payloadType) {
        if (!StringUtils.hasText(rawResponse)) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        String json = extractJson(rawResponse);
        try {
            return objectMapper.readValue(json, payloadType);
        } catch (Exception e) {
            T recovered = recoverAiPayload(json, payloadType);
            if (recovered != null) {
                return recovered;
            }
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private <T> T recoverAiPayload(String json, Class<T> payloadType) {
        if (!AiDraftContentPayload.class.equals(payloadType)) {
            return null;
        }
        AiDraftContentPayload payload = new AiDraftContentPayload();
        payload.docTitle = readLenientJsonStringField(json, "docTitle");
        payload.templateCode = readLenientJsonStringField(json, "templateCode");
        payload.html = readLenientJsonStringField(json, "html");
        payload.warnings = readLenientWarnings(json);
        if (!StringUtils.hasText(payload.docTitle) && !StringUtils.hasText(payload.html)) {
            return null;
        }
        return payloadType.cast(payload);
    }

    private String readLenientJsonStringField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex < 0) {
            return null;
        }
        int colonIndex = json.indexOf(':', keyIndex + key.length());
        if (colonIndex < 0) {
            return null;
        }
        int valueStart = colonIndex + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        if (json.startsWith("null", valueStart)) {
            return null;
        }
        if (valueStart >= json.length() || json.charAt(valueStart) != '"') {
            return null;
        }
        int valueEnd = findLenientJsonStringEnd(json, valueStart + 1);
        if (valueEnd < 0) {
            return null;
        }
        return decodeLenientJsonString(json.substring(valueStart + 1, valueEnd));
    }

    private int findLenientJsonStringEnd(String json, int start) {
        boolean escaped = false;
        for (int i = start; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (ch == '\\') {
                escaped = true;
                continue;
            }
            if (ch == '"') {
                return i;
            }
        }
        return -1;
    }

    private String decodeLenientJsonString(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 2);
        escaped.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '\n') {
                escaped.append("\\n");
            } else if (ch == '\r') {
                escaped.append("\\r");
            } else if (ch == '\t') {
                escaped.append("\\t");
            } else {
                escaped.append(ch);
            }
        }
        escaped.append('"');
        try {
            return objectMapper.readValue(escaped.toString(), String.class);
        } catch (Exception e) {
            return value;
        }
    }

    private List<String> readLenientWarnings(String json) {
        String key = "\"warnings\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex < 0) {
            return List.of();
        }
        int arrayStart = json.indexOf('[', keyIndex + key.length());
        int arrayEnd = json.indexOf(']', arrayStart + 1);
        if (arrayStart < 0 || arrayEnd < arrayStart) {
            return List.of();
        }
        try {
            return objectMapper.readValue(
                    json.substring(arrayStart, arrayEnd + 1),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return List.of();
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

        if (StringUtils.hasText(request.getTmplatCd()) && byCode.containsKey(request.getTmplatCd())) {
            if (StringUtils.hasText(aiTemplateCode) && !request.getTmplatCd().equals(aiTemplateCode)) {
                warnings.add("AI가 선택한 결재 양식 대신 요청한 양식을 사용했습니다.");
            }
            return byCode.get(request.getTmplatCd());
        }
        if (StringUtils.hasText(aiTemplateCode)) {
            ApprovalTemplateResponse aiSelected = byCode.get(aiTemplateCode);
            if (aiSelected != null) {
                return aiSelected;
            }
            warnings.add("AI가 선택한 결재 양식이 유효하지 않아 양식 없이 생성했습니다.");
        }
        return null;
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

    private List<ApprovalAiApproverCandidateDTO> selectApprovers(
            List<ApprovalStepRequestDTO> approvalLines,
            List<ApprovalAiApproverCandidateDTO> candidates) {
        Map<Long, ApprovalAiApproverCandidateDTO> candidateById = candidates.stream()
                .collect(Collectors.toMap(
                        ApprovalAiApproverCandidateDTO::getEmpId,
                        candidate -> candidate,
                        (left, right) -> left));
        List<ApprovalAiApproverCandidateDTO> approvers = new ArrayList<>();
        for (ApprovalStepRequestDTO step : approvalLines) {
            if (step.getAprvrEmpIds() == null) {
                continue;
            }
            for (Long approverId : step.getAprvrEmpIds()) {
                ApprovalAiApproverCandidateDTO candidate = candidateById.get(approverId);
                if (candidate != null) {
                    approvers.add(candidate);
                }
            }
        }
        return approvers;
    }

    private String ensureSignatureTokens(
            String html,
            List<ApprovalStepRequestDTO> approvalLines,
            List<ApprovalAiApproverCandidateDTO> candidates) {
        String normalizedHtml = StringUtils.hasText(html)
                ? html
                : "<!DOCTYPE html><html lang=\"ko\"><body></body></html>";
        if (approvalLines == null || approvalLines.isEmpty()) {
            return normalizedHtml;
        }

        Set<Long> existingOrders = readSignatureOrders(normalizedHtml);
        Map<Long, ApprovalAiApproverCandidateDTO> candidateById = candidates == null
                ? Map.of()
                : candidates.stream()
                .collect(Collectors.toMap(
                        ApprovalAiApproverCandidateDTO::getEmpId,
                        candidate -> candidate,
                        (left, right) -> left));
        List<ApprovalStepRequestDTO> missing = approvalLines.stream()
                .filter(step -> step.getAprvlOrd() != null && !existingOrders.contains(step.getAprvlOrd()))
                .toList();
        if (missing.isEmpty()) {
            return normalizedHtml;
        }

        List<SignatureColumn> columns = missing.stream()
                .map(step -> new SignatureColumn(step.getAprvlOrd(), resolveApproverLabel(step, candidateById)))
                .toList();
        return appendSignatureBlock(normalizedHtml, columns);
    }

    private String ensureMinimumSignatureToken(String html) {
        String normalizedHtml = StringUtils.hasText(html)
                ? html
                : "<!DOCTYPE html><html lang=\"ko\"><body></body></html>";
        if (!readSignatureOrders(normalizedHtml).isEmpty()) {
            return normalizedHtml;
        }
        return appendSignatureBlock(normalizedHtml, List.of(new SignatureColumn(1L, "결재")));
    }

    private Set<Long> readSignatureOrders(String html) {
        Set<Long> orders = new HashSet<>();
        java.util.regex.Matcher matcher = SIGN_TOKEN_PATTERN.matcher(html);
        while (matcher.find()) {
            orders.add(Long.valueOf(matcher.group(1)));
        }
        return orders;
    }

    private String appendSignatureBlock(String html, List<SignatureColumn> columns) {
        if (columns == null || columns.isEmpty()) {
            return html;
        }
        String signBlock = buildSignatureBlock(columns);
        int bodyEnd = html.toLowerCase().lastIndexOf("</body>");
        if (bodyEnd >= 0) {
            return html.substring(0, bodyEnd)
                    + signBlock
                    + html.substring(bodyEnd);
        }
        return html + signBlock;
    }

    private String resolveApproverLabel(
            ApprovalStepRequestDTO step,
            Map<Long, ApprovalAiApproverCandidateDTO> candidateById) {
        if (step.getAprvrEmpIds() == null || step.getAprvrEmpIds().isEmpty()) {
            return "결재";
        }
        ApprovalAiApproverCandidateDTO candidate = candidateById.get(step.getAprvrEmpIds().get(0));
        return candidate == null || !StringUtils.hasText(candidate.getEmpNm())
                ? "결재"
                : candidate.getEmpNm();
    }

    private String buildSignatureBlock(List<SignatureColumn> columns) {
        StringBuilder header = new StringBuilder();
        StringBuilder body = new StringBuilder();
        for (SignatureColumn column : columns) {
            header.append("<th style=\"border:1px solid #334155;padding:6px 12px;background:#f1f5f9;\">")
                    .append(escapeHtml(column.label()))
                    .append("</th>");
            body.append("<td style=\"border:1px solid #334155;width:96px;height:64px;text-align:center;vertical-align:middle;\">")
                    .append("{{SIGN:")
                    .append(column.order())
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

    private ApprovalAiDraftJobResponseDTO toJobResponse(AiJob<ApprovalAiDraftResponseDTO> job) {
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

    private ApprovalAiContentJobResponseDTO toContentJobResponse(AiJob<ApprovalAiContentResponseDTO> job) {
        ApprovalAiContentResponseDTO result = job.result();
        return new ApprovalAiContentJobResponseDTO(
                job.jobId(),
                job.status().name(),
                result == null ? null : result.getDocTtl(),
                result == null ? null : result.getTmplatCd(),
                result == null ? null : result.getAprvlFullCn(),
                result == null ? null : result.getWarnings(),
                job.errorMessage(),
                job.createdAt(),
                job.updatedAt());
    }

    private ApprovalAiApprovalLineJobResponseDTO toApprovalLineJobResponse(
            AiJob<ApprovalAiApprovalLineResponseDTO> job) {
        ApprovalAiApprovalLineResponseDTO result = job.result();
        return new ApprovalAiApprovalLineJobResponseDTO(
                job.jobId(),
                job.status().name(),
                result == null ? null : result.getApprovalLines(),
                result == null ? null : result.getApprovers(),
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

    private String stripHtml(String html) {
        if (!StringUtils.hasText(html)) {
            return "";
        }
        return html
                .replaceAll("(?is)<script[^>]*>.*?</script>", " ")
                .replaceAll("(?is)<style[^>]*>.*?</style>", " ")
                .replaceAll("(?is)<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
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

    private record DraftContentContext(
            Long empId,
            EmployeeProfileDTO drafter,
            List<ApprovalTemplateResponse> templates) {
    }

    private record SignatureColumn(
            Long order,
            String label) {
    }

    private enum ApprovalAiDraftJobStatus {
        PENDING,
        RUNNING,
        SUCCEEDED,
        FAILED
    }

    private static class AiJob<T> {
        private final Long jobId;
        private final Long empId;
        private final LocalDateTime createdAt;
        private volatile LocalDateTime updatedAt;
        private volatile ApprovalAiDraftJobStatus status;
        private volatile T result;
        private volatile String errorMessage;

        private AiJob(Long jobId, Long empId) {
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

        private T result() {
            return result;
        }

        private String errorMessage() {
            return errorMessage;
        }

        private void running() {
            this.status = ApprovalAiDraftJobStatus.RUNNING;
            this.updatedAt = LocalDateTime.now();
        }

        private void succeed(T result) {
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
