package com.mycrewsoft.domain.video.service;

import com.mycrewsoft.domain.video.dto.response.VideoTokenResponse;

public interface VideoConfService {

    // LiveKit 입장 토큰 발급 (입장 로그 기록)
    VideoTokenResponse issueToken(Long vconfId);

    // 회의 퇴장 처리
    void leaveConf(Long vconfId);

    // 화상회의 종료 처리 (퇴장 로그 정리 + AI 회의록 초안 생성)
    void endConf(Long vconfId);

}
