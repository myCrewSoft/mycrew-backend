package com.mycrewsoft.domain.mtng.service;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.approval.dto.request.ApprovalDraftRequestDTO;
import com.mycrewsoft.domain.approval.dto.request.ApprovalStepRequestDTO;
import com.mycrewsoft.domain.approval.service.ApprovalDraftWriteService;
import com.mycrewsoft.domain.approval.service.ApprovalRequestService;
import com.mycrewsoft.domain.mtng.dto.mom.request.MtngMomUpdateRequest;
import com.mycrewsoft.domain.mtng.dto.mom.response.MtngMomResponse;
import com.mycrewsoft.domain.mtng.mapper.MtngMapper;
import com.mycrewsoft.domain.mtng.mapper.MtngMomDtoMapper;
import com.mycrewsoft.domain.mtng.mapper.MtngMomMapper;
import com.mycrewsoft.domain.mtng.vo.MtngPtcptDetailVO;
import com.mycrewsoft.domain.mtng.vo.mom.MtngMomHistVO;
import com.mycrewsoft.domain.mtng.vo.mom.MtngMomVO;
import com.mycrewsoft.security.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;

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

        MtngMomVO momVO = MtngMomVO.builder()
                .mtngId(mtngId)
                .momCn("")
                .momSttusCd("02") // OFFLINE은 AI초안 단계 없이 편집중으로 시작
                .edtrId(empId)
                .build();

        mtngMomMapper.createMtngMom(momVO);
        return momVO.getMomId();
    }

    @Override
    @Transactional
    public Long createAiDraftMom(Long mtngId, String draftCn) {

        MtngMomVO momVO = MtngMomVO.builder()
                .mtngId(mtngId)
                .momCn(draftCn)
                .momSttusCd("01") // AI 초안
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
        MtngMomHistVO histVO = MtngMomHistVO.builder()
                .momId(momVO.getMomId())
                .momCn(momVO.getMomCn())
                .edtrId(momVO.getEdtrId())
                .build();
        mtngMomMapper.createMtngMomHist(histVO);

        // 회의록 갱신 (편집중 상태 유지)
        MtngMomVO updateVO = MtngMomVO.builder()
                .momId(momVO.getMomId())
                .momCn(request.getMomCn())
                .momSttusCd("02")
                .edtrId(empId)
                .build();
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
        List<Long> aprvrEmpIds = ptcptList.stream()
                .map(MtngPtcptDetailVO::getEmpId)
                .filter(id -> !id.equals(empId)) // 작성자 본인 제외
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
        draftRequest.setDrftDocSn(null);           // null이면 신규 INSERT
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
        mtngMomAiService.regenerateAiDraft(mtngId);
    }
    
    // 회의 참여자인지 검증 (회의록 조회/작성/수정 공통)
    private void validatePtcpt(Long mtngId, Long empId) {
        boolean isPtcpt = mtngMapper.selectMtngPtcptDetailList(mtngId).stream()
                .anyMatch(p -> p.getEmpId().equals(empId));

        if (!isPtcpt) {
            throw new CustomException(ErrorCode.MTNG_PTCPT_FORBIDDEN);
        }
    }

}