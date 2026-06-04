package com.mycrewsoft.domain.room.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Schema(description = "회의실 생성 요청 DTO")
@Getter
public class RoomCreateRequest {
	
    @Schema(description = "회의실명", example = "한라 회의실")
    @NotBlank(message = "회의실명은 필수입니다.")
    private String confRmNm;

    @Schema(description = "회의실 호수", example = "101호")
    @NotBlank(message = "회의실 호수는 필수입니다.")
    private String confRmHo;

    @Schema(description = "회의실 층수", example = "3")
    @NotNull(message = "회의실 층수는 필수입니다.")
    private Integer confRmFlr;

    @Schema(description = "회의실 관리자 사원 ID", example = "1001")
    @NotNull(message = "회의실 관리자는 필수입니다.")
    private Long confRmMngrId;

    @Schema(description = "사용 여부 (Y/N)", example = "Y")
    @NotBlank(message = "사용 여부는 필수입니다.")
    private String useYn;

    @Schema(description = "회의실 색상 코드", example = "#3B82F6")
    private String confRmColor;

}
