package com.mycrewsoft.domain.schedule.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "관리자 일정 목록 응답 DTO")
public class AdminSchdListResponse {

    @Schema(description = "일정 ID", example = "1")
    private Long schdId;

    @Schema(description = "일정 구분 코드", example = "C001")
    private String schdClsfCd;

    @Schema(description = "일정명", example = "전사 워크샵")
    private String schdNm;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "시작 일시")
    private LocalDateTime beginDt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "종료 일시")
    private LocalDateTime endDt;

    @Schema(description = "종일 여부", example = "N")
    private String allDayYn;

    @Schema(description = "반복 여부", example = "N")
    private String reptYn;

    @Schema(description = "등록자 사원 ID")
    private Long schdWrtrId;

    @Schema(description = "등록자 이름", example = "김관리")
    private String wrtrNm;

    @Schema(description = "등록자 부서명", example = "인사팀")
    private String wrtrDeptNm;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "등록 일시")
    private LocalDateTime schdRegstDt;
}