package com.mycrewsoft.domain.employee.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "프로필 이미지 변경 요청 DTO")
public class ChangeProfileImageRequestDTO {
	@Schema(description = "변경할 프로필 이미지 파일 ID", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "프로필 이미지 파일 ID는 필수입니다.")
	private Long prflImgFileId;
}
