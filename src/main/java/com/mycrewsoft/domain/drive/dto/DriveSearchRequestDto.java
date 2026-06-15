package com.mycrewsoft.domain.drive.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "드라이브 목록 조회 요청 DTO")
public class DriveSearchRequestDto {
	@Schema(description = "부모 드라이브 아이템 ID")
    private Long prntDriveItemId;

    @Schema(description = "페이지 번호 (0-based)", example = "0")
    private int page = 0;

    @Schema(description = "페이지 크기", example = "기본값 10")
    private int size = 10;
}
