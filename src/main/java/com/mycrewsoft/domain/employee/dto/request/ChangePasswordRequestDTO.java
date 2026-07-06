package com.mycrewsoft.domain.employee.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "비밀번호 변경 요청 DTO")
public class ChangePasswordRequestDTO {
	@Schema(description = "현재 비밀번호", example = "currentPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "현재 비밀번호는 필수입니다.")
	private String currentPassword;
	
	@Schema(description = "새 비밀번호", example = "newPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
	@NotBlank(message = "새 비밀번호는 필수입니다.")
	private String newPassword;
}
