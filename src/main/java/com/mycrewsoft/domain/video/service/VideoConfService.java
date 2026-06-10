package com.mycrewsoft.domain.video.service;

import java.util.List;

import com.mycrewsoft.domain.video.dto.request.VideoConfCreateRequest;
import com.mycrewsoft.domain.video.dto.request.VideoMomAprvlRequest;
import com.mycrewsoft.domain.video.dto.request.VideoMomUpdateRequest;
import com.mycrewsoft.domain.video.dto.response.VideoConfResponse;
import com.mycrewsoft.domain.video.dto.response.VideoMomResponse;
import com.mycrewsoft.domain.video.dto.response.VideoTokenResponse;

public interface VideoConfService {

    // 화상회의 생성 (참여자 등록 포함)
    VideoConfResponse createConf(VideoConfCreateRequest request);

    // 내가 참여 중인 화상회의 목록 조회
    List<VideoConfResponse> getConfList();

    // 화상회의 단건 조회
    VideoConfResponse getConf(Long vconfId);

    // LiveKit 입장 토큰 발급
    VideoTokenResponse issueToken(Long vconfId);
    
    // 화상회의 종료 (상태 코드 03으로 변경)
    void endConf(Long vconfId);

    // 회의록 조회
    VideoMomResponse getMom(Long vconfId);

    // 회의록 수정 (수정 이력 자동 저장)
    VideoMomResponse updateMom(Long vconfId, VideoMomUpdateRequest request);

    // 참여자 전원에게 회의록 검토 요청 발송
    void requestMomReview(Long vconfId);

    // 회의록 결재 처리 (전원 승인 시 자동 확정)
    void approveMom(Long vconfId, VideoMomAprvlRequest request);

    // 녹취록 파일 저장 (업로드된 파일의 ATCH_FILE_ID 연결)
    void saveRcrdg(Long vconfId, Long atchFileId);
}