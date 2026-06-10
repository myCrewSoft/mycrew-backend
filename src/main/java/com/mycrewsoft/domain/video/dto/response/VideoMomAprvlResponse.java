package com.mycrewsoft.domain.video.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "회의록 결재 응답 DTO")
@Getter
@Setter
public class VideoMomAprvlResponse {

    @Schema(description = "결재 ID")
    private Long aprvlId;

    @Schema(description = "참여자 ID")
    private Long ptcptId;

    @Schema(description = "결재 상태 코드 (01: 대기 / 02: 승인 / 03: 반려)")
    private String aprvlSttusCd;

    @Schema(description = "결재 일시")
    private LocalDateTime aprvlDt;

    @Schema(description = "결재 회차")
    private Integer aprvlRoundNo;
}