package com.mycrewsoft.domain.employee.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "전자서명 변경 요청 DTO")
public class ChangeSignatureRequestDTO {
	@Schema(description = "변경할 전자서명 파일 ID", example = "20", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotNull(message = "전자서명 파일 ID는 필수입니다.")
	private Long mbrStampFileId;
}
