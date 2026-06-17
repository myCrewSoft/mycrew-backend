package com.mycrewsoft.domain.room.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회의실 조회 응답 DTO")
public class RoomResponse {
    @Schema(description = "회의실 ID", example = "1")
    private Long roomId;

    @Schema(description = "회의실 이름", example = "한라 회의실")
    private String roomName;

    @Schema(description = "회의실 호수", example = "101호")
    private String ho;

    @Schema(description = "회의실 층수", example = "3")
    private Integer floor;

    @Schema(description = "회의실 관리자 사원 ID", example = "1001")
    private Long confRmMngrId;

    @Schema(description = "사용 여부 (Y/N)", example = "Y")
    private String useYn;

    @Schema(description = "회의실 색상 코드", example = "#3B82F6")
    private String confRmColor;
}
