package com.mycrewsoft.domain.room.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Schema(description = "회의실 수정 요청 DTO")
@Getter
public class RoomUpdateRequest {

    @Schema(description = "회의실 ID", example = "1")
    @NotNull(message = "회의실 ID는 필수입니다.")
    private Long confRmId;

    @Schema(description = "회의실명", example = "한라 회의실")
    private String confRmNm;

    @Schema(description = "회의실 호수", example = "101호")
    private String confRmHo;

    @Schema(description = "회의실 층수", example = "3")
    private Integer confRmFlr;

    @Schema(description = "회의실 관리자 사원 ID", example = "1001")
    private Long confRmMngrId;

    @Schema(description = "사용 여부 (Y/N)", example = "Y")
    private String useYn;

    @Schema(description = "회의실 색상 코드", example = "#3B82F6")
    private String confRmColor;
}