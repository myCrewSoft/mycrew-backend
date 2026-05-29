package com.mycrewsoft.domain.employee.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminMeResponseDTO {
	private Long empId;

    private boolean adminAccessible;
}
