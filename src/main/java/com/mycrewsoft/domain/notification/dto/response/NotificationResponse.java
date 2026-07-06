package com.mycrewsoft.domain.notification.dto.response;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Builder;

@Getter
@Builder
@Schema(description = "알림 응답 DTO")
public class NotificationResponse {

    @Schema(description = "알림 수신 ID", example = "1")
    private Long alrmRcvrId;

    @Schema(description = "알림 ID", example = "101")
    private Long alrmId;

    @Schema(description = "알림 타입 코드", example = "05")
    private String alrmTypeCd;

    @Schema(description = "알림 제목", example = "결재 요청이 도착했습니다.")
    private String alrmTtln;

    @Schema(description = "알림 내용", example = "신데렐라님이 휴가신청서 결재를 요청했습니다.")
    private String alrmCn;

    @Schema(
        description = "알림 이동 대상 종류",
        example = "TASK",
        allowableValues = {"APPROVAL", "SCHEDULE", "MEETING", "PROJECT", "TASK"},
        nullable = true
    )
    private String targetType;

    @Schema(description = "알림 대상 ID", example = "45", nullable = true)
    private Long targetId;

    @Schema(description = "상위 대상 ID. 업무 알림에서는 프로젝트 ID", example = "12", nullable = true)
    private Long parentTargetId;

    @Schema(description = "알림 발송 일시", example = "2025-06-01T09:00:00")
    private LocalDateTime alrmSndngDt;

    @Schema(description = "알림 확인 일시 (null이면 미읽음)", example = "2025-06-01T09:05:00", nullable = true)
    private LocalDateTime alrmCfmtnDt;
}
