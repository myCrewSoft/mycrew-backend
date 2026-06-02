package com.mycrewsoft.domain.drive.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "드라이브 아이템 이름 수정 요청")
public class DriveRenameRequestDto {
	
	@NotBlank(message = "이름은 필수입니다.")
	@Schema(description = "변경할 폴더명", example = "새 폴더명")
	private String itemNm;
}
