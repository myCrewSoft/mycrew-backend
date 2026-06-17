package com.mycrewsoft.domain.mtng.service;

import com.mycrewsoft.domain.mtng.dto.mom.request.MtngMomUpdateRequest;
import com.mycrewsoft.domain.mtng.dto.mom.response.MtngMomResponse;

public interface MtngMomService {

    // OFFLINE 회의: 참여자가 직접 빈 회의록 작성을 시작 (MOM_STTUS_CD = '02' 편집중)
    Long createEmptyMom(Long mtngId);

    // ONLINE/HYBRID 회의: STT 기반 AI 초안 생성 (MOM_STTUS_CD = '01' AI초안)
    // 화상회의 종료 처리 로직에서 호출됨
    Long createAiDraftMom(Long mtngId, String draftCn);

    // 회의록 조회
    MtngMomResponse getMom(Long mtngId);

    // 회의록 내용 수정 (수정 이력 함께 적재)
    void updateMom(Long mtngId, MtngMomUpdateRequest request);

    // 결재 요청 (전자결재 연동 지점)
    void requestApproval(Long mtngId);
    
    // 회의록 생성 버튼
    void regenerateAiDraft(Long mtngId);

}