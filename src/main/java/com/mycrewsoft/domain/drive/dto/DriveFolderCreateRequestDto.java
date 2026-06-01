package com.mycrewsoft.domain.drive.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "드라이브 아이템 생성 요청")
public class DriveFolderCreateRequestDto {
	
	@Schema(description = "상위 드라이브 아이템 ID (루트 생성 시 null)", example = "1")
    private Long prntDriveItemId;

    @NotBlank(message = "폴더명은 필수입니다.")
    @Size(max = 100, message = "폴더명은 100자 이하여야 합니다.")
    @Schema(description = "폴더명", example = "프로젝트 자료")
    private String itemNm;
    
}
