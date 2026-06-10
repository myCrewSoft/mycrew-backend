package com.mycrewsoft.domain.video.config;

import org.springframework.stereotype.Component;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;

import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LiveKitTokenProvider {

    private final LiveKitProperties liveKitProperties;

    // roomName과 참여자 empId를 받아 LiveKit 입장 토큰을 생성해 반환
    public String createToken(String roomName, String empId) {
        try {
            AccessToken token = new AccessToken(
                    liveKitProperties.getApiKey(),
                    liveKitProperties.getApiSecret()
            );

            // 토큰에 참여자 identity 설정 (empId를 식별자로 사용)
            token.setName(empId);
            token.setIdentity(empId);

            // 해당 roomName에 입장 권한 부여
            token.addGrants(new RoomJoin(true), new RoomName(roomName));

            return token.toJwt();

        } catch (Exception e) {
            log.error("[LiveKit] 토큰 발급 실패 - roomName: {}, empId: {}", roomName, empId, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}