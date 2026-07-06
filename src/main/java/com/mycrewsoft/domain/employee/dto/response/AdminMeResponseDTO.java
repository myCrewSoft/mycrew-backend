package com.mycrewsoft.domain.employee.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "관리자 정보 응답 DTO")
public class AdminMeResponseDTO {
	@Schema(description = "사원 ID", example = "1")
	private Long empId;

	@Schema(description = "사원 이름", example = "홍길동")
    private boolean adminAccessible;
}
