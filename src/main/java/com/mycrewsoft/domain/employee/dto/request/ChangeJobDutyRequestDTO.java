package com.mycrewsoft.domain.employee.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "직무 변경 요청 DTO")
public class ChangeJobDutyRequestDTO {
	@Schema(description = "변경할 직무 내용", example = "백엔드 API 개발", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "직무 내용은 필수입니다.")
	private String jobDutyCn;
}
