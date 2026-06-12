package com.mycrewsoft.domain.approval.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.mycrewsoft.common.constant.PermissionCode;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.approval.approvaldocVO.ApprovalDocVO;
import com.mycrewsoft.domain.approval.approvalfileVO.ApprovalFileVO;
import com.mycrewsoft.domain.approval.approvallineVO.ApprovalLineVO;
import com.mycrewsoft.domain.approval.approvalstepVO.ApprovalStepVO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalStepRequestDTO;
import com.mycrewsoft.domain.approval.dto.response.ApprovalDocumentDetailResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalStepStatusResponse;
import com.mycrewsoft.domain.approval.dto.response.ApprovalAvailabilityResponse;
import com.mycrewsoft.domain.approval.mapper.ApprovalDraftMapper;
import com.mycrewsoft.domain.approval.mapper.DTOtoVOMapper;
import com.mycrewsoft.security.authz.AuthorizationService;
import com.mycrewsoft.security.authz.ResourceContext;
import com.mycrewsoft.security.authz.ResourceType;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApprovalServiceSupport {

    private final AuthorizationService authorizationService;
    private final DTOtoVOMapper dtoToVOMapper;
    private final ApprovalDraftMapper approvalDraftMapper;
    private final com.mycrewsoft.domain.file.service.FileService fileService;

    // 결재란 서명 토큰: {{SIGN:1}}, {{ SIGN : 2 }} 등 공백 허용
    private static final java.util.regex.Pattern SIGN_TOKEN_PATTERN =
            java.util.regex.Pattern.compile("\\{\\{\\s*SIGN\\s*:\\s*(\\d+)\\s*\\}\\}");

    public void assertCreatePermission() {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.APPROVAL)
                .build();
        authorizationService.assertCurrentUserPermission(PermissionCode.APPROVAL_DRAFT_CREATE, resource);
    }
    public void assertUpdatePermission(Long empId) {
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.APPROVAL)
                .ownerEmpId(empId)
                .build();
        authorizationService.assertCurrentUserPermission(PermissionCode.APPROVAL_DRAFT_UPDATE, resource);
    }
    public void assertTemplateCreatePermission() {
    	ResourceContext resource = ResourceContext.builder()
    			.resourceType(ResourceType.APPROVAL)
    			.build();
    	authorizationService.assertCurrentUserPermission(PermissionCode.APPROVAL_TEMPLATE_CREATE, resource);
    }
    public void assertTemplateUpdatePermission(Long empId) {
    	ResourceContext resource = ResourceContext.builder()
    			.resourceType(ResourceType.APPROVAL)
    			.ownerEmpId(empId)
    			.build();
    	authorizationService.assertCurrentUserPermission(PermissionCode.APPROVAL_TEMPLATE_UPDATE, resource);
    }
    public void assertTemplateDeletePermission(Long empId) {
    	// 제작자(SELF 범위) 또는 결재 양식 삭제 권한(GLOBAL)을 가진 사용자만 삭제 가능
    	ResourceContext resource = ResourceContext.builder()
    			.resourceType(ResourceType.APPROVAL)
    			.ownerEmpId(empId)
    			.build();
    	authorizationService.assertCurrentUserPermission(PermissionCode.APPROVAL_TEMPLATE_DELETE, resource);
    }
    
    
    public void replaceApprovalFile(Long drftDocSn, Long atchFileId) {
        approvalDraftMapper.deleteApprovalFileByDocSn(drftDocSn);
        if (atchFileId != null) {
            ApprovalFileVO approvalFile = dtoToVOMapper.toApprovalFileVO(atchFileId);
            approvalFile.setDrftDocSn(drftDocSn);
            approvalDraftMapper.insertApprovalFile(approvalFile);
        }
    }

    public void replaceApprovalLine(
            Long empId,
            Long drftDocSn,
            List<ApprovalStepRequestDTO> approvalSteps,
            LocalDateTime now) {
        List<ApprovalStepRequestDTO> sortedSteps = validateApprovalLine(approvalSteps);
        approvalDraftMapper.deleteApprovalLinesByDocSn(drftDocSn);
        approvalDraftMapper.deleteApprovalStepsByDocSn(drftDocSn);
        for (ApprovalStepRequestDTO request : sortedSteps) {
            ApprovalStepVO approvalStep = dtoToVOMapper.toApprovalStepVO(
                    request,
                    empId,
                    now,
                    ApprovalConstants.STEP_STATUS_WAITING,
                    ApprovalConstants.LINE_STATUS_WAITING
            );
            approvalStep.setDrftDocSn(drftDocSn);
            approvalDraftMapper.insertApprovalStep(approvalStep);

            for (ApprovalLineVO approvalLine : approvalStep.getApprovalLines()) {
                approvalLine.setDrftDocSn(drftDocSn);
                approvalLine.setAprvlStepSn(approvalStep.getAprvlStepSn());
                approvalDraftMapper.insertApprovalLine(approvalLine);
            }
        }
    }

    public List<ApprovalStepRequestDTO> validateApprovalLine(List<ApprovalStepRequestDTO> approvalSteps) {
        if (approvalSteps == null || approvalSteps.isEmpty()) {
            throw new CustomException(ErrorCode.APPROVAL_LINE_INVALID);
        }
        Set<Long> approvalOrders = new HashSet<>();
        for (ApprovalStepRequestDTO approvalStep : approvalSteps) {
            if (approvalStep.getAprvlOrd() == null || approvalStep.getAprvlOrd() < 1) {
                throw new CustomException(ErrorCode.APPROVAL_LINE_INVALID);
            }
            if (!approvalOrders.add(approvalStep.getAprvlOrd())) {
                throw new CustomException(ErrorCode.APPROVAL_LINE_INVALID);
            }
            if (approvalStep.getAprvrEmpIds() == null || approvalStep.getAprvrEmpIds().isEmpty()) {
                throw new CustomException(ErrorCode.APPROVAL_LINE_INVALID);
            }
            if (approvalStep.getAprvrEmpIds().stream().anyMatch(approverEmpId -> approverEmpId == null)) {
                throw new CustomException(ErrorCode.APPROVAL_LINE_INVALID);
            }
            if (new HashSet<>(approvalStep.getAprvrEmpIds()).size() != approvalStep.getAprvrEmpIds().size()) {
                throw new CustomException(ErrorCode.APPROVAL_LINE_INVALID);
            }
        }
        List<ApprovalStepRequestDTO> sortedSteps = new ArrayList<>(approvalSteps);
        sortedSteps.sort(Comparator.comparing(ApprovalStepRequestDTO::getAprvlOrd));
        return sortedSteps;
    }

    public void validateSubmitApproval(ApprovalDocVO savedDoc) {
        if (!StringUtils.hasText(savedDoc.getDocTtl())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (!StringUtils.hasText(savedDoc.getAprvlFullCn()) || savedDoc.getAprvlFullCn().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (approvalDraftMapper.countApprovalSteps(savedDoc.getDrftDocSn()) == 0
                || approvalDraftMapper.countApprovalLines(savedDoc.getDrftDocSn()) == 0) {
            throw new CustomException(ErrorCode.APPROVAL_LINE_INVALID);
        }
    }

    public ApprovalDocVO requireDocForUpdate(Long drftDocSn) {
        ApprovalDocVO savedDoc = approvalDraftMapper.selectApprovalDocByDocSnForUpdate(drftDocSn);
        if (savedDoc == null) {
            throw new CustomException(ErrorCode.APPROVAL_DOC_NOT_FOUND);
        }
        return savedDoc;
    }

    public ApprovalDocumentDetailResponse requireDetail(Long drftDocSn) {
        ApprovalDocumentDetailResponse detail = approvalDraftMapper.selectApprovalDocumentDetail(drftDocSn);
        if (detail == null) {
            throw new CustomException(ErrorCode.APPROVAL_DOC_NOT_FOUND);
        }
        return detail;
    }

    public ApprovalStepVO requireCurrentStep(Long drftDocSn) {
        ApprovalStepVO currentStep = approvalDraftMapper.selectCurrentApprovalStep(drftDocSn);
        if (currentStep == null) {
            throw new CustomException(ErrorCode.APPROVAL_DOC_STATUS_INVALID);
        }
        return currentStep;
    }

    public ApprovalLineVO requireMyWaitingLine(ApprovalStepVO currentStep, Long empId) {
        ApprovalLineVO myLine = approvalDraftMapper.selectApprovalLineInStep(
                currentStep.getAprvlStepSn(),
                empId
        );
        if (myLine == null || !ApprovalConstants.LINE_STATUS_WAITING.equals(myLine.getAprvlPrgrsCd())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
        return myLine;
    }

    public ApprovalAvailabilityResponse checkApprovalAvailability(Long drftDocSn, String availableMessage) {
        Long empId = SecurityUtil.getCurrentEmpId();
        ApprovalDocVO savedDoc = approvalDraftMapper.selectApprovalDocByDocSn(drftDocSn);
        if (savedDoc == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (!ApprovalConstants.DOC_STATUS_IN_PROGRESS.equals(savedDoc.getAprvlDocSttsCd())) {
            return new ApprovalAvailabilityResponse(drftDocSn, false, "결재 진행 중인 문서가 아닙니다.");
        }
        ApprovalStepVO currentStep = approvalDraftMapper.selectCurrentApprovalStep(drftDocSn);
        if (currentStep == null) {
            return new ApprovalAvailabilityResponse(drftDocSn, false, "현재 진행 중인 결재 단계가 없습니다.");
        }
        ApprovalLineVO myLine = approvalDraftMapper.selectApprovalLineInStep(currentStep.getAprvlStepSn(), empId);
        if (myLine == null) {
            return new ApprovalAvailabilityResponse(drftDocSn, false, "현재 결재 단계의 결재자가 아닙니다.");
        }
        if (!ApprovalConstants.LINE_STATUS_WAITING.equals(myLine.getAprvlPrgrsCd())) {
            return new ApprovalAvailabilityResponse(drftDocSn, false, "이미 처리된 결재자입니다.");
        }
        return new ApprovalAvailabilityResponse(drftDocSn, true, availableMessage);
    }

    // The above method is not used by services directly; keep specific helper methods below.

    public void assertDrafter(ApprovalDocVO savedDoc, Long empId) {
        if (!empId.equals(savedDoc.getEmpId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }
    }

    public void assertDocStatus(ApprovalDocVO savedDoc, String expectedStatus) {
        if (!expectedStatus.equals(savedDoc.getAprvlDocSttsCd())) {
            throw new CustomException(ErrorCode.APPROVAL_DOC_STATUS_INVALID);
        }
    }

    public boolean canReadApprovalDocument(ApprovalDocumentDetailResponse detail, Long empId) {
        if (detail.getEmpId().equals(empId)) {
            return true;
        }
        if (approvalDraftMapper.existsApprovalParticipant(detail.getDrftDocSn(), empId) > 0) {
            return true;
        }
        ResourceContext resource = ResourceContext.builder()
                .resourceType(ResourceType.APPROVAL)
                .build();
        return authorizationService.canAccess(PermissionCode.ADMIN_CONSOLE_ACCESS, resource);
    }

    public String normalizeClobForTemporarySave(String value) {
        return StringUtils.hasText(value) ? value : " ";
    }

    public void populateStatus(ApprovalDocumentDetailResponse detail) {
        List<ApprovalStepStatusResponse> steps =
                approvalDraftMapper.selectApprovalStepStatuses(detail.getDrftDocSn());
        detail.setApprovalSteps(steps);
        detail.setStatusMessages(makeStatusMessages(detail, steps));
        injectSignatures(detail, steps);
    }

    /**
     * 본문 HTML의 결재란 토큰 {{SIGN:순서}} 를 해당 결재자의 전자서명 이미지로 치환한다.
     * - 승인 완료된 단계의 서명(승인 시점 스냅샷 파일)을 base64 data URI로 주입한다.
     *   (문서 뷰어가 스크립트 차단 sandbox iframe + 이미지 엔드포인트가 JWT 보호라, 자체 포함 data URI가 필요)
     * - 아직 승인되지 않았거나 매칭되는 결재자가 없는 토큰은 빈 문자열로 제거한다.
     */
    private void injectSignatures(
            ApprovalDocumentDetailResponse detail,
            List<ApprovalStepStatusResponse> steps) {
        String html = detail.getAprvlFullCn();
        if (!StringUtils.hasText(html) || !html.contains("{{")) {
            return;
        }

        java.util.Map<Long, String> imgByOrder = new java.util.HashMap<>();
        for (ApprovalStepStatusResponse step : steps) {
            if (!ApprovalConstants.LINE_STATUS_APPROVED.equals(step.getAprvlPrgrsCd())) {
                continue;
            }
            if (step.getAprvrStampFileId() == null || step.getAprvlOrd() == null) {
                continue;
            }
            String img = buildSignatureImg(step.getAprvrStampFileId(), step.getAprvrEmpNm());
            if (img != null) {
                imgByOrder.put(step.getAprvlOrd(), img);
            }
        }

        java.util.regex.Matcher matcher = SIGN_TOKEN_PATTERN.matcher(html);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            Long order = Long.valueOf(matcher.group(1));
            String replacement = imgByOrder.getOrDefault(order, "");
            matcher.appendReplacement(sb, java.util.regex.Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        detail.setAprvlFullCn(sb.toString());
    }

    /** 전자서명 파일을 base64 data URI img 태그로 만든다. 파일이 없거나 손상되면 null. */
    private String buildSignatureImg(Long stampFileId, String approverName) {
        try {
            org.springframework.core.io.Resource resource = fileService.serveImage(stampFileId);
            byte[] bytes = resource.getContentAsByteArray();
            String mime = com.mycrewsoft.common.util.FileUtil.resolveImageContentType(
                    com.mycrewsoft.common.util.FileUtil.getExtension(resource.getFilename()));
            String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
            String alt = StringUtils.hasText(approverName) ? approverName + " 전자서명" : "전자서명";
            return "<img src=\"data:" + mime + ";base64," + base64
                    + "\" alt=\"" + alt
                    + "\" style=\"max-height:64px;max-width:180px;object-fit:contain\" />";
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> makeStatusMessages(
            ApprovalDocumentDetailResponse detail,
            List<ApprovalStepStatusResponse> steps) {
        List<String> messages = new ArrayList<>();
        for (ApprovalStepStatusResponse step : steps) {
            String name = step.getAprvrEmpNm();
            if (ApprovalConstants.LINE_STATUS_APPROVED.equals(step.getAprvlPrgrsCd())) {
                messages.add(name + " 승인");
            } else if (ApprovalConstants.LINE_STATUS_REJECTED.equals(step.getAprvlPrgrsCd())) {
                messages.add(name + " 반려");
            } else if (ApprovalConstants.LINE_STATUS_SKIPPED.equals(step.getAprvlPrgrsCd())
                    || ApprovalConstants.LINE_STATUS_WITHDRAW_CANCELLED.equals(step.getAprvlPrgrsCd())) {
                messages.add(name + " 생략");
            } else if (ApprovalConstants.STEP_STATUS_IN_PROGRESS.equals(step.getStepPrgrsCd())) {
                messages.add(name + " 결재 중");
            } else {
                messages.add(name + " 대기");
            }
        }
        if (ApprovalConstants.DOC_STATUS_COMPLETED.equals(detail.getAprvlDocSttsCd())) {
            messages.add("결재 완료");
        } else if (ApprovalConstants.DOC_STATUS_REJECTED.equals(detail.getAprvlDocSttsCd())) {
            messages.add("결재 반려");
        } else if (ApprovalConstants.DOC_STATUS_WITHDRAWN.equals(detail.getAprvlDocSttsCd())) {
            messages.add("결재 회수");
        }
        return messages;
    }

    // Small wrapper class to allow throwing structured error in checkApprovalAvailability placeholder.
    public static class ApprovalAvailabilityWrapper extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public ApprovalAvailabilityWrapper(Long drftDocSn, boolean b, String message) {
            super(message);
        }
    }
}
