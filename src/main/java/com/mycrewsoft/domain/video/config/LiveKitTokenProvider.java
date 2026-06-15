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

    // roomName과 참여자 정보를 받아 LiveKit 입장 토큰을 생성해 반환
    public String createToken(String roomName, String empId, String empNm,
            String deptNm, String jobGrdNm, String prflImgFileId) {
        try {
            AccessToken token = new AccessToken(
                    liveKitProperties.getApiKey(),
                    liveKitProperties.getApiSecret()
            );

            // identity는 empId(고유 식별자), name은 표시용 실명
            token.setIdentity(empId);
            token.setName(empNm != null ? empNm : empId);

            // 사원 정보를 metadata JSON으로 포함 → 클라이언트가 참여자 정보 표시에 활용
            String metadata = buildMetadata(empId, deptNm, jobGrdNm, prflImgFileId);
            token.setMetadata(metadata);

            // 해당 roomName에 입장 권한 부여
            token.addGrants(new RoomJoin(true), new RoomName(roomName));

            return token.toJwt();

        } catch (Exception e) {
            log.error("[LiveKit] 토큰 발급 실패 - roomName: {}, empId: {}", roomName, empId, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private String buildMetadata(String empId, String deptNm, String jobGrdNm, String prflImgFileId) {
        return "{\"empId\":\"%s\",\"deptNm\":\"%s\",\"jobGrdNm\":\"%s\",\"prflImgFileId\":\"%s\"}".formatted(
                empId != null ? empId : "",
                deptNm != null ? deptNm : "",
                jobGrdNm != null ? jobGrdNm : "",
                prflImgFileId != null ? prflImgFileId : ""
        );
    }
}